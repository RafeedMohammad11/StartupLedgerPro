package com.example.startupledgerpro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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

            // 2. Load the login gateway screen directly from the resources root
            FXMLLoader fxmlLoader = new FXMLLoader(StartupLedgerApp.class.getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // 3. Configure the Primary Application Window Stage
            stage.setTitle("StartupLedgerPro - Security Gateway");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            System.err.println("❌ CRITICAL: Application failed to boot!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}