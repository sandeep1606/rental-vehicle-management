package com.rvms.backend.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Wires up the LangChain4j building blocks used by the AI assistant:
 * an embedding model + in-memory vector store for RAG over rental policy documents,
 * a content retriever on top of that store, and a chat model (real OpenAI-backed
 * model if configured, otherwise a deterministic mock so the app runs with zero
 * external AI dependencies).
 */
@Configuration
@Slf4j
public class AiModelConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        // Runs fully locally (ONNX), no API key required — used for the policy RAG store.
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.4)
                .build();
    }

    @Bean
    public ChatLanguageModel chatLanguageModel(AiProperties aiProperties) {
        if (aiProperties.mockMode() || !aiProperties.hasUsableApiKey()) {
            log.warn("AI running in MOCK mode (no OPENAI_API_KEY configured or langchain4j.ai.mock-mode=true). "
                    + "Set OPENAI_API_KEY and AI_MOCK_MODE=false for real LLM responses.");
            return new MockChatLanguageModel();
        }
        return OpenAiChatModel.builder()
                .apiKey(aiProperties.openai().apiKey())
                .modelName(aiProperties.openai().modelName())
                .baseUrl(aiProperties.openai().baseUrl())
                .temperature(aiProperties.openai().temperature())
                .timeout(Duration.ofSeconds(30))
                .build();
    }
}
