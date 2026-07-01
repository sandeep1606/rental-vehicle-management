package com.rvms.backend.controller;

import com.rvms.backend.dto.vehicle.VehicleRequest;
import com.rvms.backend.dto.vehicle.VehicleResponse;
import com.rvms.backend.dto.vehicle.VehicleSearchRequest;
import com.rvms.backend.service.VehicleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle fleet management and availability search")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public List<VehicleResponse> getAll() {
        return vehicleService.getAll();
    }

    @GetMapping("/branch/{branchId}")
    public List<VehicleResponse> getByBranch(@PathVariable Long branchId) {
        return vehicleService.getByBranch(branchId);
    }

    @GetMapping("/{id}")
    public VehicleResponse getById(@PathVariable Long id) {
        return vehicleService.getById(id);
    }

    @GetMapping("/search")
    public List<VehicleResponse> search(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal maxDailyRate
    ) {
        return vehicleService.search(new VehicleSearchRequest(branchId, type, startDate, endDate, maxDailyRate));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','STAFF')")
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','STAFF')")
    public VehicleResponse update(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        return vehicleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
