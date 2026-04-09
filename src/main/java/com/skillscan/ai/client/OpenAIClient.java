package com.skillscan.ai.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillscan.ai.dto.response.AIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAIClient {

    //  Allow comments in JSON
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true);

    private final RestTemplate restTemplate;

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    public AIResponse callAI(String prompt) {

        Map<String, Object> request = new HashMap<>();
        request.put("model", "llama3");
        request.put("prompt", prompt);
        request.put("stream", false);
        request.put("options", Map.of("temperature", 0.1));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                OLLAMA_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        if (response.getBody() == null || response.getBody().get("response") == null) {
            throw new RuntimeException("Invalid response from LLM");
        }

        String responseText = response.getBody().get("response").toString();

        log.debug("Received AI response, length={} chars", responseText.length());

        try {
            String sanitized = sanitizeJson(responseText);
            String cleanJson = extractJson(sanitized);

            log.debug("Sanitized AI JSON, length={} chars", cleanJson.length());

            return parseSafe(cleanJson);

        } catch (Exception e) {

            log.error("Parsing failed for AI response, length={} chars", responseText.length(), e);

            //  fallback
            AIResponse fallback = new AIResponse();
            fallback.setScore(0);
            fallback.getSuggestions().add("Invalid AI response. Please try again.");

            return fallback;
        }
    }

    //  Remove garbage around JSON
    private String sanitizeJson(String text) {

        // remove markdown blocks
        text = text.replaceAll("```json", "")
                .replaceAll("```", "");

        // fix smart quotes
        text = text.replace("“", "\"")
                .replace("”", "\"")
                .replace("’", "'");


        return text.trim();
    }

    //  Extract only JSON part
    private String extractJson(String text) {

        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");

        if (start == -1 || end == -1 || start > end) {
            throw new RuntimeException("No valid JSON found in response");
        }

        return text.substring(start, end + 1);
    }

    //  Safe parsing
    private AIResponse parseSafe(String json) throws Exception {

        JsonNode node = objectMapper.readTree(json);

        AIResponse res = new AIResponse();

        res.setScore(node.path("score").asInt(0));
        res.setSkills(parseList(node.get("skills")));
        res.setKeywords(parseList(node.get("keywords")));
        res.setSuggestions(parseList(node.get("suggestions")));

        return res;
    }

    //  Handles both string and array
    private List<String> parseList(JsonNode node) {

        if (node == null || node.isNull()) {
            return new ArrayList<>();
        }

        if (node.isArray()) {
            return new ArrayList<>(objectMapper.convertValue(node, List.class));
        }

        if (node.isTextual()) {
            return Arrays.stream(node.asText().split(","))
                    .map(String::trim)
                    .toList();
        }

        return new ArrayList<>();
    }
}