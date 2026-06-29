package com.example.startupledgerpro.service;

import com.example.startupledgerpro.model.Project;
import com.example.startupledgerpro.model.enums.ProjectCategory;
import com.example.startupledgerpro.model.enums.ProjectStatus;
import com.example.startupledgerpro.repository.ProjectRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // ── CREATE PROJECT ────────────────────────────────────────────
    public Project createProject(String name, String description, String managerId,
                                 ProjectCategory category, double budget, String deadline) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name must not be empty.");
        }
        if (budget <= 0) {
            throw new IllegalArgumentException("Budget must be greater than zero.");
        }

        Project project = new Project.Builder()
                .id("prj-" + UUID.randomUUID().toString().substring(0, 8))
                .name(name)
                .description(description)
                .managerId(managerId)
                .category(category)
                .status(ProjectStatus.PLANNING)
                .budget(budget)
                .deadline(deadline)
                .build();

        projectRepository.save(project);
        return project;
    }

    // ── GET ALL ───────────────────────────────────────────────────
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // ── GET BY MANAGER ────────────────────────────────────────────
    public List<Project> getProjectsByManager(String managerId) {
        return projectRepository.findByManagerId(managerId);
    }

    // ── GET BY STATUS ─────────────────────────────────────────────
    public List<Project> getProjectsByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }

    // ── GET BY CATEGORY ───────────────────────────────────────────
    public List<Project> getProjectsByCategory(ProjectCategory category) {
        return projectRepository.findByCategory(category);
    }

    // ── GET BY ID ─────────────────────────────────────────────────
    public Optional<Project> getProjectById(String id) {
        return projectRepository.findById(id);
    }

    // ── UPDATE STATUS ─────────────────────────────────────────────
    public void updateStatus(String projectId, ProjectStatus newStatus) {
        projectRepository.findById(projectId).ifPresent(project -> {
            project.setStatus(newStatus);
            projectRepository.save(project);
        });
    }

    // ── DELETE ────────────────────────────────────────────────────
    public void deleteProject(String id) {
        projectRepository.delete(id);
    }
}