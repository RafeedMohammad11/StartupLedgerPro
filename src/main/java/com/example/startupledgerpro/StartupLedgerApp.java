package com.example.startupledgerpro;

import javafx.application.Application;
import javafx.stage.Stage;

public class StartupLedgerApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // 1. Fire up the database table initialization schema
            SchemaInitializer.initialize();
            System.out.println("=================================================");
            System.out.println("✅ SYSTEM: SQLite Schema Initialized successfully!");
            System.out.println("✅ SYSTEM: Default Admin account verified/created.");
            System.out.println("=================================================");
        } catch (Exception e) {
            System.err.println("❌ CRITICAL: Database schema initialization failed!");
            e.printStackTrace();
        }

        // 2. Temporary clean exit point for Day 1 infrastructure verification
        System.out.println("🚀 Day 1 Core Ready. Exiting cleanly to proceed with building Models.");
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}