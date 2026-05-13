package com.ai.promptmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Prompt Run 创建 DTO
 *
 * 支持模板变量：
 * {
 *   "modelName": "gpt-4",
 *   "variables": {
 *     "role": "Java工程师",
 *     "task": "优化代码"
 *   }
 * }
 */
public class PromptRunCreateDTO {

    /**
     * 用户输入文本（可选，用于追加到渲染后的 Prompt）
     */
    @Size(max = 10000, message = "输入文本长度不能超过10000个字符")
    private String inputText;

    /**
     * 模板变量映射
     * key: 变量名, value: 变量值
     */
    private Map<String, String> variables;

    /**
     * 使用的模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称长度不能超过100个字符")
    private String modelName;

    /**
     * LLM 提供商名称 (可选，默认为 "mock")
     * 可选值: mock, openai, claude, deepseek
     */
    @Size(max = 50, message = "Provider name cannot exceed 50 characters")
    private String providerName;

    // ==================== Constructors ====================

    public PromptRunCreateDTO() {
    }

    public PromptRunCreateDTO(String inputText, Map<String, String> variables, String modelName) {
        this.inputText = inputText;
        this.variables = variables;
        this.modelName = modelName;
    }

    public PromptRunCreateDTO(String inputText, Map<String, String> variables, String modelName, String providerName) {
        this.inputText = inputText;
        this.variables = variables;
        this.modelName = modelName;
        this.providerName = providerName;
    }

    // ==================== Getters & Setters ====================

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
}
