package com.ai.promptmanager.controller;

import com.ai.promptmanager.TestBase;
import com.ai.promptmanager.entity.Prompt;
import com.ai.promptmanager.repository.PromptRepository;
import com.ai.promptmanager.repository.PromptRunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PromptRunController
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PromptRunControllerTest extends TestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private PromptRunRepository promptRunRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateRun_ValidPrompt_ReturnsSuccess() throws Exception {
        // Given
        Prompt savedPrompt = promptRepository.save(createPrompt("Test Prompt", "Test content", "Testing"));
        String runPayload = "{\"inputText\":\"User input\",\"modelName\":\"gpt-4\"}";

        // When & Then
        mockMvc.perform(post("/api/prompts/" + savedPrompt.getId() + "/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(runPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("运行成功"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.promptId").value(savedPrompt.getId()))
                .andExpect(jsonPath("$.data.modelName").value("gpt-4"))
                .andExpect(jsonPath("$.data.inputText").value("User input"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.latencyMs").exists())
                .andExpect(jsonPath("$.data.responseText", containsString("Mock response")));
    }

    @Test
    void testCreateRun_NonExistentPrompt_ReturnsNotFound() throws Exception {
        // Given
        String runPayload = "{\"inputText\":\"User input\",\"modelName\":\"gpt-4\"}";

        // When & Then
        mockMvc.perform(post("/api/prompts/9999/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(runPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message", containsString("Prompt不存在")));
    }

    @Test
    void testCreateRun_InvalidPayload_ReturnsBadRequest() throws Exception {
        // Given
        Prompt savedPrompt = promptRepository.save(createPrompt("Test Prompt", "Test content", "Testing"));
        String invalidPayload = "{\"inputText\":\"User input\",\"modelName\":\"\"}";

        // When & Then
        mockMvc.perform(post("/api/prompts/" + savedPrompt.getId() + "/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data.modelName").exists());
    }

    @Test
    void testGetRunHistory_ReturnsEmptyList() throws Exception {
        // Given
        Prompt savedPrompt = promptRepository.save(createPrompt("Test Prompt", "Test content", "Testing"));

        // When & Then
        mockMvc.perform(get("/api/prompts/" + savedPrompt.getId() + "/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void testGetRunHistory_ReturnsRuns() throws Exception {
        // Given
        Prompt savedPrompt = promptRepository.save(createPrompt("Test Prompt", "Test content", "Testing"));

        // Create 2 runs
        String runPayload1 = "{\"inputText\":\"First input\",\"modelName\":\"gpt-4\"}";
        String runPayload2 = "{\"inputText\":\"Second input\",\"modelName\":\"claude-3\"}";

        mockMvc.perform(post("/api/prompts/" + savedPrompt.getId() + "/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(runPayload1))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/prompts/" + savedPrompt.getId() + "/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(runPayload2))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(get("/api/prompts/" + savedPrompt.getId() + "/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].inputText").value("Second input"))  // Most recent first
                .andExpect(jsonPath("$.data[1].inputText").value("First input"));
    }
}
