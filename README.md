# 🚀 AI Prompt Manager

> **Industrial-Grade Prompt Management System** built with Spring Boot 3.4, Java 21, and MySQL

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A production-ready RESTful API and Web UI for managing AI prompts and code snippets with advanced features like keyword search, caching, soft delete, and filtering.

---

## ✨ Key Features

### 🏗️ Industrial-Grade Architecture
- **Three-Layer Architecture**: Controller → Service → Repository
- **DTO Pattern with MapStruct**: Compile-time object mapping (100x faster than BeanUtils)
  - All REST API endpoints use DTOs instead of exposing Entity objects
  - Separate DTOs for create, update, and query operations
- **Unified Response Wrapper**: Consistent API response format with `Result<T>`
- **Global Exception Handling**: Centralized error handling with field-level validation errors

### ⚡ Performance Optimizations
- **Local Caching**: Caffeine cache with 10-minute TTL
- **Dynamic Queries**: JPA Specification for flexible multi-criteria filtering
- **Connection Pooling**: HikariCP with optimized settings

### 🛡️ Data Safety & Auditing
- **Soft Delete**: Data marked as deleted instead of physical removal
- **JPA Auditing**: Automatic `createdAt` and `updatedAt` timestamps
- **Data Validation**: Bean Validation API with custom error messages

### 📊 Monitoring & Observability
- **Spring Boot Actuator**: Health checks, metrics, and Prometheus endpoints
- **Structured Logging**: SLF4J with request/response tracking
- **Database Metrics**: HikariCP connection pool monitoring

### 🎨 Modern Web UI
- **Responsive Design**: Bootstrap 5 with gradient effects
- **CRUD Operations**: Create, Read, Update, Delete with real-time feedback
- **Search & Filter**: Keyword search and category filtering
- **Pagination**: Efficient data loading with customizable page sizes

### 🤖 Agent Foundation (Mock LLM)
- **Template Variables**: Support `{variable}` placeholders in prompts
- **Variable Extraction**: Automatically detect template variables
- **Prompt Preview**: Preview rendered prompts without execution
- **Prompt Run Execution**: Record and track prompt executions
- **Mock LLM Responses**: Simulated model responses for development
- **Run History**: Query execution history per prompt
- **Metrics Tracking**: Latency, status, and response tracking
- **Preparation for Real LLM**: Foundation for OpenAI/Claude/DeepSeek integration

### 🔌 LLM Provider Abstraction
- **Extensible Architecture**: Clean provider interface for multiple LLM backends
- **Mock Provider**: Default simulated LLM for development and testing
- **Provider Registry**: Automatic discovery and routing of LLM providers
- **Error Handling**: Graceful handling of unknown providers and API failures
- **Future-Ready**: Prepared for OpenAI, Claude, and DeepSeek integration

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Java 21 (Latest LTS) |
| **Framework** | Spring Boot 3.4.0 |
| **ORM** | Spring Data JPA + Hibernate 6.6 |
| **Database** | MySQL 8.0+ |
| **Cache** | Caffeine (Local In-Memory) |
| **Template Engine** | Thymeleaf + Bootstrap 5 |
| **Object Mapping** | MapStruct 1.5.5 |
| **Validation** | Hibernate Validator (Bean Validation 3.0) |
| **Build Tool** | Maven 3.9+ |
| **Monitoring** | Spring Boot Actuator (metrics endpoints) |

---

## 📂 Project Structure

```
src/main/java/com/ai/promptmanager/
├── controller/          # REST API & Web Controllers
│   ├── PromptController.java    # RESTful API endpoints
│   └── ViewController.java       # Web UI page handlers
├── service/             # Business Logic Layer
│   └── PromptService.java        # CRUD + Caching logic
├── repository/          # Data Access Layer
│   └── PromptRepository.java     # JPA Repository
├── entity/              # Database Entities
│   └── Prompt.java               # JPA Entity with soft delete
├── dto/                 # Data Transfer Objects
│   ├── Result.java               # Unified response wrapper
│   ├── PromptDTO.java            # Query response DTO
│   ├── PromptCreateDTO.java      # Create request DTO
│   └── PromptUpdateDTO.java      # Update request DTO
├── mapper/              # MapStruct Mappers
│   └── PromptMapper.java         # Entity ↔ DTO conversion
├── specification/       # Dynamic Query Specifications
│   └── PromptSpecification.java  # JPA Criteria API
└── exception/           # Exception Handling
    └── GlobalExceptionHandler.java
```

---

## 🚀 Quick Start

### Prerequisites
- **Java 21+** ([Download](https://adoptium.net/))
- **Maven 3.9+** ([Download](https://maven.apache.org/download.cgi))
- **MySQL 8.0+** ([Download](https://dev.mysql.com/downloads/))

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/ai-prompt-manager.git
cd ai-prompt-manager
```

### 2. Configure Database
Create MySQL database:
```bash
mysql -u root -p
CREATE DATABASE promptdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/promptdb
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### 3. Build & Run
```bash
mvn clean spring-boot:run
```

### 4. Access the Application
- **Web UI**: http://localhost:8080/manager
- **REST API**: http://localhost:8080/api/prompts
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics

---

## 📡 API Endpoints

### REST API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/prompts` | Get all prompts (with optional category filter) |
| `GET` | `/api/prompts/page?page=0&size=10` | Get paginated prompts |
| `GET` | `/api/prompts/{id}` | Get prompt by ID |
| `GET` | `/api/prompts/search?keyword=java` | Search prompts by title |
| `POST` | `/api/prompts` | Create new prompt |
| `PUT` | `/api/prompts/{id}` | Update prompt |
| `DELETE` | `/api/prompts/{id}` | Delete prompt (soft delete) |

### Prompt Run API (with LLM Provider Abstraction)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/prompts/{id}/variables` | Extract template variables from a prompt |
| `POST` | `/api/prompts/{id}/preview` | Preview rendered prompt (no execution) |
| `POST` | `/api/prompts/{id}/runs` | Execute a prompt run (supports multiple providers) |
| `GET` | `/api/prompts/{id}/runs` | Get run history for a prompt |

**Supported Providers**: Currently only `mock` provider is implemented. Ready for real LLM integration (OpenAI/Claude/DeepSeek).

#### Example 1: Extract Template Variables
```bash
# Prompt content: "You are a {role}, please complete {task} in {format} format."
GET /api/prompts/1/variables
```

Response:
```json
{
  "code": 200,
  "data": ["role", "task", "format"]
}
```

#### Example 2: Preview Rendered Prompt
```bash
POST /api/prompts/1/preview
{
  "variables": {
    "role": "Java后端工程师",
    "task": "优化Service层代码",
    "format": "Markdown"
  }
}
```

Response:
```json
{
  "code": 200,
  "data": {
    "renderedPrompt": "You are a Java后端工程师, please complete 优化Service层代码 in Markdown format."
  }
}
```

#### Example 3: Execute Prompt Run with Provider Selection
```bash
POST /api/prompts/1/runs
{
  "providerName": "mock",
  "modelName": "gpt-4",
  "variables": {
    "role": "Java后端工程师",
    "task": "优化Service层代码"
  }
}
```

Response:
```json
{
  "code": 200,
  "message": "运行成功",
  "data": {
    "id": 1,
    "promptId": 1,
    "providerName": "mock",
    "modelName": "gpt-4",
    "variablesJson": "{\"role\":\"Java后端工程师\",\"task\":\"优化Service层代码\"}",
    "renderedPrompt": "You are a Java后端工程师, please complete 优化Service层代码.",
    "responseText": "Mock response from model [gpt-4]:\n\nThis is a simulated response...",
    "status": "SUCCESS",
    "latencyMs": 15,
    "errorMessage": null,
    "tokenUsageJson": null,
    "createdAt": "2026-05-13 19:00:00"
  }
}
```

**Note**: If `providerName` is omitted, defaults to `"mock"`.

#### Example 4: Unknown Provider Returns Error
```bash
POST /api/prompts/1/runs
{
  "providerName": "openai",
  "modelName": "gpt-4"
}
```

Response:
```json
{
  "code": 400,
  "message": "Provider not found: openai. Available providers: mock",
  "data": null
}
```

#### Example 5: Missing Variables Returns Error
```bash
POST /api/prompts/1/runs
{
  "variables": {
    "role": "Engineer"
  },
  "modelName": "gpt-4"
}
```

Response:
```json
{
  "code": 400,
  "message": "缺少必需的模板变量: task, format",
  "data": null
}
```

### Request/Response DTOs

All endpoints use DTOs for type safety and security:
- **PromptDTO**: Read responses (excludes internal fields like `deleted`)
- **PromptCreateDTO**: Create requests with validation (@NotBlank, @Size)
- **PromptUpdateDTO**: Update requests with validation

### Example Response
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "title": "Java Code Review",
    "content": "Please review this Java code...",
    "category": "Coding",
    "createdAt": "2026-05-06 18:00:00",
    "updatedAt": "2026-05-06 18:00:00"
  },
  "timestamp": "2026-05-06T18:30:00"
}
```

### Example Validation Error Response
```json
{
  "code": 400,
  "message": "参数校验失败",
  "data": {
    "title": "标题不能为空",
    "content": "内容长度必须在 0 到 5000 之间"
  },
  "timestamp": "2026-05-11T18:30:00"
}
```

---

## 🎯 Core Features Explained

### 1. **Template Variables**
**Why?** Enable dynamic prompt generation with reusable templates.

**How it works:**
```java
// Define template with {variables}
String template = "You are a {role}, complete {task}.";

// Extract variables
List<String> vars = PromptTemplateUtil.extractVariables(template);
// Returns: ["role", "task"]

// Render with values
Map<String, String> values = Map.of("role", "Engineer", "task", "code review");
String rendered = PromptTemplateUtil.render(template, values);
// Returns: "You are a Engineer, complete code review."
```

### 2. **DTO Pattern with MapStruct**
**Why?** Prevents exposing internal entity structure and sensitive fields.

**Performance Comparison (1000 mappings):**
- MapStruct: ~1ms (compile-time code generation)
- BeanUtils: ~150ms (runtime reflection)

### 3. **Soft Delete**
Deleted records are marked with `deleted=true` instead of physical removal:
```sql
-- Hibernate intercepts DELETE and converts to UPDATE
UPDATE prompts SET deleted = true WHERE id = ?

-- Queries automatically filter deleted records
SELECT * FROM prompts WHERE deleted = false
```

### 4. **LLM Provider Abstraction**
**Why?** Enable seamless switching between multiple LLM backends without changing business logic.

**Architecture:**
```
LlmProvider (interface)
├── MockLlmProvider (current)
├── OpenAIProvider (future)
├── ClaudeProvider (future)
└── DeepSeekProvider (future)

LlmProviderRegistry
├── Auto-discovers all providers
├── Routes by providerName
└── Defaults to "mock" if unspecified
```

**How it works:**
```java
// 1. Build LLM request
LlmRequest request = LlmRequest.builder()
        .prompt(renderedPrompt)
        .modelName("gpt-4")
        .build();

// 2. Get provider from registry
LlmProvider provider = registry.getProvider("mock");

// 3. Execute
LlmResponse response = provider.generate(request);

// 4. Record result in PromptRun
run.setProviderName(response.getProviderName());
run.setResponseText(response.getResponseText());
run.setStatus(response.getStatus());
```

**Adding new providers:**
```java
@Component
public class OpenAIProvider implements LlmProvider {
    @Override
    public String getProviderName() {
        return "openai";
    }
    
    @Override
    public LlmResponse generate(LlmRequest request) {
        // Call OpenAI API
        // Return response
    }
}
```

Provider auto-registration happens via Spring component scanning.

### 5. **Category Filtering and Pagination**
Efficient filtering with support for both list and paginated queries:
```java
// Filter by category
GET /api/prompts?category=Coding

// Paginated with category filter
GET /api/prompts/page?page=0&size=10&category=Coding

// Keyword search (case-insensitive)
GET /api/prompts/search?keyword=java
```

### 5. **Local Caching Strategy**
- **Read**: Check cache first, query DB on miss, then cache result
- **Write**: Update DB, then invalidate cache
- **Eviction**: 10-minute TTL with LRU policy

---

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```
Response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

### Metrics
```bash
curl http://localhost:8080/actuator/metrics
```
Available metrics:
- `jvm.memory.used`
- `hikaricp.connections.active`
- `http.server.requests.count`
- `cache.gets.hit`
- `cache.gets.miss`

---

## 🧪 Testing

### Run Tests
```bash
./mvnw test
```

### Run Full Build with Tests
```bash
./mvnw clean verify
```

### Test Coverage
The project includes comprehensive test coverage:
- **Unit Tests**: Service layer logic with mocked dependencies
- **Integration Tests**: Repository layer with H2 in-memory database
- **Controller Tests**: REST API endpoints and web UI handlers
- **Exception Handler Tests**: Global error handling verification

All tests use H2 in-memory database for fast, isolated testing without requiring MySQL.

### Continuous Integration
GitHub Actions automatically runs the full test suite on every push and pull request. The CI pipeline:
- Tests on multiple operating systems (Ubuntu, macOS)
- Verifies Java 21 compatibility
- Uploads test results as artifacts
- Ensures code quality before merging

---

## 🎓 Interview Highlights

This project demonstrates knowledge of:

1. **Architectural Design**
   - Three-layer architecture with clear separation of concerns
   - DTO pattern to decouple API from database schema
   - Repository pattern for data access abstraction

2. **Performance Optimization**
   - Compile-time object mapping with MapStruct
   - Local caching with eviction strategies
   - Connection pooling with HikariCP

3. **Data Integrity**
   - Soft delete for audit trail
   - JPA auditing for automatic timestamps
   - Bean validation for input sanitization

4. **Production Readiness**
   - Global exception handling
   - Structured logging
   - Health checks and metrics
   - Actuator monitoring endpoints

5. **Modern Java Features**
   - Java 21 Records (used in error responses)
   - Spring Boot 3.4 with Jakarta EE
   - Functional programming with Optional and Streams

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**L203** - Backend Developer @ Beijing Tech Company

---

## 🙏 Acknowledgments

- Built with [Spring Boot](https://spring.io/projects/spring-boot)
- UI powered by [Bootstrap 5](https://getbootstrap.com/)
- Caching by [Caffeine](https://github.com/ben-manes/caffeine)
- Object mapping by [MapStruct](https://mapstruct.org/)

---

**⭐ If this project helps you, please give it a star!**
