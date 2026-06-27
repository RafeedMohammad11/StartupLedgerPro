package com.example.startupledgerpro;

import com.example.startupledgerpro.config.DatabaseManager;
import com.example.startupledgerpro.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {

    public static void initialize() {
        // 1. All 5 fundamental schema tables + foreign keys matching your Hard Scope
        String[] schemaTables = {
                """
            CREATE TABLE IF NOT EXISTS users (
                id              TEXT PRIMARY KEY,
                name            TEXT NOT NULL,
                email           TEXT NOT NULL UNIQUE,
                password_hash   TEXT NOT NULL,
                role            TEXT NOT NULL CHECK(role IN ('ADMIN','MANAGER','EMPLOYEE')),
                phone           TEXT,
                is_active       INTEGER NOT NULL DEFAULT 1,
                created_at      TEXT NOT NULL DEFAULT (datetime('now'))
            );
            """,
                """
            CREATE TABLE IF NOT EXISTS projects (
                id          TEXT PRIMARY KEY,
                name        TEXT NOT NULL,
                description TEXT,
                manager_id  TEXT NOT NULL,
                category    TEXT NOT NULL,
                status      TEXT NOT NULL DEFAULT 'PLANNING',
                budget      REAL NOT NULL,
                deadline    TEXT NOT NULL,
                created_at  TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (manager_id) REFERENCES users(id)
            );
            """,
                """
            CREATE TABLE IF NOT EXISTS tasks (
                id          TEXT PRIMARY KEY,
                project_id  TEXT NOT NULL,
                assignee_id TEXT,
                title       TEXT NOT NULL,
                description TEXT,
                status      TEXT NOT NULL DEFAULT 'TODO',
                due_date    TEXT,
                created_at  TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (project_id)  REFERENCES projects(id) ON DELETE CASCADE,
                FOREIGN KEY (assignee_id) REFERENCES users(id)    ON DELETE SET NULL
            );
            """,
                """
            CREATE TABLE IF NOT EXISTS financial_records (
                id          TEXT PRIMARY KEY,
                project_id  TEXT NOT NULL,
                type        TEXT NOT NULL CHECK(type IN ('EXPENSE','REVENUE')),
                amount      REAL NOT NULL,
                description TEXT,
                category    TEXT,
                recorded_by TEXT NOT NULL,
                recorded_at TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (project_id)  REFERENCES projects(id) ON DELETE CASCADE,
                FOREIGN KEY (recorded_by) REFERENCES users(id)
            );
            """,
                """
            CREATE TABLE IF NOT EXISTS notifications (
                id           TEXT PRIMARY KEY,
                recipient_id TEXT NOT NULL,
                title        TEXT NOT NULL,
                message      TEXT NOT NULL,
                is_read      INTEGER NOT NULL DEFAULT 0,
                created_at   TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
            );
            """
        };

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            // Execute table creations
            for (String tableSql : schemaTables) {
                stmt.execute(tableSql);
            }

            // 2. Seed Default System Admin with securely hashed password "Admin@1234"
            String seedAdminSql = """
                INSERT OR IGNORE INTO users (id, name, email, password_hash, role)
                VALUES (?, ?, ?, ?, ?);
            """;

            try (PreparedStatement pStmt = conn.prepareStatement(seedAdminSql)) {
                pStmt.setString(1, "usr-admin-001");
                pStmt.setString(2, "System Admin");
                pStmt.setString(3, "admin@startupledger.com");
                pStmt.setString(4, PasswordUtil.hash("Admin@1234"));
                pStmt.setString(5, "ADMIN");
                pStmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Critical failure initializing application database schema", e);
        }
    }
}