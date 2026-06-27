// FinancialRecord.java (Abstract Base)
package com.example.startupledgerpro.model;

public abstract class FinancialRecord {
    private final String id;
    private final String projectId;
    private final double amount;
    private final String description;
    private final String recordedBy;

    protected FinancialRecord(String id, String projectId, double amount, String description, String recordedBy) {
        this.id = id;
        this.projectId = projectId;
        this.amount = amount;
        this.description = description;
        this.recordedBy = recordedBy;
    }

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getRecordedBy() { return recordedBy; }

    public abstract String getType(); // Returns "EXPENSE" or "REVENUE"
}