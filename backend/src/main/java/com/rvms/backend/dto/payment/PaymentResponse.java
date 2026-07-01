package com.rvms.backend.dto.payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long rentalId,
        BigDecimal amount,
        String status,
        String method,
        String transactionRef,
        Instant paidAt
) {}
