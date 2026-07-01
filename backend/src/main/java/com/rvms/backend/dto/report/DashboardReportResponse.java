package com.rvms.backend.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record DashboardReportResponse(
        long totalVehicles,
        long availableVehicles,
        long rentedVehicles,
        long maintenanceVehicles,
        long activeRentals,
        long upcomingReturns,
        BigDecimal totalRevenue,
        List<BranchReportResponse> branchReports
) {}
