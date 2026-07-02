package com.example.startupledgerpro.util;

import com.example.startupledgerpro.exception.ValidationException;

import java.util.regex.Pattern;

public final class Validator {

    // ── Patterns ──────────────────────────────────────────────────
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^01[3-9]\\d{8}$"); // Bangladesh: 01XXXXXXXXX

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z][A-Za-z\\s]{1,99}$");

    private static final Pattern PROJECT_NAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9][A-Za-z0-9\\s\\-]{2,99}$");

    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    private static final Pattern DATE_PATTERN =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$"); // YYYY-MM-DD

    // Prevent instantiation — utility class
    private Validator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void requireValid(ValidationResult result) {
        if (!result.isValid()) {
            throw new ValidationException(result.getErrorMessage());
        }
    }

    public static void requireValid(String field, ValidationResult result) {
        if (!result.isValid()) {
            throw new ValidationException(field, result.getErrorMessage());
        }
    }

    // ── Name ──────────────────────────────────────────────────────
    public static ValidationResult validateName(String name) {
        if (name == null || name.isBlank())
            return ValidationResult.invalid("Name cannot be empty.");
        if (name.trim().length() < 2)
            return ValidationResult.invalid("Name must be at least 2 characters.");
        if (!NAME_PATTERN.matcher(name.trim()).matches())
            return ValidationResult.invalid("Name must contain only letters and spaces.");
        return ValidationResult.valid();
    }

    // ── Email ─────────────────────────────────────────────────────
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.isBlank())
            return ValidationResult.invalid("Email cannot be empty.");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches())
            return ValidationResult.invalid("Enter a valid email address (e.g. name@company.com).");
        return ValidationResult.valid();
    }

    // ── Password ──────────────────────────────────────────────────
    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isBlank())
            return ValidationResult.invalid("Password cannot be empty.");
        if (password.length() < 8)
            return ValidationResult.invalid("Password must be at least 8 characters.");
        if (!PASSWORD_PATTERN.matcher(password).matches())
            return ValidationResult.invalid(
                    "Password must contain uppercase, lowercase, a number, and a special character (@$!%*?&)."
            );
        return ValidationResult.valid();
    }

    // ── Phone (Bangladesh) ────────────────────────────────────────
    public static ValidationResult validatePhone(String phone) {
        if (phone == null || phone.isBlank())
            return ValidationResult.valid(); // Phone is optional
        if (!PHONE_PATTERN.matcher(phone.trim()).matches())
            return ValidationResult.invalid("Phone must be a valid Bangladeshi number (e.g. 01XXXXXXXXX).");
        return ValidationResult.valid();
    }

    // ── Budget (the case you mentioned) ───────────────────────────
    public static ValidationResult validateBudget(String budgetText) {
        if (budgetText == null || budgetText.isBlank())
            return ValidationResult.invalid("Budget is required.");
        if (!AMOUNT_PATTERN.matcher(budgetText.trim()).matches())
            return ValidationResult.invalid("Budget must be a valid positive number (e.g. 50000 or 50000.00).");
        double value = Double.parseDouble(budgetText.trim());
        if (value <= 0)
            return ValidationResult.invalid("Budget must be greater than zero.");
        if (value > 99_999_999)
            return ValidationResult.invalid("Budget cannot exceed Tk 9,99,99,999.");
        return ValidationResult.valid();
    }

    // ── Amount (for expenses / revenue) ───────────────────────────
    public static ValidationResult validateAmount(String amountText, String fieldName) {
        String label = (fieldName == null || fieldName.isBlank()) ? "Amount" : fieldName;
        if (amountText == null || amountText.isBlank())
            return ValidationResult.invalid(label + " is required.");
        if (!AMOUNT_PATTERN.matcher(amountText.trim()).matches())
            return ValidationResult.invalid(label + " must be a valid positive number.");
        double value = Double.parseDouble(amountText.trim());
        if (value <= 0)
            return ValidationResult.invalid(label + " must be greater than zero.");
        return ValidationResult.valid();
    }

    // Overload — validate a pre-parsed double directly
    public static ValidationResult validateAmount(double amount, String fieldName) {
        String label = (fieldName == null || fieldName.isBlank()) ? "Amount" : fieldName;
        if (amount < 0)
            return ValidationResult.invalid(label + " cannot be negative.");
        if (amount == 0)
            return ValidationResult.invalid(label + " must be greater than zero.");
        return ValidationResult.valid();
    }

    // ── Project name ──────────────────────────────────────────────
    public static ValidationResult validateProjectName(String name) {
        if (name == null || name.isBlank())
            return ValidationResult.invalid("Project name is required.");
        if (name.trim().length() < 3)
            return ValidationResult.invalid("Project name must be at least 3 characters.");
        if (!PROJECT_NAME_PATTERN.matcher(name.trim()).matches())
            return ValidationResult.invalid("Project name can only contain letters, numbers, spaces, and hyphens.");
        return ValidationResult.valid();
    }

    // ── Date (YYYY-MM-DD) ─────────────────────────────────────────
    public static ValidationResult validateDate(String date, String fieldName) {
        String label = (fieldName == null || fieldName.isBlank()) ? "Date" : fieldName;
        if (date == null || date.isBlank())
            return ValidationResult.invalid(label + " is required.");
        if (!DATE_PATTERN.matcher(date.trim()).matches())
            return ValidationResult.invalid(label + " must be in YYYY-MM-DD format.");
        try {
            java.time.LocalDate.parse(date.trim());
        } catch (Exception e) {
            return ValidationResult.invalid(label + " is not a valid date.");
        }
        return ValidationResult.valid();
    }

    // ── Future date (deadline must not be in the past) ────────────
    public static ValidationResult validateFutureDate(String date, String fieldName) {
        ValidationResult basic = validateDate(date, fieldName);
        if (!basic.isValid()) return basic;
        java.time.LocalDate parsed = java.time.LocalDate.parse(date.trim());
        if (!parsed.isAfter(java.time.LocalDate.now()))
            return ValidationResult.invalid(
                    (fieldName != null ? fieldName : "Date") + " must be a future date."
            );
        return ValidationResult.valid();
    }
}