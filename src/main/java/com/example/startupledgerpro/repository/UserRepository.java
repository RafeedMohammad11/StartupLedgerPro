package com.example.startupledgerpro.repository;

import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.model.enums.UserRole;

import java.util.List;
import java.util.Optional;

// Extending the base generic Repository with concrete types <User, String>
public interface UserRepository extends Repository<User, String> {

    Optional<User> findById(String id);

    // Custom query definitions specific to user actions
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
}