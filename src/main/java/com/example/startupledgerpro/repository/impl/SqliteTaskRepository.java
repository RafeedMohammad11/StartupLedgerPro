package com.example.startupledgerpro.repository.impl;

import com.example.startupledgerpro.config.DatabaseManager;
import com.example.startupledgerpro.model.Task;
import com.example.startupledgerpro.model.enums.TaskStatus;
import com.example.startupledgerpro.repository.TaskRepository;
import com.example.startupledgerpro.util.ErrorMessages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteTaskRepository implements TaskRepository {

    private Task mapRow(ResultSet rs) throws SQLException {
        return new Task.Builder()
                .id(rs.getString("id"))
                .projectId(rs.getString("project_id"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .assigneeId(rs.getString("assignee_id"))
                .status(TaskStatus.valueOf(rs.getString("status")))
                .dueDate(rs.getString("due_date"))
                .build();
    }

    @Override
    public List<Task> findByProjectId(String projectId) {
        String sql = "SELECT * FROM tasks WHERE project_id = ?";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return tasks;
    }

    @Override
    public Optional<Task> findById(String id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";

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
    public List<Task> getTasksByAssignee(String assigneeId) {
        String sql = "SELECT * FROM tasks WHERE assignee_id = ?";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, assigneeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return tasks;
    }

    @Override
    public List<Task> findAll() {
        String sql = "SELECT * FROM tasks";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tasks.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return tasks;
    }

    @Override
    public Task save(Task task) {
        String sql = """
                INSERT OR REPLACE INTO tasks
                    (id, project_id, assignee_id, title, description, status, due_date)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getId());
            stmt.setString(2, task.getProjectId());
            stmt.setString(3, task.getAssigneeId());
            stmt.setString(4, task.getTitle());
            stmt.setString(5, task.getDescription());
            stmt.setString(6, task.getStatus() != null ? task.getStatus().name() : TaskStatus.TODO.name());
            stmt.setString(7, task.getDueDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return task;
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
    }
}
