package com.ai.promptmanager.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 缓存配置
 *
 * 面试要点:
 * 1. 本地缓存 vs 分布式缓存 (Caffeine vs Redis)
 * 2. 缓存过期策略: TTL (Time To Live)
 * 3. 缓存淘汰策略: LRU/LFU (Caffeine 使用 W-TinyLFU 算法)
 * 4. 缓存容量限制: 防止内存溢出
 * 5. 缓存一致性: @CacheEvict 保证数据更新后缓存失效
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置 Caffeine 缓存管理器
     *
     * 参数说明:
     * - initialCapacity(100): 初始缓存容量
     * - maximumSize(1000): 最大缓存条目数，超过后按 LFU 淘汰
     * - expireAfterWrite(10, MINUTES): 写入后 10 分钟过期
     * - recordStats(): 开启统计功能，可监控缓存命中率
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("prompts", "promptList");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}
