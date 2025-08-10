package com.mohamed.langchain_milvus_rag.dto;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

public class DocumentRequest {
    
    @NotBlank(message = "Content cannot be empty")
    @Size(max = 50000, message = "Content must not exceed 50000 characters")
    private String content;
    
    private Map<String, String> metadata = new HashMap<>();
    
    public DocumentRequest() {}
    
    public DocumentRequest(String content, Map<String, String> metadata) {
        this.content = content;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
    
    @Override
    public String toString() {
        return "DocumentRequest{" +
                "content='" + (content != null ? content.substring(0, Math.min(content.length(), 100)) + "..." : null) + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}