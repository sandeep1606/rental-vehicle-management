package com.rvms.backend.dto.auth;

public record AuthResponse(
        String token,
        String refreshToken,
        String tokenType,
        Long userId,
        String email,
        String fullName,
        String role,
        Long branchId,
        long expiresInMs
) {}
