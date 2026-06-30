package com.example.startupledgerpro.repository;

import com.example.startupledgerpro.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends Repository<Task, String> {

    /**
     * 💡 Contract method to filter task tracks by their specific project parent token
     */
    List<Task> findByProjectId(String projectId);
    List<Task> getTasksByAssignee(String assigneeId);
}