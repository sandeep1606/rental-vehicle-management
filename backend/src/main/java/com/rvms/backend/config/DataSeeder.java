package com.rvms.backend.config;

import com.rvms.backend.entity.Branch;
import com.rvms.backend.entity.Role;
import com.rvms.backend.entity.User;
import com.rvms.backend.repository.BranchRepository;
import com.rvms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Seeds demo login accounts on first startup. Flyway seeds branches/vehicles/customers
 * (see V2__seed_data.sql); user accounts are created here so their passwords go through
 * the real PasswordEncoder instead of a hand-computed hash baked into a SQL migration.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Password123!";

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        List<Branch> branches = branchRepository.findAll();
        Optional<Branch> downtown = branches.stream().filter(b -> b.getName().equals("Downtown Central")).findFirst();
        Optional<Branch> airport = branches.stream().filter(b -> b.getName().equals("Airport Terminal")).findFirst();
        Optional<Branch> uptown = branches.stream().filter(b -> b.getName().equals("Uptown")).findFirst();
        Optional<Branch> suburban = branches.stream().filter(b -> b.getName().equals("Suburban Mall")).findFirst();

        createUser("admin@rvms.com", "System Admin", Role.ADMIN, null);
        downtown.ifPresent(b -> createUser("manager.downtown@rvms.com", "Alice Johnson", Role.BRANCH_MANAGER, b));
        airport.ifPresent(b -> createUser("manager.airport@rvms.com", "Brian Lee", Role.BRANCH_MANAGER, b));
        uptown.ifPresent(b -> createUser("manager.uptown@rvms.com", "Carla Gomez", Role.BRANCH_MANAGER, b));
        suburban.ifPresent(b -> createUser("manager.suburban@rvms.com", "David Kim", Role.BRANCH_MANAGER, b));
        downtown.ifPresent(b -> createUser("staff.downtown@rvms.com", "Nina Rossi", Role.STAFF, b));
        airport.ifPresent(b -> createUser("staff.airport@rvms.com", "Omar Farouk", Role.STAFF, b));
        createUser("customer.demo@rvms.com", "Demo Customer", Role.CUSTOMER, null);

        log.info("Seeded demo user accounts (password for all: '{}')", DEMO_PASSWORD);
    }

    private void createUser(String email, String fullName, Role role, Branch branch) {
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                .fullName(fullName)
                .role(role)
                .branch(branch)
                .enabled(true)
                .build();
        userRepository.save(user);
    }
}
