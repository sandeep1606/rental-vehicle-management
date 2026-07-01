package com.rvms.backend.service;

import com.rvms.backend.dto.maintenance.MaintenanceCompleteRequest;
import com.rvms.backend.dto.maintenance.MaintenanceRequest;
import com.rvms.backend.dto.maintenance.MaintenanceResponse;
import com.rvms.backend.entity.MaintenanceRecord;
import com.rvms.backend.entity.MaintenanceStatus;
import com.rvms.backend.entity.Vehicle;
import com.rvms.backend.entity.VehicleStatus;
import com.rvms.backend.exception.BusinessRuleException;
import com.rvms.backend.exception.ResourceNotFoundException;
import com.rvms.backend.mapper.MaintenanceMapper;
import com.rvms.backend.repository.MaintenanceRecordRepository;
import com.rvms.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceMapper maintenanceMapper;

    public List<MaintenanceResponse> getAll() {
        return maintenanceRecordRepository.findAll().stream().map(maintenanceMapper::toResponse).toList();
    }

    public List<MaintenanceResponse> getByVehicle(Long vehicleId) {
        return maintenanceRecordRepository.findByVehicleId(vehicleId).stream().map(maintenanceMapper::toResponse).toList();
    }

    @Transactional
    public MaintenanceResponse create(MaintenanceRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> ResourceNotFoundException.of("Vehicle", request.vehicleId()));

        if (vehicle.getStatus() == VehicleStatus.RENTED) {
            throw new BusinessRuleException("Cannot schedule maintenance for a vehicle that is currently rented.");
        }

        MaintenanceRecord record = MaintenanceRecord.builder()
                .vehicle(vehicle)
                .branch(vehicle.getBranch())
                .description(request.description())
                .cost(request.cost())
                .scheduledDate(request.scheduledDate())
                .status(MaintenanceStatus.SCHEDULED)
                .build();
        record = maintenanceRecordRepository.save(record);

        vehicle.setStatus(VehicleStatus.MAINTENANCE);
        vehicleRepository.save(vehicle);

        return maintenanceMapper.toResponse(record);
    }

    @Transactional
    public MaintenanceResponse complete(Long id, MaintenanceCompleteRequest request) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("MaintenanceRecord", id));

        if (record.getStatus() == MaintenanceStatus.COMPLETED) {
            throw new BusinessRuleException("Maintenance record is already completed.");
        }

        record.setStatus(MaintenanceStatus.COMPLETED);
        record.setCompletedDate(request.completedDate());
        record = maintenanceRecordRepository.save(record);

        Vehicle vehicle = record.getVehicle();
        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        return maintenanceMapper.toResponse(record);
    }
}
