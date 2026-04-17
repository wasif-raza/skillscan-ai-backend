package com.skillscan.ai.services.impl;

import com.skillscan.ai.client.OpenAIClient;
import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.metrics.SkillScanAIMetrics;
import com.skillscan.ai.services.LLMService;
import com.skillscan.ai.util.CacheKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMServiceImpl implements LLMService {

    private final OpenAIClient client;
    private final SkillScanAIMetrics metrics;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Override
    public AIResponse analyze(String resumeText, String jd) {

        String key = CacheKeyUtil.hashKey(resumeText, jd);

        //  Check Cache
        AIResponse cached = (AIResponse) redisTemplate.opsForValue().get(key);

        if (cached != null) {
            log.info("Cache HIT | key={}", key);
            metrics.recordCacheHit();
            return cached;
        }

        log.info("Cache MISS | key={}", key);
        metrics.recordCacheMiss();

        try {
            //  Call LLM with timing
            AIResponse response = metrics.timeLlm(() -> {
                metrics.recordLlmCall();
                return client.callAI(buildPrompt(resumeText, jd));
            });

            //  Validate response
            if (response == null) {
                metrics.recordError("llm_null_response");
                return fallback();
            }

            // Store in Redis (only valid response)
            if (response.getLlmScore() > 0) {
                redisTemplate.opsForValue().set(key, response, CACHE_TTL);
            }

            return response;

        } catch (Exception e) {
            log.error("LLMService failed", e);
            metrics.recordError("llm_exception");
            return fallback();
        }
    }

    //  Prompt Builder
    private String buildPrompt(String resumeText, String jd) {
        return """
        You are an AI resume analyzer.

        TASK:
        Analyze the resume and (if provided) job description.

        Return ONLY valid JSON (no extra text):

        {
          "score": number (0-100),
          "skills": [],
          "suggestions": []
        }

        RESUME:
        %s

        JOB DESCRIPTION:
        %s
        """.formatted(
                resumeText == null ? "" : resumeText,
                jd == null ? "" : jd
        );
    }

    //  Fallback
    private AIResponse fallback() {
        return AIResponse.builder()
                .llmScore(0)
                .skills(null)
                .suggestions(List.of("AI analysis failed"))
                .build();
    }
}