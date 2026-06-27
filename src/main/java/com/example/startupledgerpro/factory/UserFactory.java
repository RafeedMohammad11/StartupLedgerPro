package com.example.startupledgerpro.factory;

import com.example.startupledgerpro.model.Admin;
import com.example.startupledgerpro.model.Employee;
import com.example.startupledgerpro.model.Manager;
import com.example.startupledgerpro.model.User;
import com.example.startupledgerpro.model.enums.UserRole;

public class UserFactory {

    public static User create(String id, String name, String email, String passwordHash, UserRole role, String phone, boolean isActive) {
        return switch (role) {
            case ADMIN -> new Admin(id, name, email, passwordHash, phone, isActive);
            case MANAGER -> new Manager(id, name, email, passwordHash, phone, isActive);
            case EMPLOYEE -> new Employee(id, name, email, passwordHash, phone, isActive);
        };
    }
}