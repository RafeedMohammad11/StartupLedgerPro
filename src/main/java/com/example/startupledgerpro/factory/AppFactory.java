package com.example.startupledgerpro.factory;

import com.example.startupledgerpro.repository.UserRepository;
import com.example.startupledgerpro.repository.impl.SqliteUserRepository;

public class AppFactory {
    private static UserRepository userRepository;

    // Singleton provider for the UserRepository instance
    public static synchronized UserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = new SqliteUserRepository();
        }
        return userRepository;
    }
}