package com.ai.promptmanager;

import com.ai.promptmanager.entity.Prompt;

import java.time.LocalDateTime;

/**
 * Test utility class providing common test fixtures and helper methods
 */
public abstract class TestBase {

    /**
     * Create a sample Prompt for testing
     */
    protected Prompt createSamplePrompt() {
        Prompt prompt = new Prompt();
        prompt.setTitle("Test Prompt");
        prompt.setContent("Test content for prompt");
        prompt.setCategory("Testing");
        prompt.setDeleted(false);
        return prompt;
    }

    /**
     * Create a sample Prompt with custom values
     */
    protected Prompt createPrompt(String title, String content, String category) {
        Prompt prompt = new Prompt();
        prompt.setTitle(title);
        prompt.setContent(content);
        prompt.setCategory(category);
        prompt.setDeleted(false);
        return prompt;
    }

    /**
     * Create a Prompt with ID for mock scenarios
     */
    protected Prompt createPromptWithId(Long id, String title, String content, String category) {
        Prompt prompt = new Prompt();
        prompt.setId(id);
        prompt.setTitle(title);
        prompt.setContent(content);
        prompt.setCategory(category);
        prompt.setCreatedAt(LocalDateTime.now());
        prompt.setUpdatedAt(LocalDateTime.now());
        prompt.setDeleted(false);
        return prompt;
    }
}
