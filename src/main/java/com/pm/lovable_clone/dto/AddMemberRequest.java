package com.pm.lovable_clone.dto;

import com.pm.lovable_clone.entity.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMemberRequest {

    @NotBlank(message = "User email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotNull(message = "Project role is required")
    private ProjectRole role;
}
