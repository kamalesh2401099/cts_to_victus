package com.example.demo.Service;

import com.example.demo.Entity.Notification;
import com.example.demo.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // ADD THIS IMPORT
import java.time.LocalDate; // ADD THIS IMPORT
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public Notification createNotification(Long memberId, String message, String type, Long relatedEntityId) {
        Notification notification = new Notification(memberId, message, type, relatedEntityId);
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByMemberId(Long memberId) {
        return notificationRepository.findByMemberIdOrderByDateSentDesc(memberId);
    }

    public List<Notification> getUnreadNotificationsByMemberId(Long memberId) {
        return notificationRepository.findByMemberIdAndIsReadFalseOrderByDateSentDesc(memberId);
    }

    // ADD THIS METHOD (was called from controller)
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    @Transactional
    public Optional<Notification> markNotificationAsRead(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .map(notification -> {
                    notification.setIsRead(true);
                    return notificationRepository.save(notification);
                });
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    // --- Specific Notification Creation Methods (called by other services) ---

    public void createOverdueBookNotification(Long memberId, String bookTitle, Long bookId) {
        String message = String.format("The book \"%s\" is overdue. Please return it immediately to avoid fines.", bookTitle);
        createNotification(memberId, message, "OVERDUE", bookId);
        System.out.println("Notification (OVERDUE) created for Member " + memberId + " for Book " + bookId);
    }

    public void createDueDateReminderNotification(Long memberId, String bookTitle, Long transactionId, LocalDate dueDate) {
        String message = String.format("Reminder: The book \"%s\" is due on %s. Please return it soon.",
                bookTitle, dueDate.toString());
        createNotification(memberId, message, "DUE_DATE_REMINDER", transactionId);
        System.out.println("Notification (DUE_DATE_REMINDER) created for Member " + memberId + " for Transaction " + transactionId);
    }

    public void createFineNotification(Long memberId, Long fineId, BigDecimal amount, String bookTitle) {
        String message = String.format("You have a pending fine of ₹%.2f for the late return of \"%s\". Fine ID: %d. Please pay it.",
                amount.doubleValue(), bookTitle, fineId);
        createNotification(memberId, message, "FINE", fineId);
        System.out.println("Notification (FINE) created for Member " + memberId + " for Fine " + fineId);
    }

    /**
     * Deletes a fine notification after it's paid.
     * @param fineId The ID of the fine that was paid.
     */
    @Transactional
    public void deleteFineNotification(Long fineId) {
        notificationRepository.findByRelatedEntityIdAndType(fineId, "FINE")
                .ifPresent(notification -> {
                    notificationRepository.delete(notification);
                    System.out.println("Fine notification for Fine ID " + fineId + " deleted.");
                });
    }
}
