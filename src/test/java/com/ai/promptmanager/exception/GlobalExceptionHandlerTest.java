package com.ai.promptmanager.exception;

import com.ai.promptmanager.dto.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleValidationException_ReturnsFieldErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("prompt", "title", "标题不能为空");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        Result<Map<String, String>> result = handler.handleValidationExceptions(ex);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("参数校验失败");
        assertThat(result.getData()).containsEntry("title", "标题不能为空");
    }

    @Test
    void testHandleResponseStatusException_ReturnsCorrectStatus() {
        // Given
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Resource not found");

        // When
        Result<Void> result = handler.handleResponseStatusException(ex);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(404);
        assertThat(result.getMessage()).isEqualTo("Resource not found");
    }

    @Test
    void testHandleRuntimeException_Returns500() {
        // Given
        RuntimeException ex = new RuntimeException("Database connection failed");

        // When
        Result<Void> result = handler.handleRuntimeException(ex);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("系统繁忙");
    }

    @Test
    void testHandleGenericException_Returns500() {
        // Given
        Exception ex = new Exception("Unexpected error");

        // When
        Result<Void> result = handler.handleGenericException(ex);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("系统错误");
    }
}
