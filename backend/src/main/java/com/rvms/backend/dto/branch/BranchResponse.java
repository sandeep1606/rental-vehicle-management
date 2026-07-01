package com.rvms.backend.dto.branch;

import java.time.Instant;

public record BranchResponse(
        Long id,
        String name,
        String address,
        String phone,
        String managerName,
        String openingHours,
        boolean active,
        Instant createdAt
) {}
