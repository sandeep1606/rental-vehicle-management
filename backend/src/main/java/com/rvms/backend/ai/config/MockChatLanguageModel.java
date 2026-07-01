package com.rvms.backend.ai.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;

import java.util.List;

/**
 * Deterministic stand-in for a real LLM so the assistant is fully runnable without an
 * OPENAI_API_KEY (offline demo / CI). It never invents vehicle, price, or booking facts —
 * it only ever paraphrases whatever context/tool-call results were already placed in the
 * prompt by the calling node. Swapped out for a real OpenAiChatModel when
 * langchain4j.ai.mock-mode=false and an API key is configured.
 */
public class MockChatLanguageModel implements ChatLanguageModel {

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        String lastUserText = messages.isEmpty() ? "" : messages.get(messages.size() - 1).text();
        String reply = "Based on the information available: " + summarize(lastUserText);
        return Response.from(AiMessage.from(reply));
    }

    private String summarize(String text) {
        if (text == null || text.isBlank()) {
            return "I don't have enough information to answer that.";
        }
        String trimmed = text.strip();
        return trimmed.length() > 600 ? trimmed.substring(0, 600) + "..." : trimmed;
    }
}
