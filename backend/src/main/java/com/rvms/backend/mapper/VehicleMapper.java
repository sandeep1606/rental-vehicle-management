package com.rvms.backend.mapper;

import com.rvms.backend.dto.vehicle.VehicleResponse;
import com.rvms.backend.entity.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getPlateNumber(),
                vehicle.getVin(),
                vehicle.getType().name(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getMileage(),
                vehicle.getFuelType().name(),
                vehicle.getTransmission().name(),
                vehicle.getDailyRate(),
                vehicle.getStatus().name(),
                vehicle.getBranch().getId(),
                vehicle.getBranch().getName()
        );
    }
}
