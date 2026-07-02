package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.session.SessionManager;
import com.example.startupledgerpro.util.ExceptionHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private HBox errorBox;
    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        clearError();

        emailField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password are required.");
            return;
        }

        try {
            var result = AppFactory.authService.login(email, password);

            if (!result.isSuccess()) {
                passwordField.clear();
                showError(result.getMessage());
                return;
            }

            User user = SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                navigateToDashboard(user);
            } else {
                showError("Session error: User data not populated.");
            }
        } catch (RuntimeException ex) {
            passwordField.clear();
            showError(ExceptionHandler.resolveMessage(ex));
        }
    }

    private void navigateToDashboard(User user) {
        try {
            String fxmlPath = user.getDashboardFxmlPath();

            // Accessing root FXML folders cleanly
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("StartupLedger Pro — " + user.getName());
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            double width = Math.min(1320, bounds.getWidth() * 0.96);
            double height = Math.min(860, bounds.getHeight() * 0.94);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setMinWidth(1100);
            stage.setMinHeight(700);
            stage.centerOnScreen();
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            showError("Unable to open the dashboard. Please try again.");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (errorBox != null) {
            errorBox.setVisible(true);
            errorBox.setManaged(true);
        }
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
    }

    @FXML
    private void clearError() {
        if (errorBox != null) {
            errorBox.setVisible(false);
            errorBox.setManaged(false);
        }
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }
}