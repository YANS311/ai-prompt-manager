package com.ai.promptmanager.repository;

import com.ai.promptmanager.TestBase;
import com.ai.promptmanager.entity.Prompt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PromptRepository
 *
 * Uses @DataJpaTest which provides:
 * - H2 in-memory database
 * - Auto-configuration of Spring Data JPA
 * - Transaction rollback after each test
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class PromptRepositoryTest extends TestBase {

    @Autowired
    private PromptRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testSave_AndRetrieve() {
        // Given
        Prompt prompt = createPrompt("Test Title", "Test content", "Testing");

        // When
        Prompt saved = repository.save(prompt);
        entityManager.flush();
        Optional<Prompt> retrieved = repository.findById(saved.getId());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getTitle()).isEqualTo("Test Title");
        assertThat(retrieved.get().getContent()).isEqualTo("Test content");
        assertThat(retrieved.get().getCategory()).isEqualTo("Testing");
    }

    @Test
    void testFindByCategory_ReturnsFilteredResults() {
        // Given
        repository.save(createPrompt("Java Prompt", "Java content", "Coding"));
        repository.save(createPrompt("Python Prompt", "Python content", "Coding"));
        repository.save(createPrompt("Design Pattern", "Design content", "Architecture"));
        entityManager.flush();

        // When
        List<Prompt> codingPrompts = repository.findByCategoryOrderByCreatedAtDesc("Coding");

        // Then
        assertThat(codingPrompts).hasSize(2);
        assertThat(codingPrompts).extracting(Prompt::getCategory)
                .containsOnly("Coding");
    }

    @Test
    void testFindByTitleContainingIgnoreCase_ReturnsMatches() {
        // Given
        repository.save(createPrompt("Java Best Practices", "Content 1", "Coding"));
        repository.save(createPrompt("Python Tips", "Content 2", "Coding"));
        repository.save(createPrompt("JavaScript Guide", "Content 3", "Coding"));
        entityManager.flush();

        // When
        List<Prompt> results = repository.findByTitleContainingIgnoreCase("java");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Prompt::getTitle)
                .containsExactlyInAnyOrder("Java Best Practices", "JavaScript Guide");
    }

    @Test
    void testSoftDelete_HidesFromQueries() {
        // Given
        Prompt prompt = createPrompt("To Be Deleted", "Content", "Testing");
        Prompt saved = repository.save(prompt);
        Long id = saved.getId();
        entityManager.flush();

        // When - delete (soft delete via @SQLDelete)
        repository.deleteById(id);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to force fresh query

        // Then - deleted record should not appear in queries due to @SQLRestriction
        Optional<Prompt> retrieved = repository.findById(id);
        assertThat(retrieved).isEmpty();

        // Verify it still exists in database with deleted=true
        List<Prompt> all = repository.findAll();
        assertThat(all).noneMatch(p -> p.getId().equals(id));
    }

    @Test
    void testFindAllByOrderByCreatedAtDesc_SortsCorrectly() {
        // Given
        Prompt prompt1 = createPrompt("First", "Content 1", "Testing");
        Prompt prompt2 = createPrompt("Second", "Content 2", "Testing");
        Prompt prompt3 = createPrompt("Third", "Content 3", "Testing");

        repository.save(prompt1);
        Thread.yield(); // Small delay to ensure different timestamps
        repository.save(prompt2);
        Thread.yield();
        repository.save(prompt3);
        entityManager.flush();

        // When
        List<Prompt> sorted = repository.findAllByOrderByCreatedAtDesc();

        // Then
        assertThat(sorted).hasSize(3);
        // Most recent first
        assertThat(sorted.get(0).getTitle()).isEqualTo("Third");
        assertThat(sorted.get(2).getTitle()).isEqualTo("First");
    }

    @Test
    void testJpaAuditing_AutoPopulatesTimestamps() {
        // Given
        Prompt prompt = createPrompt("Audit Test", "Content", "Testing");

        // When
        Prompt saved = repository.save(prompt);
        entityManager.flush();

        // Then - createdAt and updatedAt should be auto-populated
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
    }

    @Test
    void testUpdate_UpdatesTimestamp() throws InterruptedException {
        // Given
        Prompt prompt = createPrompt("Original Title", "Original content", "Testing");
        Prompt saved = repository.save(prompt);
        entityManager.flush();

        // Wait a bit to ensure timestamp difference
        Thread.sleep(100);

        // When - update the prompt
        saved.setTitle("Updated Title");
        Prompt updated = repository.save(saved);
        entityManager.flush();

        // Then - updatedAt should be newer than createdAt
        assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
    }
}
