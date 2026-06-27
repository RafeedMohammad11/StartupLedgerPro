package com.example.startupledgerpro.model;

import com.example.startupledgerpro.model.enums.UserRole;

public class Admin extends User {

    public Admin(String id, String name, String email, String passwordHash, String phone, boolean isActive) {
        super(id, name, email, passwordHash, UserRole.ADMIN, phone, isActive);
    }

    @Override
    public String getDashboardFxmlPath() {
        return "/fxml/admin/admin-dashboard.fxml";
    }
}