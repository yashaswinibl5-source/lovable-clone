package com.pm.lovable_clone.mapper;

import com.pm.lovable_clone.dto.MemberResponse;
import com.pm.lovable_clone.entity.ProjectMemberEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "name")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "createdAt", target = "joinedAt")
    MemberResponse toResponse(ProjectMemberEntity entity);

    List<MemberResponse> toResponseList(List<ProjectMemberEntity> entities);
}
