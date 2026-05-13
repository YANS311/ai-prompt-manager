package com.ai.promptmanager.llm;

import com.ai.promptmanager.llm.exception.LlmProviderNotFoundException;
import com.ai.promptmanager.llm.provider.MockLlmProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for LlmProviderRegistry
 */
class LlmProviderRegistryTest {

    private LlmProviderRegistry registry;

    @BeforeEach
    void setUp() {
        LlmProvider mockProvider = new MockLlmProvider();
        registry = new LlmProviderRegistry(List.of(mockProvider));
    }

    @Test
    void testGetProvider_Mock_ReturnsProvider() {
        LlmProvider provider = registry.getProvider("mock");
        assertThat(provider).isNotNull();
        assertThat(provider.getProviderName()).isEqualTo("mock");
    }

    @Test
    void testGetProvider_Null_DefaultsToMock() {
        LlmProvider provider = registry.getProvider(null);
        assertThat(provider.getProviderName()).isEqualTo("mock");
    }

    @Test
    void testGetProvider_Empty_DefaultsToMock() {
        LlmProvider provider = registry.getProvider("");
        assertThat(provider.getProviderName()).isEqualTo("mock");
    }

    @Test
    void testGetProvider_Unknown_ThrowsException() {
        assertThatThrownBy(() -> registry.getProvider("openai"))
                .isInstanceOf(LlmProviderNotFoundException.class)
                .hasMessageContaining("Provider not found: openai")
                .hasMessageContaining("Available providers: mock");
    }

    @Test
    void testHasProvider_Mock_ReturnsTrue() {
        assertThat(registry.hasProvider("mock")).isTrue();
    }

    @Test
    void testHasProvider_Unknown_ReturnsFalse() {
        assertThat(registry.hasProvider("openai")).isFalse();
    }
}
