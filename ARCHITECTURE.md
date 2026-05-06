# 🏗️ 项目架构说明

## 技术架构图

```
┌─────────────────────────────────────────────────────────┐
│                    Client Layer                          │
│  Swagger UI │ HTTP Client │ Postman │ Browser           │
└─────────────────────────────────────────────────────────┘
                           ↓ HTTP/REST
┌─────────────────────────────────────────────────────────┐
│                 Controller Layer                         │
│              PromptController.java                       │
│  - @RestController                                       │
│  - Request Mapping: /api/prompts                         │
│  - CRUD Endpoints (GET/POST/PUT/DELETE)                  │
└─────────────────────────────────────────────────────────┘
                           ↓ Method Calls
┌─────────────────────────────────────────────────────────┐
│               Repository Layer (JPA)                     │
│            PromptRepository.java                         │
│  - extends JpaRepository<Prompt, Long>                   │
│  - Auto-generated SQL queries                            │
│  - Custom query methods                                  │
└─────────────────────────────────────────────────────────┘
                           ↓ JPA/Hibernate
┌─────────────────────────────────────────────────────────┐
│                  Entity Layer                            │
│                  Prompt.java                             │
│  - @Entity mapping                                       │
│  - Field validation (@NotBlank)                          │
│  - Lifecycle callbacks (@PrePersist)                     │
└─────────────────────────────────────────────────────────┘
                           ↓ JDBC
┌─────────────────────────────────────────────────────────┐
│               Database Layer                             │
│          H2 In-Memory Database                           │
│  - Auto DDL (create tables)                              │
│  - Console UI: /h2-console                               │
└─────────────────────────────────────────────────────────┘

Cross-Cutting Concerns (AOP):
┌─────────────────────────────────────────────────────────┐
│ GlobalExceptionHandler (@RestControllerAdvice)           │
│  - Validation errors (400)                               │
│  - Not Found errors (404)                                │
│  - Server errors (500)                                   │
└─────────────────────────────────────────────────────────┘
```

## 核心组件详解

### 1. Entity Layer - 数据模型
**文件**: `Prompt.java`

```java
@Entity  // JPA 实体标记
@Table(name = "prompts")  // 数据库表名
@Data    // Lombok: 自动生成 getter/setter
public class Prompt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "标题不能为空")  // 参数校验
    private String title;
    
    @Column(columnDefinition = "TEXT")  // 大文本字段
    private String content;
    
    @PrePersist  // 自动填充创建时间
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

**职责**:
- 定义数据结构
- 映射数据库表
- 字段校验规则
- 生命周期管理

### 2. Repository Layer - 数据访问
**文件**: `PromptRepository.java`

```java
public interface PromptRepository extends JpaRepository<Prompt, Long> {
    // Spring Data JPA 自动实现以下方法:
    // - findAll()
    // - findById(Long id)
    // - save(Prompt entity)
    // - deleteById(Long id)
    
    // 自定义查询方法（根据方法名自动生成 SQL）
    List<Prompt> findByCategory(String category);
    List<Prompt> findAllByOrderByCreatedAtDesc();
}
```

**Spring Data JPA 魔法**:
```java
findByCategory         → SELECT * FROM prompts WHERE category = ?
findByTitleContaining  → SELECT * FROM prompts WHERE title LIKE %?%
findAllByOrderByCreatedAtDesc → SELECT * FROM prompts ORDER BY created_at DESC
```

**职责**:
- CRUD 操作抽象
- 自动生成 SQL
- 事务管理

### 3. Controller Layer - API 接口
**文件**: `PromptController.java`

```java
@RestController
@RequestMapping("/api/prompts")
public class PromptController {
    @Autowired
    private PromptRepository repository;
    
    @GetMapping
    public List<Prompt> getAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Prompt create(@Valid @RequestBody Prompt prompt) {
        return repository.save(prompt);
    }
}
```

**职责**:
- HTTP 请求路由
- 参数解析与校验
- 响应格式化（自动 JSON 序列化）
- 状态码管理

### 4. Exception Handling - 全局异常处理
**文件**: `GlobalExceptionHandler.java`

```java
@RestControllerAdvice  // 全局异常拦截器
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(...) {
        // 统一处理参数校验错误
        return ResponseEntity.status(400).body(errorResponse);
    }
}
```

**职责**:
- 统一错误响应格式
- HTTP 状态码映射
- 错误日志记录

## 数据流示例

### 创建 Prompt 请求流程
```
1. Client 发送 POST 请求
   POST /api/prompts
   Body: {"title": "Test", "content": "...", "category": "Coding"}
   
2. Spring DispatcherServlet 路由到 PromptController
   
3. @Valid 触发参数校验
   ✅ 通过 → 继续
   ❌ 失败 → GlobalExceptionHandler 返回 400
   
4. Controller 调用 repository.save(prompt)
   
5. JPA/Hibernate 执行 SQL
   INSERT INTO prompts (title, content, category, created_at) 
   VALUES (?, ?, ?, ?)
   
6. 数据库返回自增 ID
   
7. Controller 返回完整的 Prompt 对象
   
8. Spring 自动序列化为 JSON
   
9. HTTP 201 Created 响应返回给客户端
```

### 查询 Prompt 请求流程
```
1. Client: GET /api/prompts?category=Coding

2. Controller 接收参数: category = "Coding"

3. 调用: repository.findByCategoryOrderByCreatedAtDesc("Coding")

4. Spring Data JPA 自动生成 SQL:
   SELECT * FROM prompts 
   WHERE category = 'Coding' 
   ORDER BY created_at DESC

5. Hibernate 执行查询并映射结果为 List<Prompt>

6. Controller 返回 List<Prompt>

7. Jackson 序列化为 JSON 数组

8. HTTP 200 OK 响应
```

## Spring Boot 自动配置魔法

### 无需手动配置的组件
```yaml
✅ 数据库连接池 (HikariCP)
✅ JPA EntityManager
✅ Transaction Manager
✅ JSON 序列化器 (Jackson)
✅ 异常处理器
✅ 静态资源服务
✅ Swagger UI 路由
```

### application.properties 配置解析
```properties
# 数据源配置
spring.datasource.url=jdbc:h2:mem:promptdb  
# ↑ 内存数据库，应用关闭后数据丢失

# JPA 配置
spring.jpa.hibernate.ddl-auto=update
# ↑ 自动根据 Entity 更新表结构
#   - create: 每次启动删除旧表
#   - update: 保留数据，增量更新
#   - validate: 只验证，不修改

# 开发调试
spring.jpa.show-sql=true
# ↑ 控制台打印 SQL（生产环境应关闭）
```

## 关键设计模式

### 1. Repository Pattern
```java
// 抽象数据访问层，隔离业务逻辑和数据库
PromptRepository → JpaRepository → Database
```

### 2. DTO Pattern (隐式)
```java
// Prompt.java 既是 Entity 也是 DTO
// 生产环境建议分离：
Prompt (Entity) ←→ PromptDTO (API)
```

### 3. Controller-Service-Repository (简化版)
```java
// 当前: Controller → Repository
// 复杂业务建议: Controller → Service → Repository
```

## 性能优化建议

### 当前架构的瓶颈
1. **N+1 查询问题** (未来扩展关联关系时)
   - 解决: 使用 `@EntityGraph` 或 JOIN FETCH

2. **无缓存层**
   - 解决: 集成 Redis 或使用 `@Cacheable`

3. **全量查询**
   - 解决: 添加分页 `Pageable`

### 生产级改进清单
```java
// 1. 添加 Service 层
@Service
public class PromptService {
    @Cacheable("prompts")
    public List<Prompt> getAllPrompts() {...}
}

// 2. 分页查询
Page<Prompt> findAll(Pageable pageable);

// 3. DTO 转换
public class PromptDTO {
    // 只返回必要字段，减少响应体积
}

// 4. 审计字段
@EntityListeners(AuditingEntityListener.class)
public class Prompt {
    @CreatedBy
    private String createdBy;
}
```

## 测试策略

### 单元测试
```java
@WebMvcTest(PromptController.class)
class PromptControllerTest {
    @MockBean
    private PromptRepository repository;
    
    @Test
    void shouldCreatePrompt() {
        // 测试 Controller 逻辑
    }
}
```

### 集成测试
```java
@SpringBootTest
@AutoConfigureMockMvc
class PromptIntegrationTest {
    @Test
    void shouldSaveAndRetrievePrompt() {
        // 测试完整流程
    }
}
```

## 扩展方向

### 短期（1-2 天）
- [ ] 添加分页和排序
- [ ] 实现标签系统（多对多关系）
- [ ] 导入/导出功能（JSON/CSV）

### 中期（1 周）
- [ ] 集成 Spring AI 实现语义搜索
- [ ] 添加 Redis 缓存
- [ ] 用户认证（Spring Security + JWT）

### 长期（1 月）
- [ ] 版本控制系统
- [ ] 多租户支持
- [ ] 微服务拆分（Gateway + Service Mesh）

---

**架构设计原则**: 
- ✅ 简单优于复杂
- ✅ 约定优于配置
- ✅ 可测试性优先
- ✅ 渐进式增强
