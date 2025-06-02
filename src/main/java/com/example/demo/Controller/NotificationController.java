package com.example.demo.Controller;// Changed package name

import com.example.demo.Entity.Notification;
import com.example.demo.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:5500"}) // Adjust for your frontend's actual origin
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * GET /api/notifications/member/{memberId}
     * Retrieves all notifications for a specific member.
     * @param memberId The ID of the member.
     * @return A list of Notification objects.
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Notification>> getNotificationsByMemberId(@PathVariable Long memberId) {
        List<Notification> notifications = notificationService.getNotificationsByMemberId(memberId);
        if (notifications.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
        }
        return new ResponseEntity<>(notifications, HttpStatus.OK);
    }

    /**
     * POST /api/notifications/mark-read/{notificationId}
     * Marks a specific notification as read.
     * @param notificationId The ID of the notification to mark as read.
     * @return The updated Notification object.
     */
    @PostMapping("/mark-read/{notificationId}")
    public ResponseEntity<Notification> markNotificationAsRead(@PathVariable Long notificationId) {
        Optional<Notification> updatedNotification = notificationService.markNotificationAsRead(notificationId);
        return updatedNotification.map(notification -> new ResponseEntity<>(notification, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE /api/notifications/{notificationId}
     * Deletes a specific notification.
     * @param notificationId The ID of the notification to delete.
     * @return 204 No Content if successful, 404 Not Found otherwise.
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        if (notificationService.getNotificationById(notificationId).isPresent()) {
            notificationService.deleteNotification(notificationId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
