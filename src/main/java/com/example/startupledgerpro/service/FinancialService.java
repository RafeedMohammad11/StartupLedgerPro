package com.example.startupledgerpro.service;

import com.example.startupledgerpro.exception.EntityNotFoundException;
import com.example.startupledgerpro.exception.InsufficientBudgetException;
import com.example.startupledgerpro.exception.ValidationException;
import com.example.startupledgerpro.model.ExpenseRecord;
import com.example.startupledgerpro.model.Project;
import com.example.startupledgerpro.repository.FinancialRepository;
import com.example.startupledgerpro.repository.ProjectRepository;
import com.example.startupledgerpro.util.Validator;

import java.util.List;
import java.util.UUID;

public class FinancialService {

    private final FinancialRepository financialRepository;
    private final ProjectRepository projectRepository;

    public FinancialService(FinancialRepository financialRepository, ProjectRepository projectRepository) {
        this.financialRepository = financialRepository;
        this.projectRepository = projectRepository;
    }

    public ExpenseRecord recordExpense(String projectId, double amount, String description,
            String category, String recordedBy) {
        Validator.requireValid(Validator.validateAmount(amount, "Expense amount"));
        if (recordedBy == null || recordedBy.isBlank()) {
            throw new ValidationException("recordedBy", "Recorded-by user is required.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project", projectId));

        double spent = financialRepository.sumExpensesByProject(projectId);
        double remaining = project.getBudget() - spent;
        if (amount > remaining) {
            throw new InsufficientBudgetException(remaining, amount);
        }

        ExpenseRecord record = new ExpenseRecord(
                "exp-" + UUID.randomUUID().toString().substring(0, 8),
                projectId,
                amount,
                description,
                recordedBy,
                category);
        return financialRepository.saveExpense(record);
    }

    public double getRemainingBalance(String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project", projectId));
        return project.getBudget() - financialRepository.sumExpensesByProject(projectId);
    }

    public double getRemainingBalanceForProjects(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            return 0.0;
        }
        double allocatedFunding = projects.stream().mapToDouble(Project::getBudget).sum();
        double approvedExpenseCosts = projects.stream()
                .mapToDouble(project -> financialRepository.sumExpensesByProject(project.getId()))
                .sum();
        return allocatedFunding - approvedExpenseCosts;
    }
}
