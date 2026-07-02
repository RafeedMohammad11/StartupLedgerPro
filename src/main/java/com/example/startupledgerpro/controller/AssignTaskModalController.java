package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.util.ExceptionHandler;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AssignTaskModalController {

    @FXML private TextField titleField;
    @FXML private ComboBox<String> engineerComboBox; // Retained as String to avoid breaking signatures
    @FXML private DatePicker dueDatePicker;
    @FXML private TextArea descriptionArea;

    private String targetProjectId;
    private boolean saveClicked = false;
    private List<User> cachedEngineers; // Cached to read employee metadata during display conversions

    @FXML
    public void initialize() {
        // 1. Pull all database user profiles via your UserService
        List<User> users = AppFactory.userService.getAllUsers();

        // Cache the engineer sublist so we can cross-reference names from IDs later
        this.cachedEngineers = users.stream()
                .filter(u -> u.getRole() != null && "EMPLOYEE".equalsIgnoreCase(u.getRole().name()))
                .collect(Collectors.toList());

        // 2. Extract only the distinct IDs for the ComboBox item list
        List<String> engineerIds = cachedEngineers.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        engineerComboBox.setItems(FXCollections.observableArrayList(engineerIds));

        // 3. 🔥 Add a StringConverter to translate raw user IDs into user-friendly display labels
        engineerComboBox.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String id) {
                if (id == null) return "";
                // Find the user object matching this ID to render their name nicely
                return cachedEngineers.stream()
                        .filter(u -> id.equals(u.getId()))
                        .findFirst()
                        .map(u -> u.getName() + " (" + u.getId() + ")")
                        .orElse(id); // Fallback to raw ID if profile metadata isn't located
            }

            @Override
            public String fromString(String string) {
                // Not required as the ComboBox remains non-editable text entry fields
                return null;
            }
        });
    }

    /**
     * 💡 Injects the parent Project scope into the form context
     */
    public void setProjectContext(String projectId) {
        this.targetProjectId = projectId;
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    private void handleAssignTask() {
        String title = titleField.getText();
        String assigneeId = engineerComboBox.getValue();
        String description = descriptionArea.getText();

        // Basic form validation checks
        if (title == null || title.isBlank() || assigneeId == null || dueDatePicker.getValue() == null) {
            ExceptionHandler.showWarning("Assign Task", "Please fill in title, assignee, and due date.");
            return;
        }

        String dueDate = dueDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        try {
            AppFactory.taskService.createTask(targetProjectId, title, description, assigneeId, dueDate);
            saveClicked = true;
            closeStage();
        } catch (RuntimeException ex) {
            ExceptionHandler.handle("Assign Task", ex);
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}