package com.ai.promptmanager.service;

import com.ai.promptmanager.entity.Prompt;
import com.ai.promptmanager.repository.PromptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Prompt 业务逻辑层
 *
 * 面试要点:
 * 1. Service 层职责: 业务逻辑、事务管理、缓存控制
 * 2. @Cacheable: 结果缓存，相同参数直接返回缓存
 * 3. @CacheEvict: 清除缓存，保证数据一致性
 * 4. @Transactional: 事务管理，保证 ACID
 * 5. 缓存三大问题:
 *    - 缓存穿透: 查询不存在的数据，可用布隆过滤器解决
 *    - 缓存击穿: 热点数据过期，可用互斥锁解决
 *    - 缓存雪崩: 大量缓存同时过期，可用随机过期时间解决
 */
@Service
public class PromptService {

    private static final Logger log = LoggerFactory.getLogger(PromptService.class);

    @Autowired
    private PromptRepository repository;

    /**
     * 根据 ID 查询单个 Prompt (带缓存)
     *
     * @Cacheable:
     * - value = "prompts": 缓存名称
     * - key = "#id": 使用 ID 作为缓存键
     * - 首次查询会从数据库读取并缓存，后续相同 ID 直接返回缓存
     */
    @Cacheable(value = "prompts", key = "#id")
    public Optional<Prompt> findById(Long id) {
        System.out.println("Cache MISS - 从数据库查询 ID: " + id);
        return repository.findById(id);
    }

    /**
     * 查询所有 Prompt (带缓存)
     *
     * 注意: 列表缓存需谨慎，数据量大时会占用大量内存
     * 生产环境建议只缓存热点数据或分页结果
     */
    @Cacheable(value = "promptList", key = "'all'")
    public List<Prompt> findAll() {
        System.out.println("Cache MISS - 从数据库查询所有数据");
        return repository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 分页查询 (不缓存)
     *
     * 分页结果通常不缓存，因为:
     * 1. 分页参数组合太多，缓存命中率低
     * 2. 数据频繁变化，缓存容易过期
     */
    public Page<Prompt> findAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * 根据分类分页查询 (不缓存)
     */
    public Page<Prompt> findByCategoryPaged(String category, Pageable pageable) {
        return repository.findByCategory(category, pageable);
    }

    /**
     * 根据分类查询
     */
    public List<Prompt> findByCategory(String category) {
        return repository.findByCategoryOrderByCreatedAtDesc(category);
    }

    /**
     * 根据标题搜索
     */
    public List<Prompt> searchByTitle(String keyword) {
        return repository.findByTitleContainingIgnoreCase(keyword);
    }

    /**
     * 创建新 Prompt (清除列表缓存)
     *
     * @CacheEvict:
     * - allEntries = true: 清除该缓存名下的所有条目
     * - 因为新增数据后，之前缓存的列表已经过期
     */
    @Transactional
    @CacheEvict(value = "promptList", allEntries = true)
    public Prompt save(Prompt prompt) {
        log.info("Creating new Prompt: title={}, category={}", prompt.getTitle(), prompt.getCategory());
        Prompt saved = repository.save(prompt);
        log.info("Prompt created successfully: id={}", saved.getId());
        return saved;
    }

    /**
     * 更新 Prompt (清除对应缓存)
     *
     * @Caching:
     * - 清除特定 ID 的缓存 (prompts)
     * - 清除所有列表缓存 (promptList)
     * - 保证下次查询时获取最新数据
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "prompts", key = "#id"),
            @CacheEvict(value = "promptList", allEntries = true)
    })
    public Prompt update(Long id, Prompt newPrompt) {
        return repository.findById(id).map(prompt -> {
            prompt.setTitle(newPrompt.getTitle());
            prompt.setContent(newPrompt.getContent());
            prompt.setCategory(newPrompt.getCategory());
            return repository.save(prompt);
        }).orElseThrow(() -> new RuntimeException("Prompt not found with id: " + id));
    }

    /**
     * 删除 Prompt (清除对应缓存)
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "prompts", key = "#id"),
            @CacheEvict(value = "promptList", allEntries = true)
    })
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * 检查是否存在
     */
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
