package edu.itc.salesreport.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SaleTransactionTest {

    @Test
    void roundsHalfUpToTwoDecimals() {
        // 3 x 0.335 = 1.005 -> 1.01
        var t = new SaleTransaction("PNH", LocalDate.of(2026, 9, 1), "PNH-1",
                "SKU-1", "Rice", "Grocery", 3, new BigDecimal("0.335"),
                BigDecimal.ZERO, PaymentMethod.CASH);
        assertEquals(new BigDecimal("1.01"), t.revenue());
    }

    @Test
    void topProductsIsUnmodifiable() {
        var top = new ProductTotal("SKU-1", "Rice", new BigDecimal("10.00"), 2);
        var s = new BranchSummary("PNH", new BigDecimal("10.00"),
                BigDecimal.ZERO, 1, Map.of(), List.of(top), Map.of());
        assertThrows(UnsupportedOperationException.class,
                () -> s.topProducts().add(top));
    }
}
