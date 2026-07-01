package com.rvms.backend.dto.ai;

import java.util.List;

public record ChatResponse(
        String sessionId,
        String reply,
        String intent,
        List<String> toolsUsed
) {}
