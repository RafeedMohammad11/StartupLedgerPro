package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.Task;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.model.enums.TaskStatus;
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
import javafx.stage.Stage;

import java.util.List;

public class EmployeeDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label emailLabel;
    @FXML private Label assignedCountLabel;
    @FXML private Label inProgressCountLabel;
    @FXML private Label completedCountLabel;

    @FXML private TableView<Task> employeeTasksTableView;
    @FXML private TableColumn<Task, String> colTaskId;
    @FXML private TableColumn<Task, String> colTaskTitle;
    @FXML private TableColumn<Task, String> colProjectId;
    @FXML private TableColumn<Task, String> colTaskStatus;
    @FXML private TableColumn<Task, String> colDueDate;

    private User loggedInUser;

    @FXML
    public void initialize() {
        // Map data tracking column factories cleanly
        colTaskId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colTaskTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colProjectId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProjectId()));

        colTaskStatus.setCellValueFactory(data -> {
            var status = data.getValue().getStatus();
            return new SimpleStringProperty(status != null ? status.name() : "TODO");
        });

        colDueDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDueDate()));

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            setUserContext(currentUser);
        }
    }

    /**
     * Injects authentication session profile from the session login gateway
     */
    public void setUserContext(User user) {
        this.loggedInUser = user;
        welcomeLabel.setText("Welcome back, " + user.getName());
        emailLabel.setText(user.getEmail() + " | Profile Badge ID: " + user.getId());

        loadEmployeeDashboardData();
    }

    private void loadEmployeeDashboardData() {
        if (loggedInUser == null) return;

        employeeTasksTableView.getItems().clear();

        // Pull task tracks from across your repositories
        List<Task> allAssignedTasks = AppFactory.taskService.getTasksByAssignee(loggedInUser.getId());

        // 💡 DIAGNOSTIC DEBUG PRINT:
        System.out.println("=== SYSTEM DEBUG ===");
        System.out.println("Logged in User Badge ID: " + loggedInUser.getId());
        System.out.println("Total Tasks Pulled from SQLite: " + allAssignedTasks.size());
        for (Task t : allAssignedTasks) {
            System.out.println(" -> Task Title: " + t.getTitle() + " | Status: " + t.getStatus() + " | Assignee: " + t.getAssigneeId());
        }
        System.out.println("====================");

        // Dynamic state aggregation calculations
        long pending = allAssignedTasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long active = allAssignedTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long completed = allAssignedTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();

        // Update dashboard numeric indicators
        assignedCountLabel.setText(String.valueOf(allAssignedTasks.size()));
        inProgressCountLabel.setText(String.valueOf(active));
        completedCountLabel.setText(String.valueOf(completed));

        // Mount collection items to the active table layout
        employeeTasksTableView.setItems(FXCollections.observableArrayList(allAssignedTasks));
    }

    @FXML
    private void handleMarkInProgress() {
        Task selectedTask = employeeTasksTableView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            AppFactory.taskService.updateTaskStatus(selectedTask.getId(), TaskStatus.IN_PROGRESS);
            loadEmployeeDashboardData();
        }
    }

    @FXML
    private void handleMarkDone() {
        Task selectedTask = employeeTasksTableView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            AppFactory.taskService.updateTaskStatus(selectedTask.getId(), TaskStatus.DONE);
            loadEmployeeDashboardData();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // 💡 FIX: Use absolute root pathing by prepending a forward slash '/'
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("StartupLedger Pro - Login");
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Logout failed: Could not resolve the login view location.");
            e.printStackTrace();
        }
    }
}
