package com.ai.promptmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Prompt实体类
 *
 * 面试要点:
 * 1. @EntityListeners(AuditingEntityListener.class)
 *    - Spring Data JPA审计功能，自动填充时间戳
 *    - 需要在主类添加 @EnableJpaAuditing
 *
 * 2. @SQLDelete 软删除
 *    - 删除时不真正删除，只是标记deleted=true
 *    - 防止误删除，可追溯历史数据
 *    - 生产环境必备功能
 *
 * 3. @SQLRestriction (Hibernate 6.2+ 替代了 @Where)
 *    - 查询时自动过滤deleted=true的数据
 *    - 对开发者透明，不需要每个查询都加WHERE条件
 *
 * 4. 为什么不用@CreatedBy/@LastModifiedBy?
 *    - 需要实现AuditorAware接口获取当前用户
 *    - 当前项目未实现用户认证，暂不添加
 *    - 实际项目中可以从Spring Security获取当前用户
 */
@Entity
@Table(name = "prompts")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE prompts SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Prompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String category;

    /**
     * 创建时间 (由Spring Data JPA自动填充)
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间 (由Spring Data JPA自动填充)
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 软删除标识 (false=正常, true=已删除)
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    // Constructors
    public Prompt() {
    }

    public Prompt(Long id, String title, String content, String category,
                  LocalDateTime createdAt, LocalDateTime updatedAt, Boolean deleted) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
