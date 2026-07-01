package com.rvms.backend.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RentalPricingServiceTest {

    private final RentalPricingService pricingService = new RentalPricingService();

    @Test
    void billableDaysIsAtLeastOneEvenForSameDayRental() {
        LocalDate day = LocalDate.of(2026, 7, 5);
        assertThat(pricingService.billableDays(day, day)).isEqualTo(1);
    }

    @Test
    void billableDaysCountsFullDaysBetweenStartAndEnd() {
        assertThat(pricingService.billableDays(LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 10))).isEqualTo(5);
    }

    @Test
    void billableDaysRejectsEndBeforeStart() {
        assertThatThrownBy(() -> pricingService.billableDays(LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 5)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void estimateTotalMultipliesDailyRateByBillableDays() {
        BigDecimal total = pricingService.estimateTotal(
                new BigDecimal("50.00"), LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 10));
        assertThat(total).isEqualByComparingTo("250.00");
    }

    @Test
    void calculateFinalPriceWithOnTimeReturnHasNoLateFee() {
        var breakdown = pricingService.calculateFinalPrice(
                new BigDecimal("50.00"),
                LocalDate.of(2026, 7, 5),
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 10),
                new BigDecimal("1.5"));

        assertThat(breakdown.billableDays()).isEqualTo(5);
        assertThat(breakdown.baseAmount()).isEqualByComparingTo("250.00");
        assertThat(breakdown.lateDays()).isZero();
        assertThat(breakdown.lateFee()).isEqualByComparingTo("0.00");
        assertThat(breakdown.totalAmount()).isEqualByComparingTo("250.00");
    }

    @Test
    void calculateFinalPriceWithLateReturnAddsMultipliedLateFee() {
        // planned 5 days at $50/day = $250 base; returned 2 days late at 1.5x = $150 late fee
        var breakdown = pricingService.calculateFinalPrice(
                new BigDecimal("50.00"),
                LocalDate.of(2026, 7, 5),
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12),
                new BigDecimal("1.5"));

        assertThat(breakdown.lateDays()).isEqualTo(2);
        assertThat(breakdown.lateFee()).isEqualByComparingTo("150.00");
        assertThat(breakdown.totalAmount()).isEqualByComparingTo("400.00");
    }
}
