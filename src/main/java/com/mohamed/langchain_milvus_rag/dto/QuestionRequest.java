package com.mohamed.langchain_milvus_rag.dto;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class QuestionRequest {
    
    @NotBlank(message = "Question cannot be empty")
    @Size(max = 1000, message = "Question must not exceed 1000 characters")
    private String question;
    
    public QuestionRequest() {}
    
    public QuestionRequest(String question) {
        this.question = question;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    @Override
    public String toString() {
        return "QuestionRequest{" +
                "question='" + question + '\'' +
                '}';
    }
}