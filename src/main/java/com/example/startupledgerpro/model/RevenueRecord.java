// RevenueRecord.java
package com.example.startupledgerpro.model;

public class RevenueRecord extends FinancialRecord {
    public RevenueRecord(String id, String projectId, double amount, String description, String recordedBy) {
        super(id, projectId, amount, description, recordedBy);
    }

    @Override
    public String getType() { return "REVENUE"; }
}