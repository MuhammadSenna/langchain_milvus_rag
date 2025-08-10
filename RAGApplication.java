package com.mohamed.langchain_milvus_rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RagApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagApplication.class, args);
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}