package com.example.startupledgerpro.factory;

import com.example.startupledgerpro.repository.UserRepository;
import com.example.startupledgerpro.repository.impl.SqliteUserRepository;
import com.example.startupledgerpro.service.AuthService;

public class AppFactory {
    private static UserRepository userRepository;

    public static final AuthService authService = new AuthService();

    // Singleton provider for the UserRepository instance
    public static synchronized UserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = new SqliteUserRepository();
        }
        return userRepository;
    }
}