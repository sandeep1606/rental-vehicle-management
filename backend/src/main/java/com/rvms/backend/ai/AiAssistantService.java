package com.rvms.backend.ai;

import com.rvms.backend.ai.graph.RentalAssistantGraphBuilder;
import com.rvms.backend.ai.graph.RentalAssistantState;
import com.rvms.backend.dto.ai.ChatRequest;
import com.rvms.backend.dto.ai.ChatResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Entry point used by AiChatController: runs a chat message through the compiled LangGraph4j workflow. */
@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private final RentalAssistantGraphBuilder graphBuilder;
    private CompiledGraph<RentalAssistantState> compiledGraph;

    @PostConstruct
    public void init() throws Exception {
        this.compiledGraph = graphBuilder.build();
    }

    public ChatResponse chat(ChatRequest request) throws Exception {
        String sessionId = request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();

        Optional<RentalAssistantState> result = compiledGraph.invoke(Map.of(
                RentalAssistantState.SESSION_ID, sessionId,
                RentalAssistantState.USER_MESSAGE, request.message()
        ));

        RentalAssistantState state = result.orElseThrow(() ->
                new IllegalStateException("AI workflow did not produce a final state"));

        return new ChatResponse(
                sessionId,
                state.finalResponse().orElse("I'm not sure how to help with that yet."),
                state.intent().map(Enum::name).orElse("GENERAL"),
                state.toolsUsed()
        );
    }
}
