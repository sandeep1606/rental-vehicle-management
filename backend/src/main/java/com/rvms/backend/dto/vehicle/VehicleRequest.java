package com.rvms.backend.dto.vehicle;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record VehicleRequest(
        @NotBlank String plateNumber,
        @NotBlank @Size(min = 11, max = 17) String vin,
        @NotBlank String type,
        @NotBlank String brand,
        @NotBlank String model,
        @Min(1980) @Max(2100) int year,
        @Min(0) int mileage,
        @NotBlank String fuelType,
        @NotBlank String transmission,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal dailyRate,
        String status,
        @NotNull Long branchId
) {}
