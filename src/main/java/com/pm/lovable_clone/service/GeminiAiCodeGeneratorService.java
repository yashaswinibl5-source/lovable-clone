package com.pm.lovable_clone.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Primary // Tells Spring to use this real AI service instead of the Mock
public class GeminiAiCodeGeneratorService implements AiCodeGeneratorService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    @Override
    public void streamAppGeneration(String userPrompt, SseEmitter emitter) {
        executor.execute(() -> {
            try {
                String url = String.format(
                        "https://generativelanguage.googleapis.com/v1beta/models/%s:streamGenerateContent?alt=sse&key=%s",
                        model, apiKey
                );

                // Build Gemini JSON Request payload
                Map<String, Object> requestPayload = Map.of(
                        "system_instruction", Map.of(
                                "parts", List.of(Map.of("text", PromptTemplates.SYSTEM_PROMPT))
                        ),
                        "contents", List.of(
                                Map.of(
                                        "role", "user",
                                        "parts", List.of(Map.of("text", userPrompt))
                                )
                        )
                );

                // Stream the response directly from Google
                restClient.post()
                        .uri(url)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(requestPayload)
                        .exchange((request, response) -> {
                            try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {

                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (line.startsWith("data: ")) {
                                        String jsonData = line.substring(6).trim();
                                        if (jsonData.isEmpty()) continue;

                                        JsonNode root = objectMapper.readTree(jsonData);
                                        JsonNode textNode = root.at("/candidates/0/content/parts/0/text");

                                        if (!textNode.isMissingNode()) {
                                            String chunk = textNode.asText();
                                            emitter.send(SseEmitter.event().name("chunk").data(chunk));
                                        }
                                    }
                                }
                            }
                            return null;
                        });

                // Signal completion to the browser
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();

            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("Error: " + e.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });
    }
}