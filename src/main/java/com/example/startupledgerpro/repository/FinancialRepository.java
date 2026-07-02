package com.example.startupledgerpro.repository;

import com.example.startupledgerpro.model.ExpenseRecord;

public interface FinancialRepository {
    double sumExpensesByProject(String projectId);

    ExpenseRecord saveExpense(ExpenseRecord record);
}
