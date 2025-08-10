package com.mohamed.langchain_milvus_rag.service;


import com.mohamed.langchain_milvus_rag.entity.Document;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.SearchResultsWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MilvusService {
    
    private static final Logger logger = LoggerFactory.getLogger(MilvusService.class);
    
    @Autowired
    private MilvusServiceClient milvusClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${milvus.collection.name}")
    private String collectionName;
    
    @Value("${rag.max-results}")
    private int maxResults;
    
    @Value("${rag.similarity-threshold}")
    private double similarityThreshold;
    
    @PostConstruct
    public void loadCollection() {
        try {
            LoadCollectionParam loadCollectionParam = LoadCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();
            
            milvusClient.loadCollection(loadCollectionParam);
            logger.info("Collection '{}' loaded successfully", collectionName);
        } catch (Exception e) {
            logger.error("Error loading collection: ", e);
        }
    }
    
    public void insertDocument(Document document) {
        try {
            List<String> ids = Arrays.asList(document.getId());
            List<List<Float>> embeddings = Arrays.asList(document.getEmbedding());
            List<String> contents = Arrays.asList(document.getContent());
            List<String> metadata = Arrays.asList(objectMapper.writeValueAsString(document.getMetadata()));
            
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("id", ids));
            fields.add(new InsertParam.Field("embedding", embeddings));
            fields.add(new InsertParam.Field("content", contents));
            fields.add(new InsertParam.Field("metadata", metadata));
            
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(fields)
                    .build();
            
            milvusClient.insert(insertParam);
            logger.debug("Document inserted successfully: {}", document.getId());
        } catch (Exception e) {
            logger.error("Error inserting document: ", e);
            throw new RuntimeException("Failed to insert document", e);
        }
    }
    
    public void insertDocuments(List<Document> documents) {
        try {
            List<String> ids = documents.stream().map(Document::getId).collect(Collectors.toList());
            List<List<Float>> embeddings = documents.stream().map(Document::getEmbedding).collect(Collectors.toList());
            List<String> contents = documents.stream().map(Document::getContent).collect(Collectors.toList());
            List<String> metadata = documents.stream().map(doc -> {
                try {
                    return objectMapper.writeValueAsString(doc.getMetadata());
                } catch (JsonProcessingException e) {
                    logger.error("Error serializing metadata for document {}: ", doc.getId(), e);
                    return "{}";
                }
            }).collect(Collectors.toList());
            
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("id", ids));
            fields.add(new InsertParam.Field("embedding", embeddings));
            fields.add(new InsertParam.Field("content", contents));
            fields.add(new InsertParam.Field("metadata", metadata));
            
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(fields)
                    .build();
            
            milvusClient.insert(insertParam);
            logger.info("Batch inserted {} documents successfully", documents.size());
        } catch (Exception e) {
            logger.error("Error inserting documents batch: ", e);
            throw new RuntimeException("Failed to insert documents batch", e);
        }
    }
    
    public List<Document> searchSimilarDocuments(List<Float> queryEmbedding) {
        try {
            List<String> searchOutputFields = Arrays.asList("id", "content", "metadata");
            List<List<Float>> searchVectors = Arrays.asList(queryEmbedding);
            
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withMetricType(io.milvus.grpc.MetricType.COSINE)
                    .withOutFields(searchOutputFields)
                    .withTopK(maxResults)
                    .withVectors(searchVectors)
                    .withVectorFieldName("embedding")
                    .withParams("{\"nprobe\":10}")
                    .build();
            
            SearchResultsWrapper searchResults = new SearchResultsWrapper(
                    milvusClient.search(searchParam).getData().getResults());
            
            List<Document> documents = new ArrayList<>();
            for (int i = 0; i < searchResults.getIDScore(0).size(); i++) {
                float score = searchResults.getIDScore(0).get(i).getScore();
                
                // Apply similarity threshold
                if (score >= similarityThreshold) {
                    String id = searchResults.getIDScore(0).get(i).getStrID();
                    String content = (String) searchResults.getFieldWrapper("content").getFieldData().get(i);
                    String metadataJson = (String) searchResults.getFieldWrapper("metadata").getFieldData().get(i);
                    
                    Map<String, String> metadata = new HashMap<>();
                    try {
                        metadata = objectMapper.readValue(metadataJson, Map.class);
                    } catch (JsonProcessingException e) {
                        logger.warn("Error deserializing metadata for document {}: ", id, e);
                    }
                    
                    Document document = new Document(id, content, null, metadata);
                    document.setScore(score);
                    documents.add(document);
                }
            }
            
            logger.debug("Found {} similar documents with score >= {}", documents.size(), similarityThreshold);
            return documents;
            
        } catch (Exception e) {
            logger.error("Error searching similar documents: ", e);
            throw new RuntimeException("Failed to search similar documents", e);
        }
    }
}