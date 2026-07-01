package com.rvms.backend.mapper;

import com.rvms.backend.dto.maintenance.MaintenanceResponse;
import com.rvms.backend.entity.MaintenanceRecord;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceMapper {

    public MaintenanceResponse toResponse(MaintenanceRecord m) {
        return new MaintenanceResponse(
                m.getId(),
                m.getVehicle().getId(),
                m.getVehicle().getPlateNumber(),
                m.getBranch().getId(),
                m.getDescription(),
                m.getCost(),
                m.getStatus().name(),
                m.getScheduledDate(),
                m.getCompletedDate()
        );
    }
}
