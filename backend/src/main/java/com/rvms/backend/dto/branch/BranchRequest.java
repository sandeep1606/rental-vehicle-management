package com.rvms.backend.dto.branch;

import jakarta.validation.constraints.NotBlank;

public record BranchRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String phone,
        String managerName,
        String openingHours,
        Boolean active
) {}
