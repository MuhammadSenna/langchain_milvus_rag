package com.mohamed.langchain_milvus_rag.entity;


import java.util.List;
import java.util.Map;

public class Document {
    private String id;
    private String content;
    private List<Float> embedding;
    private Map<String, String> metadata;
    private double score; // similarity score for search results
    
    public Document() {}
    
    public Document(String id, String content, List<Float> embedding, Map<String, String> metadata) {
        this.id = id;
        this.content = content;
        this.embedding = embedding;
        this.metadata = metadata;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public List<Float> getEmbedding() {
        return embedding;
    }
    
    public void setEmbedding(List<Float> embedding) {
        this.embedding = embedding;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = score;
    }
    
    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", score=" + score +
                ", metadata=" + metadata +
                '}';
    }
}
