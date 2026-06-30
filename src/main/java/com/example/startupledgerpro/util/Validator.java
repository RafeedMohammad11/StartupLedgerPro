package com.example.startupledgerpro.util;

import java.util.regex.Pattern;

public class Validator {
    private static final Pattern EMAIL_PATTERN  = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN  = Pattern.compile("^\\+?[0-9]{11,14}$");
    private static final Pattern ADMIN_FULL_NAME_PATTERN = Pattern.compile("^[A-Z ]{3,125}$");
    private static final Pattern ADMIN_EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern ADMIN_PHONE_PATTERN = Pattern.compile("^\\d{11}$");

    public static ValidationResult validateName(String name){
        if(name == null || name.trim().isEmpty()){
            return ValidationResult .invalid("Name cannot be empty.");
        }

        if (name.trim().length() < 3) {
            return ValidationResult.invalid("Name must be at least 3 characters.");
        }

        return ValidationResult.valid();
    }

    public static ValidationResult validateAdminFullName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.invalid("Full name cannot be empty.");
        }
        String normalized = name.trim();
        if (!ADMIN_FULL_NAME_PATTERN.matcher(normalized).matches()) {
            return ValidationResult.invalid("Full name must be 3-125 characters and contain only uppercase letters and spaces.");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.invalid("Email cannot be empty.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.invalid("Invalid email format.");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validateAdminEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.invalid("Email cannot be empty.");
        }
        if (!ADMIN_EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return ValidationResult.invalid("Enter a valid email address.");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validatePassword(String password) {
        if (password == null || password.length() < 6) {
            return ValidationResult.invalid("Password must be at least 6 characters long.");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return ValidationResult.valid(); // Optional in schema
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return ValidationResult.invalid("Invalid phone number format.");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validateAdminPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return ValidationResult.invalid("Phone number is required.");
        }
        if (!ADMIN_PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return ValidationResult.invalid("Phone number must be exactly 11 digits.");
        }
        return ValidationResult.valid();
    }

    public static ValidationResult validateAmount(double amount, String fieldName) {
        if (amount < 0) {
            return ValidationResult.invalid(fieldName + " cannot be negative.");
        }
        return ValidationResult.valid();
    }
}
