package com.rvms.backend.service;

import com.rvms.backend.dto.payment.PaymentRequest;
import com.rvms.backend.entity.Payment;
import com.rvms.backend.entity.PaymentStatus;
import com.rvms.backend.entity.Rental;
import com.rvms.backend.entity.RentalStatus;
import com.rvms.backend.exception.BusinessRuleException;
import com.rvms.backend.mapper.PaymentMapper;
import com.rvms.backend.repository.PaymentRepository;
import com.rvms.backend.repository.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private RentalRepository rentalRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, rentalRepository, new RentalPricingService(), new PaymentMapper());
    }

    private Rental rental() {
        return Rental.builder()
                .id(1L).dailyRate(new BigDecimal("50.00"))
                .startDate(LocalDate.now()).plannedEndDate(LocalDate.now().plusDays(3))
                .totalAmount(new BigDecimal("150.00"))
                .status(RentalStatus.COMPLETED)
                .build();
    }

    @Test
    void processPaymentMarksPaidForNormalMethod() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental()));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = paymentService.processPayment(new PaymentRequest(1L, "CREDIT_CARD"));

        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.amount()).isEqualByComparingTo("150.00");
        assertThat(response.transactionRef()).isNotBlank();
    }

    @Test
    void processPaymentMarksFailedForSimulatedFailureMethod() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental()));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = paymentService.processPayment(new PaymentRequest(1L, "FAIL_TEST"));

        assertThat(response.status()).isEqualTo("FAILED");
        assertThat(response.transactionRef()).isNull();
    }

    @Test
    void refundOnlyAllowedForPaidPayment() {
        Payment pending = Payment.builder().id(2L).status(PaymentStatus.PENDING).amount(BigDecimal.TEN).build();
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> paymentService.refund(2L)).isInstanceOf(BusinessRuleException.class);
    }
}
