package com.example.demo.Repository; // Changed package name

import com.example.demo.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Find all notifications for a specific member, ordered by date sent (descending)
    List<Notification> findByMemberIdOrderByDateSentDesc(Long memberId); // Changed to Long

    // Find unread notifications for a specific member
    List<Notification> findByMemberIdAndIsReadFalseOrderByDateSentDesc(Long memberId);

    // Find a notification by its related entity ID and type
    Optional<Notification> findByRelatedEntityIdAndType(Long relatedEntityId, String type);
}
