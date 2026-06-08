package com.trading.user.domain.model;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {
    @Test
    void testCreateMoney() {
        // Given
        BigDecimal amount = new BigDecimal("123.45");

        // When
        Money money = Money.of(amount);

        // Then
        assertEquals(new BigDecimal("123.45"), money.getAmount());
    }

    @Test
    void testMoneyAdd() {
        // Given
        Money first = Money.of(new BigDecimal("10.50"));
        Money second = Money.of(new BigDecimal("20.25"));

        // When
        Money result = first.add(second);

        // Then
        assertEquals(new BigDecimal("30.75"), result.getAmount());
    }

    @Test
    void testMoneySubtract() {
        // Given
        Money first = Money.of(new BigDecimal("50.00"));
        Money second = Money.of(new BigDecimal("18.25"));

        // When
        Money result = first.subtract(second);

        // Then
        assertEquals(new BigDecimal("31.75"), result.getAmount());
    }

    @Test
    void testMoneyMultiply() {
        // Given
        Money money = Money.of(new BigDecimal("12.50"));

        // When
        Money result = money.multiply(3);

        // Then
        assertEquals(new BigDecimal("37.50"), result.getAmount());
    }

    @Test
    void testMoneyComparison() {
        // Given
        Money smaller = Money.of(new BigDecimal("10.00"));
        Money larger = Money.of(new BigDecimal("20.00"));
        Money sameAsLarger = Money.of(new BigDecimal("20.00"));

        // When
        boolean lessThan = smaller.lessThan(larger);
        boolean greaterThan = !larger.lessThan(smaller) && !larger.equals(smaller);
        boolean equal = larger.equals(sameAsLarger);

        // Then
        assertTrue(lessThan);
        assertTrue(greaterThan);
        assertTrue(equal);
        assertFalse(sameAsLarger.lessThan(larger));
    }

    @Test
    void testMoneyScale() {
        // Given
        BigDecimal amount = new BigDecimal("10.126");

        // When
        Money money = Money.of(amount);

        // Then
        assertEquals(new BigDecimal("10.13"), money.getAmount());
    }

    @Test
    void testMoneyZero() {
        // Given
        // When
        Money zero = Money.zero();

        // Then
        assertEquals(new BigDecimal("0.00"), zero.getAmount());
        assertTrue(zero.isNegativeOrZero());
    }

    @Test
    void testMoneyNegative() {
        // Given
        Money negative = Money.of(new BigDecimal("-1.00"));

        // When
        boolean negativeOrZero = negative.isNegativeOrZero();

        // Then
        assertTrue(negativeOrZero);
    }

    @Test
    void testMoneyImmutability() {
        // Given
        Money original = Money.of(new BigDecimal("15.00"));

        // When
        Money result = original.add(Money.of(new BigDecimal("5.00")));

        // Then
        assertEquals(new BigDecimal("15.00"), original.getAmount());
        assertEquals(new BigDecimal("20.00"), result.getAmount());
        assertNotSame(original, result);
    }
}
