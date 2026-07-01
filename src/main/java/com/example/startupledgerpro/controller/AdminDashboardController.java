package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.Project;
import com.example.startupledgerpro.model.Task;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.model.enums.ProjectStatus;
import com.example.startupledgerpro.model.enums.TaskStatus;
import com.example.startupledgerpro.model.enums.UserRole;
import com.example.startupledgerpro.session.SessionManager;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableRow;
import com.example.startupledgerpro.util.ValidationResult;
import com.example.startupledgerpro.util.Validator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AdminDashboardController {

    @FXML
    private Label topWelcomeLabel;
    @FXML
    private Label sidebarNameLabel;
    @FXML
    private Label sidebarEmailLabel;
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label activeProjectsLabel;
    @FXML
    private Label totalBudgetLabel;
    @FXML
    private Label pendingTasksLabel;
    @FXML
    private Label statusLabel;

    @FXML
    private TableView<User> usersTableView;
    @FXML
    private TableColumn<User, String> colUserName;
    @FXML
    private TableColumn<User, String> colUserEmail;
    @FXML
    private TableColumn<User, String> colUserRole;
    @FXML
    private TableColumn<User, String> colUserStatus;
    @FXML
    private TableColumn<User, String> colUserJoinDate;

    @FXML
    private TableView<Project> projectsTableView;
    @FXML
    private TableColumn<Project, String> colProjectName;
    @FXML
    private TableColumn<Project, String> colProjectManager;
    @FXML
    private TableColumn<Project, String> colProjectStatus;
    @FXML
    private TableColumn<Project, String> colProjectBudget;
    @FXML
    private TableColumn<Project, String> colProjectDeadline;

    @FXML
    private TableView<Task> tasksTableView;
    @FXML
    private TableColumn<Task, String> colTaskTitle;
    @FXML
    private TableColumn<Task, String> colTaskProject;
    @FXML
    private TableColumn<Task, String> colTaskAssignee;
    @FXML
    private TableColumn<Task, String> colTaskStatus;
    @FXML
    private TableColumn<Task, String> colTaskDueDate;

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private DatePicker joinDatePicker;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<UserRole> roleComboBox;
    @FXML
    private ComboBox<ProjectStatus> projectStatusComboBox;
    @FXML
    private Label createUserValidationLabel;
    @FXML
    private TabPane adminTabPane;

    @FXML
    public void initialize() {
        configureSessionLabels();
        configureUserTable();
        configureProjectTable();
        configureTaskTable();

        roleComboBox.setItems(FXCollections.observableArrayList(UserRole.values()));
        roleComboBox.getSelectionModel().select(UserRole.EMPLOYEE);
        projectStatusComboBox.setItems(FXCollections.observableArrayList(ProjectStatus.values()));
        projectStatusComboBox.getSelectionModel().select(ProjectStatus.IN_PROGRESS);

        nameField.textProperty().addListener((obs, oldValue, newValue) -> updateCreateUserValidationMessage());
        emailField.textProperty().addListener((obs, oldValue, newValue) -> updateCreateUserValidationMessage());
        phoneField.textProperty().addListener((obs, oldValue, newValue) -> updateCreateUserValidationMessage());
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> updateCreateUserValidationMessage());
        roleComboBox.valueProperty().addListener((obs, oldValue, newValue) -> updateCreateUserValidationMessage());

        createUserValidationLabel.setText("");
        createUserValidationLabel.setStyle("-fx-text-fill: #71717A; -fx-font-size: 11px;");
        joinDatePicker.setValue(java.time.LocalDate.now());

        refreshDashboard();
    }

    private void configureSessionLabels() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        topWelcomeLabel.setText("Welcome back, " + currentUser.getName());
        sidebarNameLabel.setText(currentUser.getName());
        sidebarEmailLabel.setText(currentUser.getEmail());
    }

    private void configureUserTable() {
        colUserName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colUserEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colUserRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().name()));
        colUserStatus.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isActive() ? "ACTIVE" : "INACTIVE"));
        colUserJoinDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJoinDate()));

        usersTableView.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showUserDetails(row.getItem());
                }
            });
            return row;
        });
    }

    private void configureProjectTable() {
        colProjectName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colProjectManager
                .setCellValueFactory(data -> new SimpleStringProperty(resolveUserName(data.getValue().getManagerId())));
        colProjectStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        colProjectBudget
                .setCellValueFactory(data -> new SimpleStringProperty(formatMoney(data.getValue().getBudget())));
        colProjectDeadline.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDeadline()));

        projectsTableView.setRowFactory(tv -> {
            TableRow<Project> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showProjectDetails(row.getItem());
                }
            });
            return row;
        });
    }

    private void configureTaskTable() {
        colTaskTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colTaskProject.setCellValueFactory(
                data -> new SimpleStringProperty(resolveProjectName(data.getValue().getProjectId())));
        colTaskAssignee.setCellValueFactory(
                data -> new SimpleStringProperty(resolveUserName(data.getValue().getAssigneeId())));
        colTaskStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        colTaskDueDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDueDate()));

        tasksTableView.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showTaskDetails(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    private void refreshDashboard() {
        List<User> users = AppFactory.userService.getAllUsers();
        List<Project> projects = AppFactory.projectService.getAllProjects();
        List<Task> tasks = AppFactory.taskService.getAllTasks();

        users.sort(Comparator.comparing(User::getRole).thenComparing(User::getName, String.CASE_INSENSITIVE_ORDER));
        projects.sort(Comparator.comparing(Project::getDeadline, Comparator.nullsLast(String::compareTo)));
        tasks.sort(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(String::compareTo)));

        usersTableView.setItems(FXCollections.observableArrayList(users));
        projectsTableView.setItems(FXCollections.observableArrayList(projects));
        tasksTableView.setItems(FXCollections.observableArrayList(tasks));

        totalUsersLabel.setText(String.valueOf(users.size()));
        activeProjectsLabel.setText(String.valueOf(projects.stream()
                .filter(project -> project.getStatus() == ProjectStatus.IN_PROGRESS)
                .count()));
        totalBudgetLabel.setText(formatMoney(projects.stream().mapToDouble(Project::getBudget).sum()));
        pendingTasksLabel.setText(String.valueOf(tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .count()));
        statusLabel.setText("Dashboard synced with current database records.");
    }

    @FXML
    private void handleCreateUser() {
        ValidationResult validationResult = validateCreateUserForm();
        if (!validationResult.isValid()) {
            showWarning(validationResult.getErrorMessage());
            return;
        }

        String normalizedName = nameField.getText().trim().toUpperCase(Locale.ROOT);
        String normalizedEmail = emailField.getText().trim();
        String normalizedPhone = phoneField.getText().trim();
        String password = passwordField.getText();
        UserRole role = roleComboBox.getValue();
        java.time.LocalDate joinDate = joinDatePicker.getValue();
        String joinDateString = joinDate != null ? joinDate.toString() : java.time.LocalDate.now().toString();

        try {
            AppFactory.userService.createUser(normalizedName, normalizedEmail, password, role, normalizedPhone,
                    joinDateString);
            nameField.clear();
            emailField.clear();
            phoneField.clear();
            joinDatePicker.setValue(java.time.LocalDate.now());
            passwordField.clear();
            roleComboBox.getSelectionModel().select(UserRole.EMPLOYEE);
            createUserValidationLabel.setText("");
            refreshDashboard();
            statusLabel.setText("Created user account for " + normalizedName + ".");
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        }
    }

    @FXML
    private void handleDeactivateUser() {
        User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Select a user row first.");
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(selectedUser.getId())) {
            showWarning("You cannot deactivate the account you are currently using.");
            return;
        }

        AppFactory.userService.deactivateUser(selectedUser.getId());
        refreshDashboard();
        statusLabel.setText("Deactivated " + selectedUser.getName() + ".");
    }

    @FXML
    private void handleShowDashboard() {
        if (adminTabPane != null) {
            adminTabPane.getSelectionModel().select(0);
        }
    }

    @FXML
    private void handleShowUsers() {
        if (adminTabPane != null) {
            adminTabPane.getSelectionModel().select(0);
        }
    }

    @FXML
    private void handleShowProjects() {
        if (adminTabPane != null) {
            adminTabPane.getSelectionModel().select(1);
        }
    }

    @FXML
    private void handleResetPassword() {
        User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Select a user row first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Set a new password for " + selectedUser.getName());
        dialog.setContentText("New password:");

        dialog.showAndWait().ifPresent(newPassword -> {
            if (isBlank(newPassword)) {
                showWarning("Password cannot be empty.");
                return;
            }

            AppFactory.userService.resetPassword(selectedUser.getId(), newPassword);
            statusLabel.setText("Password reset for " + selectedUser.getName() + ".");
        });
    }

    @FXML
    private void handleViewUserDetails() {
        User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Select a user row first.");
            return;
        }
        showUserDetails(selectedUser);
    }

    private void showUserDetails(User user) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Details");
        alert.setHeaderText("Details for " + user.getName());
        String details = String.join("\n",
                "Name: " + user.getName(),
                "Email: " + user.getEmail(),
                "Role: " + user.getRole().name(),
                "Status: " + (user.isActive() ? "ACTIVE" : "INACTIVE"),
                "Phone: " + (user.getPhone() == null || user.getPhone().isBlank() ? "Not provided" : user.getPhone()),
                "Joining Date: " + (user.getJoinDate() == null ? "Unknown" : user.getJoinDate()));
        alert.setContentText(details);
        alert.getDialogPane().setPrefWidth(420);
        alert.showAndWait();
    }

    @FXML
    private void handleUpdateProjectStatus() {
        Project selectedProject = projectsTableView.getSelectionModel().getSelectedItem();
        ProjectStatus selectedStatus = projectStatusComboBox.getValue();
        if (selectedProject == null) {
            showWarning("Select a project row first.");
            return;
        }
        if (selectedStatus == null) {
            showWarning("Choose a project status first.");
            return;
        }

        AppFactory.projectService.updateStatus(selectedProject.getId(), selectedStatus);
        refreshDashboard();
        statusLabel.setText("Updated " + selectedProject.getName() + " to " + selectedStatus.name() + ".");
    }

    @FXML
    private void handleDeleteProject() {
        Project selectedProject = projectsTableView.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            showWarning("Select a project row first.");
            return;
        }

        AppFactory.projectService.deleteProject(selectedProject.getId());
        refreshDashboard();
        statusLabel.setText("Deleted project " + selectedProject.getName() + ".");
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("StartupLedger Pro - Login");
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            showWarning("Could not return to the login screen.");
            e.printStackTrace();
        }
    }

    private String resolveUserName(String userId) {
        if (isBlank(userId)) {
            return "Unassigned";
        }
        return AppFactory.userService.getUserById(userId)
                .map(User::getName)
                .orElse(userId);
    }

    private String resolveProjectName(String projectId) {
        if (isBlank(projectId)) {
            return "Unassigned";
        }
        return AppFactory.projectService.getProjectById(projectId)
                .map(Project::getName)
                .orElse(projectId);
    }

    private void showProjectDetails(Project project) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Project Details");
        alert.setHeaderText("Details for " + project.getName());
        String details = String.join("\n",
                "Name: " + project.getName(),
                "Manager: " + resolveUserName(project.getManagerId()),
                "Category: " + (project.getCategory() == null ? "Unspecified" : project.getCategory().name()),
                "Status: " + project.getStatus().name(),
                "Budget: " + formatMoney(project.getBudget()),
                "Deadline: " + (project.getDeadline() == null ? "Not set" : project.getDeadline()),
                "Description: "
                        + (project.getDescription() == null || project.getDescription().isBlank() ? "No description"
                                : project.getDescription()));
        alert.setContentText(details);
        alert.getDialogPane().setPrefWidth(420);
        alert.showAndWait();
    }

    private void showTaskDetails(Task task) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Task Details");
        alert.setHeaderText("Details for " + task.getTitle());
        String details = String.join("\n",
                "Title: " + task.getTitle(),
                "Project: " + resolveProjectName(task.getProjectId()),
                "Assignee: " + resolveUserName(task.getAssigneeId()),
                "Status: " + task.getStatus().name(),
                "Due Date: " + (task.getDueDate() == null ? "Not set" : task.getDueDate()),
                "Description: " + (task.getDescription() == null || task.getDescription().isBlank() ? "No description"
                        : task.getDescription()));
        alert.setContentText(details);
        alert.getDialogPane().setPrefWidth(420);
        alert.showAndWait();
    }

    private String formatMoney(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(amount);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void updateCreateUserValidationMessage() {
        ValidationResult result = validateCreateUserForm();
        if (result.isValid()) {
            createUserValidationLabel.setText("Ready to create user.");
            createUserValidationLabel.setStyle("-fx-text-fill: #166534; -fx-font-size: 11px;");
        } else {
            createUserValidationLabel.setText(result.getErrorMessage());
            createUserValidationLabel.setStyle("-fx-text-fill: #B42318; -fx-font-size: 11px;");
        }
    }

    private ValidationResult validateCreateUserForm() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        UserRole role = roleComboBox.getValue();

        if (isBlank(name) || isBlank(email) || isBlank(password) || isBlank(phone) || role == null) {
            return ValidationResult.invalid("Name, email, phone, password, and role are required.");
        }

        String normalizedName = name.trim().toUpperCase(Locale.ROOT);
        ValidationResult fullNameResult = Validator.validateAdminFullName(normalizedName);
        if (!fullNameResult.isValid()) {
            return fullNameResult;
        }

        ValidationResult emailResult = Validator.validateAdminEmail(email.trim());
        if (!emailResult.isValid()) {
            return emailResult;
        }

        ValidationResult phoneResult = Validator.validateAdminPhone(phone.trim());
        if (!phoneResult.isValid()) {
            return phoneResult;
        }

        return ValidationResult.valid();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Admin Action");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
