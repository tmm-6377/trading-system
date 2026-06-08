package com.trading.user.domain.model;

import com.trading.common.exception.BusinessException;
import com.trading.common.exception.InsufficientBalanceException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserAccountTest {
    @Test
    void testCreate() {
        // Given
        String userId = "user001";

        // When
        UserAccount account = UserAccount.create(userId);

        // Then
        assertEquals("user001", account.getUserId());
        assertEquals(new BigDecimal("0.00"), account.getBalance().getAmount());
        assertEquals(0, account.getVersion());
    }

    @Test
    void testDeductSuccessfully() {
        // Given
        UserAccount account = UserAccount.create("user001");
        account.recharge(Money.of(new BigDecimal("100.00")));

        // When
        account.deduct(Money.of(new BigDecimal("30.00")));

        // Then
        assertEquals(new BigDecimal("70.00"), account.getBalance().getAmount());
    }

    @Test
    void testDeductWithExactBalance() {
        // Given
        UserAccount account = UserAccount.create("user001");
        account.recharge(Money.of(new BigDecimal("50.00")));

        // When
        account.deduct(Money.of(new BigDecimal("50.00")));

        // Then
        assertEquals(new BigDecimal("0.00"), account.getBalance().getAmount());
    }

    @Test
    void testMultipleRecharge() {
        // Given
        UserAccount account = UserAccount.create("user001");

        // When
        account.recharge(Money.of(new BigDecimal("10.00")));
        account.recharge(Money.of(new BigDecimal("20.00")));
        account.recharge(Money.of(new BigDecimal("30.00")));

        // Then
        assertEquals(new BigDecimal("60.00"), account.getBalance().getAmount());
    }

    @Test
    void testRechargeNegativeAmount() {
        // Given
        UserAccount account = UserAccount.create("user001");

        // When
        BusinessException exception = assertThrows(BusinessException.class,
            () -> account.recharge(Money.of(new BigDecimal("-1.00"))));

        // Then
        assertEquals("INVALID_AMOUNT", exception.getCode());
    }

    @Test
    void testDeductNegativeAmount() {
        // Given
        UserAccount account = UserAccount.create("user001");

        // When
        BusinessException exception = assertThrows(BusinessException.class,
            () -> account.deduct(Money.of(new BigDecimal("-1.00"))));

        // Then
        assertEquals("INVALID_AMOUNT", exception.getCode());
    }

    @Test
    void testBalanceScale() {
        // Given
        UserAccount account = UserAccount.create("user001");

        // When
        account.recharge(Money.of(new BigDecimal("10.126")));

        // Then
        assertEquals(new BigDecimal("10.13"), account.getBalance().getAmount());
    }

    @Test
    void testVersionIncrement() {
        // Given
        UserAccount account = UserAccount.create("user001");

        // When
        Integer version = account.getVersion();

        // Then
        assertEquals(0, version);
    }

    @Test
    void should_throw_exception_when_balance_insufficient() {
        // Given
        UserAccount account = UserAccount.create("user001");
        account.recharge(Money.of(new BigDecimal("50.00")));

        // When
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class,
            () -> account.deduct(Money.of(new BigDecimal("100.00"))));

        // Then
        assertEquals("INSUFFICIENT_BALANCE", exception.getCode());
    }
}
