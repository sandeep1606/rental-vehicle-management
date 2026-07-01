package com.rvms.backend.controller;

import com.rvms.backend.dto.maintenance.MaintenanceCompleteRequest;
import com.rvms.backend.dto.maintenance.MaintenanceRequest;
import com.rvms.backend.dto.maintenance.MaintenanceResponse;
import com.rvms.backend.service.MaintenanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "Vehicle maintenance scheduling and completion")
@PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','STAFF')")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @GetMapping
    public List<MaintenanceResponse> getAll() {
        return maintenanceService.getAll();
    }

    @GetMapping("/vehicle/{vehicleId}")
    public List<MaintenanceResponse> getByVehicle(@PathVariable Long vehicleId) {
        return maintenanceService.getByVehicle(vehicleId);
    }

    @PostMapping
    public ResponseEntity<MaintenanceResponse> create(@Valid @RequestBody MaintenanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceService.create(request));
    }

    @PostMapping("/{id}/complete")
    public MaintenanceResponse complete(@PathVariable Long id, @Valid @RequestBody MaintenanceCompleteRequest request) {
        return maintenanceService.complete(id, request);
    }
}
