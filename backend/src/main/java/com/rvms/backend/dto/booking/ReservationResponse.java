package com.rvms.backend.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        Long customerId,
        String customerName,
        Long vehicleId,
        String vehiclePlate,
        Long branchId,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        BigDecimal estimatedTotal
) {}
