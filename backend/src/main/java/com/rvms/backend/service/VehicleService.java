package com.rvms.backend.service;

import com.rvms.backend.dto.vehicle.VehicleRequest;
import com.rvms.backend.dto.vehicle.VehicleResponse;
import com.rvms.backend.dto.vehicle.VehicleSearchRequest;
import com.rvms.backend.entity.*;
import com.rvms.backend.exception.BusinessRuleException;
import com.rvms.backend.exception.DuplicateResourceException;
import com.rvms.backend.exception.ResourceNotFoundException;
import com.rvms.backend.mapper.VehicleMapper;
import com.rvms.backend.repository.BranchRepository;
import com.rvms.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final BranchRepository branchRepository;
    private final VehicleMapper vehicleMapper;

    public List<VehicleResponse> getAll() {
        return vehicleRepository.findAll().stream().map(vehicleMapper::toResponse).toList();
    }

    public List<VehicleResponse> getByBranch(Long branchId) {
        return vehicleRepository.findByBranchId(branchId).stream().map(vehicleMapper::toResponse).toList();
    }

    public VehicleResponse getById(Long id) {
        return vehicleMapper.toResponse(findEntity(id));
    }

    public List<VehicleResponse> search(VehicleSearchRequest request) {
        LocalDate start = request.startDate() != null ? request.startDate() : LocalDate.now();
        LocalDate end = request.endDate() != null ? request.endDate() : start.plusDays(1);
        if (end.isBefore(start)) {
            throw new BusinessRuleException("endDate cannot be before startDate");
        }
        VehicleType type = parseType(request.type());
        return vehicleRepository.findAvailableVehicles(request.branchId(), type, request.maxDailyRate(), start, end)
                .stream().map(vehicleMapper::toResponse).toList();
    }

    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        if (vehicleRepository.existsByPlateNumberIgnoreCase(request.plateNumber())) {
            throw new DuplicateResourceException("A vehicle already exists with plate number: " + request.plateNumber());
        }
        if (vehicleRepository.existsByVinIgnoreCase(request.vin())) {
            throw new DuplicateResourceException("A vehicle already exists with VIN: " + request.vin());
        }
        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> ResourceNotFoundException.of("Branch", request.branchId()));

        Vehicle vehicle = Vehicle.builder()
                .plateNumber(request.plateNumber().toUpperCase())
                .vin(request.vin().toUpperCase())
                .type(parseType(request.type()))
                .brand(request.brand())
                .model(request.model())
                .year(request.year())
                .mileage(request.mileage())
                .fuelType(FuelType.valueOf(request.fuelType().toUpperCase()))
                .transmission(TransmissionType.valueOf(request.transmission().toUpperCase()))
                .dailyRate(request.dailyRate())
                .status(request.status() != null ? VehicleStatus.valueOf(request.status().toUpperCase()) : VehicleStatus.AVAILABLE)
                .branch(branch)
                .build();

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = findEntity(id);

        if (!vehicle.getPlateNumber().equalsIgnoreCase(request.plateNumber())
                && vehicleRepository.existsByPlateNumberIgnoreCase(request.plateNumber())) {
            throw new DuplicateResourceException("A vehicle already exists with plate number: " + request.plateNumber());
        }
        if (!vehicle.getVin().equalsIgnoreCase(request.vin())
                && vehicleRepository.existsByVinIgnoreCase(request.vin())) {
            throw new DuplicateResourceException("A vehicle already exists with VIN: " + request.vin());
        }

        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> ResourceNotFoundException.of("Branch", request.branchId()));

        vehicle.setPlateNumber(request.plateNumber().toUpperCase());
        vehicle.setVin(request.vin().toUpperCase());
        vehicle.setType(parseType(request.type()));
        vehicle.setBrand(request.brand());
        vehicle.setModel(request.model());
        vehicle.setYear(request.year());
        vehicle.setMileage(request.mileage());
        vehicle.setFuelType(FuelType.valueOf(request.fuelType().toUpperCase()));
        vehicle.setTransmission(TransmissionType.valueOf(request.transmission().toUpperCase()));
        vehicle.setDailyRate(request.dailyRate());
        if (request.status() != null) {
            vehicle.setStatus(VehicleStatus.valueOf(request.status().toUpperCase()));
        }
        vehicle.setBranch(branch);

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void delete(Long id) {
        Vehicle vehicle = findEntity(id);
        if (vehicle.getStatus() == VehicleStatus.RENTED || vehicle.getStatus() == VehicleStatus.RESERVED) {
            throw new BusinessRuleException("Cannot delete a vehicle that is currently rented or reserved.");
        }
        vehicleRepository.delete(vehicle);
    }

    Vehicle findEntity(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Vehicle", id));
    }

    private VehicleType parseType(String type) {
        if (type == null) {
            return null;
        }
        try {
            return VehicleType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Unknown vehicle type: " + type);
        }
    }
}
