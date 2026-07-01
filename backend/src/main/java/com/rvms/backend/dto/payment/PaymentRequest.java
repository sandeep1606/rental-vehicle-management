package com.rvms.backend.dto.payment;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull Long rentalId,
        @NotNull String method
) {}
