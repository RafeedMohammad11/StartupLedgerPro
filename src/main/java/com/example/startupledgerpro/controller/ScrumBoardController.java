package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.Task;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.model.enums.TaskStatus;
import com.example.startupledgerpro.service.TaskService;
import com.example.startupledgerpro.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScrumBoardController {

    // ── Inner card columns (inside ScrollPane) ────────────────────
    @FXML
    private VBox todoColumn;
    @FXML
    private VBox inProgressColumn;
    @FXML
    private VBox doneColumn;
    @FXML
    private VBox outerTodoCol;
    @FXML
    private VBox outerInProgressCol;
    @FXML
    private VBox outerDoneCol;

    // ── Header labels ─────────────────────────────────────────────
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
    private Label totalTasksChip;
    @FXML
    private Label inProgressChip;
    @FXML
    private Label doneChip;

    // ── Filter combos ─────────────────────────────────────────────
    @FXML
    private ComboBox<String> projectFilterCombo;
    @FXML
    private ComboBox<String> assigneeFilterCombo;

    // ── State ─────────────────────────────────────────────────────
    private String projectIdFilter;
    private String projectNameFilter;

    // Cache: userId → display name (avoids repeated DB lookups per card)
    private final Map<String, String> userNameCache = new HashMap<>();
    private static final TaskService taskService = AppFactory.taskService;

    // Outer column VBoxes for drop highlighting
    // These are the bordered containers — NOT the fx:id inner VBoxes

    @FXML
    public void initialize() {
        // Outer columns are injected directly from the FXML
        if (outerTodoCol == null || outerInProgressCol == null || outerDoneCol == null) {
            // Fallback: try resolving from the enclosed ScrollPane parents
            outerTodoCol = resolveOuterColumn(todoColumn, outerTodoCol);
            outerInProgressCol = resolveOuterColumn(inProgressColumn, outerInProgressCol);
            outerDoneCol = resolveOuterColumn(doneColumn, outerDoneCol);
        }

        // Wire drop targets ONCE — never in loadScrumBoard()
        configureDrop(outerTodoCol, todoColumn, TaskStatus.TODO);
        configureDrop(outerInProgressCol, inProgressColumn, TaskStatus.IN_PROGRESS);
        configureDrop(outerDoneCol, doneColumn, TaskStatus.DONE);

        // Populate filter combos
        setupFilterCombos();

        loadScrumBoard();
    }

    // ── CALLED FROM PARENT CONTROLLER ────────────────────────────
    public void setProjectFilter(String projectId, String projectName) {
        this.projectIdFilter = projectId;
        this.projectNameFilter = projectName;
        loadScrumBoard();
    }

    // ── SETUP FILTER COMBOS ───────────────────────────────────────
    private void setupFilterCombos() {
        // Projects
        List<String> projectNames = AppFactory.projectRepository
                .findAll().stream()
                .map(p -> p.getName())
                .toList();
        projectFilterCombo.setItems(
                FXCollections.observableArrayList(projectNames));
        projectFilterCombo.setOnAction(e -> loadScrumBoard());

        // Assignees — all active employees
        List<String> assigneeNames = AppFactory.userRepository
                .findAll().stream()
                .map(User::getName)
                .toList();
        assigneeFilterCombo.setItems(
                FXCollections.observableArrayList(assigneeNames));
        assigneeFilterCombo.setOnAction(e -> loadScrumBoard());
    }

    // ── CORE LOAD — only populates cards, never re-registers drops ─
    private void loadScrumBoard() {
        // Fetch tasks
        List<Task> tasks = projectIdFilter == null
                ? taskService.getAllTasks()
                : taskService.getTasksByProject(projectIdFilter);

        // Apply assignee name filter
        String assigneeFilter = assigneeFilterCombo.getValue();
        if (assigneeFilter != null) {
            tasks = tasks.stream()
                    .filter(t -> assigneeFilter.equals(resolveUserName(t.getAssigneeId())))
                    .toList();
        }

        // Populate columns — simple clear + fill, no index tricks
        populateColumn(todoColumn, tasks, TaskStatus.TODO);
        populateColumn(inProgressColumn, tasks, TaskStatus.IN_PROGRESS);
        populateColumn(doneColumn, tasks, TaskStatus.DONE);

        // Update counts
        int todo = countByStatus(tasks, TaskStatus.TODO);
        int inProgress = countByStatus(tasks, TaskStatus.IN_PROGRESS);
        int done = countByStatus(tasks, TaskStatus.DONE);
        updateCounts(todo, inProgress, done, tasks.size());

        // Status bar
        String source = projectIdFilter == null ? "all projects" : projectNameFilter;
        safeSetText(scrumStatusLabel, tasks.size() + " task(s) · " + source);
        safeSetText(scrumProjectLabel, projectIdFilter == null ? "All Projects" : projectNameFilter);
    }

    // ── POPULATE A SINGLE COLUMN ──────────────────────────────────
    private void populateColumn(VBox column, List<Task> tasks, TaskStatus status) {
        // Always clear fully — this VBox has NO persistent children (cards only)
        column.getChildren().clear();

        List<Task> matching = tasks.stream()
                .filter(t -> matchesStatus(t.getStatus(), status))
                .toList();

        if (matching.isEmpty()) {
            Label empty = new Label("No tasks here");
            empty.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-text-fill: #CBD5E1;" +
                            "-fx-padding: 20 0 10 4;");
            column.getChildren().add(empty);
            return;
        }

        for (Task task : matching) {
            column.getChildren().add(buildTaskCard(task));
        }
    }

    // ── BUILD TASK CARD ───────────────────────────────────────────
    private VBox buildTaskCard(Task task) {
        VBox card = new VBox(10);
        String cardStyle = "-fx-background-color: #FFFFFF;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #E2E8F0;" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 8, 0, 0, 2);" +
                "-fx-cursor: hand;";
        card.setStyle(cardStyle);
        card.setMaxWidth(Double.MAX_VALUE);

        // ── Type badge + priority dot ──
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label typeBadge = new Label(getTypeLabel(task));
        typeBadge.setStyle(
                "-fx-background-color: " + getTypeBg(task) + ";" +
                        "-fx-text-fill: " + getTypeColor(task) + ";" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 2 9;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane dot = new StackPane();
        dot.setPrefSize(8, 8);
        dot.setStyle(
                "-fx-background-color: " + getPriorityColor(task) + ";" +
                        "-fx-background-radius: 10;");
        topRow.getChildren().addAll(typeBadge, spacer, dot);

        // ── Title ──
        Label title = new Label(task.getTitle());
        title.setWrapText(true);
        title.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #0F172A;");

        // ── Description ──
        String desc = (task.getDescription() == null || task.getDescription().isBlank())
                ? "No description"
                : task.getDescription();
        Label descLabel = new Label(desc);
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(36);
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        // ── Project name ──
        String projectName = resolveProjectName(task.getProjectId());
        Label projectLabel = new Label(projectName);
        projectLabel.setStyle(
                "-fx-font-size: 10px;" +
                        "-fx-text-fill: #94A3B8;" +
                        "-fx-background-color: #F8FAFC;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 2 6;");

        // ── Divider ──
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.setStyle("-fx-background-color: #F1F5F9;");

        // ── Bottom row: assignee avatar + name + due date ──
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        // Resolve assignee name from ID
        String assigneeName = resolveUserName(task.getAssigneeId());
        String initials = getInitials(assigneeName);

        StackPane avatar = new StackPane();
        avatar.setPrefSize(26, 26);
        avatar.setMinSize(26, 26);
        avatar.setStyle(
                "-fx-background-color: " + getAvatarColor(task.getAssigneeId()) + ";" +
                        "-fx-background-radius: 13;");
        Label avatarLabel = new Label(initials);
        avatarLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 9px;" +
                        "-fx-font-weight: bold;");
        avatar.getChildren().add(avatarLabel);

        Label assigneeLabel = new Label(assigneeName);
        assigneeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");

        Region metaSpacer = new Region();
        HBox.setHgrow(metaSpacer, Priority.ALWAYS);

        String dueText = (task.getDueDate() == null || task.getDueDate().isBlank())
                ? "No due date"
                : task.getDueDate();
        Label dueLabel = new Label(dueText);
        dueLabel.setStyle(
                "-fx-font-size: 10px;" +
                        "-fx-text-fill: " + getDueDateColor(task) + ";");

        metaRow.getChildren().addAll(avatar, assigneeLabel, metaSpacer, dueLabel);
        card.getChildren().addAll(topRow, title, descLabel, projectLabel, divider, metaRow);

        // Hover
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #FAFBFF;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #C7D2FE;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.12), 14, 0, 0, 4);" +
                        "-fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(cardStyle));

        configureDrag(card, task);
        return card;
    }

    // ── DRAG SOURCE ───────────────────────────────────────────────
    private void configureDrag(Node node, Task task) {
        node.setOnDragDetected(event -> {
            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(task.getId());
            db.setContent(content);
            node.setOpacity(0.55);
            event.consume();
        });
        node.setOnDragDone(event -> {
            node.setOpacity(1.0);
            event.consume();
        });
    }

    // ── DROP TARGET — registered ONCE in initialize() ─────────────
    private void configureDrop(VBox outerCol, VBox innerCol, TaskStatus targetStatus) {
        if (outerCol == null)
            return;
        String baseStyle = outerCol.getStyle();

        outerCol.setOnDragOver(event -> {
            if (event.getGestureSource() != null && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                outerCol.setStyle(baseStyle +
                        "-fx-border-color: #6366F1;" +
                        "-fx-border-width: 2;" +
                        "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.15), 12, 0, 0, 0);");
            }
            event.consume();
        });

        outerCol.setOnDragExited(event -> {
            outerCol.setStyle(baseStyle);
            event.consume();
        });

        outerCol.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                taskService.updateTaskStatus(db.getString(), targetStatus);
                outerCol.setStyle(baseStyle);
                loadScrumBoard();
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // Inner VBox also needs to accept drops (it sits on top of the outer)
        innerCol.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        innerCol.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                taskService.updateTaskStatus(db.getString(), targetStatus);
                loadScrumBoard();
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    // ── RESOLVE HELPERS ───────────────────────────────────────────

    // Converts usr-emp-001 → "Rafeed Islam" using cache
    private String resolveUserName(String userId) {
        if (userId == null || userId.isBlank())
            return "Unassigned";
        return userNameCache.computeIfAbsent(userId, id -> AppFactory.userRepository.findById(id)
                .map(User::getName)
                .orElse("Unknown"));
    }

    private String resolveProjectName(String projectId) {
        if (projectId == null || projectId.isBlank())
            return "No project";
        return AppFactory.projectRepository.findById(projectId)
                .map(p -> p.getName())
                .orElse("Unknown project");
    }

    // ── COUNT & UPDATE ────────────────────────────────────────────
    private int countByStatus(List<Task> tasks, TaskStatus status) {
        return (int) tasks.stream()
                .filter(t -> matchesStatus(t.getStatus(), status))
                .count();
    }

    private void updateCounts(int todo, int inProgress, int done, int total) {
        safeSetText(todoCountLabel, String.valueOf(todo));
        safeSetText(inProgressCountLabel, String.valueOf(inProgress));
        safeSetText(doneCountLabel, String.valueOf(done));
        safeSetText(totalTasksChip, total + " Total");
        safeSetText(inProgressChip, inProgress + " Active");
        safeSetText(doneChip, done + " Done");
    }

    private VBox resolveOuterColumn(VBox innerCol, VBox existing) {
        if (existing != null)
            return existing;
        if (innerCol == null)
            return null;
        if (innerCol.getParent() instanceof ScrollPane scrollPane
                && scrollPane.getParent() instanceof VBox outer) {
            return outer;
        }
        return null;
    }

    private void safeSetText(Label label, String text) {
        if (label != null)
            label.setText(text);
    }

    private boolean matchesStatus(TaskStatus actual, TaskStatus expected) {
        if (actual == null)
            return expected == TaskStatus.TODO;
        return actual == expected;
    }

    // ── VISUAL HELPERS ────────────────────────────────────────────
    private String getInitials(String name) {
        if (name == null || name.isBlank() || name.equals("Unassigned"))
            return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2)
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String getAvatarColor(String userId) {
        if (userId == null || userId.isBlank())
            return "#94A3B8";
        return switch (Math.abs(userId.hashCode()) % 5) {
            case 0 -> "#6366F1";
            case 1 -> "#0EA5E9";
            case 2 -> "#EC4899";
            case 3 -> "#F59E0B";
            default -> "#10B981";
        };
    }

    private String getTypeLabel(Task task) {
        String t = task.getTitle() == null ? "" : task.getTitle().toLowerCase();
        if (t.contains("design"))
            return "DESIGN";
        if (t.contains("review"))
            return "REVIEW";
        if (t.contains("meeting"))
            return "MEETING";
        return "DEVELOPMENT";
    }

    private String getTypeBg(Task task) {
        String l = getTypeLabel(task);
        if (l.contains("Design"))
            return "#FDF4FF";
        if (l.contains("Review"))
            return "#FFFBEB";
        if (l.contains("Meeting"))
            return "#F0F9FF";
        return "#F0FDF4";
    }

    private String getTypeColor(Task task) {
        String l = getTypeLabel(task);
        if (l.contains("Design"))
            return "#9333EA";
        if (l.contains("Review"))
            return "#D97706";
        if (l.contains("Meeting"))
            return "#0284C7";
        return "#16A34A";
    }

    private String getPriorityColor(Task task) {
        if (task.getDueDate() == null || task.getDueDate().isBlank())
            return "#E2E8F0";
        try {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(task.getDueDate()));
            if (days < 0)
                return "#EF4444";
            if (days <= 3)
                return "#F59E0B";
            return "#22C55E";
        } catch (Exception e) {
            return "#E2E8F0";
        }
    }

    private String getDueDateColor(Task task) {
        if (task.getDueDate() == null || task.getDueDate().isBlank())
            return "#CBD5E1";
        try {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(task.getDueDate()));
            if (days < 0)
                return "#EF4444";
            if (days <= 3)
                return "#F59E0B";
            return "#64748B";
        } catch (Exception e) {
            return "#64748B";
        }
    }

    // ── FXML HANDLERS ─────────────────────────────────────────────
    @FXML
    private void handleRefreshScrumBoard() {
        loadScrumBoard();
    }

    @FXML
    private void handleAddTask() {
        System.out.println("Add Task — wire to CreateTaskController");
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) scrumStatusLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("StartupLedger Pro — Login");
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCloseBoardView() {
        ((Stage) scrumStatusLabel.getScene().getWindow()).close();
    }
}