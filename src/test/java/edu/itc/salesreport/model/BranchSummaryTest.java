package edu.itc.salesreport.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BranchSummaryTest {

    private static BranchSummary summary(String revenue, long receipts) {
        return new BranchSummary("PNH", new BigDecimal(revenue),
                BigDecimal.ZERO, receipts, Map.of(), List.of(), Map.of());
    }

    @Test
    void averageBasketIsRevenueDividedByReceipts() {
        assertEquals(new BigDecimal("3.33"), summary("10.00", 3).averageBasket());
    }

    @Test
    void averageBasketRoundsHalfUp() {
        // 1.00 / 8 = 0.125 -> 0.13
        assertEquals(new BigDecimal("0.13"), summary("1.00", 8).averageBasket());
    }

    @Test
    void averageBasketIsZeroWhenThereAreNoReceipts() {
        assertEquals(new BigDecimal("0.00"), summary("0.00", 0).averageBasket());
    }

    @Test
    void keepsACopyOfTheCallersCollections() {
        var top = new ProductTotal("SKU-1", "Rice", new BigDecimal("10.00"), 2);
        var products = new ArrayList<>(List.of(top));
        var byCategory = new HashMap<String, BigDecimal>();
        byCategory.put("Grocery", new BigDecimal("10.00"));
        var s = new BranchSummary("PNH", new BigDecimal("10.00"),
                BigDecimal.ZERO, 1, byCategory, products, Map.of());
        products.clear();
        byCategory.clear();
        assertEquals(1, s.topProducts().size());
        assertEquals(1, s.revenueByCategory().size());
    }

    @Test
    void mapsAreUnmodifiable() {
        var s = summary("10.00", 1);
        assertThrows(UnsupportedOperationException.class,
                () -> s.revenueByCategory().put("Grocery", BigDecimal.ONE));
        assertThrows(UnsupportedOperationException.class,
                () -> s.revenueByPayment().put(PaymentMethod.CASH, BigDecimal.ONE));
    }
}