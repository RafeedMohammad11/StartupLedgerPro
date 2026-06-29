package com.example.startupledgerpro.factory;

import com.example.startupledgerpro.repository.ProjectRepository;
import com.example.startupledgerpro.repository.UserRepository;
import com.example.startupledgerpro.repository.impl.SqliteProjectRepository;
import com.example.startupledgerpro.repository.impl.SqliteUserRepository;
import com.example.startupledgerpro.service.AuthService;
import com.example.startupledgerpro.service.UserService;
import com.example.startupledgerpro.service.ProjectService;

public class AppFactory {

    // ── Repositories ─────────────────────────────────────────────
    public static final UserRepository    userRepository    = new SqliteUserRepository();
    public static final ProjectRepository projectRepository = new SqliteProjectRepository();

    // ── Services ─────────────────────────────────────────────────
    public static final AuthService    authService    = new AuthService(userRepository);
    public static final UserService    userService    = new UserService(userRepository);
    public static final ProjectService projectService = new ProjectService(projectRepository);

    // Prevent instantiation
    private AppFactory() {}
}