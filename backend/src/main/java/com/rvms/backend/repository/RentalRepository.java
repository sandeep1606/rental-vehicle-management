package com.rvms.backend.repository;

import com.rvms.backend.entity.Rental;
import com.rvms.backend.entity.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByStatus(RentalStatus status);
    List<Rental> findByBranchIdAndStatus(Long branchId, RentalStatus status);
    List<Rental> findByCustomerId(Long customerId);
    List<Rental> findByStatusAndPlannedEndDateBetween(RentalStatus status, LocalDate from, LocalDate to);
    Optional<Rental> findByVehicleIdAndStatus(Long vehicleId, RentalStatus status);
    long countByBranchIdAndStatus(Long branchId, RentalStatus status);
}
