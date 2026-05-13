package com.ai.promptmanager.llm;

import com.ai.promptmanager.llm.exception.LlmProviderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM Provider Registry
 *
 * LLM 提供商注册中心，负责管理和路由所有 LLM 提供商。
 *
 * 功能：
 * - 自动发现所有实现了 LlmProvider 接口的 Spring Bean
 * - 根据 providerName 路由到对应的实现
 * - 提供默认 provider（mock）
 * - 提供 provider 存在性检查
 *
 * Spring 容器会自动注入所有 LlmProvider 实现类到构造函数的 List 参数中。
 */
@Component
public class LlmProviderRegistry {

    private static final Logger log = LoggerFactory.getLogger(LlmProviderRegistry.class);
    private static final String DEFAULT_PROVIDER = "mock";

    private final Map<String, LlmProvider> providers;

    @Autowired
    public LlmProviderRegistry(List<LlmProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(LlmProvider::getProviderName, p -> p));

        log.info("LlmProviderRegistry initialized with {} provider(s): {}",
                providers.size(),
                String.join(", ", providers.keySet()));
    }

    /**
     * 获取 LLM 提供商
     *
     * @param providerName 提供商名称，如果为 null 或空字符串，默认使用 "mock"
     * @return LlmProvider 实现
     * @throws LlmProviderNotFoundException 如果提供商不存在
     */
    public LlmProvider getProvider(String providerName) {
        // Default to mock if provider name is null or empty
        if (providerName == null || providerName.isEmpty()) {
            log.debug("Provider name is null/empty, defaulting to '{}'", DEFAULT_PROVIDER);
            providerName = DEFAULT_PROVIDER;
        }

        LlmProvider provider = providers.get(providerName);
        if (provider == null) {
            throw new LlmProviderNotFoundException(
                    "Provider not found: " + providerName + ". Available providers: " +
                            String.join(", ", providers.keySet()));
        }

        return provider;
    }

    /**
     * 检查提供商是否存在
     *
     * @param providerName 提供商名称
     * @return true 如果提供商存在，否则 false
     */
    public boolean hasProvider(String providerName) {
        return providers.containsKey(providerName);
    }
}
