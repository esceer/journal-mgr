package com.scr.journal.dao;

import com.scr.journal.model.Journal;
import com.scr.journal.model.Journals;
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
import java.util.function.Supplier;

public class ExcelWriter {

    public void save(String exportFilePath, Journals data) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("2018");

        RowBuilder rowBuilder = new RowBuilder(sheet);
        rowBuilder.addRow()
                .addCell("Date")
                .addCell("Payment direction")
                .addCell("Payment type")
                .addCell("Invoice number")
                .addCell("Amount")
                .addCell("Reason")
                .addCell("Address")
                .addCell("Expense type");

        for (Journal journal : data.getJournals()) {
            rowBuilder.addRow()
                    .addCell(journal::getDate)
                    .addCell(journal::getPaymentDirection)
                    .addCell(journal::getPaymentType)
                    .addCell(journal::getInvoiceNumber)
                    .addCell(journal::getAmount)
                    .addCell(journal::getReason)
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
            return addCell(ConversionUtils.convert(getter.get()));
        }

        public RowBuilder addCell(String strValue) {
            Cell cell = currentRow.createCell(cellNum++);
            cell.setCellValue(strValue);
            return this;
        }
    }

}