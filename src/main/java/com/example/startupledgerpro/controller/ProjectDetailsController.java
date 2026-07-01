package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.Project;
import com.example.startupledgerpro.model.Task;
import com.example.startupledgerpro.session.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class ProjectDetailsController {

    @FXML
    private Label projectNameLabel;
    @FXML
    private Label projectCategoryLabel;
    @FXML
    private Label projectBudgetLabel;
    @FXML
    private Label projectStatusLabel;
    @FXML
    private Label projectDescriptionLabel;

    @FXML
    private TableView<Task> tasksTableView;
    @FXML
    private TableColumn<Task, String> colTaskTitle;
    @FXML
    private TableColumn<Task, String> colAssignee;
    @FXML
    private TableColumn<Task, String> colTaskStatus;
    @FXML
    private TableColumn<Task, String> colDueDate;

    private Project currentProject;

    @FXML
    public void initialize() {
        // Wire up your Task board columns safely
        colTaskTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colAssignee.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAssigneeId()));

        // Convert TaskStatus enum safely to a String representation for display
        colTaskStatus.setCellValueFactory(data -> {
            var statusEnum = data.getValue().getStatus();
            return new SimpleStringProperty(statusEnum != null ? statusEnum.name() : "");
        });

        colDueDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDueDate()));
    }

    public void setProjectContext(Project project) {
        if (project == null) {
            System.err.println("Warning: Project context passed down as null.");
            return;
        }

        this.currentProject = project;

        // Populate labels
        projectNameLabel.setText(project.getName());
        projectCategoryLabel
                .setText("Track: " + (project.getCategory() != null ? project.getCategory().name() : "N/A"));
        projectBudgetLabel.setText(String.format("Tk %,.2f", project.getBudget()));

        // Safely convert ProjectStatus enum to String text for the UI label widget
        projectStatusLabel.setText(project.getStatus() != null ? project.getStatus().name() : "PLANNING");

        projectDescriptionLabel.setText(
                project.getDescription() != null && !project.getDescription().isEmpty()
                        ? project.getDescription()
                        : "No specific tracking description assigned to this project.");

        // Load project task records from the DB
        loadProjectTasks();
    }

    private void loadProjectTasks() {
        if (currentProject == null)
            return;

        // Flush any previous leftovers
        tasksTableView.getItems().clear();

        // 🔥 FIX: Live data pipeline now active and uncommented!
        List<Task> tasks = AppFactory.taskService.getTasksByProject(currentProject.getId());
        tasksTableView.setItems(FXCollections.observableArrayList(tasks));

        System.out.println("Successfully pulled and rendered tasks for project ID: " + currentProject.getId());
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/manager/manager-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) projectNameLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("StartupLedger Pro — Manager Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) projectNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("StartupLedger Pro — Login");
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddTask() {
        // 💡 Defensive Null Guard Check
        if (this.currentProject == null) {
            System.err.println("Aborting task creation: currentProject context is null.");
            return;
        }

        try {
            // 1. Load the Task Modal FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/manager/assign-task-modal.fxml"));
            Parent root = loader.load();

            // 2. Pass the current project's ID to the modal controller
            AssignTaskModalController modalController = loader.getController();
            modalController.setProjectContext(currentProject.getId());

            // 3. Create a stage to display it as a pop-up window
            Stage modalStage = new Stage();
            modalStage.setTitle("Assign New Task Ticket");

            // Block interaction with the background window while open
            modalStage.initModality(Modality.WINDOW_MODAL);
            modalStage.initOwner(projectNameLabel.getScene().getWindow());

            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);

            // 4. Show the window and wait for the user to close it
            modalStage.showAndWait();

            // 5. If they saved a new task, refresh the table view dynamically!
            if (modalController.isSaveClicked()) {
                loadProjectTasks();
            }

        } catch (Exception e) {
            System.err.println("Failed to open the Assign Task modal window.");
            e.printStackTrace();
        }
    }
}