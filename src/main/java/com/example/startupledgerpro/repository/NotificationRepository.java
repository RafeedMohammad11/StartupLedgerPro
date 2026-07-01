package com.example.startupledgerpro.repository;

import com.example.startupledgerpro.model.Notification;

import java.util.List;

public interface NotificationRepository extends Repository<Notification, String> {
    List<Notification> findByRecipientId(String recipientId);

    List<Notification> findUnreadByRecipientId(String recipientId);

    void markAsRead(String notificationId);
}
