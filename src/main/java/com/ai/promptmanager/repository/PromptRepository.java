package com.ai.promptmanager.repository;

import com.ai.promptmanager.entity.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Prompt数据访问层
 *
 * 面试要点:
 * 1. 为什么继承JpaSpecificationExecutor?
 *    - 获得动态查询能力：findAll(Specification)
 *    - 支持分页：findAll(Specification, Pageable)
 *    - 支持排序：findAll(Specification, Sort)
 *
 * 2. 方法命名查询 vs Specification?
 *    - 命名查询：简单、直观，适合固定查询
 *    - Specification：灵活、动态，适合复杂组合查询
 *    - 两者可以共存，根据场景选择
 */
@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long>, JpaSpecificationExecutor<Prompt> {

    /**
     * 根据分类查询 Prompts
     */
    List<Prompt> findByCategory(String category);

    /**
     * 根据标题模糊查询
     */
    List<Prompt> findByTitleContainingIgnoreCase(String keyword);

    /**
     * 根据分类查询并按创建时间倒序
     */
    List<Prompt> findByCategoryOrderByCreatedAtDesc(String category);

    /**
     * 查询所有并按创建时间倒序
     */
    List<Prompt> findAllByOrderByCreatedAtDesc();
}
