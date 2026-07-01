package com.rvms.backend.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        String sessionId,
        @NotBlank String message
) {}
