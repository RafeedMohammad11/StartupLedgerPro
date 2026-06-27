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

    public Task(String id, String projectId, String assigneeId, String title, String description, TaskStatus status, String dueDate) {
        this.id = id;
        this.projectId = projectId;
        this.assigneeId = assigneeId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
    }

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getAssigneeId() { return assigneeId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public String getDueDate() { return dueDate; }

    public void setStatus(TaskStatus status) { this.status = status; }
}