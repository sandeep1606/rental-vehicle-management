package com.rvms.backend.service;

import com.rvms.backend.dto.auth.RegisterRequest;
import com.rvms.backend.entity.User;
import com.rvms.backend.exception.DuplicateResourceException;
import com.rvms.backend.repository.BranchRepository;
import com.rvms.backend.repository.UserRepository;
import com.rvms.backend.security.JwtProperties;
import com.rvms.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
                "test-secret-key-that-is-long-enough-for-hmac-sha-signing-1234567890", 3600000L, 604800000L, "rvms-test");
        JwtService jwtService = new JwtService(jwtProperties);
        authService = new AuthService(userRepository, branchRepository, passwordEncoder, authenticationManager, jwtService, jwtProperties);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest("taken@example.com", "Password123!", "Someone", null, "CUSTOMER", null);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerSucceedsAndIssuesToken() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        RegisterRequest request = new RegisterRequest("new@example.com", "Password123!", "New User", null, "CUSTOMER", null);
        var response = authService.register(request);

        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.role()).isEqualTo("CUSTOMER");
        assertThat(response.token()).isNotBlank();
    }
}
