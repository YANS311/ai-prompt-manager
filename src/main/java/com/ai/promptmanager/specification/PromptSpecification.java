package com.ai.promptmanager.specification;

import com.ai.promptmanager.entity.Prompt;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态查询规约 (Specification Pattern)
 *
 * 面试要点:
 * 1. 为什么要用Specification?
 *    - 避免为每个查询组合写Repository方法（会爆炸式增长）
 *    - 动态构建WHERE子句，支持复杂的AND/OR组合
 *    - 类型安全，编译期检查，不会拼错字段名
 *
 * 2. Specification vs QueryDSL vs MyBatis?
 *    - Specification: JPA标准，学习成本低，适合简单查询
 *    - QueryDSL: 需要生成Q类，API更流畅，适合复杂查询
 *    - MyBatis: 灵活但需要手写SQL，适合复杂业务或性能优化
 *
 * 3. Criteria API 核心概念:
 *    - Root: 查询根（FROM子句）
 *    - CriteriaBuilder: 构建查询条件（WHERE、ORDER BY等）
 *    - Predicate: 查询谓词（等于、like、between等）
 *
 * 4. 性能考量:
 *    - Specification会生成动态SQL，注意索引覆盖
 *    - 模糊查询（LIKE %keyword%）无法使用索引，数据量大时慢
 *    - 可以考虑全文搜索引擎（Elasticsearch）
 */
public class PromptSpecification {

    /**
     * 根据多个条件动态组合查询
     *
     * @param keyword   标题关键词（模糊匹配）
     * @param category  分类（精确匹配）
     * @param startDate 创建时间起始
     * @param endDate   创建时间截止
     * @return Specification
     */
    public static Specification<Prompt> filterBy(
            String keyword,
            String category,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 标题关键词模糊匹配
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + keyword.toLowerCase() + "%"
                ));
            }

            // 2. 分类精确匹配
            if (category != null && !category.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            // 3. 创建时间范围查询
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), startDate
                ));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), endDate
                ));
            }

            // 4. 组合所有条件（AND关系）
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 按标题搜索（不区分大小写）
     */
    public static Specification<Prompt> titleContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction(); // 返回恒真条件（1=1）
            }
            return cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
        };
    }

    /**
     * 按分类筛选
     */
    public static Specification<Prompt> categoryEquals(String category) {
        return (root, query, cb) -> {
            if (category == null || category.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category"), category);
        };
    }

    /**
     * 创建时间在指定日期之后
     */
    public static Specification<Prompt> createdAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    /**
     * 创建时间在指定日期之前
     */
    public static Specification<Prompt> createdBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    /**
     * 组合多个Specification（链式调用示例）
     *
     * 使用方式:
     * Specification<Prompt> spec = Specification.where(titleContains("Java"))
     *     .and(categoryEquals("Coding"))
     *     .and(createdAfter(LocalDateTime.now().minusDays(7)));
     */
}
