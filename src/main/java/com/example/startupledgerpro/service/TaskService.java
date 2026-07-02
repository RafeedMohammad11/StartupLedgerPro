package com.example.startupledgerpro.service;

import com.example.startupledgerpro.exception.EntityNotFoundException;
import com.example.startupledgerpro.exception.ValidationException;
import com.example.startupledgerpro.model.Task;
import com.example.startupledgerpro.model.enums.TaskStatus;
import com.example.startupledgerpro.repository.TaskRepository;
import com.example.startupledgerpro.util.Validator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TaskService {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    public Task createTask(String projectId, String title, String description,
            String assigneeId, String dueDate) {
        if (projectId == null || projectId.isBlank()) {
            throw new ValidationException("project", "Project is required.");
        }
        if (title == null || title.isBlank()) {
            throw new ValidationException("title", "Task title is required.");
        }
        Validator.requireValid(Validator.validateDate(dueDate, "Due date"));
        if (assigneeId == null || assigneeId.isBlank()) {
            throw new ValidationException("assignee", "Assignee is required.");
        }

        Task task = new Task.Builder()
                .id("tsk-" + UUID.randomUUID().toString().substring(0, 8))
                .projectId(projectId)
                .title(title.trim())
                .description(description)
                .assigneeId(assigneeId)
                .status(TaskStatus.TODO)
                .dueDate(dueDate.trim())
                .build();

        taskRepository.save(task);
        notificationService.createIfMissing(assigneeId,
                "New task assigned: " + title,
                "You have been assigned a new task '" + title + "' for project " + projectId + ".");
        return task;
    }

    public List<Task> getTasksByProject(String projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByAssignee(String assigneeId) {
        return taskRepository.getTasksByAssignee(assigneeId);
    }

    public Optional<Task> getTaskById(String id) {
        return taskRepository.findById(id);
    }

    public void updateTaskStatus(String taskId, TaskStatus newStatus) {
        if (newStatus == null) {
            throw new ValidationException("status", "Task status is required.");
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task", taskId));
        task.setStatus(newStatus);
        taskRepository.save(task);
        if (task.getAssigneeId() != null && !task.getAssigneeId().isBlank()) {
            notificationService.createIfMissing(task.getAssigneeId(),
                    "Task status updated: " + task.getTitle(),
                    "Your task '" + task.getTitle() + "' status changed to " + newStatus.name() + ".");
        }
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

    public void deleteTask(String id) {
        if (taskRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Task", id);
        }
        taskRepository.delete(id);
    }
}
