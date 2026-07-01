package com.rvms.backend.controller;

import com.rvms.backend.dto.booking.RentalResponse;
import com.rvms.backend.dto.booking.ReservationRequest;
import com.rvms.backend.dto.booking.ReservationResponse;
import com.rvms.backend.dto.booking.ReturnVehicleRequest;
import com.rvms.backend.service.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Reservations and rentals: create, convert, return, cancel")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/api/reservations")
    public List<ReservationResponse> getReservations() {
        return bookingService.getReservations();
    }

    @PostMapping("/api/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createReservation(request));
    }

    @PostMapping("/api/reservations/{id}/cancel")
    public ReservationResponse cancelReservation(@PathVariable Long id) {
        return bookingService.cancelReservation(id);
    }

    @PostMapping("/api/reservations/{id}/convert")
    public ResponseEntity<RentalResponse> convertToRental(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.convertToRental(id));
    }

    @GetMapping("/api/rentals")
    public List<RentalResponse> getRentals() {
        return bookingService.getRentals();
    }

    @GetMapping("/api/rentals/active")
    public List<RentalResponse> getActiveRentals() {
        return bookingService.getActiveRentals();
    }

    @GetMapping("/api/rentals/{id}")
    public RentalResponse getRental(@PathVariable Long id) {
        return bookingService.getRentalById(id);
    }

    @PostMapping("/api/rentals/direct")
    public ResponseEntity<RentalResponse> createDirectRental(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createDirectRental(request));
    }

    @PostMapping("/api/rentals/{id}/return")
    public RentalResponse returnVehicle(@PathVariable Long id, @Valid @RequestBody ReturnVehicleRequest request) {
        return bookingService.returnVehicle(id, request);
    }
}
