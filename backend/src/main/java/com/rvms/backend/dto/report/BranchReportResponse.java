package com.rvms.backend.dto.report;

import java.math.BigDecimal;

public record BranchReportResponse(
        Long branchId,
        String branchName,
        long totalVehicles,
        long availableVehicles,
        long rentedVehicles,
        long maintenanceVehicles,
        long activeRentals,
        BigDecimal revenue
) {}
