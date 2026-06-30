package com.example.startupledgerpro.repository.impl;

import com.example.startupledgerpro.config.DatabaseManager;
import com.example.startupledgerpro.model.Task;
import com.example.startupledgerpro.model.enums.TaskStatus;
import com.example.startupledgerpro.repository.TaskRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteTaskRepository implements TaskRepository {

    // Add this query method to filter task records by project token
    public List<Task> findByProjectId(String projectId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE project_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // 💡 Using your exact Builder pattern mapping
                    Task task = new Task.Builder()
                            .id(rs.getString("id"))
                            .projectId(rs.getString("project_id"))
                            .title(rs.getString("title"))
                            .description(rs.getString("description"))
                            .assigneeId(rs.getString("assignee_id"))
                            .status(TaskStatus.valueOf(rs.getString("status"))) // Clean Enum mapping!
                            .dueDate(rs.getString("due_date"))
                            .build();
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // Append these methods to your existing methods inside SqliteTaskRepository.java

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();
            System.out.println("Task successfully dropped from storage: " + id);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

// 💡 If your base Repository interface also requires findById/findAll,
// ensure they are declared here so the compiler stays happy:

    @Override
    public java.util.Optional<Task> findById(String id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Task task = new Task.Builder()
                            .id(rs.getString("id"))
                            .projectId(rs.getString("project_id"))
                            .title(rs.getString("title"))
                            .description(rs.getString("description"))
                            .assigneeId(rs.getString("assignee_id"))
                            .status(com.example.startupledgerpro.model.enums.TaskStatus.valueOf(rs.getString("status")))
                            .dueDate(rs.getString("due_date"))
                            .build();
                    return java.util.Optional.of(task);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return java.util.Optional.empty();
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
                    Task task = new Task.Builder()
                            .id(rs.getString("id"))
                            .projectId(rs.getString("project_id"))
                            .title(rs.getString("title"))
                            .description(rs.getString("description"))
                            .assigneeId(rs.getString("assignee_id"))
                            .status(TaskStatus.valueOf(rs.getString("status")))
                            .dueDate(rs.getString("due_date"))
                            .build();
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    @Override
    public List<Task> findAll() {
        // Implement if required by your base Repository layer contract
        return java.util.Collections.emptyList();
    }

    @Override
    public Task save(Task task) {
        String sql = "INSERT OR REPLACE INTO tasks (id, project_id, assignee_id, title, description, status, due_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getId());
            stmt.setString(2, task.getProjectId());
            stmt.setString(3, task.getAssigneeId());
            stmt.setString(4, task.getTitle());
            stmt.setString(5, task.getDescription());
            stmt.setString(6, task.getStatus() != null ? task.getStatus().name() : "TODO");
            stmt.setString(7, task.getDueDate());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return task; // 💡 Return the task object to match the generic Repository contract!
    }

    // Your existing contract overrides (save, findById, delete) remain untouched below...
}