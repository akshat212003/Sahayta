package com.sahayta.auth.service;

import com.sahayta.auth.entity.Notification;
import com.sahayta.auth.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createNotification(Long userId, String title, String message, String type) {
        Notification notification = new Notification(userId, title, message, type);
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (list.isEmpty()) {
            // Seed a welcome notification so users always see initial system confirmation
            Notification welcome = new Notification(
                userId,
                "Welcome to Sahayta Platform!",
                "Your account is active. You will receive real-time notifications here for your donation requests and platform updates.",
                "SUCCESS"
            );
            notificationRepository.save(welcome);
            list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return list;
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
