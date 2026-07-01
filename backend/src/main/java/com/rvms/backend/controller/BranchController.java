package com.rvms.backend.controller;

import com.rvms.backend.dto.branch.BranchRequest;
import com.rvms.backend.dto.branch.BranchResponse;
import com.rvms.backend.service.BranchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Manage the 4 rental branches/locations")
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public List<BranchResponse> getAll() {
        return branchService.getAll();
    }

    @GetMapping("/{id}")
    public BranchResponse getById(@PathVariable Long id) {
        return branchService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<BranchResponse> create(@Valid @RequestBody BranchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public BranchResponse update(@PathVariable Long id, @Valid @RequestBody BranchRequest request) {
        return branchService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        branchService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
