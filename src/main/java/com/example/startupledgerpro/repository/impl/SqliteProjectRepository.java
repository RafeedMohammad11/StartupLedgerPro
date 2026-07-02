package com.example.startupledgerpro.repository.impl;

import com.example.startupledgerpro.config.DatabaseManager;
import com.example.startupledgerpro.model.Project;
import com.example.startupledgerpro.model.enums.ProjectCategory;
import com.example.startupledgerpro.model.enums.ProjectStatus;
import com.example.startupledgerpro.repository.ProjectRepository;
import com.example.startupledgerpro.util.ErrorMessages;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteProjectRepository implements ProjectRepository {

    // ── MAP ROW → Project (uses Builder — correct way) ────────────
    private Project mapRow(ResultSet rs) throws SQLException {
        return new Project.Builder()
                .id(rs.getString("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .managerId(rs.getString("manager_id"))
                .category(ProjectCategory.valueOf(rs.getString("category")))
                .status(ProjectStatus.valueOf(rs.getString("status")))
                .budget(rs.getDouble("budget"))
                .deadline(rs.getString("deadline"))
                .build();
    }

    // ── FIND ALL ───────────────────────────────────────────────────
    @Override
    public List<Project> findAll() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                projects.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return projects;
    }

    // ── FIND BY ID ─────────────────────────────────────────────────
    @Override
    public Optional<Project> findById(String id) {
        String sql = "SELECT * FROM projects WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return Optional.empty();
    }

    // ── SAVE (INSERT OR UPDATE) ────────────────────────────────────
    @Override
    public Project save(Project project) {
        String sql = """
            INSERT INTO projects (id, name, description, manager_id, category,
                                  status, budget, deadline)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                name        = excluded.name,
                description = excluded.description,
                category    = excluded.category,
                status      = excluded.status,
                budget      = excluded.budget,
                deadline    = excluded.deadline
        """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, project.getId());
            stmt.setString(2, project.getName());
            stmt.setString(3, project.getDescription());
            stmt.setString(4, project.getManagerId());
            stmt.setString(5, project.getCategory().name());
            stmt.setString(6, project.getStatus().name());
            stmt.setDouble(7, project.getBudget());
            stmt.setString(8, project.getDeadline());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return project;
    }

    // ── DELETE ─────────────────────────────────────────────────────
    @Override
    public void delete(String id) {
        String sql = "DELETE FROM projects WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
    }

    // ── FILTER QUERIES ─────────────────────────────────────────────
    @Override
    public List<Project> findByManagerId(String managerId) {
        return queryList(
                "SELECT * FROM projects WHERE manager_id = ?", managerId
        );
    }

    @Override
    public List<Project> findByStatus(ProjectStatus status) {
        return queryList(
                "SELECT * FROM projects WHERE status = ?", status.name()
        );
    }

    @Override
    public List<Project> findByCategory(ProjectCategory category) {
        return queryList(
                "SELECT * FROM projects WHERE category = ?", category.name()
        );
    }

    // ── SHARED QUERY HELPER ────────────────────────────────────────
    private List<Project> queryList(String sql, String param) {
        List<Project> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, param);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) results.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return results;
    }
}