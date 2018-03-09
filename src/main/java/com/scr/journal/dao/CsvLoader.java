package com.scr.journal.dao;

import com.scr.journal.model.Journal;
import com.scr.journal.model.Journals;
import com.scr.journal.model.PaymentDirection;
import com.scr.journal.model.PaymentType;
import com.scr.journal.util.ConversionUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvLoader {

    private static final String DELIMITER = ";";
    private static final Pattern PATTERN_AMOUNT = Pattern.compile("^(-?\\d+)(?:,\\d+)?$");

    private final Charset charset;

    public CsvLoader(String charset) {
        this.charset = Charset.forName(charset);
    }

    public Journals load(String importFilePath) {
        try (Stream<String> journalRows = Files.lines(Paths.get(importFilePath), charset)) {
            List<Journal> parseJournals = journalRows
                    .skip(1)
                    .map(row -> parseJournal(row))
                    .collect(Collectors.toList());
            return Journals.from(parseJournals);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Journal parseJournal(String row) {
        String[] fractions = row.split(DELIMITER);
        if (fractions.length < 9) {
            throw new IllegalStateException("Unexpected csv entry format");
        }
        String date = fractions[0];
        String address = fractions[3];
        String reason = fractions[8];
        String invoiceNumber = fractions[4];

        Matcher amountMatcher = PATTERN_AMOUNT.matcher(fractions[5]);
        if (!amountMatcher.matches()) {
            throw new IllegalStateException("Unexpected amount format");
        }
        long amount = ConversionUtils.convert(amountMatcher.group(1), Long.class);

        Journal journal = new Journal();
        journal.setDate(ConversionUtils.convert(date, LocalDate.class));
        journal.setPaymentType(PaymentType.BANK_TRANSFER);
        journal.setPaymentDirection(amount > 0 ? PaymentDirection.INCOMING : PaymentDirection.OUTGOING);
        journal.setInvoiceNumber(invoiceNumber);
        journal.setAmount(Math.abs(amount));
        journal.setAddress(address);
        journal.setReason(reason);
        return journal;
    }

}
