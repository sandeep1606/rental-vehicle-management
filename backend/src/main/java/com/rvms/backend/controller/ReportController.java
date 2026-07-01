package com.rvms.backend.controller;

import com.rvms.backend.dto.report.BranchReportResponse;
import com.rvms.backend.dto.report.DashboardReportResponse;
import com.rvms.backend.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
@Tag(name = "Reports", description = "Dashboard and per-branch reporting")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public DashboardReportResponse getDashboard() {
        return reportService.getDashboard();
    }

    @GetMapping("/branch/{branchId}")
    public BranchReportResponse getBranchReport(@PathVariable Long branchId) {
        return reportService.getBranchReport(branchId);
    }
}
