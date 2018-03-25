package com.scr.journal.controllers;

import com.scr.journal.UILoader;
import com.scr.journal.dao.CsvLoader;
import com.scr.journal.dao.ExcelWriter;
import com.scr.journal.model.Journal;
import com.scr.journal.model.Journals;
import com.scr.journal.model.PaymentDirection;
import com.scr.journal.model.PaymentType;
import com.scr.journal.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class JournalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JournalController.class);
    private static final String DEFAULT_EXPORTED_EXCEL_FILE_NAME = "report.xlsx";

    private volatile boolean editingMode = false;

    @FXML
    private RadioMenuItem languageHuMenu;
    @FXML
    private RadioMenuItem languageEnMenu;
    @FXML
    private RadioMenuItem searchFullDateFormatMenu;
    @FXML
    private RadioMenuItem searchShortDateFormatMenu;

    @FXML
    private Label infoLabel;
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
    private TextField commentTextField;
    @FXML
    private TextField addressTextField;
    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TabPane journalTabPane;
    @FXML
    private BorderPane journalTabBorderPane;
    @FXML
    private TableView<Journal> journalTableView;
    @FXML
    private TableColumn<Journal, LocalDate> dateColumn;
    @FXML
    private TableColumn<Journal, PaymentType> paymentTypeColumn;
    @FXML
    private TableColumn<Journal, PaymentDirection> paymentDirectionColumn;
    @FXML
    private TableColumn<Journal, Long> amountColumn;

    private final UILoader uiLoader;
    private final JournalRegistry journalRegistry;
    private final CsvLoader csvLoader;
    private final ExcelWriter excelWriter;
    private final NumberFormat numberFormat;

    private ObservableList<Journal> observableJournals;

    public JournalController(
            UILoader uiLoader,
            JournalRegistry journalRegistry,
            CsvLoader csvLoader,
            ExcelWriter excelWriter,
            NumberFormat numberFormat) {
        this.uiLoader = uiLoader;
        this.journalRegistry = journalRegistry;
        this.csvLoader = csvLoader;
        this.excelWriter = excelWriter;
        this.numberFormat = numberFormat;
    }

    @FXML
    public void initialize() {
        // Get the current resource bundle
        ResourceBundle resourceBundle = uiLoader.getResourceBundle();

        // Set input fields to their default values
        resetControls();

        // Set custom format for a few cells
        dateColumn.setCellFactory(column ->
                new TableCell<Journal, LocalDate>() {
                    @Override
                    protected void updateItem(LocalDate item, boolean empty) {
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(ConversionUtils.convert(item));
                        }
                    }
                }
        );
        amountColumn.setCellFactory(column ->
                new TableCell<Journal, Long>() {
                    @Override
                    protected void updateItem(Long item, boolean empty) {
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(numberFormat.format(item));
                        }
                    }
                }
        );
        paymentDirectionColumn.setCellFactory(column ->
                new TableCell<Journal, PaymentDirection>() {
                    @Override
                    protected void updateItem(PaymentDirection item, boolean empty) {
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            switch (item) {
                                case INCOMING:
                                    setText(resourceBundle.getString("data.payment_direction.incoming"));
                                    break;
                                case OUTGOING:
                                    setText(resourceBundle.getString("data.payment_direction.outgoing"));
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unknown payment direction");
                            }
                        }
                    }
                }
        );
        paymentTypeColumn.setCellFactory(column ->
                new TableCell<Journal, PaymentType>() {
                    @Override
                    protected void updateItem(PaymentType item, boolean empty) {
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            switch (item) {
                                case BANK_TRANSFER:
                                    setText(resourceBundle.getString("data.payment_type.bank_transfer"));
                                    break;
                                case CASH:
                                    setText(resourceBundle.getString("data.payment_type.cash"));
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unknown payment type");
                            }
                        }
                    }
                }
        );
        paymentTypeComboBox.setConverter(new StringConverter<PaymentType>() {
            private Map<PaymentType, String> internalPaymentTypeMapping = new HashMap<PaymentType, String>() {{
                put(PaymentType.BANK_TRANSFER, resourceBundle.getString("data.payment_type.bank_transfer"));
                put(PaymentType.CASH, resourceBundle.getString("data.payment_type.cash"));
            }};

            @Override
            public String toString(PaymentType paymentType) {
                return internalPaymentTypeMapping.getOrDefault(paymentType, null);
            }

            @Override
            public PaymentType fromString(String str) {
                return internalPaymentTypeMapping
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().equals(str))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElseThrow(IllegalArgumentException::new);
            }
        });

        // Add change listeners
        journalTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectYear(ConversionUtils.convert(newValue.getId(), Year.class));
            reloadJournals();
        });
        journalTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (editingMode) {
                mirrorFields();
            }
        });
        amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^\\d*$")) {
                amountTextField.setText(oldValue);
            }
        });

        // Set common error handling
        Thread.setDefaultUncaughtExceptionHandler((thread, cause) -> {
            LOGGER.error("Exception on thread: " + thread.getName(), cause);
            infoLabel.setText("Exception: " + ValidationUtils.getRootCause(cause).getMessage());
        });
    }

    @FXML
    protected void handleSetHungarianLanguage(ActionEvent event) {
        SettingsRegistry.get().setLocale("hu", "HU");
        uiLoader.setResourceBundle(SettingsRegistry.get().getResourceBundle());
        uiLoader.reload();

        flipRadioMenu(languageHuMenu, true);
        flipRadioMenu(languageEnMenu, false);

        infoLabel.setText("Language set to Hungarian");
    }

    @FXML
    protected void handleSetEnglishLanguage(ActionEvent event) {
        SettingsRegistry.get().setLocale("en", "EN");
        uiLoader.setResourceBundle(SettingsRegistry.get().getResourceBundle());
        uiLoader.reload();

        flipRadioMenu(languageEnMenu, true);
        flipRadioMenu(languageHuMenu, false);

        infoLabel.setText("Language set to English");
    }

    @FXML
    protected void handleSetFullSearchDateFormat(ActionEvent event) {
        SettingsRegistry.get().setSearchDateFormat(SearchDateFormat.FULL);

        flipRadioMenu(searchFullDateFormatMenu, true);
        flipRadioMenu(searchShortDateFormatMenu, false);

        infoLabel.setText("Search date format set to 'full'");
    }

    @FXML
    protected void handleSetShortSearchDateFormat(ActionEvent event) {
        SettingsRegistry.get().setSearchDateFormat(SearchDateFormat.SHORT);

        flipRadioMenu(searchShortDateFormatMenu, true);
        flipRadioMenu(searchFullDateFormatMenu, false);

        infoLabel.setText("Search date format set to 'short'");
    }

    @FXML
    protected void handleLoadBackup(ActionEvent event) {
        ResourceBundle resourceBundle = uiLoader.getResourceBundle();
        Optional<ButtonType> userResponse = AlertBuilder
                .alert(Alert.AlertType.CONFIRMATION)
                .withTitle(resourceBundle.getString("dialog.backup.title"))
                .withHeader(resourceBundle.getString("dialog.backup.header"))
                .withContent(resourceBundle.getString("dialog.backup.content"))
                .showAndWait();
        if (userResponse.get() == ButtonType.OK) {
            journalRegistry.resetToBackup();
            resetControls();
            infoLabel.setText("Successfully loaded backup data");
        }
    }

    @FXML
    protected void handleSaveClicked(ActionEvent event) {
        int selectedIndex = journalTableView.getSelectionModel().getSelectedIndex();
        if (editingMode && selectedIndex >= 0) {
            Journal editedJournal = createJournal();
            Journal selectedJournal = observableJournals.get(selectedIndex);
            journalRegistry.replace(selectedJournal, editedJournal);

            infoLabel.setText("Edited journal");
        } else {
            Journal journal = createJournal();
            journalRegistry.add(journal);

            infoLabel.setText("New journal created");
        }
        resetControls();
    }

    @FXML
    protected void handleEditClicked(ActionEvent event) {
        mirrorFields();
        infoLabel.setText("Editing journal...");
        enterEditingMode();
    }

    @FXML
    protected void handleCopyClicked(ActionEvent event) {
        mirrorFields();
        infoLabel.setText("Copying journal...");
        cancelEditingMode();
    }

    @FXML
    protected void handleDeleteClicked(ActionEvent event) {
        cancelEditingMode();

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
        cancelEditingMode();

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
        if (exportFilePath != null) {
            excelWriter.save(exportFilePath.getPath(), Journals.from(observableJournals), SettingsRegistry.get().getResourceBundle());
            infoLabel.setText("Successfully exported journals");
        }
    }

    @FXML
    protected void handleImport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load file");
        File importFilePath = fileChooser.showOpenDialog(null);

        if (importFilePath != null) {
            Journals journals = csvLoader.load(importFilePath.getPath());
            journalRegistry.add(journals.getJournals());

            resetControls();
            infoLabel.setText("Successfully imported journals");
        }
    }

    @FXML
    protected void handleAbout(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/esceer/journal-mgr"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mirrorFields() {
        int selectedIndex = journalTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Journal selectedJournal = observableJournals.get(selectedIndex);
            datePicker.setValue(selectedJournal.getDate());
            paymentTypeComboBox.getSelectionModel().select(selectedJournal.getPaymentType());
            paymentDirectionGroup.selectToggle(
                    paymentDirectionGroup.getToggles()
                            .stream()
                            .filter(toggle -> toggle.getUserData().equals(selectedJournal.getPaymentDirection().getAssociatedValue()))
                            .findFirst()
                            .orElseThrow(IllegalArgumentException::new));
            invoiceNumberTextField.setText(selectedJournal.getInvoiceNumber());
            amountTextField.setText(ConversionUtils.convert(selectedJournal.getAmount()));
            commentTextField.setText(selectedJournal.getComment());
            addressTextField.setText(selectedJournal.getAddress());
            categoryComboBox.setValue(selectedJournal.getExpenseType());
        } else {
            throw new IllegalArgumentException("Failed to mirror journal as no journal was selected");
        }
    }

    private void filterJournals() {
        FilteredList<Journal> filteredJournals = new FilteredList<>(observableJournals);

        LocalDate date = datePicker.getValue();
        if (date != null) {
            switch (SettingsRegistry.get().getSearchDateFormat()) {
                case FULL:
                    filteredJournals = filteredJournals.filtered(journal ->
                            journal.getDate().isEqual(date));
                    break;
                case SHORT:
                    filteredJournals = filteredJournals.filtered(journal ->
                            YearMonth.from(journal.getDate()).equals(YearMonth.from(date)));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown SearchDateFormat");
            }
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
        if (!ValidationUtils.isNullOrEmpty(invoiceNumber)) {
            filteredJournals = filteredJournals.filtered(journal -> journal.getInvoiceNumber().equalsIgnoreCase(invoiceNumber));
        }

        String amountStr = amountTextField.getText();
        if (!ValidationUtils.isNullOrEmpty(amountStr)) {
            filteredJournals = filteredJournals.filtered(journal -> journal.getAmount() == Long.parseLong(amountStr));
        }

        String comment = commentTextField.getText();
        if (!ValidationUtils.isNullOrEmpty(comment)) {
            filteredJournals = filteredJournals.filtered(journal -> journal.getComment().equalsIgnoreCase(comment));
        }

        String address = addressTextField.getText();
        if (!ValidationUtils.isNullOrEmpty(address)) {
            filteredJournals = filteredJournals.filtered(journal -> journal.getAddress().equalsIgnoreCase(address));
        }

        String expenseType = categoryComboBox.getValue();
        if (!ValidationUtils.isNullOrEmpty(expenseType)) {
            filteredJournals = filteredJournals.filtered(journal -> journal.getExpenseType().equalsIgnoreCase(expenseType));
        }

        observableJournals = FXCollections.observableArrayList(filteredJournals);
        journalTableView.setItems(observableJournals);
    }

    private Journal createJournal() {
        LocalDate date = datePicker.getValue();
        PaymentType paymentType = paymentTypeComboBox.getValue();
        PaymentDirection paymentDirection = paymentDirectionGroup.getSelectedToggle() != null
                ? PaymentDirection.tryParse(Objects.toString(paymentDirectionGroup.getSelectedToggle().getUserData()))
                : null;
        String invoiceNumber = invoiceNumberTextField.getText();
        long amount = Long.parseLong(amountTextField.getText());
        String comment = commentTextField.getText();
        String address = addressTextField.getText();
        String expenseType = categoryComboBox.getValue();

        Journal journal = new Journal(
                date,
                paymentType,
                paymentDirection,
                invoiceNumber,
                amount,
                comment,
                address,
                expenseType);

        validateJournal(journal);
        return journal;
    }

    private void validateJournal(Journal journal) {
        boolean invalid = ValidationUtils.isNullOrEmpty(
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
        rebuildJournalTabs();

        reloadJournals();
        cancelEditingMode();

        journalTableView.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        paymentTypeComboBox.setItems(FXCollections.observableArrayList(PaymentType.values()));
        paymentTypeComboBox.getSelectionModel().clearSelection();
        paymentDirectionGroup.getToggles().forEach(toggle -> toggle.setSelected(false));
        invoiceNumberTextField.clear();
        amountTextField.clear();
        commentTextField.clear();
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

        String currentLanguage = uiLoader.getResourceBundle().getLocale().getLanguage();
        flipRadioMenu(languageEnMenu, currentLanguage.equalsIgnoreCase("en"));
        flipRadioMenu(languageHuMenu, currentLanguage.equalsIgnoreCase("hu"));

        SearchDateFormat searchDateFormat = SettingsRegistry.get().getSearchDateFormat();
        flipRadioMenu(searchFullDateFormatMenu, SearchDateFormat.FULL == searchDateFormat);
        flipRadioMenu(searchShortDateFormatMenu, SearchDateFormat.SHORT == searchDateFormat);
    }

    private Year getSelectedYear() {
        String selectedTabId = journalTabPane.getSelectionModel().getSelectedItem().getId();
        if (selectedTabId != null) {
            return ConversionUtils.convert(selectedTabId, Year.class);
        } else {
            Collection<Year> distinctYears = journalRegistry.getDistinctYears();
            return distinctYears.size() > 0
                    ? distinctYears.iterator().next()
                    : Year.now();
        }
    }

    private void rebuildJournalTabs() {
        Year selectedYear = getSelectedYear();
        journalTabPane.getTabs().clear();

        for (Year year : journalRegistry.getDistinctYears()) {
            String yearStr = ConversionUtils.convert(year);
            Tab tab = new Tab(yearStr);
            tab.setId(yearStr);
            journalTabPane.getTabs().add(tab);
        }

        selectYear(selectedYear);
    }

    private void selectYear(Year year) {
        Tab previousTab = getTabFor(getSelectedYear());
        previousTab.setContent(null);
        Tab chosenTab = getTabFor(year);
        chosenTab.setContent(journalTabBorderPane);
        journalTabPane.getSelectionModel().select(chosenTab);
    }

    private Tab getTabFor(Year year) {
        String yearStr = ConversionUtils.convert(year);
        return journalTabPane.getTabs().stream()
                .filter(tab -> tab.getId().equals(yearStr))
                .findFirst()
                .<IllegalArgumentException>orElseThrow(() -> {
                    throw new IllegalArgumentException("The desired year '" + yearStr + "' is not found in the tab list");
                });
    }

    private void reloadJournals() {
        Collection<Journal> allJournals = journalRegistry.getJournals();
        List<Journal> currentYearFilteredJournals = allJournals.stream()
                .filter(journal -> Year.of(journal.getDate().getYear()).equals(getSelectedYear()))
                .collect(Collectors.toList());
        observableJournals = FXCollections.observableArrayList(currentYearFilteredJournals);
        journalTableView.setItems(observableJournals);
    }

    private void enterEditingMode() {
        editingMode = true;
    }

    private void cancelEditingMode() {
        editingMode = false;
    }

    private static void flipRadioMenu(RadioMenuItem radioMenuItem, boolean select) {
//        radioMenuItem.setDisable(select);
        radioMenuItem.setSelected(select);
    }

}
