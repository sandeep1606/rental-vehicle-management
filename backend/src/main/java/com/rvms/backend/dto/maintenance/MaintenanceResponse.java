package com.rvms.backend.dto.maintenance;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaintenanceResponse(
        Long id,
        Long vehicleId,
        String vehiclePlate,
        Long branchId,
        String description,
        BigDecimal cost,
        String status,
        LocalDate scheduledDate,
        LocalDate completedDate
) {}
