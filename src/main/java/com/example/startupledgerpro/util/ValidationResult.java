package com.example.startupledgerpro.util;

public class ValidationResult {
    private final boolean isValid;
    private final String errorMessage;

    public ValidationResult(boolean isValid, String errorMessage)
    {
        this.isValid = isValid;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {return isValid;}
    public String getErrorMessage() {return errorMessage;}

    public static ValidationResult valid() {
        return new ValidationResult(true, "");
    }

    public static ValidationResult invalid(String message){
        return new ValidationResult(false, message);
    }
}
