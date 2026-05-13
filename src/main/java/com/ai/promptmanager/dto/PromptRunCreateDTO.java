package com.ai.promptmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Prompt Run 创建 DTO
 */
public class PromptRunCreateDTO {

    /**
     * 用户输入文本
     */
    @Size(max = 10000, message = "输入文本长度不能超过10000个字符")
    private String inputText;

    /**
     * 使用的模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称长度不能超过100个字符")
    private String modelName;

    // ==================== Constructors ====================

    public PromptRunCreateDTO() {
    }

    public PromptRunCreateDTO(String inputText, String modelName) {
        this.inputText = inputText;
        this.modelName = modelName;
    }

    // ==================== Getters & Setters ====================

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
