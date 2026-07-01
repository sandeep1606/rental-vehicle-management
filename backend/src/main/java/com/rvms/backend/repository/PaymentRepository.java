package com.rvms.backend.repository;

import com.rvms.backend.entity.Payment;
import com.rvms.backend.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByRentalId(Long rentalId);
    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.rental.branch.id = :branchId AND p.status = com.rvms.backend.entity.PaymentStatus.PAID")
    BigDecimal sumRevenueByBranch(@Param("branchId") Long branchId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = com.rvms.backend.entity.PaymentStatus.PAID")
    BigDecimal sumTotalRevenue();
}
