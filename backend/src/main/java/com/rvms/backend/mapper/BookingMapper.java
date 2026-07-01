package com.rvms.backend.mapper;

import com.rvms.backend.dto.booking.RentalResponse;
import com.rvms.backend.dto.booking.ReservationResponse;
import com.rvms.backend.entity.Rental;
import com.rvms.backend.entity.Reservation;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public ReservationResponse toResponse(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getCustomer().getId(),
                r.getCustomer().getFullName(),
                r.getVehicle().getId(),
                r.getVehicle().getPlateNumber(),
                r.getBranch().getId(),
                r.getStartDate(),
                r.getEndDate(),
                r.getStatus().name(),
                r.getEstimatedTotal()
        );
    }

    public RentalResponse toResponse(Rental r) {
        return new RentalResponse(
                r.getId(),
                r.getReservation() != null ? r.getReservation().getId() : null,
                r.getCustomer().getId(),
                r.getCustomer().getFullName(),
                r.getVehicle().getId(),
                r.getVehicle().getPlateNumber(),
                r.getBranch().getId(),
                r.getStartDate(),
                r.getPlannedEndDate(),
                r.getActualReturnDate(),
                r.getDailyRate(),
                r.getTotalAmount(),
                r.getLateFee(),
                r.getStatus().name()
        );
    }
}
