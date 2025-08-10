# RAG Application with Spring Boot, LangChain4j, and Milvus

A complete Retrieval-Augmented Generation (RAG) application built with Spring Boot, LangChain4j, and Milvus vector database.

## Features

- **Document Ingestion**: Add documents that are automatically chunked and embedded
- **Vector Storage**: Documents stored in Milvus vector database with metadata
- **Semantic Search**: Find relevant document chunks based on question similarity
- **AI-Powered Responses**: Generate contextual answers using OpenAI's GPT models
- **RESTful API**: Easy-to-use HTTP endpoints
- **Docker Support**: Complete containerization with Docker Compose

## Architecture


1. User asks a question via REST API
2. Question is embedded using OpenAI embeddings
3. Similar document chunks are retrieved from Milvus
4. Retrieved context + question sent to OpenAI GPT
5. AI-generated response returned to user

## Prerequisites

- Java 21+
- Maven 3.6+
- Docker and Docker Compose
- OpenAI API Key

## Quick Start

### 1. Clone and Setup

```bash
git clone <repository-url>
cd rag-application
```

### 2. Configure Environment

Create a `.env` file in the root directory:

```bash
OPENAI_API_KEY=your-openai-api-key-here
```

### 3. Start with Docker Compose

```bash
docker-compose up -d
```

This will start:
- Milvus vector database (with etcd and minio dependencies)
- RAG application on port 8080

### 4. Test the Application

**Health Check:**
```bash
curl http://localhost:8080/api/rag/health
```

**Add a Document:**
```bash
curl -X POST http://localhost:8080/api/rag/documents \
  -H "Content-Type: application/json" \
  -d '{
    "content": "The capital of France is Paris. It is known for the Eiffel Tower and Louvre Museum.",
    "metadata": {
      "source": "geography",
      "topic": "France"
    }
  }'
```

**Ask a Question:**
```bash
curl -X POST http://localhost:8080/api/rag/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the capital of France?"
  }'
```

## API Endpoints

### POST /api/rag/ask
Ask a question and get an AI-generated response based on stored documents.

**Request Body:**
```json
{
  "question": "Your question here"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Question answered successfully",
  "data": {
    "question": "Your question here",
    "answer": "AI-generated answer based on context"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### POST /api/rag/documents
Add a new document to the knowledge base.

**Request Body:**
```json
{
  "content": "Document content here...",
  "metadata": {
    "source": "source-name",
    "category": "category-name"
  }
}
```

### GET /api/rag/health
Check application health status.

## Configuration

Key configuration properties in `application.yml`:

```yaml
# OpenAI Configuration
openai:
  api:
    key: ${OPENAI_API_KEY}
    model: gpt-3.5-turbo
  embedding:
    model: text-embedding-ada-002

# Milvus Configuration
milvus:
  host: ${MILVUS_HOST:localhost}
  port: ${MILVUS_PORT:19530}
  collection:
    name: documents
    dimension: 1536

# RAG Configuration
rag:
  max-results: 5
  similarity-threshold: 0.7
  chunk-size: 1000
  chunk-overlap: 200
```

## Development Setup

### Running Locally

1. Start Milvus dependencies:
```bash
docker-compose up -d etcd minio milvus
```

2. Set environment variables:
```bash
export OPENAI_API_KEY=your-api-key
export MILVUS_HOST=localhost
export MILVUS_PORT=19530
```

3. Run the application:
```bash
./mvnw spring-boot:run
```

### Building the Application

```bash
./mvnw clean package
```

## Project Structure

```
src/
├── main/
│   ├── java/com/example/rag/
│   │   ├── config/           # Configuration classes
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Data transfer objects
│   │   ├── entity/           # Entity classes
│   │   ├── service/          # Business logic
│   │   └── RagApplication.java
│   └── resources/
│       └── application.yml   # Configuration
├── docker-compose.yml        # Docker services
├── Dockerfile               # Application container
└── pom.xml                  # Maven dependencies
```

## Key Components

- **MilvusConfig**: Sets up Milvus connection and collection
- **LangChainConfig**: Configures OpenAI models and document processing
- **MilvusService**: Handles vector database operations
- **RAGService**: Main business logic for RAG workflow
- **RAGController**: REST API endpoints

## Customization

### Changing the LLM Provider

To use a different LLM provider, modify `LangChainConfig.java`:

```java
@Bean
public ChatLanguageModel chatLanguageModel() {
    // Replace with your preferred provider
    return HuggingFaceChatModel.builder()
            .apiKey(apiKey)
            .modelId("model-id")
            .build();
}
```

### Adjusting Chunking Strategy

Modify document splitting in `LangChainConfig.java`:

```java
@Bean
public DocumentSplitter documentSplitter() {
    return DocumentSplitters.recursive(
        chunkSize,     // Adjust chunk size
        chunkOverlap   // Adjust overlap
    );
}
```

### Custom Embedding Models

Change embedding model in `LangChainConfig.java`:

```java
@Bean
public EmbeddingModel embeddingModel() {
    return HuggingFaceEmbeddingModel.builder()
            .modelId("sentence-transformers/all-MiniLM-L6-v2")
            .build();
}
```

## Monitoring and Logging

- Application logs are available via Docker: `docker-compose logs rag-app`
- Milvus web UI available at: http://localhost:9091
- MinIO console available at: http://localhost:9001

## Troubleshooting

### Common Issues

1. **Connection to Milvus fails**: Ensure all containers are running and healthy
2. **OpenAI API errors**: Check your API key and rate limits
3. **Out of memory**: Increase Docker memory limits for large documents

## License

This project is licensed under the MIT License.