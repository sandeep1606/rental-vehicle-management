package com.rvms.backend.service;

import com.rvms.backend.dto.auth.AuthResponse;
import com.rvms.backend.dto.auth.LoginRequest;
import com.rvms.backend.dto.auth.RegisterRequest;
import com.rvms.backend.entity.Branch;
import com.rvms.backend.entity.User;
import com.rvms.backend.exception.DuplicateResourceException;
import com.rvms.backend.exception.ResourceNotFoundException;
import com.rvms.backend.repository.BranchRepository;
import com.rvms.backend.repository.UserRepository;
import com.rvms.backend.security.JwtProperties;
import com.rvms.backend.security.JwtService;
import com.rvms.backend.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account already exists with email: " + request.email());
        }

        Branch branch = null;
        if (request.branchId() != null) {
            branch = branchRepository.findById(request.branchId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Branch", request.branchId()));
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phone(request.phone())
                .role(request.roleEnum())
                .branch(branch)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        return buildAuthResponse(new SecurityUser(saved));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.email()));

        return buildAuthResponse(new SecurityUser(user));
    }

    private AuthResponse buildAuthResponse(SecurityUser securityUser) {
        String token = jwtService.generateToken(securityUser);
        String refreshToken = jwtService.generateRefreshToken(securityUser);
        User user = securityUser.getUser();

        return new AuthResponse(
                token,
                refreshToken,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getBranch() != null ? user.getBranch().getId() : null,
                jwtProperties.expirationMs()
        );
    }
}
