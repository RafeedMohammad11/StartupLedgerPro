package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.controller.ScrumBoardController;
import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.Project;
import com.example.startupledgerpro.model.Task;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.model.enums.ProjectStatus;
import com.example.startupledgerpro.model.enums.TaskStatus;
import com.example.startupledgerpro.model.enums.UserRole;
import com.example.startupledgerpro.service.TaskService;
import com.example.startupledgerpro.session.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ManagerDashboardController {

    @FXML
    private TableView<Project> projectTableView;
    @FXML
    private TableColumn<Project, String> colProjectName;
    @FXML
    private TableColumn<Project, String> colEngineer;
    @FXML
    private TableColumn<Project, Project> colProgress; // 💡 Upgraded to Project object type for custom node rendering
    @FXML
    private Label welcomeLabel;
    @FXML
    private VBox todoColumn;
    @FXML
    private VBox inProgressColumn;
    @FXML
    private VBox doneColumn;
    @FXML
    private Label scrumStatusLabel;
    @FXML
    private Label scrumProjectLabel;
    @FXML
    private Label todoCountLabel;
    @FXML
    private Label inProgressCountLabel;
    @FXML
    private Label doneCountLabel;
    @FXML
    private Label engineerCountLabel;
    @FXML
    private Label activeProjectCountLabel;
    @FXML
    private Label budgetTotalLabel;
    @FXML
    private Label pendingTasksCountLabel;
    private String selectedProjectId;
    private String selectedProjectName;
    private Stage boardViewStage;
    private static final TaskService taskService = AppFactory.taskService;

    @FXML
    public void initialize() {
        // Show logged-in manager's name
        String name = SessionManager.getInstance().getCurrentUser().getName();
        welcomeLabel.setText("Welcome back, " + name);

        // Wire table columns to Project fields
        colProjectName.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getName()));
        colEngineer.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getManagerId()));

        // ── 🔥 UPGRADE: CONFIGURING CUSTOM PROGRESS BAR GRAPHICS COLUMN ──
        colProgress.setCellValueFactory(
                data -> new SimpleObjectProperty<>(data.getValue()));
        colProgress.setCellFactory(column -> new TableCell<Project, Project>() {
            private final ProgressBar progressBar = new ProgressBar();
            private final Label statusLabel = new Label();
            private final VBox container = new VBox(4, statusLabel, progressBar);

            {
                container.setAlignment(Pos.CENTER_LEFT);
                progressBar.setMaxWidth(Double.MAX_VALUE);
                progressBar.getStyleClass().add("table-progress-bar");
                statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            }

            @Override
            protected void updateItem(Project project, boolean empty) {
                super.updateItem(project, empty);

                if (empty || project == null) {
                    setGraphic(null);
                } else {
                    double progressValue = 0.0;
                    String status = project.getStatus() != null ? project.getStatus().toString() : "PLANNING";

                    // Map structural progress percentage bands matching your Admin style blueprint
                    switch (status) {
                        case "PLANNING" -> {
                            progressValue = 0.20;
                            statusLabel.setText("PLANNING (20%)");
                            statusLabel.setStyle("-fx-text-fill: #7C4DFF;"); // Deep executive purple
                        }
                        case "ACTIVE", "IN_PROGRESS" -> {
                            progressValue = 0.65;
                            statusLabel.setText("ACTIVE (65%)");
                            statusLabel.setStyle("-fx-text-fill: #00B37E;"); // Mint team green
                        }
                        case "COMPLETED" -> {
                            progressValue = 1.0;
                            statusLabel.setText("COMPLETED (100%)");
                            statusLabel.setStyle("-fx-text-fill: #00875A;"); // Dark completion green
                        }
                        default -> {
                            progressValue = 0.0;
                            statusLabel.setText(status);
                            statusLabel.setStyle("-fx-text-fill: #848D95;");
                        }
                    }

                    progressBar.setProgress(progressValue);
                    setGraphic(container);
                }
            }
        });

        // ── 🖱️ UPGRADE: ROW SELECTION DOUBLE-CLICK LISTENER ──────────────────
        projectTableView.setRowFactory(tv -> {
            TableRow<Project> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                // Route user to detailed page view when valid record row is double-clicked
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Project selectedProject = row.getItem();
                    navigateToProjectDetails(selectedProject);
                }
            });
            return row;
        });

        projectTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            onProjectSelected(newSelection);
        });

        // Load projects from DB
        loadProjects();
        loadScrumBoard(null);
        refreshDashboardCards();
    }

    // ── LOAD PROJECTS INTO TABLE ──────────────────────────────────
    private void loadProjects() {
        String managerId = SessionManager.getInstance().getCurrentUser().getId();
        List<Project> projects = AppFactory.projectService
                .getProjectsByManager(managerId);
        projectTableView.setItems(FXCollections.observableArrayList(projects));
    }

    // ── SCRUM BOARD HELPERS ─────────────────────────────────────
    private void loadScrumBoard(String projectId) {
        List<Task> tasks = projectId == null
                ? taskService.getAllTasks()
                : taskService.getTasksByProject(projectId);

        configureDrop(todoColumn, TaskStatus.TODO);
        configureDrop(inProgressColumn, TaskStatus.IN_PROGRESS);
        configureDrop(doneColumn, TaskStatus.DONE);

        int todoCount = (int) tasks.stream().filter(task -> matchesStatus(task.getStatus(), TaskStatus.TODO)).count();
        int inProgressCount = (int) tasks.stream()
                .filter(task -> matchesStatus(task.getStatus(), TaskStatus.IN_PROGRESS)).count();
        int doneCount = (int) tasks.stream().filter(task -> matchesStatus(task.getStatus(), TaskStatus.DONE)).count();

        populateColumn(todoColumn, tasks, TaskStatus.TODO);
        populateColumn(inProgressColumn, tasks, TaskStatus.IN_PROGRESS);
        populateColumn(doneColumn, tasks, TaskStatus.DONE);

        String source = projectId == null ? "all projects" : "project " + selectedProjectName;
        scrumStatusLabel.setText("Loaded " + tasks.size() + " task(s) across " + source + ".");
        scrumProjectLabel.setText(projectId == null ? "Filter: All projects" : "Filter: " + selectedProjectName);
        todoCountLabel.setText(todoCount + " tasks");
        inProgressCountLabel.setText(inProgressCount + " tasks");
        doneCountLabel.setText(doneCount + " tasks");
        refreshDashboardCards();
    }

    private void populateColumn(VBox column, List<Task> tasks, TaskStatus targetStatus) {
        column.getChildren().clear();

        List<Task> matchingTasks = tasks.stream()
                .filter(task -> matchesStatus(task.getStatus(), targetStatus))
                .toList();

        if (matchingTasks.isEmpty()) {
            Label emptyLabel = new Label("No " + targetStatus.name().replace('_', ' ').toLowerCase() + " tasks");
            emptyLabel.setWrapText(true);
            emptyLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
            column.getChildren().add(emptyLabel);
            return;
        }

        for (Task task : matchingTasks) {
            column.getChildren().add(createTaskCard(task));
        }
    }

    private boolean matchesStatus(TaskStatus actualStatus, TaskStatus expectedStatus) {
        if (actualStatus == null) {
            return expectedStatus == TaskStatus.TODO;
        }
        return actualStatus == expectedStatus;
    }

    private Node createTaskCard(Task task) {
        VBox card = new VBox(8);
        card.setPrefWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-padding: 16px; -fx-border-color: #E5E9EB; -fx-border-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 10, 0.16, 0, 2);");

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #0F172A;");

        Label descriptionLabel = new Label(task.getDescription() == null || task.getDescription().isBlank()
                ? "No description provided"
                : task.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569; -fx-padding: 4 0 0 0;");

        String assigneeText = task.getAssigneeId() == null || task.getAssigneeId().isBlank()
                ? "Unassigned"
                : "Assignee: " + task.getAssigneeId();
        Label metaLabel = new Label(assigneeText + " • Due: "
                + (task.getDueDate() == null || task.getDueDate().isBlank() ? "TBD" : task.getDueDate()));
        metaLabel.setWrapText(true);
        metaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8; -fx-padding: 4 0 0 0;");

        Label statusBadge = new Label(task.getStatus() != null ? task.getStatus().name().replace('_', ' ') : "TODO");
        statusBadge.setStyle(
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #1D4ED8; -fx-background-color: rgba(59,130,246,0.12); -fx-background-radius: 999px; -fx-padding: 4 10 4 10;");

        card.getChildren().addAll(statusBadge, titleLabel, descriptionLabel, metaLabel);
        configureDrag(card, task);
        return card;
    }

    private void configureDrag(Node node, Task task) {
        node.setOnDragDetected(event -> {
            Dragboard dragboard = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(task.getId());
            dragboard.setContent(content);
            dragboard.setDragView(node.snapshot(null, null));
            node.setOpacity(0.5);
            event.consume();
        });

        node.setOnDragDone(event -> {
            node.setOpacity(1.0);
            event.consume();
        });
    }

    private void configureDrop(VBox column, TaskStatus targetStatus) {
        String baseStyle = column.getStyle();

        column.setOnDragOver(event -> {
            if (event.getGestureSource() != null && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                column.setStyle(baseStyle + " -fx-border-color: #60A5FA; -fx-border-width: 2px;");
            }
            event.consume();
        });

        column.setOnDragExited(event -> {
            column.setStyle(baseStyle);
            event.consume();
        });

        column.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;

            if (dragboard.hasString()) {
                taskService.updateTaskStatus(dragboard.getString(), targetStatus);
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    private void handleRefreshScrumBoard() {
        loadScrumBoard(selectedProjectId);
    }

    private void onProjectSelected(Project project) {
        if (project == null) {
            selectedProjectId = null;
            selectedProjectName = null;
        } else {
            selectedProjectId = project.getId();
            selectedProjectName = project.getName();
        }
        loadScrumBoard(selectedProjectId);
    }

    @FXML
    private void handleShowDashboardOverview() {
        loadProjects();
        loadScrumBoard(null);
        scrumStatusLabel.setText("Showing dashboard overview for your managed projects.");
    }

    @FXML
    private void handleShowEngineerDirectory() {
        loadProjects();
        long engineers = AppFactory.userService.getAllUsers().stream()
                .filter(Objects::nonNull)
                .filter(user -> user.getRole() == UserRole.EMPLOYEE)
                .filter(User::isActive)
                .map(User::getId)
                .distinct()
                .count();
        scrumStatusLabel.setText("Engineer directory summary: " + engineers + " active engineers.");
    }

    @FXML
    private void handleShowProjectsOverview() {
        loadProjects();
        scrumStatusLabel.setText("Showing all active projects for your team.");
    }

    @FXML
    private void handleShowAnalytics() {
        refreshDashboardCards();
        scrumStatusLabel.setText("Analytics refreshed for your current project and task portfolio.");
    }

    @FXML
    private void handleOpenBoardView() {
        try {
            if (boardViewStage != null && boardViewStage.isShowing()) {
                boardViewStage.toFront();
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/manager/scrum-board-view.fxml"));
            Parent root = loader.load();

            ScrumBoardController boardController = loader.getController();
            boardController.setProjectFilter(selectedProjectId, selectedProjectName);

            boardViewStage = new Stage();
            boardViewStage.setTitle("StartupLedger Pro — Scrum Board");
            boardViewStage.initOwner(projectTableView.getScene().getWindow());
            boardViewStage.setScene(new Scene(root, 1200, 700));
            boardViewStage.setResizable(true);
            boardViewStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCloseBoardView() {
        if (boardViewStage != null) {
            boardViewStage.close();
        }
    }

    // ── NAVIGATION ROUTING LOGIC ──────────────────────────────────
    private void navigateToProjectDetails(Project selectedProject) {
        try {
            // 1. Point to your details view FXML file
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/manager/project-details.fxml"));
            Parent root = loader.load(); // Load must happen first!

            // 2. Fetch the correct controller instance from the loader
            ProjectDetailsController detailsController = loader.getController();

            // 3. Inject the selected project context cleanly
            System.out.println("Routing user to workspace detail inspection: " + selectedProject.getName());
            detailsController.setProjectContext(selectedProject);

            // 4. 🔥 FIX: Find the active window stage safely without incorrect casting
            Stage stage = null;

            // Loop through all open windows to find your active dashboard window stage
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window instanceof Stage && window.isShowing()) {
                    stage = (Stage) window;
                    break;
                }
            }

            // 5. Swap the scene root view container over smoothly
            if (stage != null && stage.getScene() != null) {
                stage.getScene().setRoot(root);
                stage.setTitle("StartupLedger Pro — " + selectedProject.getName());
            } else {
                System.err.println("Error: Could not locate the primary stage window context.");
            }

        } catch (Exception e) {
            System.err.println("Failed to route transition into Project Workspace Detail view.");
            e.printStackTrace();
        }
    }

    private void refreshDashboardCards() {
        List<Project> projects = AppFactory.projectService
                .getProjectsByManager(SessionManager.getInstance().getCurrentUser().getId());
        double totalBudget = projects.stream()
                .mapToDouble(Project::getBudget)
                .sum();
        long activeProjects = projects.stream()
                .filter(project -> project.getStatus() != null && project.getStatus() != ProjectStatus.COMPLETED)
                .count();
        List<Task> tasks = projects.stream()
                .flatMap(project -> taskService.getTasksByProject(project.getId()).stream())
                .toList();
        long pendingTasks = tasks.stream().filter(task -> task.getStatus() != TaskStatus.DONE).count();
        long assignedEngineers = tasks.stream()
                .map(Task::getAssigneeId)
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .distinct()
                .count();

        engineerCountLabel.setText(String.valueOf(assignedEngineers));
        activeProjectCountLabel.setText(String.valueOf(activeProjects));
        budgetTotalLabel.setText(String.format("Tk %,.2f", totalBudget));
        pendingTasksCountLabel.setText(String.valueOf(pendingTasks));
    }

    // ── OPEN CREATE PROJECT MODAL ─────────────────────────────────
    @FXML
    private void handleCreateProject() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/manager/create-project-modal.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.setTitle("Create New Project");
            modalStage.initModality(Modality.WINDOW_MODAL);
            modalStage.initOwner(projectTableView.getScene().getWindow());
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);

            // Block until user closes the modal
            modalStage.showAndWait();

            // Refresh table if project was saved
            CreateProjectModalController controller = loader.getController();
            if (controller.isSaveClicked()) {
                loadProjects();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── LOGOUT ────────────────────────────────────────────────────
    @FXML
    private void handleLogout() {
        try {
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) projectTableView.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("StartupLedger Pro — Login");
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}