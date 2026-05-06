# 🏗️ 架构升级总结 - 工业级进阶

## ✅ 已完成的升级

### 1. **DTO模式 + MapStruct映射** 🎯

#### **为什么需要DTO?**
| 问题 | 直接返回Entity | 使用DTO |
|------|---------------|---------|
| 数据库结构泄露 | ✗ JPA注解、外键暴露 | ✓ 只暴露必要字段 |
| 字段冗余 | ✗ 前端不需要的字段也返回 | ✓ 按需裁剪 |
| 性能问题 | ✗ 懒加载导致N+1查询 | ✓ 避免级联查询 |
| 安全风险 | ✗ 敏感字段（如deleted）暴露 | ✓ 敏感字段隐藏 |

#### **创建的DTO:**
- `PromptDTO` - 查询返回
- `PromptCreateDTO` - 创建请求
- `PromptUpdateDTO` - 更新请求
- `Result<T>` - 统一返回格式

#### **MapStruct性能对比 (1000次映射):**
```
MapStruct:     ~1ms    (编译期生成代码)
BeanUtils:   ~150ms   (运行时反射)
手动映射:     ~0.5ms   (最快但代码冗余)
```

---

### 2. **JPA审计 (Auditing)** 📋

#### **自动时间戳管理:**
```java
@CreatedDate
private LocalDateTime createdAt;  // 创建时自动填充

@LastModifiedDate
private LocalDateTime updatedAt;  // 更新时自动更新
```

#### **生产环境扩展:**
```java
@CreatedBy
private String createdBy;  // 创建人（需实现AuditorAware）

@LastModifiedBy
private String lastModifiedBy;  // 修改人
```

---

### 3. **软删除 (Soft Delete)** 🗑️

#### **为什么需要软删除?**
- ❌ **物理删除**:  数据永久丢失，无法恢复
- ✅ **逻辑删除**: 标记deleted=true，数据仍可追溯

#### **实现方式:**
```java
@SQLDelete(sql = "UPDATE prompts SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")  // 查询时自动过滤
```

#### **面试要点:**
> "我们的系统采用软删除策略，所有删除操作只是标记deleted字段，定期由后台任务清理归档到历史表。这样既保证了数据安全，又满足了合规要求（如GDPR要求保留数据历史）。"

---

### 4. **动态查询 (Specification)** 🔍

#### **解决的问题:**
```java
// ❌ 方法爆炸：每个查询组合都要写一个方法
findByTitle(String title);
findByCategory(String category);
findByTitleAndCategory(String title, String category);
findByTitleAndCategoryAndDateRange(...);  // 无穷无尽...

// ✅ 动态组合：一个方法搞定所有查询
Specification<Prompt> spec = PromptSpecification.filterBy(keyword, category, startDate, endDate);
List<Prompt> results = repository.findAll(spec);
```

#### **链式调用示例:**
```java
Specification<Prompt> spec = Specification
    .where(PromptSpecification.titleContains("Java"))
    .and(PromptSpecification.categoryEquals("Coding"))
    .and(PromptSpecification.createdAfter(LocalDateTime.now().minusDays(7)));

Page<Prompt> page = repository.findAll(spec, PageRequest.of(0, 10));
```

---

### 5. **本地缓存 (Caffeine)** ⚡️

#### **缓存策略:**
| 操作 | 缓存行为 | 注解 |
|------|---------|------|
| 查询单个 | 缓存10分钟 | `@Cacheable(value="prompts", key="#id")` |
| 查询列表 | 缓存10分钟 | `@Cacheable(value="promptList")` |
| 创建/更新/删除 | 清除缓存 | `@CacheEvict(allEntries=true)` |

#### **缓存三大经典问题:**
1. **缓存穿透**: 查询不存在的数据，绕过缓存打DB
   - 解决：布隆过滤器 or 缓存null值
2. **缓存击穿**: 热点数据过期，瞬间并发打DB
   - 解决：互斥锁 or 永不过期
3. **缓存雪崩**: 大量缓存同时过期
   - 解决：随机过期时间 or 熔断降级

---

### 6. **Spring Boot Actuator (监控)** 📊

#### **开放的端点:**
```
http://localhost:8080/actuator/health    - 健康检查
http://localhost:8080/actuator/metrics   - JVM指标
http://localhost:8080/actuator/prometheus - Prometheus格式指标
```

#### **监控指标示例:**
```json
{
  "jvm.memory.used": 256MB,
  "hikaricp.connections.active": 5,
  "http.server.requests.count": 1024,
  "cache.gets.hit": 850,
  "cache.gets.miss": 174
}
```

#### **生产环境集成:**
```
Prometheus (指标采集)
    ↓
Grafana (可视化大盘)
    ↓
AlertManager (告警通知)
```

---

## 📁 新增的文件结构

```
src/main/java/com/ai/promptmanager/
├── dto/
│   ├── Result.java                 # 统一返回结果
│   ├── PromptDTO.java              # 查询返回DTO
│   ├── PromptCreateDTO.java        # 创建请求DTO
│   └── PromptUpdateDTO.java        # 更新请求DTO
│
├── mapper/
│   └── PromptMapper.java           # MapStruct映射器
│
├── specification/
│   └── PromptSpecification.java    # 动态查询规约
│
└── service/
    ├── PromptService.java          # 原有Service (兼容旧代码)
    └── PromptServiceV2.java        # 新Service (使用DTO模式)
```

---

## 💼 面试准备 - 话术模板

### **问题1: 为什么不直接返回Entity?**
> "直接暴露Entity会导致三个问题：1) 数据库结构泄露，JPA注解、外键等敏感信息暴露；2) 字段冗余，前端不需要的字段如deleted、version也返回；3) 性能问题，懒加载关联对象可能导致N+1查询。我们采用DTO模式，在Service层完成Entity到DTO的转换，Controller只返回DTO。"

### **问题2: 为什么选择MapStruct而不是BeanUtils?**
> "MapStruct是编译期生成代码，性能接近手动映射，比运行时反射的BeanUtils快100倍以上。以1000次映射为例，MapStruct只需1ms，BeanUtils需要150ms。在高并发场景下，性能差异会被放大。而且MapStruct是类型安全的，编译期就能发现字段映射错误。"

### **问题3: 软删除的实现原理?**
> "我们用Hibernate的@SQLDelete注解拦截删除操作，将DELETE语句改写为UPDATE deleted=true。同时用@SQLRestriction自动在查询时添加WHERE deleted=false条件，对开发者透明。这样既保证了数据可追溯，又满足了合规要求（如GDPR要求保留数据历史）。生产环境还会配合定时任务，定期将已删除数据归档到历史表。"

### **问题4: 如何处理复杂的动态查询?**
> "我们使用JPA Specification实现动态查询。通过Criteria API根据用户传入的参数（可能为null）动态构建WHERE子句。相比方法命名查询（会导致方法爆炸），Specification更灵活，支持AND/OR组合、分页、排序。但也要注意性能，模糊查询（LIKE %keyword%）无法使用索引，数据量大时可以考虑全文搜索引擎如Elasticsearch。"

### **问题5: 缓存一致性如何保证?**
> "我们采用Cache-Aside模式：读操作先查缓存，未命中再查DB并回填；写操作直接更新DB，然后删除缓存。删除而不是更新缓存，是因为更新可能失败导致脏数据。我们用@CacheEvict在更新和删除操作时清除缓存，保证下次查询时获取最新数据。对于更严格的场景，可以引入分布式锁或延迟双删策略。"

---

## 🚀 下一步可选升级

1. **统一异常处理** - 创建自定义异常类和全局异常处理器
2. **参数校验增强** - @Valid + 自定义校验注解
3. **API版本控制** - /v1/prompts, /v2/prompts
4. **乐观锁** - @Version防止并发修改
5. **日志追踪** - SLF4J + MDC实现链路追踪
6. **单元测试** - JUnit 5 + Mockito覆盖率>80%

---

## 📚 推荐阅读

- [Spring Data JPA Specification文档](https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl)
- [MapStruct官方文档](https://mapstruct.org/)
- [Caffeine缓存设计](https://github.com/ben-manes/caffeine/wiki/Design)
- [软删除最佳实践](https://vladmihalcea.com/the-best-way-to-soft-delete-with-hibernate/)

---

**架构升级完成时间**: 2026-05-06  
**技术栈**: Spring Boot 3.4 + JPA + MySQL + Caffeine + MapStruct + Actuator
