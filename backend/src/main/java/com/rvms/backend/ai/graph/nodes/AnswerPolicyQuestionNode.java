package com.rvms.backend.ai.graph.nodes;

import com.rvms.backend.ai.graph.RentalAssistantState;
import com.rvms.backend.ai.rag.PolicyQaAssistant;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Node 5: answerPolicyQuestion — RAG over the ingested rental policy documents
 * (see PolicyIngestionService / PolicyQaAssistant). The system prompt forbids the
 * model from answering outside the retrieved context.
 */
@Component
@RequiredArgsConstructor
public class AnswerPolicyQuestionNode implements NodeAction<RentalAssistantState> {

    private final PolicyQaAssistant policyQaAssistant;

    @Override
    public Map<String, Object> apply(RentalAssistantState state) {
        String answer = policyQaAssistant.answer(state.userMessage());

        List<String> tools = new ArrayList<>(state.toolsUsed());
        tools.add("PolicyQaAssistant.answer (RAG)");

        return Map.of(
                RentalAssistantState.POLICY_ANSWER, answer,
                RentalAssistantState.TOOLS_USED, tools
        );
    }
}
