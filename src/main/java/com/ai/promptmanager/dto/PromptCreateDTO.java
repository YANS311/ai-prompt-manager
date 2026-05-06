package com.ai.promptmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Prompt 创建DTO
 *
 * 面试要点:
 * 1. 为什么创建和更新要分开DTO?
 *    - 创建时不需要id，更新时必须有id
 *    - 校验规则可能不同（如创建时某些字段必填，更新时可选）
 *    - 防止用户恶意传入敏感字段（如deleted、createdAt等）
 *
 * 2. @NotBlank vs @NotNull vs @NotEmpty?
 *    - @NotNull: 不能为null
 *    - @NotEmpty: 不能为null且长度>0（适用于集合和字符串）
 *    - @NotBlank: 不能为null且去除空格后长度>0（仅适用于字符串）
 *
 * 3. @Size 的作用?
 *    - 限制字符串长度，防止数据库字段溢出
 *    - 前端校验可能被绕过，后端必须再次校验
 */
public class PromptCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @Size(max = 10000, message = "内容长度不能超过10000个字符")
    private String content;

    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;

    // ==================== Constructors ====================

    public PromptCreateDTO() {
    }

    public PromptCreateDTO(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    // ==================== Getters & Setters ====================

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
