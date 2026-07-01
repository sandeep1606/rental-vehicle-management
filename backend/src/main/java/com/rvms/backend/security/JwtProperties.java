package com.rvms.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long expirationMs,
        long refreshExpirationMs,
        String issuer
) {}
