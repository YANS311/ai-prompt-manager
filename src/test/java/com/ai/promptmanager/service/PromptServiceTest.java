package com.ai.promptmanager.service;

import com.ai.promptmanager.TestBase;
import com.ai.promptmanager.entity.Prompt;
import com.ai.promptmanager.repository.PromptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PromptService
 *
 * Uses Mockito to mock repository layer and test business logic in isolation
 */
@ExtendWith(MockitoExtension.class)
class PromptServiceTest extends TestBase {

    @Mock
    private PromptRepository repository;

    @InjectMocks
    private PromptService service;

    private Prompt testPrompt;

    @BeforeEach
    void setUp() {
        testPrompt = createPromptWithId(1L, "Java Best Practices", "Use immutable objects", "Coding");
    }

    @Test
    void testFindById_Success() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(testPrompt));

        // When
        Optional<Prompt> result = service.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Java Best Practices");
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Prompt> result = service.findById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(repository, times(1)).findById(999L);
    }

    @Test
    void testFindAll_WithResults() {
        // Given
        Prompt prompt2 = createPromptWithId(2L, "Python Tips", "Use list comprehensions", "Coding");
        List<Prompt> prompts = Arrays.asList(testPrompt, prompt2);
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(prompts);

        // When
        List<Prompt> result = service.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Prompt::getTitle)
                .containsExactly("Java Best Practices", "Python Tips");
        verify(repository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void testFindAllPaged_ReturnsPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prompt> page = new PageImpl<>(Arrays.asList(testPrompt));
        when(repository.findAll(pageable)).thenReturn(page);

        // When
        Page<Prompt> result = service.findAllPaged(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java Best Practices");
        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    void testFindByCategory_WithResults() {
        // Given
        when(repository.findByCategoryOrderByCreatedAtDesc("Coding"))
                .thenReturn(Arrays.asList(testPrompt));

        // When
        List<Prompt> result = service.findByCategory("Coding");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Coding");
        verify(repository, times(1)).findByCategoryOrderByCreatedAtDesc("Coding");
    }

    @Test
    void testSearchByTitle_WithKeyword() {
        // Given
        when(repository.findByTitleContainingIgnoreCase("java"))
                .thenReturn(Arrays.asList(testPrompt));

        // When
        List<Prompt> result = service.searchByTitle("java");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).containsIgnoringCase("java");
        verify(repository, times(1)).findByTitleContainingIgnoreCase("java");
    }

    @Test
    void testSave_Success() {
        // Given
        Prompt newPrompt = createPrompt("New Prompt", "New content", "Testing");
        when(repository.save(any(Prompt.class))).thenReturn(testPrompt);

        // When
        Prompt result = service.save(newPrompt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(repository, times(1)).save(newPrompt);
    }

    @Test
    void testUpdate_Success() {
        // Given
        Prompt updatedData = createPrompt("Updated Title", "Updated content", "Updated");
        when(repository.findById(1L)).thenReturn(Optional.of(testPrompt));
        when(repository.save(any(Prompt.class))).thenReturn(testPrompt);

        // When
        Prompt result = service.update(1L, updatedData);

        // Then
        assertThat(result).isNotNull();
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(testPrompt);
    }

    @Test
    void testUpdate_NotFound_ThrowsException() {
        // Given
        Prompt updatedData = createPrompt("Updated Title", "Updated content", "Updated");
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.update(999L, updatedData))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Prompt not found with id: 999");
        verify(repository, times(1)).findById(999L);
        verify(repository, never()).save(any());
    }

    @Test
    void testDeleteById_Success() {
        // Given
        doNothing().when(repository).deleteById(1L);

        // When
        service.deleteById(1L);

        // Then
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void testExistsById_ReturnsTrue() {
        // Given
        when(repository.existsById(1L)).thenReturn(true);

        // When
        boolean result = service.existsById(1L);

        // Then
        assertThat(result).isTrue();
        verify(repository, times(1)).existsById(1L);
    }

    @Test
    void testExistsById_ReturnsFalse() {
        // Given
        when(repository.existsById(999L)).thenReturn(false);

        // When
        boolean result = service.existsById(999L);

        // Then
        assertThat(result).isFalse();
        verify(repository, times(1)).existsById(999L);
    }
}
