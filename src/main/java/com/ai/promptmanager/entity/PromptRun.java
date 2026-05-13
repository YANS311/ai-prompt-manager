package com.ai.promptmanager.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Prompt 运行记录实体
 *
 * 记录每次 Prompt 的执行情况，包括输入、渲染后的 Prompt、模型响应等
 * 目前使用 Mock 响应，为后续接入真实 LLM (OpenAI/Claude/DeepSeek) 做准备
 */
@Entity
@Table(name = "prompt_runs")
@EntityListeners(AuditingEntityListener.class)
public class PromptRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的 Prompt ID
     */
    @Column(nullable = false)
    private Long promptId;

    /**
     * 用户输入文本（可选，用于追加到渲染后的 Prompt）
     */
    @Column(name = "input_text", columnDefinition = "TEXT")
    private String inputText;

    /**
     * 模板变量 JSON 存储 (Map<String, String> 序列化)
     * 示例：{"role":"工程师","task":"优化代码"}
     */
    @Column(name = "variables_json", columnDefinition = "TEXT")
    private String variablesJson;

    /**
     * 渲染后的完整 Prompt (替换变量后的结果)
     */
    @Column(name = "rendered_prompt", columnDefinition = "TEXT", nullable = false)
    private String renderedPrompt;

    /**
     * 使用的模型名称 (如 gpt-4, claude-3, deepseek)
     */
    @Column(name = "model_name", length = 100, nullable = false)
    private String modelName;

    /**
     * 模型响应文本
     */
    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;

    /**
     * 运行状态: SUCCESS, FAILED
     */
    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private RunStatus status;

    /**
     * 运行耗时（毫秒）
     */
    @Column(name = "latency_ms")
    private Long latencyMs;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 运行状态枚举
     */
    public enum RunStatus {
        SUCCESS,
        FAILED
    }

    // ==================== Constructors ====================

    public PromptRun() {
    }

    // ==================== Getters & Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPromptId() {
        return promptId;
    }

    public void setPromptId(Long promptId) {
        this.promptId = promptId;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getVariablesJson() {
        return variablesJson;
    }

    public void setVariablesJson(String variablesJson) {
        this.variablesJson = variablesJson;
    }

    public String getRenderedPrompt() {
        return renderedPrompt;
    }

    public void setRenderedPrompt(String renderedPrompt) {
        this.renderedPrompt = renderedPrompt;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public RunStatus getStatus() {
        return status;
    }

    public void setStatus(RunStatus status) {
        this.status = status;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
