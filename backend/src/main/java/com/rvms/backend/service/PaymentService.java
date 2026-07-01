package com.rvms.backend.service;

import com.rvms.backend.dto.payment.PaymentRequest;
import com.rvms.backend.dto.payment.PaymentResponse;
import com.rvms.backend.entity.Payment;
import com.rvms.backend.entity.PaymentStatus;
import com.rvms.backend.entity.Rental;
import com.rvms.backend.exception.BusinessRuleException;
import com.rvms.backend.exception.ResourceNotFoundException;
import com.rvms.backend.mapper.PaymentMapper;
import com.rvms.backend.repository.PaymentRepository;
import com.rvms.backend.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Mock payment gateway. No real money movement happens here — this simulates
 * a processor so the rest of the system (rentals, reports) has real payment
 * records to work against.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private static final String SIMULATED_FAILURE_METHOD = "FAIL_TEST";

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final RentalPricingService pricingService;
    private final PaymentMapper paymentMapper;

    public List<PaymentResponse> getByRental(Long rentalId) {
        return paymentRepository.findByRentalId(rentalId).stream().map(paymentMapper::toResponse).toList();
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Rental rental = rentalRepository.findById(request.rentalId())
                .orElseThrow(() -> ResourceNotFoundException.of("Rental", request.rentalId()));

        BigDecimal amount = rental.getTotalAmount() != null
                ? rental.getTotalAmount()
                : pricingService.estimateTotal(rental.getDailyRate(), rental.getStartDate(), rental.getPlannedEndDate());

        Payment payment = Payment.builder()
                .rental(rental)
                .amount(amount)
                .method(request.method())
                .status(PaymentStatus.PENDING)
                .build();

        boolean simulateFailure = SIMULATED_FAILURE_METHOD.equalsIgnoreCase(request.method());
        if (simulateFailure) {
            payment.setStatus(PaymentStatus.FAILED);
        } else {
            payment.setStatus(PaymentStatus.PAID);
            payment.setTransactionRef("TXN-" + UUID.randomUUID());
            payment.setPaidAt(Instant.now());
        }

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse refund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment", paymentId));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BusinessRuleException("Only a PAID payment can be refunded.");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }
}
