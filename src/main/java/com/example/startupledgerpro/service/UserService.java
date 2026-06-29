package com.example.startupledgerpro.service;

import com.example.startupledgerpro.factory.UserFactory;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.model.enums.UserRole;
import com.example.startupledgerpro.repository.UserRepository;
import com.example.startupledgerpro.util.PasswordUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    //-----CREATE USER------------------------
    public User createUser(String name, String email, String password, UserRole role, String phone){
        //check for duplicate email
        if(userRepository.findByEmail(email).isPresent())
        {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        String id = "user-" + UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = PasswordUtil.hash(password);

        User user = UserFactory.create(id, name, email, passwordHash, role, phone, true);
        userRepository.save(user);

        return user;
    }

    //-----GET ALL USERS---------------
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    //------GET BY ROLE----------------
    public List<User> getUserByRole(UserRole role){
        return userRepository.findByRole(role);
    }

    // ── GET BY ID ─────────────────────────────────────────────────
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    // ── DEACTIVATE ────────────────────────────────────────────────
    public void deactivateUser(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setActive(false);
            userRepository.save(user);
        });
    }

    // ── RESET PASSWORD ────────────────────────────────────────────
    public void resetPassword(String userId, String newPassword) {
        userRepository.findById(userId).ifPresent(user -> {
            // Can't directly set passwordHash — User is immutable on that field
            // Recreate user with new hash via UserFactory
            User updated = UserFactory.create(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    PasswordUtil.hash(newPassword),
                    user.getRole(),
                    user.getPhone(),
                    user.isActive()
            );
            userRepository.save(updated);
        });
    }
}
