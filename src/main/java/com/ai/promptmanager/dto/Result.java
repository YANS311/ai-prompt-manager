package com.ai.promptmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 统一返回结果封装
 *
 * 面试要点:
 * 1. 为什么需要统一返回格式？
 *    - 前端可以统一处理响应（统一拦截错误、显示消息）
 *    - 规范化API设计，便于团队协作
 *    - 方便接入网关、监控等中间件
 *
 * 2. 为什么用泛型 <T>?
 *    - 支持任意类型的数据返回（DTO、List、Page等）
 *    - 类型安全，编译期检查
 *
 * 3. @JsonInclude(NON_NULL) 的作用？
 *    - 减少JSON体积，null字段不序列化
 *    - 前端不需要处理null值判断
 *
 * @param <T> 数据类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /**
     * 响应码 (200=成功, 400=参数错误, 401=未授权, 403=禁止访问, 404=资源不存在, 500=服务器错误)
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳（用于追踪请求，排查问题）
     */
    private LocalDateTime timestamp;

    // ==================== 构造方法 ====================

    public Result() {
        this.timestamp = LocalDateTime.now();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 成功返回（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功返回（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败返回
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 失败返回（自定义错误码）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 参数校验失败
     */
    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, message, null);
    }

    /**
     * 资源不存在
     */
    public static <T> Result<T> notFound(String message) {
        return new Result<>(404, message, null);
    }

    // ==================== Getters & Setters ====================

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
