package com.example.startupledgerpro.model;

import com.example.startupledgerpro.model.enums.UserRole;

public class Manager extends User {

    public Manager(String id, String name, String email, String passwordHash, String phone, String joinDate,
            boolean isActive) {
        super(id, name, email, passwordHash, UserRole.MANAGER, phone, joinDate, isActive);
    }

    @Override
    public String getDashboardFxmlPath() {
        return "/fxml/manager/manager-dashboard.fxml";
    }
}