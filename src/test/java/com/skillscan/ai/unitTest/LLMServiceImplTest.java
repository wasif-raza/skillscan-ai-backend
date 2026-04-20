package com.skillscan.ai.unitTest;

import com.skillscan.ai.client.OpenAIClient;
import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.metrics.SkillScanAIMetrics;
import com.skillscan.ai.services.impl.LLMServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LLMServiceImplTest {

    @Mock
    private OpenAIClient client;

    @Mock
    private SkillScanAIMetrics metrics;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private LLMServiceImpl llmService;

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    //  CACHE HIT
    @Test
    void shouldReturnCachedResponse() {

        AIResponse cached = AIResponse.builder()
                .llmScore(90)
                .build();

        when(valueOps.get(any())).thenReturn(cached);

        AIResponse result = llmService.analyze("resume", "jd");

        assertEquals(90, result.getLlmScore());

        verify(metrics).recordCacheHit();
        verifyNoInteractions(client);
    }

    //  CACHE MISS → LLM SUCCESS → STORE CACHE
    @Test
    void shouldCallLLMAndCacheResult() {

        when(metrics.timeLlm(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        when(valueOps.get(any())).thenReturn(null);

        AIResponse response = AIResponse.builder()
                .llmScore(80)
                .build();

        when(client.callAI(any())).thenReturn(response);

        AIResponse result = llmService.analyze("resume", "jd");

        assertEquals(80, result.getLlmScore());

        verify(metrics).recordCacheMiss();
        verify(metrics).recordLlmCall();
        verify(client).callAI(any());
        verify(valueOps).set(any(), eq(response), any());
    }

    //  CACHE MISS → LLM SCORE = 0 → DO NOT CACHE
    @Test
    void shouldNotCacheIfScoreZero() {

        when(metrics.timeLlm(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        when(valueOps.get(any())).thenReturn(null);

        AIResponse response = AIResponse.builder()
                .llmScore(0)
                .build();

        when(client.callAI(any())).thenReturn(response);

        llmService.analyze("resume", "jd");

        verify(valueOps, never()).set(any(), any(), any());
    }

    //  REDIS GET FAIL → SHOULD CONTINUE
    @Test
    void shouldHandleRedisGetFailure() {

        when(metrics.timeLlm(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        when(valueOps.get(any())).thenThrow(new IllegalStateException("Redis down"));

        AIResponse response = AIResponse.builder()
                .llmScore(70)
                .build();

        when(client.callAI(any())).thenReturn(response);

        AIResponse result = llmService.analyze("resume", "jd");

        assertEquals(70, result.getLlmScore());

        verify(metrics).recordError("cache_error");
        verify(metrics).recordCacheMiss();
    }

    //  REDIS SET FAIL → SHOULD NOT BREAK
    @Test
    void shouldHandleRedisSetFailure() {

        when(metrics.timeLlm(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        when(valueOps.get(any())).thenReturn(null);

        AIResponse response = AIResponse.builder()
                .llmScore(85)
                .build();

        when(client.callAI(any())).thenReturn(response);

        doThrow(new IllegalStateException("Redis set failed"))
                .when(valueOps).set(any(), any(), any());

        AIResponse result = llmService.analyze("resume", "jd");

        assertEquals(85, result.getLlmScore());

        verify(metrics).recordError("cache_error");
    }

    //  LLM RETURNS NULL → FALLBACK
    @Test
    void shouldFallbackWhenLLMReturnsNull() {

        when(metrics.timeLlm(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        when(valueOps.get(any())).thenReturn(null);
        when(client.callAI(any())).thenReturn(null);

        AIResponse result = llmService.analyze("resume", "jd");

        assertEquals(0, result.getLlmScore());
        assertEquals("AI analysis failed", result.getSuggestions().get(0));

        verify(metrics).recordError("llm_null_response");
    }

    //  LLM THROWS EXCEPTION → FALLBACK
    @Test
    void shouldFallbackWhenLLMThrowsException() {

        when(metrics.timeLlm(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        when(valueOps.get(any())).thenReturn(null);

        when(client.callAI(any()))
                .thenThrow(new IllegalStateException("LLM crash"));

        AIResponse result = llmService.analyze("resume", "jd");

        assertEquals(0, result.getLlmScore());

        verify(metrics).recordError("llm_exception");
    }
}