package com.example.startupledgerpro.repository.impl;

import com.example.startupledgerpro.config.DatabaseManager;
import com.example.startupledgerpro.model.Notification;
import com.example.startupledgerpro.repository.NotificationRepository;
import com.example.startupledgerpro.util.ErrorMessages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteNotificationRepository implements NotificationRepository {

    private Notification mapRow(ResultSet rs) throws SQLException {
        return new Notification(
                rs.getString("id"),
                rs.getString("recipient_id"),
                rs.getString("title"),
                rs.getString("message"),
                rs.getInt("is_read") == 1);
    }

    @Override
    public Notification save(Notification notification) {
        String sql = """
                INSERT INTO notifications (id, recipient_id, title, message, is_read)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    recipient_id = excluded.recipient_id,
                    title        = excluded.title,
                    message      = excluded.message,
                    is_read      = excluded.is_read
                """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, notification.getId());
            stmt.setString(2, notification.getRecipientId());
            stmt.setString(3, notification.getTitle());
            stmt.setString(4, notification.getMessage());
            stmt.setInt(5, notification.isRead() ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return notification;
    }

    @Override
    public Optional<Notification> findById(String id) {
        String sql = "SELECT * FROM notifications WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Notification> findAll() {
        String sql = "SELECT * FROM notifications";
        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                notifications.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return notifications;
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
    }

    @Override
    public List<Notification> findByRecipientId(String recipientId) {
        String sql = "SELECT * FROM notifications WHERE recipient_id = ? ORDER BY created_at DESC";
        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return notifications;
    }

    @Override
    public List<Notification> findUnreadByRecipientId(String recipientId) {
        String sql = "SELECT * FROM notifications WHERE recipient_id = ? AND is_read = 0 ORDER BY created_at DESC";
        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return notifications;
    }

    @Override
    public void markAsRead(String notificationId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
    }
}
