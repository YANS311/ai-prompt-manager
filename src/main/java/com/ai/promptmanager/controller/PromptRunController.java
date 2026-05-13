package com.ai.promptmanager.controller;

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
        log.info("POST /api/prompts/{}/runs - model: {}", id, createDTO.getModelName());

        try {
            PromptRun run = service.executeRun(id, createDTO.getInputText(), createDTO.getModelName());
            return Result.success("运行成功", mapper.toDTO(run));
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
}
