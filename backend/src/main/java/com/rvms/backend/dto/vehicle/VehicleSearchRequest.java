package com.rvms.backend.dto.vehicle;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VehicleSearchRequest(
        Long branchId,
        String type,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal maxDailyRate
) {}
