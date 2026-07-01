package com.rvms.backend.repository;

import com.rvms.backend.entity.MaintenanceRecord;
import com.rvms.backend.entity.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    List<MaintenanceRecord> findByVehicleId(Long vehicleId);
    List<MaintenanceRecord> findByBranchId(Long branchId);
    List<MaintenanceRecord> findByStatus(MaintenanceStatus status);
}
