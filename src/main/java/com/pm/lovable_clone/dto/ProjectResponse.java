package com.pm.lovable_clone.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder  //helps create obj instances in a readable way better than calling a long constructor in controller
public class ProjectResponse {
    private UUID id;
    private String title;
    private String description;
    private String template;
    private UUID ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
