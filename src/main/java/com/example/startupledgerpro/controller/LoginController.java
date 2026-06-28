package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Email and password are required.");
            return;
        }

        // Delegate execution to your business logic layer via AppFactory
        var result = AppFactory.authService.login(email, password);

        if (!result.isSuccess()) {
            errorLabel.setText(result.getMessage());
            passwordField.clear();
            return;
        }

        // Authentication passed -> retrieve user from state manager
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            navigateToDashboard(user);
        } else {
            errorLabel.setText("Session error: User data not populated.");
        }
    }

    private void navigateToDashboard(User user) {
        try {
            String fxmlPath = user.getDashboardFxmlPath();

            // Accessing root FXML folders cleanly
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
            stage.setTitle("StartupLedger Pro — " + user.getName());
            stage.centerOnScreen();
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            errorLabel.setText("UI load error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}