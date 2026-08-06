package com.poliakov.taxplatform.processing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SyntheticCsvParser {

    public static final String VERSION = "synthetic-csv-v1";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final List<String> EXPECTED_HEADERS = Arrays.asList("document_number", "document_date", "currency", "amount");

    public List<CanonicalRecord> parse(Long jobId, Long companyId, InputStream inputStream) throws ParseException {
        List<CanonicalRecord> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new ParseException("INVALID_CSV_HEADER", "Empty CSV file");
            }

            validateHeader(headerLine);

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                records.add(parseRow(jobId, companyId, line, lineNumber));
            }
        } catch (IOException e) {
            throw new ParseException("READ_ERROR", "Failed to read CSV content: " + e.getMessage());
        }
        return records;
    }

    private void validateHeader(String headerLine) throws ParseException {
        String[] headers = headerLine.split(",");
        if (headers.length != EXPECTED_HEADERS.size()) {
            throw new ParseException("INVALID_CSV_HEADER", "Expected " + EXPECTED_HEADERS.size() + " columns but got " + headers.length);
        }
        for (int i = 0; i < EXPECTED_HEADERS.size(); i++) {
            if (!EXPECTED_HEADERS.get(i).equalsIgnoreCase(headers[i].trim())) {
                throw new ParseException("INVALID_CSV_HEADER", "Expected header '" + EXPECTED_HEADERS.get(i) + "' at column " + (i + 1));
            }
        }
    }

    private CanonicalRecord parseRow(Long jobId, Long companyId, String line, int lineNumber) throws ParseException {
        String[] columns = line.split(",", -1);
        if (columns.length != EXPECTED_HEADERS.size()) {
             throw new ParseException("INVALID_CSV_ROW", "Line " + lineNumber + ": Expected " + EXPECTED_HEADERS.size() + " columns but got " + columns.length);
        }

        String docNumber = columns[0].trim();
        String docDateStr = columns[1].trim();
        String currency = columns[2].trim();
        String amountStr = columns[3].trim();

        if (docNumber.isEmpty()) {
            throw new ParseException("INVALID_CSV_ROW", "Line " + lineNumber + ": document_number is empty");
        }

        LocalDate docDate;
        try {
            docDate = LocalDate.parse(docDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ParseException("INVALID_DOCUMENT_DATE", "Line " + lineNumber + ": Invalid date format. Expected yyyy-MM-dd");
        }

        if (currency.length() != 3) {
            throw new ParseException("INVALID_CURRENCY", "Line " + lineNumber + ": Invalid currency code. Expected 3 chars");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            throw new ParseException("INVALID_AMOUNT", "Line " + lineNumber + ": Invalid amount format");
        }

        return new CanonicalRecord(jobId, companyId, docNumber, docDate, currency, amount);
    }

    public static class ParseException extends Exception {
        private final String code;
        public ParseException(String code, String message) {
            super(message);
            this.code = code;
        }
        public String getCode() { return code; }
    }
}
