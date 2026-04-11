package com.skillscan.ai.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillscan.ai.dto.response.AIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAIClient {

    private final RestTemplate restTemplate;

    @Value("${ollama.model:llama3}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true);

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    public AIResponse callAI(String prompt) {

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {

                HttpEntity<Map<String, Object>> entity = buildRequestEntity(prompt);

                ResponseEntity<Map> response = restTemplate.exchange(
                        OLLAMA_URL,
                        HttpMethod.POST,
                        entity,
                        Map.class
                );

                String responseText = extractResponse(response);

                log.debug("LLM preview={}",
                        responseText.substring(0, Math.min(200, responseText.length())));

                String cleanJson = extractJson(sanitizeJson(responseText));

                return parseSafe(cleanJson);

            } catch (ResourceAccessException e) {
                log.warn("LLM timeout on attempt {}", attempt);
                if (attempt == 2) break;
                try{
                    Thread.sleep(2000);
                }catch (InterruptedException ie){
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                log.error("LLM processing failed", e);
                break;
            }
        }

        return fallback("LLM unavailable");
    }

    //  Build request
    private HttpEntity<Map<String, Object>> buildRequestEntity(String prompt) {

        Map<String, Object> body = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false,
                "options", Map.of("temperature", 0.1)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(body, headers);
    }

    //  Extract response
    private String extractResponse(ResponseEntity<Map> response) {

        if (response.getBody() == null) {
            throw new IllegalStateException("Empty response from LLM");
        }

        Object res = response.getBody().get("response");

        if (res == null) {
            throw new IllegalStateException("Missing 'response' field");
        }

        return res.toString();
    }

    //  Clean text
    private String sanitizeJson(String text) {
        return text
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("’", "'")
                .trim();
    }

    //  Extract JSON safely
    private String extractJson(String text) {
        try {
            JsonNode node = objectMapper.readTree(text);
            return node.toString();
        } catch (Exception e) {
            int start = text.indexOf("{");
            int end = text.lastIndexOf("}");
            if (start == -1 || end == -1 || start > end) {
                throw new IllegalStateException("No valid JSON found");
            }
            return text.substring(start, end + 1);
        }
    }

    //  Parse response safely
    private AIResponse parseSafe(String json) {

        try {
            JsonNode node = objectMapper.readTree(json);

            return AIResponse.builder()
                    .llmScore(node.path("score").asDouble(0))
                    .skills(parseList(node.get("skills")))
                    .suggestions(parseList(node.get("suggestions")))
                    .build();

        } catch (Exception e) {
            log.error("JSON parsing failed", e);
            return fallback("Invalid AI response format");
        }
    }

    //  Robust list parsing
    private List<String> parseList(JsonNode node) {

        if (node == null || node.isNull()) {
            return Collections.emptyList();
        }

        if (node.isArray()) {
            List<String> list = new ArrayList<>();
            node.forEach(n -> list.add(n.asText().toLowerCase().trim()));
            return list;
        }

        if (node.isTextual()) {
            return Arrays.stream(node.asText().split(","))
                    .map(String::toLowerCase)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        return Collections.emptyList();
    }

    //  Fallback
    private AIResponse fallback(String message) {

        return AIResponse.builder()
                .llmScore(0)
                .skills(Collections.emptyList())
                .suggestions(List.of(message))
                .build();
    }
}