package com.example.startupledgerpro.exception;

public class InsufficientBudgetException extends RuntimeException {
    private final double budget;
    private final double requested;

    public InsufficientBudgetException(double budget, double requested) {
        super(String.format(
                "Amount Tk %.2f exceeds remaining project budget of Tk %.2f.",
                requested, budget
        ));
        this.budget    = budget;
        this.requested = requested;
    }

    public double getBudget()    { return budget; }
    public double getRequested() { return requested; }
}