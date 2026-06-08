package com.trading.merchant.domain.model;

import com.trading.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SKUTest {

    @Test
    void testCreateSKU() {
        SKU sku = SKU.of("SKU-001");

        assertEquals("SKU-001", sku.getValue());
    }

    @Test
    void testSKUValidation_NullThrows() {
        BusinessException exception = assertThrows(BusinessException.class, () -> SKU.of(null));

        assertEquals("INVALID_SKU", exception.getCode());
    }

    @Test
    void testSKUValidation_BlankThrows() {
        BusinessException exception = assertThrows(BusinessException.class, () -> SKU.of("   "));

        assertEquals("INVALID_SKU", exception.getCode());
    }

    @Test
    void testSKUEquality() {
        SKU first = SKU.of("SKU-001");
        SKU second = SKU.of("SKU-001");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void testSKUImmutability() {
        SKU sku = SKU.of("SKU-IMMUTABLE");

        assertEquals("SKU-IMMUTABLE", sku.getValue());
        assertTrue(sku.getValue().equals(sku.getValue()));
    }
}
