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
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class JournalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JournalController.class);
    private static final String DEFAULT_EXPORTED_EXCEL_FILE_NAME = "report.xlsx";

    private volatile Mode currentMode = Mode.NONE;

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
    private Journal clipboardedJournal;

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
            if (newValue != null && oldValue != null && !newValue.equals(oldValue)) {
                selectYearTab(ConversionUtils.convert(newValue.getId(), Year.class));
                reloadJournals();
            }
        });
        journalTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (isMode(Mode.EDIT) && journalTableView.getSelectionModel().getSelectedIndex() >= 0) {
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

        infoLabel.setText("Search date format set to 'year/month/day'");
    }

    @FXML
    protected void handleSetShortSearchDateFormat(ActionEvent event) {
        SettingsRegistry.get().setSearchDateFormat(SearchDateFormat.SHORT);

        flipRadioMenu(searchShortDateFormatMenu, true);
        flipRadioMenu(searchFullDateFormatMenu, false);

        infoLabel.setText("Search date format set to 'year/month'");
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
        switch (currentMode) {
            case EDIT:
                if (clipboardedJournal != null) {
                    Journal editedJournal = createJournal();
                    journalRegistry.replace(clipboardedJournal, editedJournal);
                    infoLabel.setText("Edited journal");
                }
                break;
            case COPY:
            case NONE:
                Journal journal = createJournal();
                journalRegistry.add(journal);
                infoLabel.setText("New journal created");
                break;
            default:
                throw new IllegalStateException("Unexpected mode '" + currentMode + "'");
        }

        rebuildJournalTabs();
        Optional.ofNullable(datePicker.getValue()).ifPresent(localDate ->
                selectYearTab(Year.of(localDate.getYear())));

        resetControls();
    }

    @FXML
    protected void handleEditClicked(ActionEvent event) {
        mirrorFields();
        enterEditingMode();
    }

    @FXML
    protected void handleCopyClicked(ActionEvent event) {
        mirrorFields();
        enterCopyMode();
    }

    @FXML
    protected void handleDeleteClicked(ActionEvent event) {
        int selectedIndex = journalTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Journal selectedJournal = observableJournals.get(selectedIndex);
            journalRegistry.remove(selectedJournal);
            infoLabel.setText("Deleted journal");

            rebuildJournalTabs();
            reloadJournals();

            if (!isMode(Mode.EDIT) && !isMode(Mode.COPY)) {
                filterJournals();
            }
        } else {
            throw new IllegalArgumentException("Failed to delete journal as no journal was selected");
        }
    }

    @FXML
    protected void handleSearchClicked(ActionEvent event) {
        Optional.ofNullable(datePicker.getValue()).ifPresent(localDate ->
                selectYearTab(Year.of(localDate.getYear())));

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
    protected void handleExportMonthEndForSeason(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Excel files", "*.xlsx"));
        fileChooser.setInitialFileName(DEFAULT_EXPORTED_EXCEL_FILE_NAME);
        File exportFilePath = fileChooser.showSaveDialog(null);
        if (exportFilePath != null) {
            Year thisYear = getCurrentSelectedYear();
            Year lastYear = thisYear.minusYears(1);
            List<YearMonth> season = Arrays.asList(
                    lastYear.atMonth(Month.MARCH),
                    lastYear.atMonth(Month.APRIL),
                    lastYear.atMonth(Month.MAY),
                    lastYear.atMonth(Month.JUNE),
                    lastYear.atMonth(Month.JULY),
                    lastYear.atMonth(Month.AUGUST),
                    lastYear.atMonth(Month.SEPTEMBER),
                    lastYear.atMonth(Month.OCTOBER),
                    lastYear.atMonth(Month.NOVEMBER),
                    lastYear.atMonth(Month.DECEMBER),
                    thisYear.atMonth(Month.JANUARY),
                    thisYear.atMonth(Month.FEBRUARY)
            );

            Collection<Journal> allJournals = journalRegistry.getJournals();
            List<Journal> filteredJournals = allJournals.stream()
                    .filter(journal -> season.contains(YearMonth.from(journal.getDate())))
                    .sorted()
                    .collect(Collectors.toList());

            excelWriter.saveMonthEndBooking(
                    String.format("%s-%s", lastYear.toString(), thisYear.toString()),
                    exportFilePath.getPath(),
                    Journals.from(filteredJournals),
                    true);
            infoLabel.setText("Successfully exported month end booking for the current season");
        }
    }

    @FXML
    protected void handleExportMonthEndForCalendarYear(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Excel files", "*.xlsx"));
        fileChooser.setInitialFileName(DEFAULT_EXPORTED_EXCEL_FILE_NAME);
        File exportFilePath = fileChooser.showSaveDialog(null);
        if (exportFilePath != null) {
            Year thisYear = getCurrentSelectedYear();

            Collection<Journal> allJournals = journalRegistry.getJournals();
            List<Journal> filteredJournals = allJournals.stream()
                    .filter(journal -> Year.from(journal.getDate()).equals(thisYear))
                    .sorted()
                    .collect(Collectors.toList());

            excelWriter.saveMonthEndBooking(
                    thisYear.toString(),
                    exportFilePath.getPath(),
                    Journals.from(filteredJournals),
                    false);
            infoLabel.setText("Successfully exported month end booking for the entire calendar year");
        }
    }

    @FXML
    protected void handleExportJournals(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.setInitialFileName(DEFAULT_EXPORTED_EXCEL_FILE_NAME);
        File exportFilePath = fileChooser.showSaveDialog(null);
        if (exportFilePath != null) {
            excelWriter.saveJournals(
                    getCurrentSelectedYear().toString(),
                    exportFilePath.getPath(),
                    Journals.from(observableJournals));
            infoLabel.setText("Successfully exported journals");
        }
    }

    @FXML
    protected void handleImport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Csv files", "*.csv"));
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
            clipboardedJournal = observableJournals.get(selectedIndex);
            datePicker.setValue(clipboardedJournal.getDate());
            paymentTypeComboBox.getSelectionModel().select(clipboardedJournal.getPaymentType());
            paymentDirectionGroup.selectToggle(
                    paymentDirectionGroup.getToggles()
                            .stream()
                            .filter(toggle -> toggle.getUserData().equals(clipboardedJournal.getPaymentDirection().getAssociatedValue()))
                            .findFirst()
                            .orElseThrow(IllegalArgumentException::new));
            invoiceNumberTextField.setText(clipboardedJournal.getInvoiceNumber());
            amountTextField.setText(ConversionUtils.convert(clipboardedJournal.getAmount()));
            commentTextField.setText(clipboardedJournal.getComment());
            addressTextField.setText(clipboardedJournal.getAddress());
            categoryComboBox.setValue(clipboardedJournal.getExpenseType());
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
        clipboardedJournal = null;

        rebuildJournalTabs();

        reloadJournals();
        resetMode();

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

    private Year getCurrentSelectedYear() {
        Tab selectedItem = journalTabPane.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            return ConversionUtils.convert(selectedItem.getId(), Year.class);
        } else {
            Collection<Year> distinctYears = journalRegistry.getDistinctYears();
            return distinctYears.size() > 0
                    ? distinctYears.iterator().next()
                    : Year.now();
        }
    }

    private void rebuildJournalTabs() {
        Year selectedYear = getCurrentSelectedYear();
        journalTabPane.getTabs().clear();

        for (Year year : journalRegistry.getDistinctYears()) {
            String yearStr = ConversionUtils.convert(year);
            Tab tab = new Tab(yearStr);
            tab.setId(yearStr);
            tab.setTooltip(getMonthEndBalancesTooltipFor(year));
            journalTabPane.getTabs().add(tab);
        }

        selectYearTab(selectedYear);
    }

    private void selectYearTab(Year year) {
        Optional<Tab> previousTab = getTabFor(getCurrentSelectedYear());
        previousTab.ifPresent(tab -> tab.setContent(null));

        Optional<Tab> chosenTab = getTabFor(year);
        chosenTab.ifPresent(tab -> {
            tab.setContent(journalTabBorderPane);
            journalTabPane.getSelectionModel().select(tab);
        });
    }

    private Optional<Tab> getTabFor(Year year) {
        String yearStr = ConversionUtils.convert(year);
        Optional<Tab> foundTab = journalTabPane.getTabs().stream()
                .filter(tab -> tab.getId().equals(yearStr))
                .findFirst();
        if (foundTab.isPresent()) {
            return foundTab;
        } else {
            return journalTabPane.getTabs().stream()
                    .findFirst();
        }
    }

    private void reloadJournals() {
        Collection<Journal> allJournals = journalRegistry.getJournals();
        List<Journal> currentYearFilteredJournals = allJournals.stream()
                .filter(journal -> Year.of(journal.getDate().getYear()).equals(getCurrentSelectedYear()))
                .collect(Collectors.toList());
        observableJournals = FXCollections.observableArrayList(currentYearFilteredJournals);
        journalTableView.setItems(observableJournals);
    }

    private Tooltip getMonthEndBalancesTooltipFor(Year year) {
        Map<Month, Long> monthEndBalanceMap = new TreeMap<>(Comparator.comparingInt(Month::getValue));

        Collection<Journal> allJournals = journalRegistry.getJournals();
        allJournals.stream()
                .filter(journal -> Year.of(journal.getDate().getYear()).equals(year))
                .sorted()
                .forEach(journal -> {
                    Month month = Month.from(journal.getDate());
                    Long balance = monthEndBalanceMap.computeIfAbsent(month, key -> 0L);
                    monthEndBalanceMap.put(month, balance + journal.getSignedAmount());
                });

        Locale locale = SettingsRegistry.get().getLocale();

        StringBuilder tooltipBuilder = new StringBuilder();
        monthEndBalanceMap.forEach((month, balance) ->
                tooltipBuilder
                        .append(month.getDisplayName(TextStyle.FULL_STANDALONE, locale))
                        .append(": ")
                        .append(numberFormat.format(balance))
                        .append("\n"));
        return new Tooltip(tooltipBuilder.toString());
    }

    private void enterEditingMode() {
        currentMode = Mode.EDIT;
        infoLabel.setText("Editing journal...");
    }

    private void enterCopyMode() {
        currentMode = Mode.COPY;
        infoLabel.setText("Copying journal...");
    }

    private void resetMode() {
        infoLabel.setText(null);
        currentMode = Mode.NONE;
    }

    private boolean isMode(Mode mode) {
        return currentMode == mode;
    }

    private static void flipRadioMenu(RadioMenuItem radioMenuItem, boolean select) {
//        radioMenuItem.setDisable(select);
        radioMenuItem.setSelected(select);
    }

    private enum Mode {
        EDIT,
        COPY,
        NONE
    }

}
