package com.example.startupledgerpro.service;

import com.example.startupledgerpro.exception.EntityNotFoundException;
import com.example.startupledgerpro.exception.ValidationException;
import com.example.startupledgerpro.model.Project;
import com.example.startupledgerpro.model.enums.ProjectCategory;
import com.example.startupledgerpro.model.enums.ProjectStatus;
import com.example.startupledgerpro.repository.ProjectRepository;
import com.example.startupledgerpro.util.Validator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProjectService {

    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;

    public ProjectService(ProjectRepository projectRepository, NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.notificationService = notificationService;
    }

    public Project createProject(String name, String description, String managerId,
            ProjectCategory category, double budget, String deadline) {
        Validator.requireValid(Validator.validateProjectName(name));
        Validator.requireValid(Validator.validateAmount(budget, "Budget"));
        Validator.requireValid(Validator.validateDate(deadline, "Deadline"));
        if (category == null) {
            throw new ValidationException("category", "Project category is required.");
        }
        if (managerId == null || managerId.isBlank()) {
            throw new ValidationException("manager", "Project manager is required.");
        }

        Project project = new Project.Builder()
                .id("prj-" + UUID.randomUUID().toString().substring(0, 8))
                .name(name.trim())
                .description(description)
                .managerId(managerId)
                .category(category)
                .status(ProjectStatus.PLANNING)
                .budget(budget)
                .deadline(deadline.trim())
                .build();

        projectRepository.save(project);
        notificationService.createIfMissing(managerId,
                "New project assigned: " + name,
                "A new project '" + name + "' has been assigned to you.");
        return project;
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getProjectsByManager(String managerId) {
        return projectRepository.findByManagerId(managerId);
    }

    public List<Project> getProjectsByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }

    public List<Project> getProjectsByCategory(ProjectCategory category) {
        return projectRepository.findByCategory(category);
    }

    public Optional<Project> getProjectById(String id) {
        return projectRepository.findById(id);
    }

    public void updateStatus(String projectId, ProjectStatus newStatus) {
        if (newStatus == null) {
            throw new ValidationException("status", "Project status is required.");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project", projectId));
        project.setStatus(newStatus);
        projectRepository.save(project);
    }

    public void deleteProject(String id) {
        if (projectRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Project", id);
        }
        projectRepository.delete(id);
    }
}
