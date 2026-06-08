package com.trading.user.domain.model;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderTest {
    @Test
    void testCreateOrder() {
        // Given
        Money unitPrice = Money.of(new BigDecimal("12.50"));

        // When
        Order order = Order.create("ORD-001", "user001", "merchant001", "SKU-001", 2, unitPrice);

        // Then
        assertEquals("ORD-001", order.getOrderNo());
        assertEquals("user001", order.getUserId());
        assertEquals("merchant001", order.getMerchantId());
        assertEquals("SKU-001", order.getSku());
        assertEquals(2, order.getQuantity());
        assertNotNull(order.getCreatedAt());
    }

    @Test
    void testOrderNoGeneration() {
        // Given
        Money unitPrice = Money.of(new BigDecimal("9.90"));

        // When
        Order order = Order.create("ORD-ABC123", "user001", "merchant001", "SKU-001", 1, unitPrice);

        // Then
        assertEquals("ORD-ABC123", order.getOrderNo());
    }

    @Test
    void testCalculateTotalAmount() {
        // Given
        Money unitPrice = Money.of(new BigDecimal("19.99"));

        // When
        Order order = Order.create("ORD-002", "user001", "merchant001", "SKU-002", 3, unitPrice);

        // Then
        assertEquals(new BigDecimal("59.97"), order.getTotalAmount().getAmount());
    }

    @Test
    void testOrderStatusTransition() {
        // Given
        Money unitPrice = Money.of(new BigDecimal("15.00"));

        // When
        Order order = Order.create("ORD-003", "user001", "merchant001", "SKU-003", 2, unitPrice);

        // Then
        assertEquals("CREATED", order.getStatus());
    }

    @Test
    void testInvalidQuantity() {
        // Given
        Money unitPrice = Money.of(new BigDecimal("8.00"));

        // When
        Order order = Order.create("ORD-004", "user001", "merchant001", "SKU-004", 0, unitPrice);

        // Then
        assertEquals(0, order.getQuantity());
        assertEquals(new BigDecimal("0.00"), order.getTotalAmount().getAmount());
    }
}
