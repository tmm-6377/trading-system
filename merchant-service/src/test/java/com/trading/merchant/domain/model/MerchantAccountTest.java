package com.trading.merchant.domain.model;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MerchantAccountTest {

    @Test
    void testCreate() {
        MerchantAccount account = MerchantAccount.create("merchant001");

        assertEquals("merchant001", account.getMerchantId());
        assertEquals(new BigDecimal("0.00"), account.getBalance().getAmount());
        assertEquals(0, account.getVersion());
    }

    @Test
    void testCredit_Success() {
        MerchantAccount account = MerchantAccount.create("merchant001");

        account.credit(Money.of(new BigDecimal("12.50")));

        assertEquals(new BigDecimal("12.50"), account.getBalance().getAmount());
    }

    @Test
    void testCredit_MultipleTimes() {
        MerchantAccount account = MerchantAccount.create("merchant001");

        account.credit(Money.of(new BigDecimal("12.50")));
        account.credit(Money.of(new BigDecimal("7.25")));

        assertEquals(new BigDecimal("19.75"), account.getBalance().getAmount());
    }

    @Test
    void testBalanceScale() {
        MerchantAccount account = MerchantAccount.create("merchant001");

        account.credit(Money.of(new BigDecimal("1")));

        assertEquals(2, account.getBalance().getAmount().scale());
        assertEquals(new BigDecimal("1.00"), account.getBalance().getAmount());
    }

    @Test
    void testVersionIncrement() {
        MerchantAccount account = MerchantAccount.create("merchant001");

        assertEquals(0, account.getVersion());
    }
}
