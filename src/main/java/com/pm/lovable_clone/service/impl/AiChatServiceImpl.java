package com.pm.lovable_clone.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.lovable_clone.dto.ChatMessageResponse;
import com.pm.lovable_clone.entity.ChatMessageEntity;
import com.pm.lovable_clone.entity.MessageRole;
import com.pm.lovable_clone.entity.ProjectEntity;
import com.pm.lovable_clone.entity.User;
import com.pm.lovable_clone.exception.BadRequestException;
import com.pm.lovable_clone.exception.ResourceNotFoundException;
import com.pm.lovable_clone.repository.ChatMessageRepository;
import com.pm.lovable_clone.repository.ProjectRepository;
import com.pm.lovable_clone.service.AiChatService;
import com.pm.lovable_clone.service.PromptTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    private final ProjectRepository projectRepository;
    private final ChatMessageRepository chatMessageRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    @Override
    public void streamProjectCodeGeneration(UUID projectId, String userPrompt, SseEmitter emitter) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        User owner = project.getOwner();

        // Free-tier users get a daily generation limit; PRO users are unlimited.
        if ("FREE".equalsIgnoreCase(owner.getSubscriptionTier())) {
            long todayCount = chatMessageRepository.countByProjectOwnerIdAndCreatedAtAfter(
                    owner.getId(), LocalDate.now().atStartOfDay());

            if (todayCount >= 10) {
                throw new BadRequestException("Daily limit of 10 AI generations reached on FREE tier. Please upgrade to PRO!");
            }
        }

        // Save the user's prompt first so the project chat history is persistent.
        ChatMessageEntity userMessage = ChatMessageEntity.builder()
                .project(project)
                .role(MessageRole.USER)
                .content(userPrompt)
                .build();
        chatMessageRepository.save(userMessage);

        // Load previous messages so the AI can continue the same design conversation.
        List<ChatMessageEntity> history = chatMessageRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
        int startIndex = Math.max(0, history.size() - 6);
        List<ChatMessageEntity> recentHistory = history.subList(startIndex, history.size());

        executor.execute(() -> {
            StringBuilder fullAiResponse = new StringBuilder();

            try {
                // Gemini expects a conversation-style payload with prior turns included.
                List<Map<String, Object>> contents = new ArrayList<>();

                for (ChatMessageEntity msg : recentHistory) {
                    contents.add(Map.of(
                            "role", msg.getRole() == MessageRole.USER ? "user" : "model",
                            "parts", List.of(Map.of("text", msg.getContent()))
                    ));
                }

                Map<String, Object> requestPayload = Map.of(
                        "system_instruction", Map.of(
                                "parts", List.of(Map.of("text", PromptTemplates.SYSTEM_PROMPT))
                        ),
                        "contents", contents
                );

                String url = String.format(
                        "https://generativelanguage.googleapis.com/v1beta/models/%s:streamGenerateContent?alt=sse&key=%s",
                        geminiModel, geminiApiKey
                );

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
                                            fullAiResponse.append(chunk);

                                            // Stream each partial response to the browser.
                                            emitter.send(SseEmitter.event().name("chunk").data(chunk));
                                        }
                                    }
                                }
                            }
                            return null;
                        });

                // Save the AI's final response as an assistant message for future context.
                ChatMessageEntity assistantMessage = ChatMessageEntity.builder()
                        .project(project)
                        .role(MessageRole.ASSISTANT)
                        .content(fullAiResponse.toString())
                        .build();
                chatMessageRepository.save(assistantMessage);

                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                log.error("AI generation error: {}", e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("Generation failed: " + e.getMessage()));
                } catch (Exception ignored) {
                    // The emitter may already be closed; ignore it to avoid secondary errors.
                }
                emitter.completeWithError(e);
            }
        });
    }

    @Override
    public List<ChatMessageResponse> getProjectChatHistory(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }

        return chatMessageRepository.findByProjectIdOrderByCreatedAtAsc(projectId).stream()
                .map(msg -> ChatMessageResponse.builder()
                        .id(msg.getId())
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .createdAt(msg.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
