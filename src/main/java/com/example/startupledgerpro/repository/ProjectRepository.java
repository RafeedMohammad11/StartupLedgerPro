package com.example.startupledgerpro.repository;

import com.example.startupledgerpro.model.Project;
import com.example.startupledgerpro.model.enums.ProjectCategory;
import com.example.startupledgerpro.model.enums.ProjectStatus;

import java.util.List;

public interface ProjectRepository extends Repository<Project, String> {
    List<Project> findByManagerId(String managerId);
    List<Project> findByStatus(ProjectStatus status);
    List<Project> findByCategory(ProjectCategory category);
}