package com.pm.lovable_clone.controller;

import com.pm.lovable_clone.dto.AddMemberRequest;
import com.pm.lovable_clone.dto.MemberResponse;
import com.pm.lovable_clone.entity.ProjectRole;
import com.pm.lovable_clone.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("@projectSecurity.isOwner(#projectId)") // All member-management endpoints require owner-level access.
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @PostMapping
    public ResponseEntity<MemberResponse> addMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectMemberService.addMember(projectId, request));
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getMembers(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<MemberResponse> updateRole(
            @PathVariable UUID projectId,
            @PathVariable UUID memberId,
            @RequestParam ProjectRole role) {
        return ResponseEntity.ok(projectMemberService.updateMemberRole(projectId, memberId, role));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID memberId) {
        projectMemberService.removeMember(projectId, memberId);
        return ResponseEntity.noContent().build();
    }
}
