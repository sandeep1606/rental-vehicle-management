package com.rvms.backend.dto.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationRequest(
        @NotNull Long customerId,
        @NotNull Long vehicleId,
        @NotNull @FutureOrPresent LocalDate startDate,
        @NotNull @Future LocalDate endDate
) {}
