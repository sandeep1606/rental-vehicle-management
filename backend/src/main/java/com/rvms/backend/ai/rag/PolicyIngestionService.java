package com.rvms.backend.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Loads the sample rental policy documents (src/main/resources/policies/*.txt) at startup,
 * splits them into chunks, embeds each chunk, and stores the embeddings in the vector store
 * so PolicyQaAssistant (RAG) can retrieve relevant passages instead of the LLM guessing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyIngestionService implements ApplicationRunner {

    private static final String POLICY_LOCATION_PATTERN = "classpath:policies/*.txt";

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(POLICY_LOCATION_PATTERN);

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 30);
        int totalSegments = 0;

        for (Resource resource : resources) {
            String text = readAsString(resource);
            Document document = Document.from(text);
            List<TextSegment> segments = splitter.split(document);

            for (TextSegment segment : segments) {
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
                totalSegments++;
            }
        }

        log.info("Ingested {} policy document(s) into {} embedded segment(s) for RAG.", resources.length, totalSegments);
    }

    private String readAsString(Resource resource) throws IOException {
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
