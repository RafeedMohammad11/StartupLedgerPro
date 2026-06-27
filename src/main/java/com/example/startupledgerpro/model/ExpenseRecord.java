// ExpenseRecord.java
package com.example.startupledgerpro.model;

public class ExpenseRecord extends FinancialRecord {
    private final String category;

    public ExpenseRecord(String id, String projectId, double amount, String description, String recordedBy, String category) {
        super(id, projectId, amount, description, recordedBy);
        this.category = category;
    }

    public String getCategory() { return category; }

    @Override
    public String getType() { return "EXPENSE"; }
}