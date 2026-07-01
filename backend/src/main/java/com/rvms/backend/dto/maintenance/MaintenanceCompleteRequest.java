package com.rvms.backend.dto.maintenance;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MaintenanceCompleteRequest(
        @NotNull LocalDate completedDate
) {}
