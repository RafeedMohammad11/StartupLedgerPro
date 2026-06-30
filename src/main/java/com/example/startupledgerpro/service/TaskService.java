package com.example.startupledgerpro.service;

import com.example.startupledgerpro.model.Task;
import com.example.startupledgerpro.model.enums.TaskStatus;
import com.example.startupledgerpro.repository.TaskRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TaskService {

    private final TaskRepository taskRepository;

    // Standard constructor injection matching your ProjectService
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // ── CREATE TASK ───────────────────────────────────────────────
    public Task createTask(String projectId, String title, String description,
                           String assigneeId, String dueDate) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title must not be empty.");
        }

        Task task = new Task.Builder()
                .id("tsk-" + UUID.randomUUID().toString().substring(0, 8))
                .projectId(projectId)
                .title(title)
                .description(description)
                .assigneeId(assigneeId)
                .status(TaskStatus.TODO) // Set default status on instantiation
                .dueDate(dueDate)
                .build();

        taskRepository.save(task);
        return task;
    }

    // ── GET BY PROJECT ────────────────────────────────────────────
    public List<Task> getTasksByProject(String projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByAssignee(String assigneeId) {
        return taskRepository.getTasksByAssignee(assigneeId);
    }

    // ── GET BY ID ─────────────────────────────────────────────────
    public Optional<Task> getTaskById(String id) {
        return taskRepository.findById(id);
    }

    // ── UPDATE STATUS ─────────────────────────────────────────────
    public void updateTaskStatus(String taskId, TaskStatus newStatus) {
        taskRepository.findById(taskId).ifPresent(task -> {
            task.setStatus(newStatus);
            taskRepository.save(task);
        });
    }

    // ── DELETE TASK ───────────────────────────────────────────────
    public void deleteTask(String id) {
        taskRepository.delete(id);
    }
}
