package com.ai.promptmanager.llm.provider;

import com.ai.promptmanager.llm.LlmRequest;
import com.ai.promptmanager.llm.LlmResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for MockLlmProvider
 */
class MockLlmProviderTest {

    private final MockLlmProvider provider = new MockLlmProvider();

    @Test
    void testGetProviderName() {
        assertThat(provider.getProviderName()).isEqualTo("mock");
    }

    @Test
    void testGenerate_ReturnsSuccessResponse() {
        // Given
        LlmRequest request = LlmRequest.builder()
                .prompt("You are a Java Engineer.")
                .modelName("mock-gpt")
                .promptId(1L)
                .build();

        // When
        LlmResponse response = provider.generate(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProviderName()).isEqualTo("mock");
        assertThat(response.getModelName()).isEqualTo("mock-gpt");
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getResponseText()).contains("Mock response from model [mock-gpt]");
        assertThat(response.getLatencyMs()).isGreaterThan(0);
        assertThat(response.getErrorMessage()).isNull();
    }

    @Test
    void testGenerate_IncludesPromptLength() {
        // Given
        String longPrompt = "Test ".repeat(100);
        LlmRequest request = LlmRequest.builder()
                .prompt(longPrompt)
                .modelName("gpt-4")
                .build();

        // When
        LlmResponse response = provider.generate(request);

        // Then
        assertThat(response.getResponseText()).contains(String.valueOf(longPrompt.length()));
    }
}
