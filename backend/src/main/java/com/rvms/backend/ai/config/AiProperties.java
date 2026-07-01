package com.rvms.backend.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "langchain4j.ai")
public record AiProperties(String provider, OpenAi openai, boolean mockMode) {

    public record OpenAi(String apiKey, String modelName, String baseUrl, double temperature) {}

    public boolean hasUsableApiKey() {
        return openai != null && openai.apiKey() != null && !openai.apiKey().isBlank();
    }
}
