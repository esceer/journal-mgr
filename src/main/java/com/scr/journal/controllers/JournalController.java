package com.scr.journal.controllers;

import com.scr.journal.dao.DataLoader;
import com.scr.journal.dao.DataPersister;
import com.scr.journal.model.Journal;
import com.scr.journal.model.Journals;
import com.scr.journal.model.PaymentDirection;
import com.scr.journal.model.PaymentType;
import com.scr.journal.util.DateUtil;
import com.scr.journal.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.Objects;

public class JournalController {

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<PaymentType> paymentTypeComboBox;

    @FXML
    private ToggleGroup paymentDirectionGroup;

    @FXML
    private TextField invoiceNumberTextField;

    @FXML
    private TextField amountTextField;

    @FXML
    private TextField reasonTextField;

    @FXML
    private TextField addressTextField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TableView<Journal> journalTableView;

    private final ObservableList<Journal> journals;

    private final DataPersister<Journals> journalPersister;

    public JournalController(DataLoader<Journals> journalLoader, DataPersister<Journals> journalPersister) {
        this.journals = FXCollections.observableArrayList(journalLoader.load().getJournals());
        this.journalPersister = journalPersister;
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
    }

    @FXML
    protected void handleAddClicked(ActionEvent event) {
        Journal journal = createJournal();
        journals.add(journal);
        journalPersister.persist(Journals.from(journals));

        resetControls();
    }

    @FXML
    protected void handleDeleteClicked(ActionEvent event) {
        // Todo: Implement
    }

    @FXML
    protected void handleSearchClicked(ActionEvent event) {
        FilteredList<Journal> filteredJournals = new FilteredList<>(FXCollections.observableArrayList(journals));

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

        journalTableView.setItems(FXCollections.observableArrayList(filteredJournals));
    }

    @FXML
    protected void handleResetClicked(ActionEvent event) {
        resetControls();
    }

    private Journal createJournal() {
        String date = DateUtil.toString(datePicker.getValue());
        PaymentType paymentType = paymentTypeComboBox.getValue();
        PaymentDirection paymentDirection = PaymentDirection.tryParse(
                Objects.toString(paymentDirectionGroup.getSelectedToggle().getUserData()));
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
        journalTableView.setItems(journals);
        datePicker.setValue(null);
        paymentTypeComboBox.setItems(FXCollections.observableArrayList(PaymentType.values()));
        paymentTypeComboBox.getSelectionModel().clearSelection();
        paymentDirectionGroup.getToggles().forEach(toggle -> toggle.setSelected(false));
        invoiceNumberTextField.clear();
        amountTextField.clear();
        reasonTextField.clear();
        addressTextField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        categoryComboBox.setValue(null);
    }

}
