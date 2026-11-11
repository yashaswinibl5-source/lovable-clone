package com.pm.lovable_clone.service;

import com.pm.lovable_clone.dto.AddMemberRequest;
import com.pm.lovable_clone.dto.MemberResponse;
import com.pm.lovable_clone.entity.ProjectRole;

import java.util.List;
import java.util.UUID;

public interface ProjectMemberService {
    MemberResponse addMember(UUID projectId, AddMemberRequest request);
    List<MemberResponse> getProjectMembers(UUID projectId);
    MemberResponse updateMemberRole(UUID projectId, UUID memberId, ProjectRole newRole);
    void removeMember(UUID projectId, UUID memberId);
}
