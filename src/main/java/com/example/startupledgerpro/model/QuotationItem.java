package com.example.startupledgerpro.model;

public class QuotationItem {

    private String description;
    private double amount;
    private String frequency;

    public QuotationItem(String description, double amount, String frequency) {
        this.description = description;
        this.amount = amount;
        this.frequency = frequency;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getAmountText() {
        return String.format("Tk %,.2f", amount);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}