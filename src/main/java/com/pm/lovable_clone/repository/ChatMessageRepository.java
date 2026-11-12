package com.pm.lovable_clone.repository;

import com.pm.lovable_clone.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

    // Fetch the full conversation for a project in chronological order.
    List<ChatMessageEntity> findByProjectIdOrderByCreatedAtAsc(UUID projectId);

    // Count how many AI generations this project owner has used today for free-tier quota checks.
    long countByProjectOwnerIdAndCreatedAtAfter(UUID ownerId, LocalDateTime after);
}
