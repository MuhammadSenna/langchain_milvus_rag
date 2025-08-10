package com.mohamed.langchain_milvus_rag.config;


import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.document.splitter.DocumentSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LangChainConfig {
    
    @Value("${openai.api.key}")
    private String openaiApiKey;
    
    @Value("${openai.api.model}")
    private String chatModel;
    
    @Value("${openai.embedding.model}")
    private String embeddingModel;
    
    @Value("${rag.chunk-size}")
    private int chunkSize;
    
    @Value("${rag.chunk-overlap}")
    private int chunkOverlap;
    
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName(chatModel)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(3)
                .build();
    }
    
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(openaiApiKey)
                .modelName(embeddingModel)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(3)
                .build();
    }
    
    @Bean
    public DocumentSplitter documentSplitter() {
        return DocumentSplitters.recursive(chunkSize, chunkOverlap);
    }
} {
    
}
