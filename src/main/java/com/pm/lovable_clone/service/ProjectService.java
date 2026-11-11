package com.pm.lovable_clone.service;

import com.pm.lovable_clone.dto.CreateProjectRequest;
import com.pm.lovable_clone.dto.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    ProjectResponse createProject(CreateProjectRequest request);
    List<ProjectResponse> getAllProjects();
    ProjectResponse getProjectById(UUID id);
    void deleteProject(UUID id);
}
