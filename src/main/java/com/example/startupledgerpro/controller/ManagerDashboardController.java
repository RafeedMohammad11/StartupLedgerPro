package com.example.startupledgerpro.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;

public class ManagerDashboardController {

    @FXML private TableView<Object> projectTableView; // Replace Object with your real Project domain model later
    @FXML private TableColumn<Object, String> colProjectName;
    @FXML private TableColumn<Object, String> colEngineer;
    @FXML private TableColumn<Object, String> colTask;
    @FXML private TableColumn<Object, String> colProgress;

    @FXML
    public void initialize() {
        // This runs automatically when JavaFX instantiates the dashboard.
        // We will wire this up to pull projects straight from your ProjectRepository next!
    }

    @FXML
    private void handleCreateProject() {
        System.out.println("Opening Create Project modal popup form card...");
        // Intent: Launch a clean dialogue modal box to create database items
    }

    @FXML
    private void handleLogout() {
        try {
            // Drop session state and fall back cleanly to your login layout frame
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) projectTableView.getScene().getWindow();
            stage.setScene(new Scene(root, 420, 550));
            stage.setTitle("StartupLedger Pro — Security Gateway");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}