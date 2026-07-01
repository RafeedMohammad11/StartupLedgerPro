package com.example.startupledgerpro.controller;

import com.example.startupledgerpro.factory.AppFactory;
import com.example.startupledgerpro.model.Project;
import com.example.startupledgerpro.model.Quotation;
import com.example.startupledgerpro.model.QuotationItem;
import com.example.startupledgerpro.service.QuotationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.io.File;

public class QuotationGeneratorController {

    @FXML
    private Label projectTitleLabel;
    @FXML
    private Label projectIdLabel;
    @FXML
    private TableView<QuotationItem> quotationTableView;
    @FXML
    private TableColumn<QuotationItem, String> colDescription;
    @FXML
    private TableColumn<QuotationItem, String> colFrequency;
    @FXML
    private TableColumn<QuotationItem, Double> colAmount;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private Button savePdfButton;

    private Project project;
    private Quotation quotation;
    private final QuotationService quotationService = AppFactory.quotationService;

    @FXML
    public void initialize() {
        quotationTableView.setEditable(true);

        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDescription.setCellFactory(TextFieldTableCell.forTableColumn());
        colDescription.setOnEditCommit(event -> {
            QuotationItem item = event.getRowValue();
            item.setDescription(event.getNewValue());
            quotationTableView.refresh();
        });

        colFrequency.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        colFrequency.setCellFactory(TextFieldTableCell.forTableColumn());
        colFrequency.setOnEditCommit(event -> {
            QuotationItem item = event.getRowValue();
            item.setFrequency(event.getNewValue());
            quotationTableView.refresh();
        });

        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colAmount.setOnEditCommit(event -> {
            QuotationItem item = event.getRowValue();
            item.setAmount(event.getNewValue() == null ? 0.0 : event.getNewValue());
            quotationTableView.refresh();
            updateTotal();
        });
    }

    public void setProjectContext(Project project) {
        this.project = project;
        projectTitleLabel.setText(project.getName());
        projectIdLabel.setText("Project ID: " + project.getId());
        loadQuotation();
    }

    private void loadQuotation() {
        quotation = quotationService.createQuotation(project.getId(), project.getName());
        ObservableList<QuotationItem> items = FXCollections.observableList(quotation.getItems());
        quotationTableView.setItems(items);
        updateTotal();
    }

    private void updateTotal() {
        double total = quotationTableView.getItems().stream()
                .mapToDouble(QuotationItem::getAmount)
                .sum();
        totalAmountLabel.setText(String.format("Total: Tk %,.2f", total));
    }

    @FXML
    private void handleAddLineItem() {
        QuotationItem newItem = new QuotationItem("New item", 0.0, "One-time");
        quotationTableView.getItems().add(newItem);
        quotationTableView.getSelectionModel().select(newItem);
        updateTotal();
    }

    @FXML
    private void handleRemoveSelectedItem() {
        QuotationItem selected = quotationTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            quotationTableView.getItems().remove(selected);
            updateTotal();
        }
    }

    @FXML
    private void handleExportPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Quotation PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        chooser.setInitialFileName(project.getName().replaceAll("\\s+", "_") + "_quotation.pdf");

        File target = chooser.showSaveDialog(projectTitleLabel.getScene().getWindow());
        if (target == null) {
            return;
        }

        try {
            quotationService.exportToPdf(quotation, target);
            savePdfButton.setText("Saved");
            savePdfButton.setDisable(true);
        } catch (Exception e) {
            e.printStackTrace();
            savePdfButton.setText("Export failed");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) projectTitleLabel.getScene().getWindow();
        stage.close();
    }
}