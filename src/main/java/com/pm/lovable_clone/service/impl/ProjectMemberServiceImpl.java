package com.pm.lovable_clone.service.impl;

import com.pm.lovable_clone.dto.AddMemberRequest;
import com.pm.lovable_clone.dto.MemberResponse;
import com.pm.lovable_clone.entity.ProjectEntity;
import com.pm.lovable_clone.entity.ProjectMemberEntity;
import com.pm.lovable_clone.entity.ProjectRole;
import com.pm.lovable_clone.entity.User;
import com.pm.lovable_clone.exception.BadRequestException;
import com.pm.lovable_clone.exception.ResourceNotFoundException;
import com.pm.lovable_clone.mapper.MemberMapper;
import com.pm.lovable_clone.repository.ProjectMemberRepository;
import com.pm.lovable_clone.repository.ProjectRepository;
import com.pm.lovable_clone.repository.UserRepository;
import com.pm.lovable_clone.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MemberMapper memberMapper;

    @Override
    public MemberResponse addMember(UUID projectId, AddMemberRequest request) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        User invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(request.getEmail())
                        .name(request.getEmail().split("@")[0])
                        .password("tempPass123")
                        .build()));

        projectMemberRepository.findByProjectIdAndUserId(projectId, invitedUser.getId())
                .ifPresent(m -> {
                    throw new BadRequestException("User is already a member of this project");
                });

        ProjectMemberEntity member = ProjectMemberEntity.builder()
                .project(project)
                .user(invitedUser)
                .role(request.getRole())
                .build();

        ProjectMemberEntity saved = projectMemberRepository.save(member);
        return memberMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> getProjectMembers(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        return memberMapper.toResponseList(projectMemberRepository.findByProjectId(projectId));
    }

    @Override
    public MemberResponse updateMemberRole(UUID projectId, UUID memberId, ProjectRole newRole) {
        ProjectMemberEntity member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        if (!member.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Member does not belong to project: " + projectId);
        }

        member.setRole(newRole);
        return memberMapper.toResponse(member);
    }

    @Override
    public void removeMember(UUID projectId, UUID memberId) {
        ProjectMemberEntity member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        if (!member.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Member does not belong to project: " + projectId);
        }

        if (member.getRole() == ProjectRole.OWNER) {
            throw new BadRequestException("Cannot remove the project OWNER");
        }

        projectMemberRepository.delete(member);
    }
}
