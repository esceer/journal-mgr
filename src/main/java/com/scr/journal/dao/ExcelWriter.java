package com.scr.journal.dao;

import com.scr.journal.model.Journal;
import com.scr.journal.model.Journals;
import com.scr.journal.model.PaymentDirection;
import com.scr.journal.model.PaymentType;
import com.scr.journal.util.ConversionUtils;
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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExcelWriter {

    public void save(String exportFilePath, Journals data, ResourceBundle resourceBundle) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("2018");

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

        Map<PaymentType, String> internalPaymentTypeMapping =new HashMap<PaymentType, String>() {{
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
                    .addCell(journal::getAmount)
                    .addCell(journal::getComment)
                    .addCell(journal::getAddress)
                    .addCell(journal::getExpenseType);
        }


        try (OutputStream outputStream = Files.newOutputStream(Paths.get(exportFilePath),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class RowBuilder {
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
    }

}
