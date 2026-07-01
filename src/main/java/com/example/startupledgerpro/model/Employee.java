package com.example.startupledgerpro.model;

import com.example.startupledgerpro.model.enums.UserRole;

public class Employee extends User {

    public Employee(String id, String name, String email, String passwordHash, String phone, String joinDate,
            boolean isActive) {
        super(id, name, email, passwordHash, UserRole.EMPLOYEE, phone, joinDate, isActive);
    }

    @Override
    public String getDashboardFxmlPath() {
        return "/fxml/employee/employee-dashboard.fxml";
    }
}