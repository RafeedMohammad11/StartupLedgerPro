package com.example.startupledgerpro.service;

import com.example.startupledgerpro.model.Task;
import com.example.startupledgerpro.model.enums.TaskStatus;
import com.example.startupledgerpro.repository.TaskRepository;
import com.example.startupledgerpro.service.NotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TaskService {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    // Standard constructor injection matching your ProjectService
    public TaskService(TaskRepository taskRepository, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
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
        if (assigneeId != null && !assigneeId.isBlank()) {
            notificationService.createIfMissing(assigneeId,
                    "New task assigned: " + title,
                    "You have been assigned a new task '" + title + "' for project " + projectId + ".");
        }
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
            if (task.getAssigneeId() != null && !task.getAssigneeId().isBlank()) {
                notificationService.createIfMissing(task.getAssigneeId(),
                        "Task status updated: " + task.getTitle(),
                        "Your task '" + task.getTitle() + "' status changed to " + newStatus.name() + ".");
            }
        });
    }

    public void checkTaskDeadlinesForAssignee(String assigneeId) {
        if (assigneeId == null || assigneeId.isBlank()) {
            return;
        }
        List<Task> tasks = taskRepository.getTasksByAssignee(assigneeId);
        for (Task task : tasks) {
            if (task.getDueDate() == null || task.getDueDate().isBlank()) {
                continue;
            }
            try {
                LocalDate dueDate = LocalDate.parse(task.getDueDate());
                LocalDate today = LocalDate.now();
                if (dueDate.isBefore(today)) {
                    notificationService.createIfMissing(assigneeId,
                            "Task overdue: " + task.getTitle(),
                            "Your task '" + task.getTitle() + "' was due on " + task.getDueDate()
                                    + ". Please update status.");
                } else if (!dueDate.isAfter(today.plusDays(3))) {
                    notificationService.createIfMissing(assigneeId,
                            "Task due soon: " + task.getTitle(),
                            "Your task '" + task.getTitle() + "' is due on " + task.getDueDate() + ".");
                }
            } catch (Exception ignored) {
            }
        }
    }

    // ── DELETE TASK ───────────────────────────────────────────────
    public void deleteTask(String id) {
        taskRepository.delete(id);
    }
}
