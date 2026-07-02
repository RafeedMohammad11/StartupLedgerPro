package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.Project;
import com.example.startupledgerpro.model.Task;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.model.enums.ProjectStatus;
import com.example.startupledgerpro.model.enums.TaskStatus;
import com.example.startupledgerpro.model.enums.UserRole;
import com.example.startupledgerpro.util.CurrencyUtil;
import com.example.startupledgerpro.service.TaskService;
import com.example.startupledgerpro.session.SessionManager;
import com.example.startupledgerpro.util.ExceptionHandler;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ManagerDashboardController {

    @FXML private TableView<Project>           projectTableView;
    @FXML private TableColumn<Project, String>  colProjectName;
    @FXML private TableColumn<Project, String>  colEngineer;
    @FXML private TableColumn<Project, Project> colProgress;
    @FXML private Label                         welcomeLabel;
    @FXML private VBox                          todoColumn;
    @FXML private VBox                          inProgressColumn;
    @FXML private VBox                          doneColumn;
    @FXML private Label                         scrumStatusLabel;
    @FXML private Label                         scrumProjectLabel;
    @FXML private Label                         todoCountLabel;
    @FXML private Label                         inProgressCountLabel;
    @FXML private Label                         doneCountLabel;
    @FXML private Label                         engineerCountLabel;
    @FXML private Label                         activeProjectCountLabel;
    @FXML private Label                         budgetTotalLabel;
    @FXML private Label                         remainingBalanceLabel;
    @FXML private Label                         financialHealthLabel;
    @FXML private Label                         pendingTasksCountLabel;

    private String selectedProjectId;
    private String selectedProjectName;
    private Stage  boardViewStage;

    private static final TaskService taskService = AppFactory.taskService;

    // Cache userId → name to avoid repeated DB hits per card
    private final Map<String, String> userNameCache = new HashMap<>();

    // ── INIT ──────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        String name = SessionManager.getInstance().getCurrentUser().getName();
        welcomeLabel.setText("Welcome back, " + name);

        setupTableColumns();
        loadProjects();
        loadMiniScrumBoard(null);
        refreshDashboardCards();
    }

    // ── TABLE SETUP ───────────────────────────────────────────────
    private void setupTableColumns() {
        colProjectName.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getName())
        );

        // Resolve manager name from ID
        colEngineer.setCellValueFactory(data -> {
            String managerId = data.getValue().getManagerId();
            String managerName = resolveUserName(managerId);
            return new SimpleStringProperty(managerName);
        });

        colProgress.setCellValueFactory(
                data -> new SimpleObjectProperty<>(data.getValue())
        );
        colProgress.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar bar   = new ProgressBar();
            private final Label       lbl   = new Label();
            private final VBox        box   = new VBox(4, lbl, bar);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                bar.setMaxWidth(Double.MAX_VALUE);
                lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            }
            @Override
            protected void updateItem(Project p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) { setGraphic(null); return; }
                String status = p.getStatus() != null ? p.getStatus().name() : "PLANNING";
                switch (status) {
                    case "PLANNING"    -> { bar.setProgress(0.20); lbl.setText("PLANNING (20%)");   lbl.setStyle("-fx-text-fill: #7C4DFF; -fx-font-size: 11px; -fx-font-weight: bold;"); }
                    case "ACTIVE"      -> { bar.setProgress(0.65); lbl.setText("ACTIVE (65%)");     lbl.setStyle("-fx-text-fill: #00B37E; -fx-font-size: 11px; -fx-font-weight: bold;"); }
                    case "COMPLETED"   -> { bar.setProgress(1.00); lbl.setText("COMPLETED (100%)"); lbl.setStyle("-fx-text-fill: #00875A; -fx-font-size: 11px; -fx-font-weight: bold;"); }
                    default            -> { bar.setProgress(0.00); lbl.setText(status);              lbl.setStyle("-fx-text-fill: #848D95; -fx-font-size: 11px; -fx-font-weight: bold;"); }
                }
                setGraphic(box);
            }
        });

        // Double-click → project details
        projectTableView.setRowFactory(tv -> {
            TableRow<Project> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    navigateToProjectDetails(row.getItem());
            });
            return row;
        });

        // Single-click → filter mini scrum board
        projectTableView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, newVal) -> onProjectSelected(newVal));
    }

    // ── PROJECTS ──────────────────────────────────────────────────
    private void loadProjects() {
        String managerId = SessionManager.getInstance().getCurrentUser().getId();
        List<Project> projects = AppFactory.projectService.getProjectsByManager(managerId);
        projectTableView.setItems(FXCollections.observableArrayList(projects));
    }

    // ── MINI SCRUM BOARD (compact — for dashboard panel) ──────────
    private void loadMiniScrumBoard(String projectId) {
        List<Task> tasks = projectId == null
                ? taskService.getAllTasks()
                : taskService.getTasksByProject(projectId);

        int todoCount       = countByStatus(tasks, TaskStatus.TODO);
        int inProgressCount = countByStatus(tasks, TaskStatus.IN_PROGRESS);
        int doneCount       = countByStatus(tasks, TaskStatus.DONE);

        populateMiniColumn(todoColumn,       tasks, TaskStatus.TODO);
        populateMiniColumn(inProgressColumn, tasks, TaskStatus.IN_PROGRESS);
        populateMiniColumn(doneColumn,       tasks, TaskStatus.DONE);

        // Configure drop targets
        configureDrop(todoColumn,       TaskStatus.TODO);
        configureDrop(inProgressColumn, TaskStatus.IN_PROGRESS);
        configureDrop(doneColumn,       TaskStatus.DONE);

        String source = projectId == null
                ? "all projects"
                : selectedProjectName;
        scrumStatusLabel.setText("Loaded " + tasks.size() + " task(s) across " + source + ".");
        scrumProjectLabel.setText(projectId == null ? "Filter: All projects" : "Filter: " + selectedProjectName);
        todoCountLabel.setText(todoCount + " tasks");
        inProgressCountLabel.setText(inProgressCount + " tasks");
        doneCountLabel.setText(doneCount + " tasks");
    }

    // ── COMPACT COLUMN — title + assignee chip only ───────────────
    private void populateMiniColumn(VBox column, List<Task> tasks, TaskStatus status) {
        column.getChildren().clear();

        List<Task> matching = tasks.stream()
                .filter(t -> matchesStatus(t.getStatus(), status))
                .toList();

        if (matching.isEmpty()) {
            Label empty = new Label("No tasks");
            empty.setStyle(
                    "-fx-font-size: 11px;" +
                            "-fx-text-fill: #CBD5E1;" +
                            "-fx-padding: 8 0 0 0;"
            );
            column.getChildren().add(empty);
            return;
        }

        // Show max 3 cards in mini view — keeps panel clean
        List<Task> visible = matching.size() > 3
                ? matching.subList(0, 3)
                : matching;

        for (Task task : visible) {
            column.getChildren().add(buildMiniCard(task));
        }

        // If more than 3, show a "+N more" label
        if (matching.size() > 3) {
            Label more = new Label("+" + (matching.size() - 3) + " more  →  See Board");
            more.setStyle(
                    "-fx-font-size: 10px;" +
                            "-fx-text-fill: #00B37E;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 4 0 0 0;"
            );
            more.setOnMouseClicked(e -> handleOpenBoardView());
            column.getChildren().add(more);
        }
    }

    // ── COMPACT TASK CARD ─────────────────────────────────────────
    private Node buildMiniCard(Task task) {
        VBox card = new VBox(6);
        card.setMaxWidth(Double.MAX_VALUE);
        String cardStyle =
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #E2E8F0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 8 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 4, 0, 0, 1);" +
                        "-fx-cursor: hand;";
        card.setStyle(cardStyle);

        // Title — truncated, single line
        Label title = new Label(task.getTitle());
        title.setMaxWidth(Double.MAX_VALUE);
        title.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #0F172A;" +
                        "-fx-max-width: Infinity;"
        );
        title.setEllipsisString("...");
        title.setWrapText(false);

        // Bottom row: assignee avatar + name + due date
        HBox meta = new HBox(6);
        meta.setAlignment(Pos.CENTER_LEFT);

        // Resolve real name
        String assigneeName = resolveUserName(task.getAssigneeId());
        String initials     = getInitials(assigneeName);

        // Tiny avatar
        StackPane avatar = new StackPane();
        avatar.setPrefSize(18, 18);
        avatar.setMinSize(18, 18);
        avatar.setStyle(
                "-fx-background-color: " + getAvatarColor(task.getAssigneeId()) + ";" +
                        "-fx-background-radius: 9;"
        );
        Label avLbl = new Label(initials);
        avLbl.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 7px;" +
                        "-fx-font-weight: bold;"
        );
        avatar.getChildren().add(avLbl);

        Label assigneeLbl = new Label(assigneeName);
        assigneeLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String due = (task.getDueDate() == null || task.getDueDate().isBlank())
                ? "—" : task.getDueDate();
        Label dueLbl = new Label(due);
        dueLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: " + getDueDateColor(task) + ";");

        meta.getChildren().addAll(avatar, assigneeLbl, spacer, dueLbl);
        card.getChildren().addAll(title, meta);

        // Hover
        card.setOnMouseEntered(e -> card.setStyle(cardStyle.replace("#FFFFFF", "#F8FAFF").replace("#E2E8F0", "#C7D2FE")));
        card.setOnMouseExited(e -> card.setStyle(cardStyle));

        configureDrag(card, task);
        return card;
    }

    // ── DRAG ──────────────────────────────────────────────────────
    private void configureDrag(Node node, Task task) {
        node.setOnDragDetected(event -> {
            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.putString(task.getId());
            db.setContent(cc);
            node.setOpacity(0.5);
            event.consume();
        });
        node.setOnDragDone(event -> {
            node.setOpacity(1.0);
            event.consume();
        });
    }

    // ── DROP ──────────────────────────────────────────────────────
    private void configureDrop(VBox column, TaskStatus targetStatus) {
        String baseStyle = column.getStyle();
        column.setOnDragOver(event -> {
            if (event.getGestureSource() != null && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                column.setStyle(baseStyle + "-fx-border-color: #6366F1; -fx-border-width: 2;");
            }
            event.consume();
        });
        column.setOnDragExited(event -> {
            column.setStyle(baseStyle);
            event.consume();
        });
        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                try {
                    taskService.updateTaskStatus(db.getString(), targetStatus);
                    loadMiniScrumBoard(selectedProjectId);
                    refreshDashboardCards();
                    success = true;
                } catch (RuntimeException ex) {
                    String message = ExceptionHandler.resolveMessage(ex);
                    scrumStatusLabel.setText(message);
                    ExceptionHandler.showWarning("Update Task", message);
                }
            }
            event.setDropCompleted(success);
            column.setStyle(baseStyle);
            event.consume();
        });
    }

    // ── HELPERS ───────────────────────────────────────────────────
    private String resolveUserName(String userId) {
        if (userId == null || userId.isBlank()) return "Unassigned";
        return userNameCache.computeIfAbsent(userId, id ->
                AppFactory.userRepository.findById(id)
                        .map(User::getName)
                        .orElse("Unknown")
        );
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank() || name.equals("Unassigned")) return "?";
        String[] parts = name.trim().split("\\s+");
        return parts.length >= 2
                ? ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase()
                : name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String getAvatarColor(String userId) {
        if (userId == null || userId.isBlank()) return "#94A3B8";
        return switch (Math.abs(userId.hashCode()) % 5) {
            case 0  -> "#6366F1";
            case 1  -> "#0EA5E9";
            case 2  -> "#EC4899";
            case 3  -> "#F59E0B";
            default -> "#10B981";
        };
    }

    private String getDueDateColor(Task task) {
        if (task.getDueDate() == null || task.getDueDate().isBlank()) return "#CBD5E1";
        try {
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                    java.time.LocalDate.now(),
                    java.time.LocalDate.parse(task.getDueDate())
            );
            if (days < 0)   return "#EF4444";
            if (days <= 3)  return "#F59E0B";
            return "#64748B";
        } catch (Exception e) { return "#64748B"; }
    }

    private boolean matchesStatus(TaskStatus actual, TaskStatus expected) {
        if (actual == null) return expected == TaskStatus.TODO;
        return actual == expected;
    }

    private int countByStatus(List<Task> tasks, TaskStatus status) {
        return (int) tasks.stream()
                .filter(t -> matchesStatus(t.getStatus(), status))
                .count();
    }

    // ── DASHBOARD STAT CARDS ──────────────────────────────────────
    private void refreshDashboardCards() {
        String managerId = SessionManager.getInstance().getCurrentUser().getId();
        List<Project> projects = AppFactory.projectService.getProjectsByManager(managerId);

        double totalBudget = projects.stream().mapToDouble(Project::getBudget).sum();
        long activeProjects = projects.stream()
                .filter(p -> p.getStatus() != null && p.getStatus() != ProjectStatus.COMPLETED)
                .count();

        List<Task> allTasks = projects.stream()
                .flatMap(p -> taskService.getTasksByProject(p.getId()).stream())
                .toList();

        long pendingTasks = allTasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE)
                .count();
        long engineers = allTasks.stream()
                .map(Task::getAssigneeId)
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .distinct()
                .count();
        double remainingBalance = AppFactory.financialService.getRemainingBalanceForProjects(projects);

        engineerCountLabel.setText(String.valueOf(engineers));
        activeProjectCountLabel.setText(String.valueOf(activeProjects));
        budgetTotalLabel.setText(String.format("Tk %,.0f", totalBudget));
        remainingBalanceLabel.setText(CurrencyUtil.formatBdt(remainingBalance));
        financialHealthLabel.setText(remainingBalance >= 0 ? "Healthy balance" : "Over budget");
        pendingTasksCountLabel.setText(String.valueOf(pendingTasks));
    }

    // ── PROJECT SELECTION ─────────────────────────────────────────
    private void onProjectSelected(Project project) {
        if (project == null) {
            selectedProjectId   = null;
            selectedProjectName = null;
        } else {
            selectedProjectId   = project.getId();
            selectedProjectName = project.getName();
        }
        loadMiniScrumBoard(selectedProjectId);
    }

    // ── SIDEBAR NAV ───────────────────────────────────────────────
    @FXML private void handleShowDashboardOverview() {
        loadProjects();
        loadMiniScrumBoard(null);
        refreshDashboardCards();
    }

    @FXML private void handleShowEngineerDirectory() {
        long engineers = AppFactory.userService.getAllUsers().stream()
                .filter(u -> u.getRole() == UserRole.EMPLOYEE && u.isActive())
                .distinct().count();
        scrumStatusLabel.setText("Engineer directory: " + engineers + " active engineers.");
    }

    @FXML private void handleShowProjectsOverview() {
        loadProjects();
        scrumStatusLabel.setText("Showing all projects for your team.");
    }

    @FXML private void handleShowAnalytics() {
        refreshDashboardCards();
        scrumStatusLabel.setText("Analytics refreshed.");
    }

    // ── SCRUM BOARD (full window) ─────────────────────────────────
    @FXML
    private void handleRefreshScrumBoard() {
        loadMiniScrumBoard(selectedProjectId);
    }

    @FXML
    private void handleOpenBoardView() {
        try {
            if (boardViewStage != null && boardViewStage.isShowing()) {
                boardViewStage.toFront();
                return;
            }
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/manager/scrum-board-view.fxml")
            );
            Parent root = loader.load();
            ScrumBoardController ctrl = loader.getController();
            ctrl.setProjectFilter(selectedProjectId, selectedProjectName);

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

    // ── CREATE PROJECT MODAL ──────────────────────────────────────
    @FXML
    private void handleCreateProject() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/manager/create-project-modal.fxml")
            );
            Parent root = loader.load();
            Stage modal = new Stage();
            modal.setTitle("Create New Project");
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(projectTableView.getScene().getWindow());
            modal.setScene(new Scene(root));
            modal.setResizable(false);
            modal.showAndWait();

            CreateProjectModalController ctrl = loader.getController();
            if (ctrl.isSaveClicked()) {
                loadProjects();
                refreshDashboardCards();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRecordExpense() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/shared/record-expense-modal.fxml")
            );
            Parent root = loader.load();
            RecordExpenseModalController ctrl = loader.getController();
            String managerId = SessionManager.getInstance().getCurrentUser().getId();
            ctrl.setProjects(AppFactory.projectService.getProjectsByManager(managerId));

            Stage modal = new Stage();
            modal.setTitle("Record Expense");
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(projectTableView.getScene().getWindow());
            modal.setScene(new Scene(root));
            modal.setResizable(false);
            modal.showAndWait();

            if (ctrl.wasSaved()) {
                refreshDashboardCards();
                scrumStatusLabel.setText("Expense recorded successfully.");
            }
        } catch (Exception e) {
            ExceptionHandler.handle("Record Expense", e);
        }
    }

    // ── PROJECT DETAILS ───────────────────────────────────────────
    private void navigateToProjectDetails(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/manager/project-details.fxml")
            );
            Parent root = loader.load();
            ProjectDetailsController ctrl = loader.getController();
            ctrl.setProjectContext(project);

            Stage stage = null;
            for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                if (w instanceof Stage && w.isShowing()) {
                    stage = (Stage) w;
                    break;
                }
            }
            if (stage != null && stage.getScene() != null) {
                stage.getScene().setRoot(root);
                stage.setTitle("StartupLedger Pro — " + project.getName());
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
                    getClass().getResource("/fxml/login.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) projectTableView.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 680));
            stage.setTitle("StartupLedger Pro — Login");
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}