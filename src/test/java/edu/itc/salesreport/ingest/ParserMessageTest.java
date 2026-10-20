package edu.itc.salesreport.ingest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ParserMessageTest {

    private final TransactionParser parser = new CsvTransactionParser();

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
        "XXX,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,CASH;branch",
        "PNH,2026-09-31,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,CASH;date",
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Furniture,2,6.50,0.00,CASH;category",
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,two,6.50,0.00,CASH;quantity",
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,0,6.50,0.00,CASH;quantity",
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,-1.00,CASH;discount",
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,13.01,CASH;discount",
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,BITCOIN;payment_method"
    })
    void errorMessageNamesTheField(String line, String field) {
        var e = assertThrows(InvalidRowException.class,
                () -> parser.parse(line));
        assertTrue(e.getMessage().contains(field),
                () -> "message '" + e.getMessage() + "' should name " + field);
    }
}
