package com.pm.lovable_clone.service;

import com.pm.lovable_clone.dto.ChatMessageResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

public interface AiChatService {
    void streamProjectCodeGeneration(UUID projectId, String userPrompt, SseEmitter emitter);
    List<ChatMessageResponse> getProjectChatHistory(UUID projectId);
}
