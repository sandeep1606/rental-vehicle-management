package com.rvms.backend.service;

import com.rvms.backend.config.RentalProperties;
import com.rvms.backend.dto.booking.RentalResponse;
import com.rvms.backend.dto.booking.ReservationRequest;
import com.rvms.backend.dto.booking.ReservationResponse;
import com.rvms.backend.dto.booking.ReturnVehicleRequest;
import com.rvms.backend.entity.*;
import com.rvms.backend.exception.BusinessRuleException;
import com.rvms.backend.exception.ResourceNotFoundException;
import com.rvms.backend.exception.VehicleNotAvailableException;
import com.rvms.backend.mapper.BookingMapper;
import com.rvms.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final ReservationRepository reservationRepository;
    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final BookingMapper bookingMapper;
    private final RentalPricingService pricingService;
    private final RentalProperties rentalProperties;

    public List<ReservationResponse> getReservations() {
        return reservationRepository.findAll().stream().map(bookingMapper::toResponse).toList();
    }

    public List<RentalResponse> getRentals() {
        return rentalRepository.findAll().stream().map(bookingMapper::toResponse).toList();
    }

    public List<RentalResponse> getActiveRentals() {
        return rentalRepository.findByStatus(RentalStatus.ACTIVE).stream().map(bookingMapper::toResponse).toList();
    }

    public RentalResponse getRentalById(Long id) {
        return bookingMapper.toResponse(findRental(id));
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        Customer customer = findCustomer(request.customerId());
        Vehicle vehicle = findVehicle(request.vehicleId());
        assertBookable(customer, vehicle, request.startDate(), request.endDate());

        Reservation reservation = Reservation.builder()
                .customer(customer)
                .vehicle(vehicle)
                .branch(vehicle.getBranch())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(ReservationStatus.CONFIRMED)
                .estimatedTotal(pricingService.estimateTotal(vehicle.getDailyRate(), request.startDate(), request.endDate()))
                .build();
        reservation = reservationRepository.save(reservation);

        vehicle.setStatus(VehicleStatus.RESERVED);
        vehicleRepository.save(vehicle);

        return bookingMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reservation", reservationId));

        if (reservation.getStatus() == ReservationStatus.CONVERTED) {
            throw new BusinessRuleException("Cannot cancel a reservation that has already been converted to a rental.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        Vehicle vehicle = reservation.getVehicle();
        if (vehicle.getStatus() == VehicleStatus.RESERVED) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        return bookingMapper.toResponse(reservation);
    }

    @Transactional
    public RentalResponse convertToRental(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reservation", reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED && reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessRuleException("Only a pending/confirmed reservation can be converted to a rental.");
        }

        Vehicle vehicle = reservation.getVehicle();
        Rental rental = Rental.builder()
                .reservation(reservation)
                .customer(reservation.getCustomer())
                .vehicle(vehicle)
                .branch(reservation.getBranch())
                .startDate(reservation.getStartDate())
                .plannedEndDate(reservation.getEndDate())
                .dailyRate(vehicle.getDailyRate())
                .status(RentalStatus.ACTIVE)
                .build();
        rental = rentalRepository.save(rental);

        reservation.setStatus(ReservationStatus.CONVERTED);
        reservationRepository.save(reservation);

        vehicle.setStatus(VehicleStatus.RENTED);
        vehicleRepository.save(vehicle);

        return bookingMapper.toResponse(rental);
    }

    /** Walk-in rental: skips the reservation step entirely for staff-assisted bookings. */
    @Transactional
    public RentalResponse createDirectRental(ReservationRequest request) {
        Customer customer = findCustomer(request.customerId());
        Vehicle vehicle = findVehicle(request.vehicleId());
        assertBookable(customer, vehicle, request.startDate(), request.endDate());

        Rental rental = Rental.builder()
                .customer(customer)
                .vehicle(vehicle)
                .branch(vehicle.getBranch())
                .startDate(request.startDate())
                .plannedEndDate(request.endDate())
                .dailyRate(vehicle.getDailyRate())
                .status(RentalStatus.ACTIVE)
                .build();
        rental = rentalRepository.save(rental);

        vehicle.setStatus(VehicleStatus.RENTED);
        vehicleRepository.save(vehicle);

        return bookingMapper.toResponse(rental);
    }

    @Transactional
    public RentalResponse returnVehicle(Long rentalId, ReturnVehicleRequest request) {
        Rental rental = findRental(rentalId);
        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new BusinessRuleException("Only an active rental can be returned.");
        }
        if (request.actualReturnDate().isBefore(rental.getStartDate())) {
            throw new BusinessRuleException("Return date cannot be before the rental start date.");
        }

        var breakdown = pricingService.calculateFinalPrice(
                rental.getDailyRate(),
                rental.getStartDate(),
                rental.getPlannedEndDate(),
                request.actualReturnDate(),
                rentalProperties.lateFeeMultiplier()
        );

        rental.setActualReturnDate(request.actualReturnDate());
        rental.setTotalAmount(breakdown.totalAmount());
        rental.setLateFee(breakdown.lateFee());
        rental.setStatus(RentalStatus.COMPLETED);
        rental = rentalRepository.save(rental);

        Vehicle vehicle = rental.getVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);

        return bookingMapper.toResponse(rental);
    }

    private void assertBookable(Customer customer, Vehicle vehicle, java.time.LocalDate start, java.time.LocalDate end) {
        if (customer.isBlacklisted()) {
            throw new BusinessRuleException("Customer " + customer.getFullName() + " is blacklisted and cannot book vehicles.");
        }
        List<Vehicle> available = vehicleRepository.findAvailableVehicles(vehicle.getBranch().getId(), null, null, start, end);
        boolean isAvailable = available.stream().anyMatch(v -> v.getId().equals(vehicle.getId()));
        if (!isAvailable) {
            throw new VehicleNotAvailableException(
                    "Vehicle " + vehicle.getPlateNumber() + " is not available for the requested date range.");
        }
    }

    private Rental findRental(Long id) {
        return rentalRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Rental", id));
    }

    private Vehicle findVehicle(Long id) {
        return vehicleRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Vehicle", id));
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Customer", id));
    }
}
