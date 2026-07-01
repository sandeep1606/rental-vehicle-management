package com.rvms.backend.repository;

import com.rvms.backend.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByActiveTrue();
    boolean existsByNameIgnoreCase(String name);
}
