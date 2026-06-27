package com.example.startupledgerpro.model;

import com.example.startupledgerpro.model.enums.UserRole;

public abstract class User {
    private final String id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final String phone;
    private boolean isActive;

    protected User(String id, String name, String email, String passwordHash, UserRole role, String phone, boolean isActive) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.phone = phone;
        this.isActive = isActive;
    }

    // --- CRITICAL GETTERS FOR THE REPOSITORY LAYER ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public String getPhone() { return phone; }
    public boolean isActive() { return isActive; }

    public void setActive(boolean active) { this.isActive = active; }

    // Polymorphic UI Hook for Day 2
    public abstract String getDashboardFxmlPath();
}