package com.example.startupledgerpro.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Quotation {

    private final String projectId;
    private final String projectName;
    private final List<QuotationItem> items = new ArrayList<>();

    public Quotation(String projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public List<QuotationItem> getItems() {
        return items;
    }

    public void addItem(QuotationItem item) {
        if (item != null) {
            items.add(item);
        }
    }

    public double getTotal() {
        return items.stream().mapToDouble(QuotationItem::getAmount).sum();
    }
}