package com.example.startupledgerpro.config;

import com.example.startupledgerpro.exception.DatabaseConnectionException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
            try (Statement statement = this.connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON;");
            }
        } catch (ClassNotFoundException e) {
            throw new DatabaseConnectionException("SQLite JDBC driver was not found on the classpath.", e);
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Failed to initialize SQLite database connection.", e);
        }
    }

    public Connection getConnection() {
        Connection activeConnection = null;
        try {
            if (connection == null || connection.isClosed()) {
                initializeConnection();
            }
            activeConnection = connection;
            return activeConnection;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Unable to access the SQLite connection.", e);
        }
    }
}