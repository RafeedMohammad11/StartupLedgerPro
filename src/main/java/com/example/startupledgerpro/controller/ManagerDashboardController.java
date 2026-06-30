package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.Project;
import com.example.startupledgerpro.session.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class ManagerDashboardController {

    @FXML private TableView<Project>           projectTableView;
    @FXML private TableColumn<Project, String> colProjectName;
    @FXML private TableColumn<Project, String> colEngineer;
    @FXML private TableColumn<Project, Project> colProgress; // 💡 Upgraded to Project object type for custom node rendering
    @FXML private Label                        welcomeLabel;

    @FXML
    public void initialize() {
        // Show logged-in manager's name
        String name = SessionManager.getInstance().getCurrentUser().getName();
        welcomeLabel.setText("Welcome back, " + name);

        // Wire table columns to Project fields
        colProjectName.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getName())
        );
        colEngineer.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getManagerId())
        );

        // ── 🔥 UPGRADE: CONFIGURING CUSTOM PROGRESS BAR GRAPHICS COLUMN ──
        colProgress.setCellValueFactory(
                data -> new SimpleObjectProperty<>(data.getValue())
        );
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

        // Load projects from DB
        loadProjects();
    }

    // ── LOAD PROJECTS INTO TABLE ──────────────────────────────────
    private void loadProjects() {
        String managerId = SessionManager.getInstance().getCurrentUser().getId();
        List<Project> projects = AppFactory.projectService
                .getProjectsByManager(managerId);
        projectTableView.setItems(FXCollections.observableArrayList(projects));
    }

    // ── NAVIGATION ROUTING LOGIC ──────────────────────────────────
    // Inside ManagerDashboardController.java

    // Inside ManagerDashboardController.java

    private void navigateToProjectDetails(Project selectedProject) {
        try {
            // 1. Point to your details view FXML file
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/manager/project-details.fxml")
            );
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

    // ── OPEN CREATE PROJECT MODAL ─────────────────────────────────
    @FXML
    private void handleCreateProject() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/manager/create-project-modal.fxml")
            );
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
                    getClass().getResource("/fxml/login.fxml")
            );
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