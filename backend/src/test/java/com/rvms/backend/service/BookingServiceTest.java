package com.rvms.backend.service;

import com.rvms.backend.config.RentalProperties;
import com.rvms.backend.dto.booking.ReservationRequest;
import com.rvms.backend.dto.booking.ReturnVehicleRequest;
import com.rvms.backend.entity.*;
import com.rvms.backend.exception.BusinessRuleException;
import com.rvms.backend.exception.VehicleNotAvailableException;
import com.rvms.backend.mapper.BookingMapper;
import com.rvms.backend.repository.CustomerRepository;
import com.rvms.backend.repository.RentalRepository;
import com.rvms.backend.repository.ReservationRepository;
import com.rvms.backend.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private RentalRepository rentalRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private CustomerRepository customerRepository;

    private BookingService bookingService;

    private Branch branch;
    private Vehicle vehicle;
    private Customer customer;

    @BeforeEach
    void setUp() {
        RentalProperties rentalProperties = new RentalProperties(new BigDecimal("1.5"), 2);
        bookingService = new BookingService(
                reservationRepository, rentalRepository, vehicleRepository, customerRepository,
                new BookingMapper(), new RentalPricingService(), rentalProperties);

        branch = Branch.builder().id(1L).name("Downtown Central").active(true).build();
        vehicle = Vehicle.builder()
                .id(10L).plateNumber("DTN-1001").vin("VIN1").type(VehicleType.SEDAN)
                .brand("Toyota").model("Camry").year(2023).mileage(1000)
                .fuelType(FuelType.PETROL).transmission(TransmissionType.AUTOMATIC)
                .dailyRate(new BigDecimal("50.00")).status(VehicleStatus.AVAILABLE).branch(branch)
                .build();
        customer = Customer.builder()
                .id(20L).fullName("John Smith").email("john@example.com").phone("555-0000")
                .driverLicenseNumber("DL-1").blacklisted(false)
                .build();
    }

    @Test
    void createReservationRejectsBlacklistedCustomer() {
        customer.setBlacklisted(true);
        when(customerRepository.findById(20L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));

        ReservationRequest request = new ReservationRequest(20L, 10L, LocalDate.now(), LocalDate.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createReservation(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("blacklisted");

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservationRejectsUnavailableVehicle() {
        when(customerRepository.findById(20L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.findAvailableVehicles(any(), any(), any(), any(), any())).thenReturn(List.of());

        ReservationRequest request = new ReservationRequest(20L, 10L, LocalDate.now(), LocalDate.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createReservation(request))
                .isInstanceOf(VehicleNotAvailableException.class);
    }

    @Test
    void createReservationSucceedsAndMarksVehicleReserved() {
        when(customerRepository.findById(20L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.findAvailableVehicles(any(), any(), any(), any(), any())).thenReturn(List.of(vehicle));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationRequest request = new ReservationRequest(20L, 10L, LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 10));
        var response = bookingService.createReservation(request);

        assertThat(response.status()).isEqualTo("CONFIRMED");
        assertThat(response.estimatedTotal()).isEqualByComparingTo("250.00");

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertThat(vehicleCaptor.getValue().getStatus()).isEqualTo(VehicleStatus.RESERVED);
    }

    @Test
    void cancelReservationRejectsAlreadyConvertedReservation() {
        Reservation reservation = Reservation.builder()
                .id(1L).customer(customer).vehicle(vehicle).branch(branch)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(1))
                .status(ReservationStatus.CONVERTED).build();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> bookingService.cancelReservation(1L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void returnVehicleAppliesLateFeeAndFreesUpVehicle() {
        vehicle.setStatus(VehicleStatus.RENTED);
        Rental rental = Rental.builder()
                .id(5L).customer(customer).vehicle(vehicle).branch(branch)
                .startDate(LocalDate.of(2026, 7, 5))
                .plannedEndDate(LocalDate.of(2026, 7, 10))
                .dailyRate(new BigDecimal("50.00"))
                .status(RentalStatus.ACTIVE)
                .lateFee(BigDecimal.ZERO)
                .build();
        when(rentalRepository.findById(5L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = bookingService.returnVehicle(5L, new ReturnVehicleRequest(LocalDate.of(2026, 7, 12)));

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.lateFee()).isEqualByComparingTo("150.00");
        assertThat(response.totalAmount()).isEqualByComparingTo("400.00");

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertThat(vehicleCaptor.getValue().getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }
}
