package com.pm.lovable_clone.controller;

import com.pm.lovable_clone.dto.ChatMessageResponse;
import com.pm.lovable_clone.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiChatController {

    private final AiChatService aiChatService;

    // AI generation is allowed only for project members who can edit the app.
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("@projectSecurity.canEdit(#projectId)")
    public SseEmitter streamCodeGeneration(
            @PathVariable UUID projectId,
            @RequestParam String prompt) {

        SseEmitter emitter = new SseEmitter(180_000L);
        aiChatService.streamProjectCodeGeneration(projectId, prompt, emitter);
        return emitter;
    }

    // Chat history is visible to any project member, but generation is still restricted by role.
    @GetMapping("/history")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(@PathVariable UUID projectId) {
        return ResponseEntity.ok(aiChatService.getProjectChatHistory(projectId));
    }
}
