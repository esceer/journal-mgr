package com.scr.journal.dao;

import com.scr.journal.model.Journal;
import com.scr.journal.model.Journals;
import com.scr.journal.model.PaymentDirection;
import com.scr.journal.model.PaymentType;
import com.scr.journal.util.ConversionUtils;
import com.scr.journal.util.MonthProvider;
import com.scr.journal.util.SettingsRegistry;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExcelWriter {

    private final NumberFormat numberFormat;

    public ExcelWriter(NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public void saveMonthEndBooking(String outputFilePath, Journals data, boolean shiftSeason) {
        MonthProvider monthProvider = MonthProvider.create();
        if (shiftSeason) {
            // Start from March to next year's February
            monthProvider.shiftMonths(2);
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Month end booking");

        Map<String, Map<Month, Long>> expenseTypeMonthEndBalanceMap = new TreeMap<>();

        Collection<Journal> journals = data.getJournals();
        journals.stream()
                .sorted()
                .forEach(journal -> {
                    String expenseType = journal.getExpenseType();
                    Map<Month, Long> monthEndBalanceMap = expenseTypeMonthEndBalanceMap.computeIfAbsent(expenseType, key -> new TreeMap<>());

                    Month month = Month.from(journal.getDate());
                    Long balance = monthEndBalanceMap.computeIfAbsent(month, key -> 0L);

                    monthEndBalanceMap.put(month, balance + journal.getSignedAmount());
                });

        ResourceBundle resourceBundle = SettingsRegistry.get().getResourceBundle();
        Locale locale = resourceBundle.getLocale();

        RowBuilder rowBuilder = new RowBuilder(sheet);
        rowBuilder.addRow()
                .addCell(resourceBundle.getString("label.expense_type").toLowerCase())
                .addCell(monthProvider.first().getDisplayName(TextStyle.FULL_STANDALONE, locale))
                .addCell(monthProvider.second().getDisplayName(TextStyle.FULL_STANDALONE, locale))
                .addCell(monthProvider.third().getDisplayName(TextStyle.FULL_STANDALONE, locale))
                .addCell(monthProvider.fourth().getDisplayName(TextStyle.FULL_STANDALONE, locale))
                .addCell(monthProvider.fifth().getDisplayName(TextStyle.FULL_STANDALONE, locale))
                .addCell(monthProvider.sixth().getDisplayName(TextStyle.FULL_STANDALONE, locale))
                .addCell(monthProvider.seventh().getDisplayName(TextStyle.FULL_STANDALONE, locale))
                .addCell(monthProvider.eighth().getDisplayName(TextStyle.FULL_STANDALONE, locale))
                .addCell(monthProvider.ninth().getDisplayName(TextStyle.FULL_STANDALONE, locale))
                .addCell(monthProvider.tenth().getDisplayName(TextStyle.FULL_STANDALONE, locale))
                .addCell(monthProvider.eleventh().getDisplayName(TextStyle.FULL_STANDALONE, locale))
                .addCell(monthProvider.twelfth().getDisplayName(TextStyle.FULL_STANDALONE, locale));

        expenseTypeMonthEndBalanceMap.forEach((expenseType, monthEndBalanceMap) -> {
            rowBuilder.addRow()
                    .addCell(expenseType)
                    .addMoneyCell(monthEndBalanceMap.getOrDefault(monthProvider.first(), 0L))
                    .addMoneyCell(monthEndBalanceMap.getOrDefault(monthProvider.second(), 0L))
                    .addMoneyCell(monthEndBalanceMap.getOrDefault(monthProvider.third(), 0L))
                    .addMoneyCell(monthEndBalanceMap.getOrDefault(monthProvider.fourth(), 0L))
                    .addMoneyCell(monthEndBalanceMap.getOrDefault(monthProvider.fifth(), 0L))
                    .addMoneyCell(monthEndBalanceMap.getOrDefault(monthProvider.sixth(), 0L))
                    .addMoneyCell(monthEndBalanceMap.getOrDefault(monthProvider.seventh(), 0L))
                    .addMoneyCell(monthEndBalanceMap.getOrDefault(monthProvider.eighth(), 0L))
                    .addMoneyCell(monthEndBalanceMap.getOrDefault(monthProvider.ninth(), 0L))
                    .addMoneyCell(monthEndBalanceMap.getOrDefault(monthProvider.tenth(), 0L))
                    .addMoneyCell(monthEndBalanceMap.getOrDefault(monthProvider.eleventh(), 0L))
                    .addMoneyCell(monthEndBalanceMap.getOrDefault(monthProvider.twelfth(), 0L));
        });

        Map<Month, Long> totalMonthEndBalanceMap = new TreeMap<>(Comparator.comparingInt(Month::getValue));
        journals.stream()
                .sorted()
                .forEach(journal -> {
                    Month month = Month.from(journal.getDate());
                    Long balance = totalMonthEndBalanceMap.computeIfAbsent(month, key -> 0L);
                    totalMonthEndBalanceMap.put(month, balance + journal.getSignedAmount());
                });

        rowBuilder.skipRow()
                .addRow()
                .addCell("sum")
                .addMoneyCell(totalMonthEndBalanceMap.getOrDefault(monthProvider.first(), 0L))
                .addMoneyCell(totalMonthEndBalanceMap.getOrDefault(monthProvider.second(), 0L))
                .addMoneyCell(totalMonthEndBalanceMap.getOrDefault(monthProvider.third(), 0L))
                .addMoneyCell(totalMonthEndBalanceMap.getOrDefault(monthProvider.fourth(), 0L))
                .addMoneyCell(totalMonthEndBalanceMap.getOrDefault(monthProvider.fifth(), 0L))
                .addMoneyCell(totalMonthEndBalanceMap.getOrDefault(monthProvider.sixth(), 0L))
                .addMoneyCell(totalMonthEndBalanceMap.getOrDefault(monthProvider.seventh(), 0L))
                .addMoneyCell(totalMonthEndBalanceMap.getOrDefault(monthProvider.eighth(), 0L))
                .addMoneyCell(totalMonthEndBalanceMap.getOrDefault(monthProvider.ninth(), 0L))
                .addMoneyCell(totalMonthEndBalanceMap.getOrDefault(monthProvider.tenth(), 0L))
                .addMoneyCell(totalMonthEndBalanceMap.getOrDefault(monthProvider.eleventh(), 0L))
                .addMoneyCell(totalMonthEndBalanceMap.getOrDefault(monthProvider.twelfth(), 0L));

        long totalBalance = totalMonthEndBalanceMap.values()
                .stream()
                .reduce((b1, b2) -> b1 + b2)
                .orElse(0L);
        rowBuilder.skipRow()
                .addRow()
                .addCell("total")
                .addMoneyCell(totalBalance);

        writeOutput(workbook, outputFilePath);
    }

    public void saveJournals(String outputFilePath, Journals data) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Journals");

        ResourceBundle resourceBundle = SettingsRegistry.get().getResourceBundle();

        RowBuilder rowBuilder = new RowBuilder(sheet);
        rowBuilder.addRow()
                .addCell(resourceBundle.getString("label.date"))
                .addCell(resourceBundle.getString("label.payment_type"))
                .addCell(resourceBundle.getString("label.payment_direction"))
                .addCell(resourceBundle.getString("label.invoice_number"))
                .addCell(resourceBundle.getString("label.amount"))
                .addCell(resourceBundle.getString("label.comment"))
                .addCell(resourceBundle.getString("label.address"))
                .addCell(resourceBundle.getString("label.expense_type"));

        Map<PaymentType, String> internalPaymentTypeMapping = new HashMap<PaymentType, String>() {{
            put(PaymentType.BANK_TRANSFER, resourceBundle.getString("data.payment_type.bank_transfer"));
            put(PaymentType.CASH, resourceBundle.getString("data.payment_type.cash"));
        }};
        Map<PaymentDirection, String> internalPaymentDirectionMapping = new HashMap<PaymentDirection, String>() {{
            put(PaymentDirection.INCOMING, resourceBundle.getString("data.payment_direction.incoming"));
            put(PaymentDirection.OUTGOING, resourceBundle.getString("data.payment_direction.outgoing"));
        }};

        for (Journal journal : data.getJournals()) {
            rowBuilder.addRow()
                    .addCell(journal::getDate)
                    .addCell(journal::getPaymentType, internalPaymentTypeMapping::get)
                    .addCell(journal::getPaymentDirection, internalPaymentDirectionMapping::get)
                    .addCell(journal::getInvoiceNumber)
                    .addMoneyCell(journal::getAmount)
                    .addCell(journal::getComment)
                    .addCell(journal::getAddress)
                    .addCell(journal::getExpenseType);
        }

        writeOutput(workbook, outputFilePath);
    }

    private static void writeOutput(Workbook workbook, String outputFilePath) {
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(outputFilePath),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private class RowBuilder {
        private final Sheet sheet;
        private Row currentRow = null;
        private int rowNum = 0;
        private int cellNum = 0;

        public RowBuilder(Sheet sheet) {
            this.sheet = sheet;
        }

        public RowBuilder addRow() {
            currentRow = sheet.createRow(rowNum++);
            cellNum = 0;
            return this;
        }

        public RowBuilder addMoneyCell(Supplier<Long> getter) {
            return addMoneyCell(getter.get());
        }

        public RowBuilder addMoneyCell(long value) {
            return addCell(() -> numberFormat.format(value));
        }

        public RowBuilder addCell(Supplier<?> getter) {
            return addCell(getter, ConversionUtils::convert);
        }

        public <T> RowBuilder addCell(Supplier<T> getter, Function<T, String> converter) {
            T value = getter.get();
            return addCell(converter.apply(value));
        }

        public RowBuilder addCell(String strValue) {
            Cell cell = currentRow.createCell(cellNum++);
            cell.setCellValue(strValue);
            return this;
        }

        public RowBuilder skipRow() {
            return addRow();
        }

        public RowBuilder skipCell() {
            return addCell("");
        }
    }

}
