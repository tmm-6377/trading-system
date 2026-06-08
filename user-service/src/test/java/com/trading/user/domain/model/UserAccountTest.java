package com.trading.user.domain.model;

import com.trading.common.exception.BusinessException;
import com.trading.common.exception.InsufficientBalanceException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserAccountTest {
    @Test
    void should_recharge_successfully() {
        UserAccount account = UserAccount.create("user001");

        account.recharge(Money.of(new BigDecimal("100.00")));

        assertEquals(new BigDecimal("100.00"), account.getBalance().getAmount());
    }

    @Test
    void should_throw_exception_when_balance_insufficient() {
        UserAccount account = UserAccount.create("user001");
        account.recharge(Money.of(new BigDecimal("50.00")));

        assertThrows(InsufficientBalanceException.class,
            () -> account.deduct(Money.of(new BigDecimal("100.00"))));
    }

    @Test
    void should_reject_non_positive_amount() {
        UserAccount account = UserAccount.create("user001");

        assertThrows(BusinessException.class,
            () -> account.recharge(Money.of(BigDecimal.ZERO)));
    }
}
