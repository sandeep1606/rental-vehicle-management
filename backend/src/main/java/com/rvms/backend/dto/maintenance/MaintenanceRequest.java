package com.rvms.backend.dto.maintenance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaintenanceRequest(
        @NotNull Long vehicleId,
        @NotBlank String description,
        BigDecimal cost,
        @NotNull LocalDate scheduledDate
) {}
