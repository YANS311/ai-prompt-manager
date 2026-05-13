package com.ai.promptmanager.exception;

import com.ai.promptmanager.dto.Result;
import com.ai.promptmanager.llm.exception.LlmProviderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * 面试要点:
 * 1. @RestControllerAdvice 的作用?
 *    - 全局拦截所有 @RestController 抛出的异常
 *    - 统一处理异常，避免每个Controller都try-catch
 *    - 返回统一格式的错误信息给前端
 *
 * 2. @ExceptionHandler 的优先级?
 *    - 先匹配具体异常类型（MethodArgumentNotValidException）
 *    - 再匹配父类（Exception）
 *    - 遵循"最精确匹配"原则
 *
 * 3. 为什么需要记录日志?
 *    - 线上问题排查的唯一依据
 *    - 记录异常堆栈、请求参数、用户ID等
 *    - 可配合ELK、Sentry等工具实现告警
 *
 * 4. 生产环境的最佳实践?
 *    - 不要返回详细的异常堆栈给前端（安全风险）
 *    - 记录完整日志到服务器
 *    - 返回友好的错误提示给用户
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理参数校验异常 (如 @NotBlank, @Size 等)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("参数校验失败: {}", errors);
        return new Result<>(400, "参数校验失败", errors);
    }

    /**
     * 处理 LLM Provider 未找到异常
     */
    @ExceptionHandler(LlmProviderNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleLlmProviderNotFound(LlmProviderNotFoundException ex) {
        log.warn("LLM Provider not found: {}", ex.getMessage());
        return new Result<>(400, ex.getMessage(), null);
    }

    /**
     * 处理 ResponseStatusException (自定义HTTP状态码)
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Result<Void> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("ResponseStatusException: {}", ex.getReason());
        return Result.error(ex.getStatusCode().value(), ex.getReason());
    }

    /**
     * 处理业务异常 (RuntimeException)
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException ex) {
        log.error("运行时异常", ex);
        // 生产环境不返回详细错误信息，避免泄露系统信息
        return Result.error("系统繁忙，请稍后重试");
    }

    /**
     * 处理所有其他异常 (兜底处理)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleGenericException(Exception ex) {
        log.error("未知异常", ex);
        return Result.error("系统错误，请联系管理员");
    }
}
