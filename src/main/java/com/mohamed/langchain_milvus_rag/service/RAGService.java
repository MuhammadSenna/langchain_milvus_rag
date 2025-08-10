package com.mohamed.langchain_milvus_rag.service;


import com.mohamed.langchain_milvus_rag.entity.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RAGService {
    
    private static final Logger logger = LoggerFactory.getLogger(RAGService.class);
    
    @Autowired
    private MilvusService milvusService;
    
    @Autowired
    private ChatLanguageModel chatLanguageModel;
    
    @Autowired
    private EmbeddingModel embeddingModel;
    
    @Autowired
    private DocumentSplitter documentSplitter;
    
    private final PromptTemplate RAG_PROMPT_TEMPLATE = PromptTemplate.from("""
            You are a helpful assistant that answers questions based on the provided context.
            Use only the information from the context to answer the question.
            If the context doesn't contain enough information to answer the question, say so.
            
            Context:
            {{context}}
            
            Question: {{question}}
            
            Answer:
            """);
    
    public String askQuestion(String question) {
        try {
            logger.info("Processing question: {}", question);
            
            // Generate embedding for the question
            Embedding questionEmbedding = embeddingModel.embed(question).content();
            List<Float> questionVector = questionEmbedding.vector();
            
            // Search for relevant documents
            List<Document> relevantDocuments = milvusService.searchSimilarDocuments(questionVector);
            
            if (relevantDocuments.isEmpty()) {
                logger.info("No relevant documents found for question: {}", question);
                return "I couldn't find any relevant information to answer your question.";
            }
            
            // Combine relevant document contents as context
            String context = relevantDocuments.stream()
                    .map(doc -> doc.getContent() + " (Score: " + String.format("%.3f", doc.getScore()) + ")")
                    .collect(Collectors.joining("\n\n"));
            
            logger.debug("Found {} relevant documents for context", relevantDocuments.size());
            
            // Create prompt with context and question
            Map<String, Object> variables = new HashMap<>();
            variables.put("context", context);
            variables.put("question", question);
            
            Prompt prompt = RAG_PROMPT_TEMPLATE.apply(variables);
            
            // Generate response using the chat model
            String response = chatLanguageModel.generate(prompt.text());
            
            logger.info("Generated response for question: {}", question);
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing question: {}", question, e);
            throw new RuntimeException("Failed to process question", e);
        }
    }
    
    public void addDocument(String content, Map<String, String> metadata) {
        try {
            logger.info("Adding document with {} characters", content.length());
            
            // Parse and split document
            dev.langchain4j.data.document.Document langchainDoc = 
                    new TextDocumentParser().parse(content);
            
            List<TextSegment> segments = documentSplitter.split(langchainDoc);
            
            List<Document> documents = new ArrayList<>();
            
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);
                String segmentId = UUID.randomUUID().toString();
                
                // Generate embedding for segment
                Embedding embedding = embeddingModel.embed(segment.text()).content();
                List<Float> embeddingVector = embedding.vector();
                
                // Create metadata for segment
                Map<String, String> segmentMetadata = new HashMap<>(metadata);
                segmentMetadata.put("segment_index", String.valueOf(i));
                segmentMetadata.put("total_segments", String.valueOf(segments.size()));
                segmentMetadata.put("content_length", String.valueOf(segment.text().length()));
                
                Document document = new Document(segmentId, segment.text(), embeddingVector, segmentMetadata);
                documents.add(document);
            }
            
            // Insert documents into Milvus
            milvusService.insertDocuments(documents);
            
            logger.info("Successfully added document split into {} segments", documents.size());
            
        } catch (Exception e) {
            logger.error("Error adding document: ", e);
            throw new RuntimeException("Failed to add document", e);
        }
    }
    
    public void addDocuments(List<String> contents, List<Map<String, String>> metadataList) {
        if (contents.size() != metadataList.size()) {
            throw new IllegalArgumentException("Contents and metadata lists must have the same size");
        }
        
        for (int i = 0; i < contents.size(); i++) {
            addDocument(contents.get(i), metadataList.get(i));
        }
    }
}