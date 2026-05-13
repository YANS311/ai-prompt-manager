package com.ai.promptmanager.llm.exception;

/**
 * LLM Provider Not Found Exception
 *
 * 当请求的 LLM 提供商不存在时抛出此异常。
 * 例如，请求 providerName="openai" 但系统中未注册 OpenAIProvider。
 */
public class LlmProviderNotFoundException extends RuntimeException {

    public LlmProviderNotFoundException(String message) {
        super(message);
    }

    public LlmProviderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
