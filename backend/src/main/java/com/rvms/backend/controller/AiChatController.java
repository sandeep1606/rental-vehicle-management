package com.rvms.backend.controller;

import com.rvms.backend.ai.AiAssistantService;
import com.rvms.backend.dto.ai.ChatRequest;
import com.rvms.backend.dto.ai.ChatResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "LangChain4j + LangGraph4j powered rental assistant (staff and customers)")
public class AiChatController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) throws Exception {
        return aiAssistantService.chat(request);
    }
}
