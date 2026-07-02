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
import com.example.startupledgerpro.util.CurrencyUtil;
import com.example.startupledgerpro.util.ExceptionHandler;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    private Label sidebarAvatarLabel;
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label activeProjectsLabel;
    @FXML
    private Label totalBudgetLabel;
    @FXML
    private Label remainingBalanceLabel;
    @FXML
    private Label financialHealthLabel;
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
    private TableColumn<Project, String> colProjectRemainingBalance;
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
        sidebarAvatarLabel.setText(getInitials(currentUser.getName()));
    }

    private String getInitials(String name) {
        if (isBlank(name)) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.ROOT);
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase(Locale.ROOT);
    }

    private void configureUserTable() {
        colUserName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colUserEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colUserRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().name()));
        colUserStatus.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isActive() ? "ACTIVE" : "INACTIVE"));
        colUserJoinDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJoinDate()));

        colUserRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(role);
                badge.getStyleClass().add(switch (role) {
                    case "ADMIN" -> "badge-admin";
                    case "MANAGER" -> "badge-manager";
                    default -> "badge-employee";
                });
                setGraphic(badge);
                setText(null);
            }
        });

        colUserStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().add("ACTIVE".equals(status) ? "badge-active" : "badge-inactive");
                setGraphic(badge);
                setText(null);
            }
        });

        usersTableView.setPlaceholder(new Label("No users found. Create one using the form on the right."));

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
                .setCellValueFactory(data -> new SimpleStringProperty(CurrencyUtil.formatBdt(data.getValue().getBudget())));
        colProjectRemainingBalance.setCellValueFactory(data -> {
            double remaining = AppFactory.financialService.getRemainingBalance(data.getValue().getId());
            return new SimpleStringProperty(CurrencyUtil.formatBdt(remaining));
        });
        colProjectDeadline.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDeadline()));

        colProjectStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(status.replace('_', ' '));
                badge.getStyleClass().add("badge-status");
                setGraphic(badge);
                setText(null);
            }
        });

        projectsTableView.setRowFactory(tv -> {
            TableRow<Project> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showProjectDetails(row.getItem());
                }
            });
            return row;
        });
        projectsTableView.setPlaceholder(new Label("No projects in the system yet."));
    }

    private void configureTaskTable() {
        colTaskTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colTaskProject.setCellValueFactory(
                data -> new SimpleStringProperty(resolveProjectName(data.getValue().getProjectId())));
        colTaskAssignee.setCellValueFactory(
                data -> new SimpleStringProperty(resolveUserName(data.getValue().getAssigneeId())));
        colTaskStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        colTaskDueDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDueDate()));

        colTaskStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(status.replace('_', ' '));
                badge.getStyleClass().add(switch (status) {
                    case "DONE" -> "badge-active";
                    case "IN_PROGRESS" -> "badge-manager";
                    default -> "badge-status";
                });
                setGraphic(badge);
                setText(null);
            }
        });

        tasksTableView.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showTaskDetails(row.getItem());
                }
            });
            return row;
        });
        tasksTableView.setPlaceholder(new Label("No tasks assigned across projects."));
    }

    @FXML
    private void refreshDashboard() {
        try {
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
            totalBudgetLabel.setText(CurrencyUtil.formatBdt(projects.stream().mapToDouble(Project::getBudget).sum()));
            double remainingBalance = AppFactory.financialService.getRemainingBalanceForProjects(projects);
            remainingBalanceLabel.setText(CurrencyUtil.formatBdt(remainingBalance));
            financialHealthLabel.setText(remainingBalance >= 0 ? "Healthy balance" : "Over budget");
            pendingTasksLabel.setText(String.valueOf(tasks.stream()
                    .filter(task -> task.getStatus() != TaskStatus.DONE)
                    .count()));
            statusLabel.setText("Dashboard synced with current database records.");
        } catch (RuntimeException ex) {
            statusLabel.setText("Could not refresh dashboard.");
            showWarning(ExceptionHandler.resolveMessage(ex));
        }
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
        } catch (RuntimeException ex) {
            showWarning(ExceptionHandler.resolveMessage(ex));
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

        try {
            AppFactory.userService.deactivateUser(selectedUser.getId());
            refreshDashboard();
            statusLabel.setText("Deactivated " + selectedUser.getName() + ".");
        } catch (RuntimeException ex) {
            showWarning(ExceptionHandler.resolveMessage(ex));
        }
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

            try {
                AppFactory.userService.resetPassword(selectedUser.getId(), newPassword);
                statusLabel.setText("Password reset for " + selectedUser.getName() + ".");
            } catch (RuntimeException ex) {
                showWarning(ExceptionHandler.resolveMessage(ex));
            }
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/user-details-modal.fxml"));
            Parent root = loader.load();
            UserDetailsModalController controller = loader.getController();
            controller.setUser(user);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(usersTableView.getScene().getWindow());
            modal.setTitle("User Profile — " + user.getName());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            modal.setScene(scene);
            modal.setResizable(false);
            modal.showAndWait();

            if (controller.wasSaved()) {
                refreshDashboard();
                statusLabel.setText("Updated profile for " + user.getName() + ".");
            }
        } catch (Exception ex) {
            ExceptionHandler.handle("User Details", ex);
        }
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

        try {
            AppFactory.projectService.updateStatus(selectedProject.getId(), selectedStatus);
            refreshDashboard();
            statusLabel.setText("Updated " + selectedProject.getName() + " to " + selectedStatus.name() + ".");
        } catch (RuntimeException ex) {
            showWarning(ExceptionHandler.resolveMessage(ex));
        }
    }

    @FXML
    private void handleDeleteProject() {
        Project selectedProject = projectsTableView.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            showWarning("Select a project row first.");
            return;
        }

        try {
            AppFactory.projectService.deleteProject(selectedProject.getId());
            refreshDashboard();
            statusLabel.setText("Deleted project " + selectedProject.getName() + ".");
        } catch (RuntimeException ex) {
            showWarning(ExceptionHandler.resolveMessage(ex));
        }
    }

    @FXML
    private void handleRecordExpense() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shared/record-expense-modal.fxml"));
            Parent root = loader.load();
            RecordExpenseModalController controller = loader.getController();
            controller.setProjects(AppFactory.projectService.getAllProjects());

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(projectsTableView.getScene().getWindow());
            modal.setTitle("Record Expense");
            modal.setScene(new Scene(root));
            modal.setResizable(false);
            modal.showAndWait();

            if (controller.wasSaved()) {
                refreshDashboard();
                statusLabel.setText("Expense recorded and financial health updated.");
            }
        } catch (Exception ex) {
            ExceptionHandler.handle("Record Expense", ex);
        }
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/project-details-modal.fxml"));
            Parent root = loader.load();
            ProjectDetailsModalController controller = loader.getController();
            controller.setProject(project, resolveUserName(project.getManagerId()));

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(projectsTableView.getScene().getWindow());
            modal.setTitle("Project Details — " + project.getName());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            modal.setScene(scene);
            modal.setResizable(false);
            modal.showAndWait();
        } catch (Exception ex) {
            ExceptionHandler.handle("Project Details", ex);
        }
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
        ValidationResult fullNameResult = Validator.validateName(normalizedName);
        if (!fullNameResult.isValid()) {
            return fullNameResult;
        }

        ValidationResult emailResult = Validator.validateEmail(email.trim());
        if (!emailResult.isValid()) {
            return emailResult;
        }

        ValidationResult phoneResult = Validator.validatePhone(phone.trim());
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
