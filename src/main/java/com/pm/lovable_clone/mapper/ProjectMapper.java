package com.pm.lovable_clone.mapper;

import com.pm.lovable_clone.dto.CreateProjectRequest;
import com.pm.lovable_clone.dto.ProjectResponse;
import com.pm.lovable_clone.entity.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring") //componentModel = "spring" allows Spring to manage the mapper as a bean, enabling dependency injection and integration with Spring's ecosystem.
public interface ProjectMapper {
    // Maps ProjectEntity -> ProjectResponse DTO
    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "owner.name", target = "ownerName")
    ProjectResponse toResponse(ProjectEntity entity);
    // Automatically maps a List of Entities -> List of DTOs
    List<ProjectResponse> toResponseList(List<ProjectEntity> entities);
    // Maps CreateProjectRequest DTO -> ProjectEntity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "members", ignore = true)
    ProjectEntity toEntity(CreateProjectRequest request);
}

//Spring will automatically inject this mapper anywhere we need it using @Autowired or constructor injection!
