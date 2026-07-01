package com.rvms.backend.mapper;

import com.rvms.backend.dto.customer.CustomerResponse;
import com.rvms.backend.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getDriverLicenseNumber(),
                customer.getAddress(),
                customer.getDateOfBirth(),
                customer.isBlacklisted()
        );
    }
}
