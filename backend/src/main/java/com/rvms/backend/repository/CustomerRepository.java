package com.rvms.backend.repository;

import com.rvms.backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmailIgnoreCase(String email);

    Optional<Customer> findByDriverLicenseNumberIgnoreCase(String licenseNumber);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDriverLicenseNumberIgnoreCase(String licenseNumber);

    @Query("""
        SELECT c FROM Customer c
        WHERE lower(c.fullName) LIKE lower(concat('%', :term, '%'))
           OR lower(c.email) LIKE lower(concat('%', :term, '%'))
           OR c.phone LIKE concat('%', :term, '%')
           OR lower(c.driverLicenseNumber) LIKE lower(concat('%', :term, '%'))
        """)
    List<Customer> searchByTerm(@Param("term") String term);
}
