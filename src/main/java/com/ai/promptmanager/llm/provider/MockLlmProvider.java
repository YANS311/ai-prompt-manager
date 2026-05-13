package com.ai.promptmanager.llm.provider;

import com.ai.promptmanager.llm.LlmProvider;
import com.ai.promptmanager.llm.LlmRequest;
import com.ai.promptmanager.llm.LlmResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mock LLM Provider
 *
 * 模拟 LLM 提供商，用于开发和测试。
 * 生成可预测的 Mock 响应，不调用真实 API。
 *
 * 特性：
 * - 模拟 5-20ms 延迟
 * - 返回包含 prompt 长度的响应文本
 * - 总是返回 SUCCESS 状态
 * - 不消耗 token（tokenUsageJson 为 null）
 */
@Component
public class MockLlmProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(MockLlmProvider.class);

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        long startTime = System.currentTimeMillis();

        log.info("MockLlmProvider - Generating response for model: {}, promptLength: {}",
                request.getModelName(), request.getPrompt().length());

        // Simulate processing delay (5-20ms)
        try {
            Thread.sleep(5 + (long) (Math.random() * 15));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Mock LLM processing interrupted", e);
        }

        // Generate mock response (same logic as original PromptRunService.generateMockResponse())
        String responseText = String.format("Mock response from model [%s]:\n\n" +
                        "This is a simulated response for your prompt. " +
                        "The rendered prompt has %d characters. " +
                        "In production, this would be replaced with actual LLM API calls to OpenAI, Claude, or DeepSeek.",
                request.getModelName(), request.getPrompt().length());

        long latencyMs = System.currentTimeMillis() - startTime;

        LlmResponse response = new LlmResponse();
        response.setProviderName("mock");
        response.setModelName(request.getModelName());
        response.setResponseText(responseText);
        response.setStatus("SUCCESS");
        response.setLatencyMs(latencyMs);
        response.setErrorMessage(null);
        response.setTokenUsageJson(null);  // Mock has no token usage

        log.debug("MockLlmProvider - Response generated in {}ms", latencyMs);

        return response;
    }
}
