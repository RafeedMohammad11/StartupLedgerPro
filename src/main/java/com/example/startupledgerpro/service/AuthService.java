package com.example.startupledgerpro.service;

import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.repository.UserRepository;
import com.example.startupledgerpro.session.SessionManager;
import com.example.startupledgerpro.util.PasswordUtil;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;
    private final SessionManager sessionManager = SessionManager.getInstance();

    // Constructor injection — AppFactory passes the repo in
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResult login(String email, String password) {
        // 1. Fetch user by email
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return new LoginResult(false, "No account found with that email.");
        }

        User user = userOpt.get();

        // 2. Verify password hash
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            return new LoginResult(false, "Incorrect password.");
        }

        // 3. Check account is active
        if (!user.isActive()) {
            return new LoginResult(false, "This account has been deactivated.");
        }

        // 4. Set session
        sessionManager.login(user);
        return new LoginResult(true, "Success");
    }

    // ── Inner result class ────────────────────────────────────────
    public static class LoginResult {
        private final boolean success;
        private final String  message;

        public LoginResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String  getMessage() { return message; }
    }
}