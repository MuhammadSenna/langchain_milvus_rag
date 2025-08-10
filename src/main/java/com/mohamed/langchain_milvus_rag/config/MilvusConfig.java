package com.mohamed.langchain_milvus_rag.config;


import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class MilvusConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(MilvusConfig.class);
    
    @Value("${milvus.host}")
    private String milvusHost;
    
    @Value("${milvus.port}")
    private int milvusPort;
    
    @Value("${milvus.collection.name}")
    private String collectionName;
    
    @Value("${milvus.collection.dimension}")
    private int dimension;
    
    private MilvusServiceClient milvusClient;
    
    @Bean
    public MilvusServiceClient milvusClient() {
        if (milvusClient == null) {
            ConnectParam connectParam = ConnectParam.newBuilder()
                    .withHost(milvusHost)
                    .withPort(milvusPort)
                    .build();
            
            milvusClient = new MilvusServiceClient(connectParam);
            logger.info("Connected to Milvus at {}:{}", milvusHost, milvusPort);
        }
        return milvusClient;
    }
    
    @PostConstruct
    public void initializeCollection() {
        try {
            MilvusServiceClient client = milvusClient();
            
            // Check if collection exists
            HasCollectionParam hasCollectionParam = HasCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();
            
            boolean exists = client.hasCollection(hasCollectionParam).getData();
            
            if (!exists) {
                createCollection(client);
                createIndex(client);
                logger.info("Collection '{}' created successfully", collectionName);
            } else {
                logger.info("Collection '{}' already exists", collectionName);
            }
        } catch (Exception e) {
            logger.error("Error initializing Milvus collection: ", e);
            throw new RuntimeException("Failed to initialize Milvus collection", e);
        }
    }
    
    private void createCollection(MilvusServiceClient client) {
        List<FieldType> fields = new ArrayList<>();
        
        // ID field
        fields.add(FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.VarChar)
                .withMaxLength(255)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build());
        
        // Vector field
        fields.add(FieldType.newBuilder()
                .withName("embedding")
                .withDataType(DataType.FloatVector)
                .withDimension(dimension)
                .build());
        
        // Text content field
        fields.add(FieldType.newBuilder()
                .withName("content")
                .withDataType(DataType.VarChar)
                .withMaxLength(65535)
                .build());
        
        // Metadata field
        fields.add(FieldType.newBuilder()
                .withName("metadata")
                .withDataType(DataType.VarChar)
                .withMaxLength(1000)
                .build());
        
        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("Document embeddings for RAG")
                .withShardsNum(2)
                .withFieldTypes(fields)
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .build();
        
        client.createCollection(createCollectionParam);
    }
    
    private void createIndex(MilvusServiceClient client) {
        Map<String, Object> indexParams = new HashMap<>();
        indexParams.put("nlist", 1024);
        indexParams.put("m", 8);
        indexParams.put("nbits", 8);
        
        CreateIndexParam createIndexParam = CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName("embedding")
                .withIndexType(io.milvus.IndexType.IVF_FLAT)
                .withMetricType(io.milvus.MetricType.COSINE)
                .withExtraParam(indexParams)
                .build();
        
        client.createIndex(createIndexParam);
    }
}