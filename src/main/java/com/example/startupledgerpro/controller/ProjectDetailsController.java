package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.Project;
import com.example.startupledgerpro.model.Task;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.List;

public class ProjectDetailsController {

    @FXML private Label projectNameLabel;
    @FXML private Label projectCategoryLabel;
    @FXML private Label projectBudgetLabel;
    @FXML private Label projectStatusLabel;
    @FXML private Label projectDescriptionLabel;

    @FXML private TableView<Task> tasksTableView;
    @FXML private TableColumn<Task, String> colTaskTitle;
    @FXML private TableColumn<Task, String> colAssignee;
    @FXML private TableColumn<Task, String> colTaskStatus;
    @FXML private TableColumn<Task, String> colDueDate;

    private Project currentProject;

    @FXML
    public void initialize() {
        // Wire up your Task board columns safely
        colTaskTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colAssignee.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAssigneeId()));

        // 💡 FIX: Convert TaskStatus enum safely to a String representation for display
        colTaskStatus.setCellValueFactory(data -> {
            var statusEnum = data.getValue().getStatus();
            return new SimpleStringProperty(statusEnum != null ? statusEnum.name() : "");
        });

        colDueDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDueDate()));
    }

    public void setProjectContext(Project project) {
        this.currentProject = project;

        // Populate labels
        projectNameLabel.setText(project.getName());
        projectCategoryLabel.setText("Track: " + project.getCategory());
        projectBudgetLabel.setText(String.format("Tk %,.2f", project.getBudget()));

        // 💡 FIX: Safely convert ProjectStatus enum to String text for the UI label widget
        projectStatusLabel.setText(project.getStatus() != null ? project.getStatus().name() : "PLANNING");

        projectDescriptionLabel.setText(
                project.getDescription() != null && !project.getDescription().isEmpty()
                        ? project.getDescription()
                        : "No specific tracking description assigned to this project."
        );

        loadProjectTasks();
    }
    private void loadProjectTasks() {
        // Fetch tasks via service layout matching currentProject.getId()
        // List<Task> tasks = AppFactory.taskService.getTasksByProject(currentProject.getId());
        // tasksTableView.setItems(FXCollections.observableArrayList(tasks));

        System.out.println("Querying tasks for project identification token: " + currentProject.getId());
    }

    @FXML
    private void handleAddTask() {
        System.out.println("Opening add-task modal dialog sequence...");
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/manager/manager-dashboard.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) projectNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("StartupLedger Pro — Manager Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}