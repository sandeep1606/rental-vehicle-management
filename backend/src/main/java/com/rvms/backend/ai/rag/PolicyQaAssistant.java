package com.rvms.backend.ai.rag;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * LangChain4j AiServices interface for RAG-based policy Q&A. The framework injects
 * retrieved policy passages (via the configured ContentRetriever) into the prompt
 * automatically before calling the chat model.
 */
public interface PolicyQaAssistant {

    @SystemMessage("""
            You are a rental policy assistant for a vehicle rental company.
            Answer ONLY using the policy context provided to you.
            If the answer is not contained in the context, say plainly that you don't
            have that information and suggest contacting branch staff.
            Never invent vehicle prices, availability, or booking confirmations —
            you only answer policy questions here; real-time data comes from other tools.
            Keep answers short and factual.
            """)
    String answer(@UserMessage String question);
}
