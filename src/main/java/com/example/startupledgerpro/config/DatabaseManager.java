package com.example.startupledgerpro.config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private final String dbPath;

    // Private constructor prevents instantiation from outside (Singleton rule)
    private DatabaseManager() {
        String userHome = System.getProperty("user.home");
        this.dbPath = userHome + File.separator + "startupledger.db";
        initializeConnection();
    }

    // Static access point that SchemaInitializer is looking for
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            this.connection.createStatement().execute("PRAGMA foreign_keys = ON;");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SQLite database connection", e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeConnection();
            }
        } catch (SQLException e) {
            initializeConnection();
        }
        return connection;
    }
}