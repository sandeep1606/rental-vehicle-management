package com.rvms.backend.repository;

import com.rvms.backend.entity.Vehicle;
import com.rvms.backend.entity.VehicleStatus;
import com.rvms.backend.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByBranchId(Long branchId);

    List<Vehicle> findByStatus(VehicleStatus status);

    Optional<Vehicle> findByPlateNumberIgnoreCase(String plateNumber);

    Optional<Vehicle> findByVinIgnoreCase(String vin);

    boolean existsByPlateNumberIgnoreCase(String plateNumber);

    boolean existsByVinIgnoreCase(String vin);

    long countByBranchId(Long branchId);

    long countByBranchIdAndStatus(Long branchId, VehicleStatus status);

    /**
     * A vehicle is available for a date range if it is not RETIRED/MAINTENANCE
     * and has no overlapping ACTIVE rental or PENDING/CONFIRMED reservation.
     */
    @Query("""
        SELECT v FROM Vehicle v
        WHERE (:branchId IS NULL OR v.branch.id = :branchId)
        AND (:type IS NULL OR v.type = :type)
        AND (:maxDailyRate IS NULL OR v.dailyRate <= :maxDailyRate)
        AND v.status <> com.rvms.backend.entity.VehicleStatus.RETIRED
        AND v.status <> com.rvms.backend.entity.VehicleStatus.MAINTENANCE
        AND v.id NOT IN (
            SELECT r.vehicle.id FROM Rental r
            WHERE r.status = com.rvms.backend.entity.RentalStatus.ACTIVE
            AND r.startDate <= :endDate AND r.plannedEndDate >= :startDate
        )
        AND v.id NOT IN (
            SELECT res.vehicle.id FROM Reservation res
            WHERE res.status IN (com.rvms.backend.entity.ReservationStatus.PENDING, com.rvms.backend.entity.ReservationStatus.CONFIRMED)
            AND res.startDate <= :endDate AND res.endDate >= :startDate
        )
        """)
    List<Vehicle> findAvailableVehicles(
            @Param("branchId") Long branchId,
            @Param("type") VehicleType type,
            @Param("maxDailyRate") BigDecimal maxDailyRate,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
