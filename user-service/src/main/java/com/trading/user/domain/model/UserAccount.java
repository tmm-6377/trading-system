package com.trading.user.domain.model;

import com.trading.common.exception.BusinessException;
import com.trading.common.exception.InsufficientBalanceException;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserAccount {
    private final String userId;
    private Money balance;
    private Integer version;

    public static UserAccount create(String userId) {
        return UserAccount.builder()
            .userId(userId)
            .balance(Money.zero())
            .version(0)
            .build();
    }

    public void recharge(Money amount) {
        validatePositive(amount);
        this.balance = this.balance.add(amount);
    }

    public void deduct(Money amount) {
        validatePositive(amount);
        if (this.balance.lessThan(amount)) {
            throw new InsufficientBalanceException("账户余额不足");
        }
        this.balance = this.balance.subtract(amount);
    }

    private void validatePositive(Money amount) {
        if (amount == null || amount.isNegativeOrZero()) {
            throw new BusinessException("INVALID_AMOUNT", "金额必须大于0");
        }
    }
}
