package com.scr.journal.controllers;

import com.scr.journal.dao.CsvLoader;
import com.scr.journal.dao.ExcelWriter;
import com.scr.journal.model.Journal;
import com.scr.journal.model.Journals;
import com.scr.journal.model.PaymentDirection;
import com.scr.journal.model.PaymentType;
import com.scr.journal.util.DateUtil;
import com.scr.journal.util.JournalRegistry;
import com.scr.journal.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Collectors;

public class JournalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JournalController.class);
    private static final String DEFAULT_EXPORTED_EXCEL_FILE_NAME = "report.xlsx";

    @FXML private Label infoLabel;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<PaymentType> paymentTypeComboBox;
    @FXML private ToggleGroup paymentDirectionGroup;
    @FXML private TextField invoiceNumberTextField;
    @FXML private TextField amountTextField;
    @FXML private TextField reasonTextField;
    @FXML private TextField addressTextField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TableView<Journal> journalTableView;

    private final JournalRegistry journalRegistry;
    private final CsvLoader csvLoader;
    private final ExcelWriter excelWriter;

    private ObservableList<Journal> observableJournals;

    public JournalController(JournalRegistry journalRegistry, CsvLoader csvLoader, ExcelWriter excelWriter) {
        this.journalRegistry = journalRegistry;
        this.csvLoader = csvLoader;
        this.excelWriter = excelWriter;
    }

    @FXML
    public void initialize() {
        // Set input fields to their default values
        resetControls();

        // Add change listeners
        amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^\\d*$")) {
                amountTextField.setText(oldValue);
            }
        });

        Thread.setDefaultUncaughtExceptionHandler((thread, cause) -> {
            LOGGER.error("Exception on thread: " + thread.getName(), cause);
            infoLabel.setText("Exception: " + ValidationUtil.getRootCause(cause).getMessage());
        });
    }

    @FXML
    protected void handleAddClicked(ActionEvent event) {
        Journal journal = createJournal();
        journalRegistry.add(journal);

        resetControls();

        infoLabel.setText("New journal created");
    }

    @FXML
    protected void handleDeleteClicked(ActionEvent event) {
        int selectedIndex = journalTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Journal selectedJournal = observableJournals.get(selectedIndex);
            journalRegistry.remove(selectedJournal);
            infoLabel.setText("Deleted journal");

            reloadJournals();
            filterJournals();
        } else {
            throw new IllegalArgumentException("Failed to delete journal as no journal was selected");
        }
    }

    @FXML
    protected void handleSearchClicked(ActionEvent event) {
        infoLabel.setText("Filtering journals...");
        reloadJournals();
        filterJournals();
        infoLabel.setText("Filtering journals... Finished");
    }

    @FXML
    protected void handleResetClicked(ActionEvent event) {
        resetControls();
        infoLabel.setText(null);
    }

    @FXML
    protected void handleExport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.setInitialFileName(DEFAULT_EXPORTED_EXCEL_FILE_NAME);
        File exportFilePath = fileChooser.showSaveDialog(null);
        excelWriter.save(exportFilePath.getPath(), Journals.from(observableJournals));
    }

    @FXML
    protected void handleImport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load file");
        File importFilePath = fileChooser.showOpenDialog(null);

        Journals journals = csvLoader.load(importFilePath.getPath());
        journalRegistry.add(journals.getJournals());

        resetControls();
    }

    private void filterJournals() {
        FilteredList<Journal> filteredJournals = new FilteredList<>(observableJournals);

        LocalDate date = datePicker.getValue();
        if (date != null) {
            filteredJournals = filteredJournals.filtered(journal -> journal.getDate().equals(DateUtil.toString(date)));
        }

        PaymentType paymentType = paymentTypeComboBox.getValue();
        if (paymentType != null) {
            filteredJournals = filteredJournals.filtered(journal -> journal.getPaymentType() == paymentType);
        }

        Toggle paymentDirectionToggle = paymentDirectionGroup.getSelectedToggle();
        if (paymentDirectionToggle != null) {
            PaymentDirection paymentDirection = PaymentDirection.tryParse(
                    Objects.toString(paymentDirectionToggle.getUserData()));
            if (paymentDirection != null) {
                filteredJournals = filteredJournals.filtered(journal -> journal.getPaymentDirection() == paymentDirection);
            }
        }

        String invoiceNumber = invoiceNumberTextField.getText();
        if (!ValidationUtil.isNullOrEmpty(invoiceNumber)) {
            filteredJournals = filteredJournals.filtered(journal -> journal.getInvoiceNumber().equalsIgnoreCase(invoiceNumber));
        }

        String amountStr = amountTextField.getText();
        if (!ValidationUtil.isNullOrEmpty(amountStr)) {
            filteredJournals = filteredJournals.filtered(journal -> journal.getAmount() == Long.parseLong(amountStr));
        }

        String reason = reasonTextField.getText();
        if (!ValidationUtil.isNullOrEmpty(reason)) {
            filteredJournals = filteredJournals.filtered(journal -> journal.getReason().equalsIgnoreCase(reason));
        }

        String address = addressTextField.getText();
        if (!ValidationUtil.isNullOrEmpty(address)) {
            filteredJournals = filteredJournals.filtered(journal -> journal.getAddress().equalsIgnoreCase(address));
        }

        String expenseType = categoryComboBox.getValue();
        if (!ValidationUtil.isNullOrEmpty(expenseType)) {
            filteredJournals = filteredJournals.filtered(journal -> journal.getExpenseType().equalsIgnoreCase(expenseType));
        }

        observableJournals = FXCollections.observableArrayList(filteredJournals);
        journalTableView.setItems(observableJournals);
    }

    private Journal createJournal() {
        String date = DateUtil.toString(datePicker.getValue());
        PaymentType paymentType = paymentTypeComboBox.getValue();
        PaymentDirection paymentDirection = paymentDirectionGroup.getSelectedToggle() != null
                ? PaymentDirection.tryParse(Objects.toString(paymentDirectionGroup.getSelectedToggle().getUserData()))
                : null;
        String invoiceNumber = invoiceNumberTextField.getText();
        long amount = Long.parseLong(amountTextField.getText());
        String reason = reasonTextField.getText();
        String address = addressTextField.getText();
        String expenseType = categoryComboBox.getValue();

        Journal journal = new Journal(
                date,
                paymentType,
                paymentDirection,
                invoiceNumber,
                amount,
                reason,
                address,
                expenseType);

        validateJournal(journal);
        return journal;
    }

    private void validateJournal(Journal journal) {
        boolean invalid = ValidationUtil.isNullOrEmpty(
                journal.getDate(),
                journal.getPaymentType(),
                journal.getPaymentDirection(),
                journal.getInvoiceNumber(),
                journal.getAmount(),
                journal.getExpenseType());
        if (invalid) {
            throw new IllegalArgumentException("Journal contains invalid parameter(s): " + journal);
        }
    }

    private void resetControls() {
        reloadJournals();

        journalTableView.setItems(observableJournals);
        datePicker.setValue(null);
        paymentTypeComboBox.setItems(FXCollections.observableArrayList(PaymentType.values()));
        paymentTypeComboBox.getSelectionModel().clearSelection();
        paymentDirectionGroup.getToggles().forEach(toggle -> toggle.setSelected(false));
        invoiceNumberTextField.clear();
        amountTextField.clear();
        reasonTextField.clear();
        addressTextField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        categoryComboBox.setItems(
                FXCollections.observableList(
                        observableJournals.stream()
                                .map(Journal::getExpenseType)
                                .distinct()
                                .sorted()
                                .collect(Collectors.toList()))
        );
        categoryComboBox.setValue(null);
    }

    private void reloadJournals() {
        observableJournals = FXCollections.observableArrayList(journalRegistry.getJournals());
    }

}
