package com.example.startupledgerpro.service;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.repository.UserRepository;
import com.example.startupledgerpro.session.SessionManager;
import com.example.startupledgerpro.util.PasswordUtil;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository = AppFactory.getUserRepository();
    private final SessionManager sessionManager = SessionManager.getInstance();

    /**
     * Pure business authentication service method.
     * Takes raw text inputs from the controller, checks them, and updates state.
     * * @param email The user's input email
     * @param password The user's input raw password
     * @return A LoginResult indicating success status and any error messages
     */
    public LoginResult login(String email, String password) {
        // 1. Fetch user by email from SQLite repository
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return new LoginResult(false, "No account found with that email.");
        }

        User user = userOpt.get();

        // 2. Securely verify the password hash using utilities
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            return new LoginResult(false, "Invalid password.");
        }

        // 3. Verify administrative lifecycle rules
        if (!user.isActive()) {
            return new LoginResult(false, "Account deactivated.");
        }

        // 4. Authentication success! Save to global singleton state tracking
        sessionManager.login(user);

        return new LoginResult(true, "Success");
    }
}