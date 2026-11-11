package com.pm.lovable_clone.security;

import com.pm.lovable_clone.entity.ProjectMemberEntity;
import com.pm.lovable_clone.entity.ProjectRole;
import com.pm.lovable_clone.entity.User;
import com.pm.lovable_clone.repository.ProjectMemberRepository;
import com.pm.lovable_clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("projectSecurity") // Bean name used in SpEL expressions like @projectSecurity.isOwner(#id).
@RequiredArgsConstructor
public class ProjectSecurity {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public boolean isOwner(UUID projectId) {
        return hasRole(projectId, ProjectRole.OWNER);
    }

    public boolean canEdit(UUID projectId) {
        return hasRole(projectId, ProjectRole.OWNER) || hasRole(projectId, ProjectRole.EDITOR);
    }

    public boolean isMember(UUID projectId) {
        String email = getCurrentUserEmail();
        if (email == null) return false;

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        return projectMemberRepository.findByProjectIdAndUserId(projectId, userOpt.get().getId()).isPresent();
    }

    private boolean hasRole(UUID projectId, ProjectRole requiredRole) {
        String email = getCurrentUserEmail();
        if (email == null) return false;

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        Optional<ProjectMemberEntity> memberOpt = projectMemberRepository.findByProjectIdAndUserId(projectId, userOpt.get().getId());
        return memberOpt.isPresent() && memberOpt.get().getRole() == requiredRole;
    }

    private String getCurrentUserEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }
}
