package com.example.startupledgerpro.service;

import com.example.startupledgerpro.exception.DuplicateEmailException;
import com.example.startupledgerpro.exception.EntityNotFoundException;
import com.example.startupledgerpro.exception.ValidationException;
import com.example.startupledgerpro.factory.UserFactory;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.model.enums.UserRole;
import com.example.startupledgerpro.repository.UserRepository;
import com.example.startupledgerpro.util.IdGenerator;
import com.example.startupledgerpro.util.PasswordUtil;
import com.example.startupledgerpro.util.Validator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String name, String email, String password, UserRole role, String phone) {
        return createUser(name, email, password, role, phone, LocalDate.now().toString());
    }

    public User createUser(String name, String email, String password, UserRole role, String phone, String joinDate) {
        Validator.requireValid(Validator.validateName(name));
        Validator.requireValid(Validator.validateEmail(email));
        Validator.requireValid(Validator.validatePassword(password));
        Validator.requireValid(Validator.validatePhone(phone));
        if (role == null) {
            throw new ValidationException("role", "Role is required.");
        }

        String normalizedEmail = email.trim();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        if (joinDate == null || joinDate.isBlank()) {
            joinDate = LocalDate.now().toString();
        }

        String id = generateUniqueUserId(role);
        String passwordHash = PasswordUtil.hash(password);

        User user = UserFactory.create(id, name.trim(), normalizedEmail, passwordHash, role, phone, joinDate, true);
        userRepository.save(user);

        return user;
    }

    public User updateUser(String userId, String name, String email, String phone, UserRole role,
            String joinDate, boolean active) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        Validator.requireValid(Validator.validateName(name));
        Validator.requireValid(Validator.validateEmail(email));
        Validator.requireValid(Validator.validatePhone(phone));
        if (role == null) {
            throw new ValidationException("role", "Role is required.");
        }
        if (joinDate == null || joinDate.isBlank()) {
            throw new ValidationException("joinDate", "Joining date is required.");
        }
        Validator.requireValid(Validator.validateDate(joinDate, "Joining date"));

        String normalizedEmail = email.trim();
        if (!existing.getEmail().equalsIgnoreCase(normalizedEmail)
                && userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        User updated = UserFactory.create(
                existing.getId(),
                name.trim(),
                normalizedEmail,
                existing.getPasswordHash(),
                role,
                phone,
                joinDate.trim(),
                active);
        userRepository.save(updated);
        return updated;
    }

    private String generateUniqueUserId(UserRole role) {
        String id;
        do {
            id = IdGenerator.generateUserId(role);
        } while (userRepository.findById(id).isPresent());
        return id;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUserByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public void deactivateUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        user.setActive(false);
        userRepository.save(user);
    }

    public void resetPassword(String userId, String newPassword) {
        Validator.requireValid(Validator.validatePassword(newPassword));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        User updated = UserFactory.create(
                user.getId(),
                user.getName(),
                user.getEmail(),
                PasswordUtil.hash(newPassword),
                user.getRole(),
                user.getPhone(),
                user.getJoinDate(),
                user.isActive());
        userRepository.save(updated);
    }
}
