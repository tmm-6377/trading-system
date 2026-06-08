package com.trading.merchant.domain.model;

import com.trading.common.exception.BusinessException;
import com.trading.common.exception.InsufficientStockException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductInventoryTest {

    @Test
    void testCreate() {
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook", Money.of(new BigDecimal("10.00")), 10);

        assertEquals("merchant001", inventory.getMerchantId());
        assertEquals("SKU-001", inventory.getSku().getValue());
        assertEquals("MacBook", inventory.getProductName());
        assertEquals(new BigDecimal("10.00"), inventory.getPrice().getAmount());
        assertEquals(10, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getSoldQuantity());
    }

    @Test
    void testAddStock() {
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook", Money.of(new BigDecimal("10.00")), 10);

        inventory.addStock(5);

        assertEquals(15, inventory.getAvailableQuantity());
    }

    @Test
    void testAddStockMultipleTimes() {
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook", Money.of(new BigDecimal("10.00")), 10);

        inventory.addStock(2);
        inventory.addStock(3);

        assertEquals(15, inventory.getAvailableQuantity());
    }

    @Test
    void testCheckAndDeductStock_ExactQuantity() {
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook", Money.of(new BigDecimal("10.00")), 3);

        inventory.checkAndDeductStock(3);

        assertEquals(0, inventory.getAvailableQuantity());
        assertEquals(3, inventory.getSoldQuantity());
    }

    @Test
    void testCheckAndDeductStock_InvalidQuantity() {
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook", Money.of(new BigDecimal("10.00")), 3);

        BusinessException exception = assertThrows(BusinessException.class, () -> inventory.checkAndDeductStock(0));

        assertEquals("INVALID_QUANTITY", exception.getCode());
    }

    @Test
    void testCheckAndDeductStock_NegativeQuantity() {
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook", Money.of(new BigDecimal("10.00")), 3);

        BusinessException exception = assertThrows(BusinessException.class, () -> inventory.checkAndDeductStock(-1));

        assertEquals("INVALID_QUANTITY", exception.getCode());
    }

    @Test
    void testCalculateSoldValue() {
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook", Money.of(new BigDecimal("10.00")), 10);
        inventory.checkAndDeductStock(4);

        Money soldValue = inventory.calculateSoldValue();

        assertEquals(new BigDecimal("40.00"), soldValue.getAmount());
    }

    @Test
    void testSoldQuantityIncrement() {
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook", Money.of(new BigDecimal("10.00")), 10);

        inventory.checkAndDeductStock(2);

        assertEquals(2, inventory.getSoldQuantity());
    }

    @Test
    void testVersionIncrement() {
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook", Money.of(new BigDecimal("10.00")), 10);

        assertEquals(0, inventory.getVersion());
    }

    @Test
    void should_throw_when_stock_insufficient() {
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook", Money.of(new BigDecimal("10.00")), 2);

        assertThrows(InsufficientStockException.class, () -> inventory.checkAndDeductStock(3));
    }
}
