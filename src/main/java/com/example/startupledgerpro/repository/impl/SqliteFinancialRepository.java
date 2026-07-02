package com.example.startupledgerpro.repository.impl;

import com.example.startupledgerpro.config.DatabaseManager;
import com.example.startupledgerpro.model.ExpenseRecord;
import com.example.startupledgerpro.repository.FinancialRepository;
import com.example.startupledgerpro.util.ErrorMessages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteFinancialRepository implements FinancialRepository {

    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    @Override
    public double sumExpensesByProject(String projectId) {
        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM financial_records
                WHERE project_id = ? AND type = 'EXPENSE'
                """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
    }

    @Override
    public ExpenseRecord saveExpense(ExpenseRecord record) {
        String sql = """
                INSERT INTO financial_records
                    (id, project_id, type, amount, description, category, recorded_by)
                VALUES (?, ?, 'EXPENSE', ?, ?, ?, ?)
                """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, record.getId());
            stmt.setString(2, record.getProjectId());
            stmt.setDouble(3, record.getAmount());
            stmt.setString(4, record.getDescription());
            stmt.setString(5, record.getCategory());
            stmt.setString(6, record.getRecordedBy());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ErrorMessages.DB_ERROR, e);
        }
        return record;
    }
}
