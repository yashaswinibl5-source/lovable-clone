package com.pm.lovable_clone.dto;

import com.pm.lovable_clone.entity.MessageRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ChatMessageResponse {
    private UUID id;
    private MessageRole role;
    private String content;
    private LocalDateTime createdAt;
}
