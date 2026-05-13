package com.ai.promptmanager.controller;

import com.ai.promptmanager.TestBase;
import com.ai.promptmanager.entity.Prompt;
import com.ai.promptmanager.repository.PromptRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PromptController
 *
 * Uses @SpringBootTest with MockMvc to test the full REST API stack
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PromptControllerTest extends TestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromptRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAll_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/prompts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void testGetAll_ReturnsPrompts() throws Exception {
        // Given
        repository.save(createPrompt("Java Guide", "Content 1", "Coding"));
        repository.save(createPrompt("Python Tips", "Content 2", "Coding"));

        // When & Then
        mockMvc.perform(get("/api/prompts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].title", containsInAnyOrder("Java Guide", "Python Tips")));
    }

    @Test
    void testGetAll_WithCategoryFilter() throws Exception {
        // Given
        repository.save(createPrompt("Java Guide", "Content 1", "Coding"));
        repository.save(createPrompt("Design Pattern", "Content 2", "Architecture"));

        // When & Then
        mockMvc.perform(get("/api/prompts")
                        .param("category", "Coding"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].category").value("Coding"));
    }

    @Test
    void testGetPage_ReturnsPaginatedResults() throws Exception {
        // Given
        for (int i = 1; i <= 15; i++) {
            repository.save(createPrompt("Prompt " + i, "Content " + i, "Testing"));
        }

        // When & Then - first page
        mockMvc.perform(get("/api/prompts/page")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content", hasSize(5)))
                .andExpect(jsonPath("$.data.totalElements").value(15))
                .andExpect(jsonPath("$.data.totalPages").value(3));
    }

    @Test
    void testGetPage_WithCategoryFilter_ReturnsFilteredResults() throws Exception {
        // Given
        repository.save(createPrompt("Java Guide", "Content 1", "Coding"));
        repository.save(createPrompt("Python Tips", "Content 2", "Coding"));
        repository.save(createPrompt("Design Pattern", "Content 3", "Architecture"));

        // When & Then
        mockMvc.perform(get("/api/prompts/page")
                        .param("page", "0")
                        .param("size", "10")
                        .param("category", "Coding"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[*].category", hasItems("Coding")));
    }

    @Test
    void testCreate_ValidPayload_ReturnsCreated() throws Exception {
        // Given
        String jsonPayload = "{\"title\":\"New Prompt\",\"content\":\"New content\",\"category\":\"Testing\"}";

        // When & Then
        mockMvc.perform(post("/api/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("创建成功"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.title").value("New Prompt"));
    }

    @Test
    void testCreate_InvalidPayload_ReturnsBadRequest() throws Exception {
        // Given - blank title violates @NotBlank validation
        String invalidPayload = "{\"title\":\"\",\"content\":\"Content\",\"category\":\"Testing\"}";

        // When & Then
        mockMvc.perform(post("/api/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data.title").exists());
    }

    @Test
    void testGetOne_ExistingId_ReturnsPrompt() throws Exception {
        // Given
        Prompt saved = repository.save(createPrompt("Test Prompt", "Test content", "Testing"));

        // When & Then
        mockMvc.perform(get("/api/prompts/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(saved.getId()))
                .andExpect(jsonPath("$.data.title").value("Test Prompt"));
    }

    @Test
    void testGetOne_NonExistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/prompts/9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message", containsString("Prompt不存在")));
    }

    @Test
    void testUpdate_ValidPayload_ReturnsOk() throws Exception {
        // Given
        Prompt saved = repository.save(createPrompt("Original Title", "Original content", "Testing"));
        String updatePayload = "{\"id\":" + saved.getId() + ",\"title\":\"Updated Title\",\"content\":\"Updated content\",\"category\":\"Updated\"}";

        // When & Then
        mockMvc.perform(put("/api/prompts/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功"))
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    void testUpdate_NonExistentId_ReturnsNotFound() throws Exception {
        // Given
        String updatePayload = "{\"id\":9999,\"title\":\"Updated Title\",\"content\":\"Updated content\",\"category\":\"Updated\"}";

        // When & Then
        mockMvc.perform(put("/api/prompts/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message", containsString("Prompt不存在")));
    }

    @Test
    void testDelete_ExistingId_ReturnsSuccess() throws Exception {
        // Given
        Prompt saved = repository.save(createPrompt("To Delete", "Content", "Testing"));

        // When & Then
        mockMvc.perform(delete("/api/prompts/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }

    @Test
    void testDelete_NonExistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/prompts/9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message", containsString("Prompt不存在")));
    }

    @Test
    void testSearch_WithKeyword_ReturnsMatches() throws Exception {
        // Given
        repository.save(createPrompt("Java Best Practices", "Content 1", "Coding"));
        repository.save(createPrompt("Python Tips", "Content 2", "Coding"));
        repository.save(createPrompt("JavaScript Guide", "Content 3", "Coding"));

        // When & Then
        mockMvc.perform(get("/api/prompts/search")
                        .param("keyword", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].title",
                        hasItems(containsString("Java"), containsString("JavaScript"))));
    }
}
