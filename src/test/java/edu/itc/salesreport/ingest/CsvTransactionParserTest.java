package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CsvTransactionParserTest {

    private final TransactionParser parser = new CsvTransactionParser();

    @Test
    void parsesAValidRow() throws InvalidRowException {
        var t = parser.parse("PNH,2026-09-01,PNH-000123,"
                + "SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR");
        assertEquals("PNH", t.branch());
        assertEquals(LocalDate.of(2026, 9, 1), t.date());
        assertEquals("PNH-000123", t.receiptNo());
        assertEquals("SKU-1001", t.sku());
        assertEquals("Jasmine Rice 5kg", t.productName());
        assertEquals("Grocery", t.category());
        assertEquals(2, t.quantity());
        assertEquals(new BigDecimal("6.50"), t.unitPrice());
        assertEquals(new BigDecimal("0.00"), t.discount());
        assertEquals(PaymentMethod.KHQR, t.paymentMethod());
        assertEquals(new BigDecimal("13.00"), t.revenue());
    }

    @Test
    void parsesARowWithADiscount() throws InvalidRowException {
        var t = parser.parse("REP,2026-09-02,REP-000007,"
                + "SKU-2002,Iced Coffee,Beverages,3,2.00,0.50,CASH");
        assertEquals(new BigDecimal("0.50"), t.discount());
        assertEquals(new BigDecimal("5.50"), t.revenue());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00",
        "PNH,2026-09-31,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,CASH",
        "XXX,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,CASH",
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Furniture,2,6.50,0.00,CASH",
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,two,6.50,0.00,CASH",
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,0,6.50,0.00,CASH",
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,-1.00,CASH",
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,13.01,CASH",
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,BITCOIN"
    })
    void rejectsInvalidRows(String line) {
        assertThrows(InvalidRowException.class, () -> parser.parse(line));
    }
}
