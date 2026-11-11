package com.pm.lovable_clone.dto;

import com.pm.lovable_clone.entity.ProjectRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MemberResponse {
    private UUID id;
    private UUID userId;
    private String name;
    private String email;
    private ProjectRole role;
    private LocalDateTime joinedAt;
}
