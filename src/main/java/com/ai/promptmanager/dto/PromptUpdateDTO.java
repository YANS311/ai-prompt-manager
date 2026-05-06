package com.ai.promptmanager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Prompt 更新DTO
 *
 * 面试要点:
 * 1. 更新操作的设计考量:
 *    - 全量更新 vs 部分更新（PATCH）
 *    - 乐观锁版本号（防止并发修改）
 *    - 业务字段禁止修改（如createdAt）
 *
 * 2. 为什么不继承CreateDTO?
 *    - 校验规则可能不同
 *    - 字段可能不同（更新可能有额外字段如version）
 *    - 语义清晰，职责单一
 */
public class PromptUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @Size(max = 10000, message = "内容长度不能超过10000个字符")
    private String content;

    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;

    // 未来可以添加乐观锁版本号
    // private Integer version;

    // ==================== Constructors ====================

    public PromptUpdateDTO() {
    }

    public PromptUpdateDTO(Long id, String title, String content, String category) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
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
}
