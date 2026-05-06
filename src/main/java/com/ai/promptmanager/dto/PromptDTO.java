package com.ai.promptmanager.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Prompt 数据传输对象 (查询返回)
 *
 * 面试要点:
 * 1. 为什么不直接返回Entity?
 *    - 数据库结构隐藏：Entity包含JPA注解、外键等敏感信息
 *    - 字段裁剪：前端可能不需要所有字段（如删除标识deleted）
 *    - 字段转换：时间格式化、枚举转换、多语言等
 *    - 防止N+1问题：避免懒加载关联对象导致性能问题
 *
 * 2. DTO vs VO 的区别？
 *    - DTO (Data Transfer Object): 用于跨层传输，如Service->Controller
 *    - VO (View Object): 专门用于展示层，如前端展示用的数据
 *    - 实际项目中，两者常混用，统一叫DTO
 *
 * 3. 为什么用@JsonFormat?
 *    - 统一时间格式，前端不需要自己格式化
 *    - 避免时区问题
 */
public class PromptDTO {

    private Long id;

    private String title;

    private String content;

    private String category;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 可以添加业务计算字段，例如：
    // private Integer contentLength; // 内容长度
    // private String contentPreview; // 内容预览（前100字）

    // ==================== Constructors ====================

    public PromptDTO() {
    }

    public PromptDTO(Long id, String title, String content, String category,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ==================== Getters & Setters ====================

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
}
