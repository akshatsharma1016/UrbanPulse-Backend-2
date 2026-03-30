package com.UrbanPulse.UrbanPulse_Backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around the Google Generative Language (Gemini) REST API.
 *
 * Model strategy:
 *   1. Try primaryModel  (gemini-3.1-flash-lite)  — fast & cheap
 *   2. On any failure, retry with fallbackModel (gemini-2.5-flash) — more reliable
 */
@Service
public class GeminiService {

    private static final String GEMINI_BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.primary.model}")
    private String primaryModel;

    @Value("${gemini.fallback.model}")
    private String fallbackModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper   = new ObjectMapper();

    /**
     * Sends {@code prompt} to Gemini, returning a clean JSON string.
     * Tries the primary model first; falls back to the secondary on any error.
     * Returns {@code null} only if both attempts fail.
     */
    public String call(String prompt) {
        String result = tryModel(primaryModel, prompt);
        if (result != null) return result;

        System.err.println("[GeminiService] Primary model failed — switching to fallback: " + fallbackModel);
        return tryModel(fallbackModel, prompt);
    }

    // ── private ──────────────────────────────────────────────────────────────

    private String tryModel(String model, String prompt) {
        try {
            String url = String.format(GEMINI_BASE_URL, model, apiKey);

            Map<String, Object> textPart   = Map.of("text", prompt);
            Map<String, Object> content    = Map.of("parts", List.of(textPart));
            Map<String, Object> body       = Map.of("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

            String rawResponse = restTemplate.postForObject(url, req, String.class);

            // Navigate the response tree to the generated text
            JsonNode root = objectMapper.readTree(rawResponse);
            String generatedText = root.path("candidates")
                                       .get(0)
                                       .path("content")
                                       .path("parts")
                                       .get(0)
                                       .path("text")
                                       .asText();

            // Strip any surrounding markdown fences — extract first '{' to last '}'
            int start = generatedText.indexOf('{');
            int end   = generatedText.lastIndexOf('}');

            if (start == -1 || end == -1 || end < start) {
                System.err.println("[GeminiService] No JSON block found. Model: " + model);
                return null;
            }

            String candidate = generatedText.substring(start, end + 1);
            // Re-parse to validate, then re-serialise to canonical form
            JsonNode validated = objectMapper.readTree(candidate);
            return objectMapper.writeValueAsString(validated);

        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            System.err.println("[GeminiService] 429 rate-limited on model: " + model);
            return null;
        } catch (Exception e) {
            System.err.println("[GeminiService] Error on model " + model + ": " + e.getMessage());
            return null;
        }
    }
}
