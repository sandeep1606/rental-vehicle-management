package com.rvms.backend.service;

import com.rvms.backend.dto.branch.BranchRequest;
import com.rvms.backend.dto.branch.BranchResponse;
import com.rvms.backend.entity.Branch;
import com.rvms.backend.exception.BusinessRuleException;
import com.rvms.backend.exception.DuplicateResourceException;
import com.rvms.backend.exception.ResourceNotFoundException;
import com.rvms.backend.mapper.BranchMapper;
import com.rvms.backend.repository.BranchRepository;
import com.rvms.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final VehicleRepository vehicleRepository;
    private final BranchMapper branchMapper;

    public List<BranchResponse> getAll() {
        return branchRepository.findAll().stream().map(branchMapper::toResponse).toList();
    }

    public BranchResponse getById(Long id) {
        return branchMapper.toResponse(findEntity(id));
    }

    @Transactional
    public BranchResponse create(BranchRequest request) {
        if (branchRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("A branch already exists with name: " + request.name());
        }
        Branch branch = Branch.builder()
                .name(request.name())
                .address(request.address())
                .phone(request.phone())
                .managerName(request.managerName())
                .openingHours(request.openingHours())
                .active(request.active() == null || request.active())
                .build();
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Transactional
    public BranchResponse update(Long id, BranchRequest request) {
        Branch branch = findEntity(id);
        branch.setName(request.name());
        branch.setAddress(request.address());
        branch.setPhone(request.phone());
        branch.setManagerName(request.managerName());
        branch.setOpeningHours(request.openingHours());
        if (request.active() != null) {
            branch.setActive(request.active());
        }
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Transactional
    public void delete(Long id) {
        Branch branch = findEntity(id);
        long vehicleCount = vehicleRepository.countByBranchId(id);
        if (vehicleCount > 0) {
            throw new BusinessRuleException(
                    "Cannot delete branch with " + vehicleCount + " assigned vehicle(s). Reassign or remove them first, or deactivate the branch instead.");
        }
        branchRepository.delete(branch);
    }

    private Branch findEntity(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Branch", id));
    }
}
