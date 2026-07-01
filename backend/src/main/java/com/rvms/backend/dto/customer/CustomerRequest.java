package com.rvms.backend.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record CustomerRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String phone,
        @NotBlank String driverLicenseNumber,
        String address,
        @Past LocalDate dateOfBirth,
        Boolean blacklisted
) {}
