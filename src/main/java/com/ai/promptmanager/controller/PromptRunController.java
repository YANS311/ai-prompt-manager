package com.ai.promptmanager.controller;

import com.ai.promptmanager.dto.PromptPreviewDTO;
import com.ai.promptmanager.dto.PromptRunCreateDTO;
import com.ai.promptmanager.dto.PromptRunDTO;
import com.ai.promptmanager.dto.Result;
import com.ai.promptmanager.entity.PromptRun;
import com.ai.promptmanager.mapper.PromptRunMapper;
import com.ai.promptmanager.service.PromptRunService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * PromptRun REST API 控制器
 *
 * 提供 Prompt 运行功能：
 * 1. 发起一次 Prompt Run (Mock LLM)
 * 2. 查询 Prompt 的运行历史
 */
@RestController
@RequestMapping("/api/prompts")
public class PromptRunController {

    private static final Logger log = LoggerFactory.getLogger(PromptRunController.class);

    private final PromptRunService service;
    private final PromptRunMapper mapper;

    @Autowired
    public PromptRunController(PromptRunService service, PromptRunMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /**
     * 发起一次 Prompt Run
     *
     * POST /api/prompts/{id}/runs
     */
    @PostMapping("/{id}/runs")
    public Result<PromptRunDTO> createRun(
            @PathVariable Long id,
            @Valid @RequestBody PromptRunCreateDTO createDTO) {
        log.info("POST /api/prompts/{}/runs - model: {}, hasVariables: {}",
                id, createDTO.getModelName(), createDTO.getVariables() != null);

        try {
            PromptRun run = service.executeRun(
                    id,
                    createDTO.getInputText(),
                    createDTO.getVariables(),
                    createDTO.getModelName());
            return Result.success("运行成功", mapper.toDTO(run));
        } catch (IllegalArgumentException e) {
            // 缺少必需的模板变量
            log.warn("Missing required variables for promptId: {}, error: {}", id, e.getMessage());
            return new Result<>(400, e.getMessage(), null);
        } catch (RuntimeException e) {
            log.error("Failed to execute Prompt Run for promptId: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Prompt不存在，id: " + id);
        }
    }

    /**
     * 查询某个 Prompt 的运行历史
     *
     * GET /api/prompts/{id}/runs
     */
    @GetMapping("/{id}/runs")
    public Result<List<PromptRunDTO>> getRunHistory(@PathVariable Long id) {
        log.info("GET /api/prompts/{}/runs", id);
        List<PromptRun> runs = service.getRunHistory(id);
        return Result.success(mapper.toDTOList(runs));
    }

    /**
     * 获取 Prompt 的模板变量列表
     *
     * GET /api/prompts/{id}/variables
     */
    @GetMapping("/{id}/variables")
    public Result<List<String>> getVariables(@PathVariable Long id) {
        log.info("GET /api/prompts/{}/variables", id);
        try {
            List<String> variables = service.extractVariables(id);
            return Result.success(variables);
        } catch (RuntimeException e) {
            log.error("Failed to extract variables for promptId: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Prompt不存在，id: " + id);
        }
    }

    /**
     * 预览渲染后的 Prompt（不创建 PromptRun）
     *
     * POST /api/prompts/{id}/preview
     */
    @PostMapping("/{id}/preview")
    public Result<Map<String, String>> previewPrompt(
            @PathVariable Long id,
            @RequestBody PromptPreviewDTO previewDTO) {
        log.info("POST /api/prompts/{}/preview - variableCount: {}",
                id, previewDTO.getVariables() != null ? previewDTO.getVariables().size() : 0);

        try {
            String renderedPrompt = service.previewPrompt(id, previewDTO.getVariables());
            return Result.success(Map.of("renderedPrompt", renderedPrompt));
        } catch (IllegalArgumentException e) {
            // 缺少必需的模板变量
            log.warn("Missing required variables for promptId: {}, error: {}", id, e.getMessage());
            return new Result<>(400, e.getMessage(), null);
        } catch (RuntimeException e) {
            log.error("Failed to preview prompt for promptId: {}", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Prompt不存在，id: " + id);
        }
    }
}
