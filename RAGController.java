package com.mohamed.langchain_milvus_ragcontroller;

import com.example.rag.dto.ApiResponse;
import com.example.rag.dto.DocumentRequest;
import com.example.rag.dto.QuestionRequest;
import com.example.rag.service.RAGService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
@Validated
@CrossOrigin(origins = "*")
public class RAGController {
    
    private static final Logger logger = LoggerFactory.getLogger(RAGController.class);
    
    @Autowired
    private RAGService ragService;
    
    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<Map<String, String>>> askQuestion(
            @Valid @RequestBody QuestionRequest request) {
        try {
            logger.info("Received question: {}", request.getQuestion());
            
            String answer = ragService.askQuestion(request.getQuestion());
            
            Map<String, String> response = new HashMap<>();
            response.put("question", request.getQuestion());
            response.put("answer", answer);
            
            return ResponseEntity.ok(ApiResponse.success("Question answered successfully", response));
            
        } catch (Exception e) {
            logger.error("Error processing question: {}", request.getQuestion(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to process question: " + e.getMessage()));
        }
    }
    
    @PostMapping("/documents")
    public ResponseEntity<ApiResponse<String>> addDocument(
            @Valid @RequestBody DocumentRequest request) {
        try {
            logger.info("Received document to add: {}", request);
            
            ragService.addDocument(request.getContent(), request.getMetadata());
            
            return ResponseEntity.ok(ApiResponse.success("Document added successfully"));
            
        } catch (Exception e) {
            logger.error("Error adding document: {}", request, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to add document: " + e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "RAG Application");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(ApiResponse.success("Health check passed", health));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        logger.error("Unexpected error: ", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error("An unexpected error occurred: " + e.getMessage()));
    }
}