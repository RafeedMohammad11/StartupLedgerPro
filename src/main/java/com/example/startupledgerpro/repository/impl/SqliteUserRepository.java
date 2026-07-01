package com.example.startupledgerpro.repository.impl;

import com.example.startupledgerpro.config.DatabaseManager;
import com.example.startupledgerpro.factory.UserFactory;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.model.enums.UserRole;
import com.example.startupledgerpro.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteUserRepository implements UserRepository {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    // ── SAVE (returns User to match Repository<User, String>) ──────
    @Override
    public User save(User user) {
        String sql = """
                    INSERT INTO users (id, name, email, password_hash, role, phone, is_active, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT(id) DO UPDATE SET
                        name          = excluded.name,
                        email         = excluded.email,
                        password_hash = excluded.password_hash,
                        role          = excluded.role,
                        phone         = excluded.phone,
                        is_active     = excluded.is_active
                """;
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPasswordHash());
            stmt.setString(5, user.getRole().name());
            stmt.setString(6, user.getPhone());
            stmt.setInt(7, user.isActive() ? 1 : 0);
            stmt.setString(8, user.getJoinDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user to database", e);
        }
        return user;
    }

    // ── FIND BY ID ─────────────────────────────────────────────────
    @Override
    public Optional<User> findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying user by ID", e);
        }
        return Optional.empty();
    }

    // ── FIND BY EMAIL ──────────────────────────────────────────────
    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying user by email", e);
        }
        return Optional.empty();
    }

    // ── FIND ALL ───────────────────────────────────────────────────
    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next())
                users.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error querying all users", e);
        }
        return users;
    }

    // ── FIND BY ROLE ───────────────────────────────────────────────
    @Override
    public List<User> findByRole(UserRole role) {
        String sql = "SELECT * FROM users WHERE role = ?";
        List<User> users = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying users by role", e);
        }
        return users;
    }

    // ── DELETE ─────────────────────────────────────────────────────
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    // ── MAP ROW → User (via UserFactory for correct subclass) ──────
    private User mapRow(ResultSet rs) throws SQLException {
        return UserFactory.create(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                UserRole.valueOf(rs.getString("role")),
                rs.getString("phone"),
                rs.getString("created_at"),
                rs.getInt("is_active") == 1);
    }
}