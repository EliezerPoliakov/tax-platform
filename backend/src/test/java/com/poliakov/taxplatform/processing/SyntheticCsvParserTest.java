package com.poliakov.taxplatform.processing;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.Assertions.*;

class SyntheticCsvParserTest {

    private final SyntheticCsvParser parser = new SyntheticCsvParser();

    @Test
    void shouldParseValidCsv() throws Exception {
        String csv = "document_number,document_date,currency,amount\n" +
                     "INV-001,2026-01-01,USD,100.00\n" +
                     "INV-002,2026-01-02,EUR,200.50";
        
        List<CanonicalRecord> records = parser.parse(1L, 1L, new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        
        assertEquals(2, records.size());
        assertEquals("INV-001", records.get(0).getDocumentNumber());
        assertEquals(LocalDate.of(2026, 1, 1), records.get(0).getDocumentDate());
        assertEquals("USD", records.get(0).getCurrency());
        assertEquals(new BigDecimal("100.00"), records.get(0).getAmount());
    }

    @Test
    void shouldThrowOnInvalidHeader() {
        String csv = "wrong,document_date,currency,amount\nINV-001,2026-01-01,USD,100.00";
        var ex = assertThrows(SyntheticCsvParser.ParseException.class, () -> 
            parser.parse(1L, 1L, new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))));
        assertEquals("INVALID_CSV_HEADER", ex.getCode());
    }

    @Test
    void shouldThrowOnInvalidDate() {
        String csv = "document_number,document_date,currency,amount\nINV-001,2026-13-01,USD,100.00";
        var ex = assertThrows(SyntheticCsvParser.ParseException.class, () -> 
            parser.parse(1L, 1L, new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))));
        assertEquals("INVALID_DOCUMENT_DATE", ex.getCode());
    }

    @Test
    void shouldThrowOnInvalidAmount() {
        String csv = "document_number,document_date,currency,amount\nINV-001,2026-01-01,USD,abc";
        var ex = assertThrows(SyntheticCsvParser.ParseException.class, () -> 
            parser.parse(1L, 1L, new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))));
        assertEquals("INVALID_AMOUNT", ex.getCode());
    }

    @Test
    void shouldThrowOnInvalidCurrency() {
        String csv = "document_number,document_date,currency,amount\nINV-001,2026-01-01,USDD,100.00";
        var ex = assertThrows(SyntheticCsvParser.ParseException.class, () -> 
            parser.parse(1L, 1L, new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))));
        assertEquals("INVALID_CURRENCY", ex.getCode());
    }
}
