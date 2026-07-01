package com.rvms.backend.repository;

import com.rvms.backend.entity.Reservation;
import com.rvms.backend.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByCustomerId(Long customerId);
    List<Reservation> findByBranchId(Long branchId);
}
