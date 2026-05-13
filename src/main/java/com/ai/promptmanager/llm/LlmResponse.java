package com.ai.promptmanager.llm;

/**
 * LLM Response DTO
 *
 * 封装 LLM 提供商返回的响应信息。
 * 包含生成的文本、状态、延迟、错误信息等。
 */
public class LlmResponse {

    private String providerName;
    private String modelName;
    private String responseText;
    private String status;  // SUCCESS or FAILED
    private Long latencyMs;
    private String errorMessage;
    private String tokenUsageJson;  // Optional: JSON string for token counts

    public LlmResponse() {
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTokenUsageJson() {
        return tokenUsageJson;
    }

    public void setTokenUsageJson(String tokenUsageJson) {
        this.tokenUsageJson = tokenUsageJson;
    }

    /**
     * 创建 Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 内部类，用于流式构造 LlmResponse 对象
     */
    public static class Builder {
        private String providerName;
        private String modelName;
        private String responseText;
        private String status;
        private Long latencyMs;
        private String errorMessage;
        private String tokenUsageJson;

        public Builder providerName(String providerName) {
            this.providerName = providerName;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder responseText(String responseText) {
            this.responseText = responseText;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder latencyMs(Long latencyMs) {
            this.latencyMs = latencyMs;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder tokenUsageJson(String tokenUsageJson) {
            this.tokenUsageJson = tokenUsageJson;
            return this;
        }

        public LlmResponse build() {
            LlmResponse response = new LlmResponse();
            response.providerName = this.providerName;
            response.modelName = this.modelName;
            response.responseText = this.responseText;
            response.status = this.status;
            response.latencyMs = this.latencyMs;
            response.errorMessage = this.errorMessage;
            response.tokenUsageJson = this.tokenUsageJson;
            return response;
        }
    }
}
