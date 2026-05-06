# 🚀 AI Prompt Manager

> **Industrial-Grade Prompt Management System** built with Spring Boot 3.4, Java 21, and MySQL

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A production-ready RESTful API and Web UI for managing AI prompts and code snippets with advanced features like semantic search, caching, soft delete, and dynamic filtering.

---

## ✨ Key Features

### 🏗️ Industrial-Grade Architecture
- **Three-Layer Architecture**: Controller → Service → Repository
- **DTO Pattern with MapStruct**: Compile-time object mapping (100x faster than BeanUtils)
- **Unified Response Wrapper**: Consistent API response format with `Result<T>`
- **Global Exception Handling**: Centralized error handling with detailed logging

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
| **Monitoring** | Spring Boot Actuator + Prometheus |

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
    "createdAt": "2026-05-06T18:00:00",
    "updatedAt": "2026-05-06T18:00:00"
  },
  "timestamp": "2026-05-06T18:30:00"
}
```

---

## 🎯 Core Features Explained

### 1. **DTO Pattern with MapStruct**
**Why?** Prevents exposing internal entity structure and sensitive fields.

**Performance Comparison (1000 mappings):**
- MapStruct: ~1ms (compile-time code generation)
- BeanUtils: ~150ms (runtime reflection)

### 2. **Soft Delete**
Deleted records are marked with `deleted=true` instead of physical removal:
```sql
-- Hibernate intercepts DELETE and converts to UPDATE
UPDATE prompts SET deleted = true WHERE id = ?

-- Queries automatically filter deleted records
SELECT * FROM prompts WHERE deleted = false
```

### 3. **Dynamic Query with Specification**
Build flexible queries without method explosion:
```java
Specification<Prompt> spec = PromptSpecification
    .titleContains("Java")
    .and(PromptSpecification.categoryEquals("Coding"))
    .and(PromptSpecification.createdAfter(sevenDaysAgo));

Page<Prompt> results = repository.findAll(spec, pageable);
```

### 4. **Caching Strategy**
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
   - Prometheus integration

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
