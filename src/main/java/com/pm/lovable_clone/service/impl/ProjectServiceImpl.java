package com.pm.lovable_clone.service.impl;

import com.pm.lovable_clone.dto.CreateProjectRequest;
import com.pm.lovable_clone.dto.ProjectResponse;
import com.pm.lovable_clone.entity.ProjectEntity;
import com.pm.lovable_clone.entity.ProjectMemberEntity;
import com.pm.lovable_clone.entity.ProjectRole;
import com.pm.lovable_clone.entity.User;
import com.pm.lovable_clone.exception.ResourceNotFoundException;
import com.pm.lovable_clone.mapper.ProjectMapper;
import com.pm.lovable_clone.repository.ProjectMemberRepository;
import com.pm.lovable_clone.repository.ProjectRepository;
import com.pm.lovable_clone.repository.UserRepository;
import com.pm.lovable_clone.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMapper projectMapper;

    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {
        // The authenticated user is available inside Spring Security context
        // because the JWT filter sets that before the controller method is executed.
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Load the real user from the database using the email from the JWT.
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // MapStruct converts the API request into an entity, while ignoring fields like owner/member relations.
        ProjectEntity project = projectMapper.toEntity(request);
        project.setOwner(currentUser);

        // Save the project itself.
        ProjectEntity savedProject = projectRepository.save(project);

        // Create the project membership record so the owner is also a member.
        ProjectMemberEntity member = ProjectMemberEntity.builder()
                .project(savedProject)
                .user(currentUser)
                .role(ProjectRole.OWNER)
                .build();

        projectMemberRepository.save(member);

        // Return clean API response DTO instead of exposing JPA entity internals.
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectMapper.toResponseList(projectRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return projectMapper.toResponse(project);
    }

    @Override
    public void deleteProject(UUID id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
    }
}
