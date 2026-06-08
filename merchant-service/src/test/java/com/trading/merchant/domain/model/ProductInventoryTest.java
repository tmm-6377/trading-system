package com.trading.merchant.domain.model;

import com.trading.common.exception.InsufficientStockException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductInventoryTest {
    @Test
    void should_deduct_stock_and_increase_sold_quantity() {
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook", Money.of(new BigDecimal("10.00")), 10);

        inventory.checkAndDeductStock(3);

        assertEquals(7, inventory.getAvailableQuantity());
        assertEquals(3, inventory.getSoldQuantity());
    }

    @Test
    void should_throw_when_stock_insufficient() {
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook", Money.of(new BigDecimal("10.00")), 2);

        assertThrows(InsufficientStockException.class, () -> inventory.checkAndDeductStock(3));
    }
}
