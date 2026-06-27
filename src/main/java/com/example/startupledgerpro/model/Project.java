package com.example.startupledgerpro.model;

import com.example.startupledgerpro.model.enums.ProjectCategory;
import com.example.startupledgerpro.model.enums.ProjectStatus;

public class Project {
    private final String id;
    private final String name;
    private final String description;
    private final String managerId;
    private final ProjectCategory category;
    private ProjectStatus status;
    private final double budget;
    private final String deadline;

    private Project(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.managerId = builder.managerId;
        this.category = builder.category;
        this.status = builder.status;
        this.budget = builder.budget;
        this.deadline = builder.deadline;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getManagerId() { return managerId; }
    public ProjectCategory getCategory() { return category; }
    public ProjectStatus getStatus() { return status; }
    public double getBudget() { return budget; }
    public String getDeadline() { return deadline; }

    public void setStatus(ProjectStatus status) { this.status = status; }

    // Static Inner Builder Class
    public static class Builder {
        private String id;
        private String name;
        private String description;
        private String managerId;
        private ProjectCategory category;
        private ProjectStatus status = ProjectStatus.PLANNING; // default
        private double budget;
        private String deadline;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder managerId(String managerId) { this.managerId = managerId; return this; }
        public Builder category(ProjectCategory category) { this.category = category; return this; }
        public Builder status(ProjectStatus status) { this.status = status; return this; }
        public Builder budget(double budget) { this.budget = budget; return this; }
        public Builder deadline(String deadline) { this.deadline = deadline; return this; }

        public Project build() {
            return new Project(this);
        }
    }
}