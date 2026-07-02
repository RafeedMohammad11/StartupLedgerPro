package com.example.startupledgerpro.service;

import com.example.startupledgerpro.exception.ValidationException;
import com.example.startupledgerpro.model.Notification;
import com.example.startupledgerpro.repository.NotificationRepository;

import java.util.List;
import java.util.UUID;

public class NotificationService {
    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public Notification createNotification(String recipientId, String title, String message) {
        if (recipientId == null || recipientId.isBlank()) {
            throw new ValidationException("recipient", "Notification recipient is required.");
        }
        String id = "notif-" + UUID.randomUUID().toString().substring(0, 8);
        Notification notification = new Notification(id, recipientId, title, message, false);
        return repository.save(notification);
    }

    public void createIfMissing(String recipientId, String title, String message) {
        if (recipientId == null || recipientId.isBlank()) {
            return;
        }
        boolean alreadyExists = repository.findByRecipientId(recipientId).stream()
                .anyMatch(n -> n.getTitle().equals(title) && n.getMessage().equals(message));
        if (!alreadyExists) {
            createNotification(recipientId, title, message);
        }
    }

    public List<Notification> getNotificationsForUser(String recipientId) {
        return repository.findByRecipientId(recipientId);
    }

    public List<Notification> getUnreadNotificationsForUser(String recipientId) {
        return repository.findUnreadByRecipientId(recipientId);
    }

    public void markAsRead(String notificationId) {
        repository.markAsRead(notificationId);
    }
}
