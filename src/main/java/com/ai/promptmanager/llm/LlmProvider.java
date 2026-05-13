package com.ai.promptmanager.llm;

/**
 * LLM Provider Interface
 *
 * 定义所有 LLM 提供商的核心契约。
 * 实现类需要使用 @Component 注解以便 Spring 自动发现和注册。
 *
 * 支持的提供商：
 * - MockLlmProvider: 模拟 LLM 响应，用于开发和测试
 * - OpenAIProvider (未来): 接入 OpenAI API
 * - ClaudeProvider (未来): 接入 Anthropic Claude API
 * - DeepSeekProvider (未来): 接入 DeepSeek API
 */
public interface LlmProvider {

    /**
     * 获取提供商名称
     *
     * @return 提供商标识，例如 "mock", "openai", "claude", "deepseek"
     */
    String getProviderName();

    /**
     * 生成 LLM 响应
     *
     * @param request LLM 请求对象，包含 prompt、modelName 等信息
     * @return LLM 响应对象，包含生成的文本、状态、延迟等信息
     */
    LlmResponse generate(LlmRequest request);
}
