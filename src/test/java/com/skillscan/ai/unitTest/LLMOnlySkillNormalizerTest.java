package com.skillscan.ai.unitTest;

import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.services.LLMService;
import com.skillscan.ai.services.impl.LLMOnlySkillNormalizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LLMOnlySkillNormalizerTest {

    @Mock
    private LLMService llmService;

    @InjectMocks
    private LLMOnlySkillNormalizer normalizer;

    //  Empty input → should return empty list
    @Test
    void shouldReturnEmptyWhenInputIsNullOrEmpty() {

        assertTrue(normalizer.normalize(null).isEmpty());
        assertTrue(normalizer.normalize(List.of()).isEmpty());

        verifyNoInteractions(llmService);
    }

    //  Successful LLM normalization
    @Test
    void shouldNormalizeUsingLLM() {

        List<String> input = List.of(" Java ", "spring", "JAVA");

        AIResponse llmResponse = AIResponse.builder()
                .skills(List.of("Java", "Spring Boot"))
                .build();

        when(llmService.analyze(any(), any())).thenReturn(llmResponse);

        List<String> result = normalizer.normalize(input);

        assertEquals(2, result.size());
        assertTrue(result.contains("java"));
        assertTrue(result.contains("spring boot"));

        verify(llmService, times(1)).analyze(any(), any());
    }

    //  LLM returns empty → fallback to cleaned list
    @Test
    void shouldFallbackWhenLLMReturnsEmptySkills() {

        List<String> input = List.of(" Java ", "spring");

        AIResponse llmResponse = AIResponse.builder()
                .skills(List.of()) // empty
                .build();

        when(llmService.analyze(any(), any())).thenReturn(llmResponse);

        List<String> result = normalizer.normalize(input);

        assertEquals(List.of("java", "spring"), result);
    }

    //  LLM returns null → fallback
    @Test
    void shouldFallbackWhenLLMReturnsNullSkills() {

        List<String> input = List.of(" Java ", "spring");

        AIResponse llmResponse = AIResponse.builder()
                .skills(null)
                .build();

        when(llmService.analyze(any(), any())).thenReturn(llmResponse);

        List<String> result = normalizer.normalize(input);

        assertEquals(List.of("java", "spring"), result);
    }

    //  LLM throws exception → fallback
    @Test
    void shouldFallbackWhenLLMThrowsException() {

        List<String> input = List.of(" Java ", "spring");

        when(llmService.analyze(any(), any()))
                .thenThrow(new IllegalStateException("LLM failure"));

        List<String> result = normalizer.normalize(input);

        assertEquals(List.of("java", "spring"), result);

        verify(llmService).analyze(any(), any());
    }

    //  Ensure cleaning logic works (lowercase, trim, sort, remove empty)
    @Test
    void shouldCleanInputBeforeSendingToLLM() {

        List<String> input = List.of("  Java  ", " ", "SPRING", "java");

        AIResponse llmResponse = AIResponse.builder()
                .skills(List.of("Java"))
                .build();

        when(llmService.analyze(any(), any())).thenReturn(llmResponse);

        List<String> result = normalizer.normalize(input);

        assertEquals(List.of("java"), result);

        verify(llmService).analyze(any(), any());
    }
}