package com.rvms.backend.service;

import com.rvms.backend.cache.CustomerSearchCacheService;
import com.rvms.backend.dto.customer.CustomerRequest;
import com.rvms.backend.dto.customer.CustomerResponse;
import com.rvms.backend.entity.Customer;
import com.rvms.backend.exception.DuplicateResourceException;
import com.rvms.backend.exception.ResourceNotFoundException;
import com.rvms.backend.mapper.CustomerMapper;
import com.rvms.backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerSearchCacheService searchCacheService;

    public List<CustomerResponse> getAll() {
        return customerRepository.findAll().stream().map(customerMapper::toResponse).toList();
    }

    public CustomerResponse getById(Long id) {
        return customerMapper.toResponse(findEntity(id));
    }

    /** Cached via LRU (Redis, falling back to in-memory) — see CustomerSearchCacheService. */
    public List<CustomerResponse> search(String term) {
        return searchCacheService.search(term);
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("A customer already exists with email: " + request.email());
        }
        if (customerRepository.existsByDriverLicenseNumberIgnoreCase(request.driverLicenseNumber())) {
            throw new DuplicateResourceException("A customer already exists with license number: " + request.driverLicenseNumber());
        }

        Customer customer = Customer.builder()
                .fullName(request.fullName())
                .email(request.email())
                .phone(request.phone())
                .driverLicenseNumber(request.driverLicenseNumber())
                .address(request.address())
                .dateOfBirth(request.dateOfBirth())
                .blacklisted(request.blacklisted() != null && request.blacklisted())
                .build();

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findEntity(id);

        if (!customer.getEmail().equalsIgnoreCase(request.email())
                && customerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("A customer already exists with email: " + request.email());
        }
        if (!customer.getDriverLicenseNumber().equalsIgnoreCase(request.driverLicenseNumber())
                && customerRepository.existsByDriverLicenseNumberIgnoreCase(request.driverLicenseNumber())) {
            throw new DuplicateResourceException("A customer already exists with license number: " + request.driverLicenseNumber());
        }

        customer.setFullName(request.fullName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setDriverLicenseNumber(request.driverLicenseNumber());
        customer.setAddress(request.address());
        customer.setDateOfBirth(request.dateOfBirth());
        if (request.blacklisted() != null) {
            customer.setBlacklisted(request.blacklisted());
        }

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = findEntity(id);
        customerRepository.delete(customer);
    }

    private Customer findEntity(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", id));
    }
}
