package com.rvms.backend.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Pure pricing calculations for rentals: no repository/database access, so this
 * is trivially unit-testable in isolation (see RentalPricingServiceTest).
 */
@Service
public class RentalPricingService {

    /** Minimum billable duration for any rental/reservation is one full day. */
    public long billableDays(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end date cannot be before start date");
        }
        return Math.max(1, ChronoUnit.DAYS.between(start, end));
    }

    public BigDecimal estimateTotal(BigDecimal dailyRate, LocalDate start, LocalDate end) {
        return dailyRate.multiply(BigDecimal.valueOf(billableDays(start, end)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public RentalPriceBreakdown calculateFinalPrice(
            BigDecimal dailyRate,
            LocalDate start,
            LocalDate plannedEnd,
            LocalDate actualReturn,
            BigDecimal lateFeeMultiplier
    ) {
        long plannedDays = billableDays(start, plannedEnd);
        BigDecimal baseAmount = dailyRate.multiply(BigDecimal.valueOf(plannedDays)).setScale(2, RoundingMode.HALF_UP);

        long lateDays = 0;
        BigDecimal lateFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (actualReturn.isAfter(plannedEnd)) {
            lateDays = ChronoUnit.DAYS.between(plannedEnd, actualReturn);
            lateFee = dailyRate.multiply(lateFeeMultiplier)
                    .multiply(BigDecimal.valueOf(lateDays))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal total = baseAmount.add(lateFee);
        return new RentalPriceBreakdown(plannedDays, baseAmount, lateDays, lateFee, total);
    }

    public record RentalPriceBreakdown(
            long billableDays,
            BigDecimal baseAmount,
            long lateDays,
            BigDecimal lateFee,
            BigDecimal totalAmount
    ) {}
}
