package com.rvms.backend.mapper;

import com.rvms.backend.dto.branch.BranchResponse;
import com.rvms.backend.entity.Branch;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public BranchResponse toResponse(Branch branch) {
        return new BranchResponse(
                branch.getId(),
                branch.getName(),
                branch.getAddress(),
                branch.getPhone(),
                branch.getManagerName(),
                branch.getOpeningHours(),
                branch.isActive(),
                branch.getCreatedAt()
        );
    }
}
