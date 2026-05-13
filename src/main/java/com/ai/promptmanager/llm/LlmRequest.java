package com.ai.promptmanager.llm;

import java.util.Map;

/**
 * LLM Request DTO
 *
 * 封装发送给 LLM 提供商的请求信息。
 * 使用 Builder 模式构造对象，提高可读性和灵活性。
 */
public class LlmRequest {

    private String prompt;
    private String modelName;
    private Long promptId;
    private Integer promptVersionNumber;
    private Map<String, String> variables;

    public LlmRequest() {
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Long getPromptId() {
        return promptId;
    }

    public void setPromptId(Long promptId) {
        this.promptId = promptId;
    }

    public Integer getPromptVersionNumber() {
        return promptVersionNumber;
    }

    public void setPromptVersionNumber(Integer promptVersionNumber) {
        this.promptVersionNumber = promptVersionNumber;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    /**
     * 创建 Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 内部类，用于流式构造 LlmRequest 对象
     */
    public static class Builder {
        private String prompt;
        private String modelName;
        private Long promptId;
        private Integer promptVersionNumber;
        private Map<String, String> variables;

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder promptId(Long promptId) {
            this.promptId = promptId;
            return this;
        }

        public Builder promptVersionNumber(Integer version) {
            this.promptVersionNumber = version;
            return this;
        }

        public Builder variables(Map<String, String> variables) {
            this.variables = variables;
            return this;
        }

        public LlmRequest build() {
            LlmRequest request = new LlmRequest();
            request.prompt = this.prompt;
            request.modelName = this.modelName;
            request.promptId = this.promptId;
            request.promptVersionNumber = this.promptVersionNumber;
            request.variables = this.variables;
            return request;
        }
    }
}
