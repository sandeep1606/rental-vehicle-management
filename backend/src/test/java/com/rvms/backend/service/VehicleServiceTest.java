package com.rvms.backend.service;

import com.rvms.backend.dto.vehicle.VehicleRequest;
import com.rvms.backend.entity.Branch;
import com.rvms.backend.entity.Vehicle;
import com.rvms.backend.entity.VehicleStatus;
import com.rvms.backend.exception.BusinessRuleException;
import com.rvms.backend.exception.DuplicateResourceException;
import com.rvms.backend.mapper.VehicleMapper;
import com.rvms.backend.repository.BranchRepository;
import com.rvms.backend.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private BranchRepository branchRepository;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(vehicleRepository, branchRepository, new VehicleMapper());
    }

    private VehicleRequest sampleRequest() {
        return new VehicleRequest("DTN-9999", "VIN12345678900", "SEDAN", "Toyota", "Camry",
                2023, 1000, "PETROL", "AUTOMATIC", new BigDecimal("55.00"), "AVAILABLE", 1L);
    }

    @Test
    void createRejectsDuplicatePlateNumber() {
        when(vehicleRepository.existsByPlateNumberIgnoreCase("DTN-9999")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.create(sampleRequest()))
                .isInstanceOf(DuplicateResourceException.class);

        verify(branchRepository, never()).findById(any());
    }

    @Test
    void createRejectsDuplicateVin() {
        when(vehicleRepository.existsByPlateNumberIgnoreCase("DTN-9999")).thenReturn(false);
        when(vehicleRepository.existsByVinIgnoreCase("VIN12345678900")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.create(sampleRequest()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void deleteRejectsRentedVehicle() {
        Branch branch = Branch.builder().id(1L).name("Downtown Central").build();
        Vehicle vehicle = Vehicle.builder().id(1L).status(VehicleStatus.RENTED).branch(branch).build();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> vehicleService.delete(1L))
                .isInstanceOf(BusinessRuleException.class);

        verify(vehicleRepository, never()).delete(any());
    }

    @Test
    void deleteAllowsAvailableVehicle() {
        Branch branch = Branch.builder().id(1L).name("Downtown Central").build();
        Vehicle vehicle = Vehicle.builder().id(1L).status(VehicleStatus.AVAILABLE).branch(branch).build();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        vehicleService.delete(1L);

        verify(vehicleRepository).delete(vehicle);
    }
}
