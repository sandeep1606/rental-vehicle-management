package com.rvms.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.rental")
public record RentalProperties(
        BigDecimal lateFeeMultiplier,
        int gracePeriodHours
) {}
