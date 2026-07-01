package com.rvms.backend.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RentalResponse(
        Long id,
        Long reservationId,
        Long customerId,
        String customerName,
        Long vehicleId,
        String vehiclePlate,
        Long branchId,
        LocalDate startDate,
        LocalDate plannedEndDate,
        LocalDate actualReturnDate,
        BigDecimal dailyRate,
        BigDecimal totalAmount,
        BigDecimal lateFee,
        String status
) {}
