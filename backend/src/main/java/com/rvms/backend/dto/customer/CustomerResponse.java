package com.rvms.backend.dto.customer;

import java.io.Serializable;
import java.time.LocalDate;

public record CustomerResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String driverLicenseNumber,
        String address,
        LocalDate dateOfBirth,
        boolean blacklisted
) implements Serializable {}
