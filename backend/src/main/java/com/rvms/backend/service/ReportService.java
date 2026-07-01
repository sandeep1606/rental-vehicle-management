package com.rvms.backend.service;

import com.rvms.backend.dto.report.BranchReportResponse;
import com.rvms.backend.dto.report.DashboardReportResponse;
import com.rvms.backend.entity.Branch;
import com.rvms.backend.entity.RentalStatus;
import com.rvms.backend.entity.VehicleStatus;
import com.rvms.backend.repository.BranchRepository;
import com.rvms.backend.repository.PaymentRepository;
import com.rvms.backend.repository.RentalRepository;
import com.rvms.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final int UPCOMING_RETURN_WINDOW_DAYS = 3;

    private final BranchRepository branchRepository;
    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;

    public DashboardReportResponse getDashboard() {
        List<Branch> branches = branchRepository.findAll();
        List<BranchReportResponse> branchReports = branches.stream().map(this::buildBranchReport).toList();

        long totalVehicles = vehicleRepository.count();
        long availableVehicles = vehicleRepository.findByStatus(VehicleStatus.AVAILABLE).size();
        long rentedVehicles = vehicleRepository.findByStatus(VehicleStatus.RENTED).size();
        long maintenanceVehicles = vehicleRepository.findByStatus(VehicleStatus.MAINTENANCE).size();
        long activeRentals = rentalRepository.findByStatus(RentalStatus.ACTIVE).size();
        long upcomingReturns = rentalRepository.findByStatusAndPlannedEndDateBetween(
                RentalStatus.ACTIVE, LocalDate.now(), LocalDate.now().plusDays(UPCOMING_RETURN_WINDOW_DAYS)).size();

        return new DashboardReportResponse(
                totalVehicles,
                availableVehicles,
                rentedVehicles,
                maintenanceVehicles,
                activeRentals,
                upcomingReturns,
                paymentRepository.sumTotalRevenue(),
                branchReports
        );
    }

    public BranchReportResponse getBranchReport(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new com.rvms.backend.exception.ResourceNotFoundException("Branch not found with id: " + branchId));
        return buildBranchReport(branch);
    }

    private BranchReportResponse buildBranchReport(Branch branch) {
        long total = vehicleRepository.countByBranchId(branch.getId());
        long available = vehicleRepository.countByBranchIdAndStatus(branch.getId(), VehicleStatus.AVAILABLE);
        long rented = vehicleRepository.countByBranchIdAndStatus(branch.getId(), VehicleStatus.RENTED);
        long maintenance = vehicleRepository.countByBranchIdAndStatus(branch.getId(), VehicleStatus.MAINTENANCE);
        long activeRentals = rentalRepository.countByBranchIdAndStatus(branch.getId(), RentalStatus.ACTIVE);

        return new BranchReportResponse(
                branch.getId(),
                branch.getName(),
                total,
                available,
                rented,
                maintenance,
                activeRentals,
                paymentRepository.sumRevenueByBranch(branch.getId())
        );
    }
}
