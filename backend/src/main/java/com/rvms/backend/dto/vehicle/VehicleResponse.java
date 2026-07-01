package com.rvms.backend.dto.vehicle;

import java.io.Serializable;
import java.math.BigDecimal;

public record VehicleResponse(
        Long id,
        String plateNumber,
        String vin,
        String type,
        String brand,
        String model,
        int year,
        int mileage,
        String fuelType,
        String transmission,
        BigDecimal dailyRate,
        String status,
        Long branchId,
        String branchName
) implements Serializable {}
