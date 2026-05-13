package com.ai.promptmanager.dto;

import java.util.Map;

/**
 * Prompt 预览请求 DTO
 *
 * 用于预览渲染后的 Prompt，不会创建 PromptRun
 */
public class PromptPreviewDTO {

    /**
     * 模板变量映射
     */
    private Map<String, String> variables;

    // ==================== Constructors ====================

    public PromptPreviewDTO() {
    }

    public PromptPreviewDTO(Map<String, String> variables) {
        this.variables = variables;
    }

    // ==================== Getters & Setters ====================

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
}
