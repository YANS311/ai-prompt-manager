package com.ai.promptmanager.controller;

import com.ai.promptmanager.dto.PromptCreateDTO;
import com.ai.promptmanager.dto.PromptDTO;
import com.ai.promptmanager.dto.PromptUpdateDTO;
import com.ai.promptmanager.dto.Result;
import com.ai.promptmanager.entity.Prompt;
import com.ai.promptmanager.mapper.PromptMapper;
import com.ai.promptmanager.service.PromptService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    private final PromptService service;
    private final PromptMapper mapper;

    @Autowired
    public PromptController(PromptService service, PromptMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /**
     * 查询所有 Prompts（支持按分类筛选）
     */
    @GetMapping
    public Result<List<PromptDTO>> getAll(@RequestParam(required = false) String category) {
        log.info("GET /api/prompts - category: {}", category);
        List<Prompt> prompts;
        if (category != null && !category.isEmpty()) {
            prompts = service.findByCategory(category);
        } else {
            prompts = service.findAll();
        }
        return Result.success(mapper.toDTOList(prompts));
    }

    /**
     * 分页查询 Prompts
     */
    @GetMapping("/page")
    public Result<Page<PromptDTO>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category) {
        log.info("GET /api/prompts/page - page: {}, size: {}, category: {}", page, size, category);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Prompt> promptPage;
        if (category != null && !category.isEmpty()) {
            promptPage = service.findByCategoryPaged(category, pageable);
        } else {
            promptPage = service.findAllPaged(pageable);
        }

        Page<PromptDTO> dtoPage = promptPage.map(mapper::toDTO);
        return Result.success(dtoPage);
    }

    /**
     * 创建新 Prompt
     */
    @PostMapping
    public Result<PromptDTO> create(@Valid @RequestBody PromptCreateDTO createDTO) {
        log.info("POST /api/prompts - title: {}", createDTO.getTitle());
        Prompt entity = mapper.toEntity(createDTO);
        Prompt created = service.save(entity);
        return Result.success("创建成功", mapper.toDTO(created));
    }

    /**
     * 根据 ID 查询单个 Prompt
     */
    @GetMapping("/{id}")
    public Result<PromptDTO> getOne(@PathVariable Long id) {
        log.info("GET /api/prompts/{}", id);
        return service.findById(id)
                .map(mapper::toDTO)
                .map(Result::success)
                .orElseGet(() -> Result.notFound("Prompt不存在: id=" + id));
    }

    /**
     * 更新 Prompt
     */
    @PutMapping("/{id}")
    public Result<PromptDTO> update(@PathVariable Long id, @Valid @RequestBody PromptUpdateDTO updateDTO) {
        log.info("PUT /api/prompts/{} - title: {}", id, updateDTO.getTitle());
        Prompt existing = service.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Prompt不存在，id: " + id));

        mapper.updateEntityFromDTO(updateDTO, existing);
        Prompt updated = service.update(id, existing);
        return Result.success("更新成功", mapper.toDTO(updated));
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
    public Result<List<PromptDTO>> search(@RequestParam String keyword) {
        log.info("GET /api/prompts/search - keyword: {}", keyword);
        List<Prompt> results = service.searchByTitle(keyword);
        return Result.success(mapper.toDTOList(results));
    }
}
