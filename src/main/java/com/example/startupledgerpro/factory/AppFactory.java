package com.example.startupledgerpro.factory;

import com.example.startupledgerpro.repository.NotificationRepository;
import com.example.startupledgerpro.repository.ProjectRepository;
import com.example.startupledgerpro.repository.TaskRepository;
import com.example.startupledgerpro.repository.UserRepository;
import com.example.startupledgerpro.repository.impl.SqliteNotificationRepository;
import com.example.startupledgerpro.repository.impl.SqliteProjectRepository;
import com.example.startupledgerpro.repository.impl.SqliteTaskRepository;
import com.example.startupledgerpro.repository.impl.SqliteUserRepository;
import com.example.startupledgerpro.service.AuthService;
import com.example.startupledgerpro.service.NotificationService;
import com.example.startupledgerpro.service.QuotationService;
import com.example.startupledgerpro.service.TaskService;
import com.example.startupledgerpro.service.UserService;
import com.example.startupledgerpro.service.ProjectService;

public class AppFactory {

    // ── Repositories ─────────────────────────────────────────────
    public static final UserRepository userRepository = new SqliteUserRepository();
    public static final ProjectRepository projectRepository = new SqliteProjectRepository();
    public static final TaskRepository taskRepository = new SqliteTaskRepository();
    public static final NotificationRepository notificationRepository = new SqliteNotificationRepository();

    // ── Services ─────────────────────────────────────────────────
    public static final AuthService authService = new AuthService(userRepository);
    public static final UserService userService = new UserService(userRepository);
    public static final NotificationService notificationService = new NotificationService(notificationRepository);
    public static final ProjectService projectService = new ProjectService(projectRepository, notificationService);
    public static final TaskService taskService = new TaskService(taskRepository, notificationService);
    public static final QuotationService quotationService = new QuotationService();

    // Prevent instantiation
    private AppFactory() {
    }
}