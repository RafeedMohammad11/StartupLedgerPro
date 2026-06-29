package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.Project;
import com.example.startupledgerpro.model.enums.ProjectCategory;
import com.example.startupledgerpro.model.enums.ProjectStatus;
import com.example.startupledgerpro.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.UUID;

public class CreateProjectModalController {

    @FXML private TextField             nameField;
    @FXML private ComboBox<ProjectCategory> categoryComboBox;
    @FXML private TextField             budgetField;
    @FXML private TextField             deadlineField;
    @FXML private TextArea              descriptionArea;
    @FXML private Label                 errorLabel;

    private boolean saveClicked = false;

    @FXML
    public void initialize() {
        // Populate from enum — uses toString() which returns displayName
        categoryComboBox.setItems(
                FXCollections.observableArrayList(ProjectCategory.values())
        );
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    private void handleSave() {
        errorLabel.setText("");

        // Read inputs
        String name        = nameField.getText().trim();
        ProjectCategory cat = categoryComboBox.getValue();
        String budgetText  = budgetField.getText().trim();
        String deadline    = deadlineField.getText().trim();
        String description = descriptionArea.getText().trim();

        // Validate
        if (name.isEmpty()) {
            errorLabel.setText("Project name is required.");
            return;
        }
        if (cat == null) {
            errorLabel.setText("Please select a category.");
            return;
        }
        if (budgetText.isEmpty()) {
            errorLabel.setText("Budget is required.");
            return;
        }
        if (deadline.isEmpty()) {
            errorLabel.setText("Deadline is required (YYYY-MM-DD).");
            return;
        }

        double budget;
        try {
            budget = Double.parseDouble(budgetText);
            if (budget <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errorLabel.setText("Budget must be a positive number.");
            return;
        }

        // Get current logged-in manager's ID
        String managerId = SessionManager.getInstance()
                .getCurrentUser()
                .getId();

        // Build Project using Builder pattern (correct way — no raw strings)
        Project newProject = new Project.Builder()
                .id("prj-" + UUID.randomUUID().toString().substring(0, 8))
                .name(name)
                .description(description)
                .managerId(managerId)
                .category(cat)                   // ProjectCategory enum
                .status(ProjectStatus.PLANNING)  // ProjectStatus enum — always starts as PLANNING
                .budget(budget)
                .deadline(deadline)
                .build();

        // Persist to SQLite
        AppFactory.projectRepository.save(newProject);

        saveClicked = true;
        closeStage();
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