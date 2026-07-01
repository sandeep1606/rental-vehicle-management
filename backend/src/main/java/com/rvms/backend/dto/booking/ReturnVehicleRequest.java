package com.rvms.backend.dto.booking;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReturnVehicleRequest(
        @NotNull LocalDate actualReturnDate
) {}
