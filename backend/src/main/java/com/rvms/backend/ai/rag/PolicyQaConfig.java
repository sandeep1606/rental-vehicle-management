package com.rvms.backend.ai.rag;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PolicyQaConfig {

    @Bean
    public PolicyQaAssistant policyQaAssistant(ChatLanguageModel chatLanguageModel, ContentRetriever contentRetriever) {
        return AiServices.builder(PolicyQaAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .build();
    }
}
