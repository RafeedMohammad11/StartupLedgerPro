package com.example.startupledgerpro.model;

import com.example.startupledgerpro.model.enums.TaskStatus;

public class Task {
    private final String id;
    private final String projectId;
    private final String assigneeId; // Can be null if unassigned
    private final String title;
    private final String description;
    private TaskStatus status;
    private final String dueDate;

    // 💡 Private constructor driven explicitly via the Builder instance
    private Task(Builder builder) {
        this.id = builder.id;
        this.projectId = builder.projectId;
        this.assigneeId = builder.assigneeId;
        this.title = builder.title;
        this.description = builder.description;
        this.status = builder.status;
        this.dueDate = builder.deadline; // mapping internal field string matching parameters
    }

    // Getters
    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getAssigneeId() { return assigneeId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public String getDueDate() { return dueDate; }

    // Mutator for lifecycle states
    public void setStatus(TaskStatus status) { this.status = status; }

    // ── 🧠 STATIC INNER BUILDER CLASS ──────────────────────────────
    public static class Builder {
        private String id;
        private String projectId;
        private String assigneeId;
        private String title;
        private String description;
        private TaskStatus status = TaskStatus.TODO; // Clear default state assignment mapping
        private String deadline;

        public Builder id(String id) { this.id = id; return this; }
        public Builder projectId(String projectId) { this.projectId = projectId; return this; }
        public Builder assigneeId(String assigneeId) { this.assigneeId = assigneeId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder status(TaskStatus status) { this.status = status; return this; }
        public Builder dueDate(String dueDate) { this.deadline = dueDate; return this; }

        public Task build() {
            return new Task(this);
        }
    }
}