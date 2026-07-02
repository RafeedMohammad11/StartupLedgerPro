package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.model.enums.ProjectCategory;
import com.example.startupledgerpro.model.enums.UserRole;
import com.example.startupledgerpro.session.SessionManager;
import com.example.startupledgerpro.util.ExceptionHandler;
import com.example.startupledgerpro.util.ValidationResult;
import com.example.startupledgerpro.util.Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateProjectModalController {

    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<ProjectCategory> categoryComboBox;
    @FXML
    private ComboBox<User> managerComboBox;
    @FXML
    private TextField budgetField;
    @FXML
    private TextField deadlineField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label errorLabel;

    private boolean saveClicked = false;

    @FXML
    public void initialize() {
        categoryComboBox.setItems(
                FXCollections.observableArrayList(ProjectCategory.values()));
        managerComboBox.setItems(
                FXCollections.observableArrayList(AppFactory.userService.getUserByRole(UserRole.MANAGER)));

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == UserRole.MANAGER) {
            managerComboBox.getSelectionModel().select(currentUser);
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    private void handleSave() {
        errorLabel.setText("");

        String name = nameField.getText().trim();
        ProjectCategory cat = categoryComboBox.getValue();
        String budgetText = budgetField.getText().trim();
        String deadline = deadlineField.getText().trim();
        String description = descriptionArea.getText().trim();

        ValidationResult nameResult = Validator.validateProjectName(name);
        if (!nameResult.isValid()) {
            errorLabel.setText(nameResult.getErrorMessage());
            return;
        }
        if (cat == null) {
            errorLabel.setText("Please select a category.");
            return;
        }

        ValidationResult budgetResult = Validator.validateBudget(budgetText);
        if (!budgetResult.isValid()) {
            errorLabel.setText(budgetResult.getErrorMessage());
            return;
        }

        ValidationResult deadlineResult = Validator.validateDate(deadline, "Deadline");
        if (!deadlineResult.isValid()) {
            errorLabel.setText(deadlineResult.getErrorMessage());
            return;
        }

        double budget;
        try {
            budget = Double.parseDouble(budgetText);
        } catch (NumberFormatException e) {
            errorLabel.setText(ExceptionHandler.resolveMessage(e));
            return;
        }

        User selectedManager = managerComboBox.getValue();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String managerId = null;
        if (selectedManager != null) {
            managerId = selectedManager.getId();
        } else if (currentUser != null && currentUser.getRole() == UserRole.MANAGER) {
            managerId = currentUser.getId();
        }

        if (managerId == null || managerId.isBlank()) {
            errorLabel.setText("Please assign a project manager.");
            return;
        }

        try {
            AppFactory.projectService.createProject(name, description, managerId, cat, budget, deadline);
            saveClicked = true;
            closeStage();
        } catch (RuntimeException ex) {
            errorLabel.setText(ExceptionHandler.resolveMessage(ex));
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
