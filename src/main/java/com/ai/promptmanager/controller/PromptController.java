package com.ai.promptmanager.controller;

import com.ai.promptmanager.dto.Result;
import com.ai.promptmanager.entity.Prompt;
import com.ai.promptmanager.service.PromptService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Prompt REST API 控制器
 *
 * 面试要点:
 * 1. 为什么使用 Result<T> 封装返回值?
 *    - 统一响应格式，便于前端统一处理
 *    - 包含状态码、消息、数据、时间戳，便于问题排查
 *    - 规范化团队API设计
 *
 * 2. @RestController vs @Controller?
 *    - @RestController = @Controller + @ResponseBody
 *    - 返回值直接序列化为JSON，不走视图解析器
 *
 * 3. 日志记录的最佳实践?
 *    - 记录关键操作（创建、更新、删除）
 *    - 记录请求参数和响应时间
 *    - 生产环境可配合MDC实现链路追踪
 */
@RestController
@RequestMapping("/api/prompts")
public class PromptController {

    private static final Logger log = LoggerFactory.getLogger(PromptController.class);

    @Autowired
    private PromptService service;

    /**
     * 查询所有 Prompts（支持按分类筛选）
     */
    @GetMapping
    public Result<List<Prompt>> getAll(@RequestParam(required = false) String category) {
        log.info("GET /api/prompts - category: {}", category);
        List<Prompt> prompts;
        if (category != null && !category.isEmpty()) {
            prompts = service.findByCategory(category);
        } else {
            prompts = service.findAll();
        }
        return Result.success(prompts);
    }

    /**
     * 分页查询 Prompts
     */
    @GetMapping("/page")
    public Result<Page<Prompt>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category) {
        log.info("GET /api/prompts/page - page: {}, size: {}, category: {}", page, size, category);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Prompt> promptPage = service.findAllPaged(pageable);
        return Result.success(promptPage);
    }

    /**
     * 创建新 Prompt
     */
    @PostMapping
    public Result<Prompt> create(@Valid @RequestBody Prompt prompt) {
        log.info("POST /api/prompts - title: {}", prompt.getTitle());
        Prompt created = service.save(prompt);
        return Result.success("创建成功", created);
    }

    /**
     * 根据 ID 查询单个 Prompt
     */
    @GetMapping("/{id}")
    public Result<Prompt> getOne(@PathVariable Long id) {
        log.info("GET /api/prompts/{}", id);
        return service.findById(id)
                .map(Result::success)
                .orElseGet(() -> Result.notFound("Prompt不存在: id=" + id));
    }

    /**
     * 更新 Prompt
     */
    @PutMapping("/{id}")
    public Result<Prompt> update(@PathVariable Long id, @Valid @RequestBody Prompt newPrompt) {
        log.info("PUT /api/prompts/{} - title: {}", id, newPrompt.getTitle());
        try {
            Prompt updated = service.update(id, newPrompt);
            return Result.success("更新成功", updated);
        } catch (RuntimeException e) {
            log.error("Update failed for id: {}", id, e);
            return Result.notFound("Prompt不存在: id=" + id);
        }
    }

    /**
     * 删除 Prompt（软删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/prompts/{}", id);
        if (!service.existsById(id)) {
            return Result.notFound("Prompt不存在: id=" + id);
        }
        service.deleteById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 搜索 Prompts（按标题）
     */
    @GetMapping("/search")
    public Result<List<Prompt>> search(@RequestParam String keyword) {
        log.info("GET /api/prompts/search - keyword: {}", keyword);
        List<Prompt> results = service.searchByTitle(keyword);
        return Result.success(results);
    }
}
