package com.example.startupledgerpro.service;

import com.example.startupledgerpro.model.Quotation;
import com.example.startupledgerpro.model.QuotationItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class QuotationService {

    public Quotation createQuotation(String projectId, String projectName) {
        Quotation quotation = new Quotation(projectId, projectName);
        quotation.addItem(new QuotationItem("Hosting", 1000.00, "Year"));
        quotation.addItem(new QuotationItem("Development Cost", 400000.00, "One-time"));
        quotation.addItem(new QuotationItem("Domain Charge", 1200.00, "Year"));
        return quotation;
    }

    public File exportToPdf(Quotation quotation, File outputFile) throws Exception {
        Objects.requireNonNull(quotation, "Quotation must not be null");
        Objects.requireNonNull(outputFile, "Output file must not be null");

        String html = buildHtml(quotation);
        try (OutputStream out = new FileOutputStream(outputFile)) {
            out.write(html.getBytes(StandardCharsets.UTF_8));
        }
        return outputFile;
    }

    private String buildHtml(Quotation quotation) {
        StringBuilder rows = new StringBuilder();
        for (QuotationItem item : quotation.getItems()) {
            rows.append("<tr>")
                    .append("<td>").append(item.getDescription()).append("</td>")
                    .append("<td>").append(item.getFrequency()).append("</td>")
                    .append("<td style='text-align:right;'>").append(String.format("Tk %,.2f", item.getAmount()))
                    .append("</td>")
                    .append("</tr>");
        }

        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><title>Quotation</title>" +
                "<style>body{font-family:Arial,sans-serif;margin:0;padding:24px;color:#111827;}" +
                "table{width:100%;border-collapse:collapse;margin-top:16px;}" +
                "th,td{padding:12px 14px;border:1px solid #E5E7EB;}" +
                "th{background:#F8FAFC;text-align:left;color:#111827;}" +
                "h1{font-size:24px;margin-bottom:4px;}" +
                ".summary{margin-top:24px;font-size:16px;font-weight:bold;}" +
                "</style></head><body>" +
                "<h1>Quotation for " + quotation.getProjectName() + "</h1>" +
                "<p>Project ID: " + quotation.getProjectId() + "</p>" +
                "<table><thead><tr><th>Description</th><th>Frequency</th><th>Amount</th></tr></thead><tbody>" +
                rows +
                "</tbody></table>" +
                "<div class='summary'>Total: " + String.format("Tk %,.2f", quotation.getTotal()) + "</div>" +
                "</body></html>";
    }
}