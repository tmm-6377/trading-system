package com.trading.merchant.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Money {
    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount == null ? BigDecimal.ZERO : amount);
    }

    public static Money zero() {
        return of(BigDecimal.ZERO);
    }

    public Money add(Money other) {
        return of(amount.add(other.amount));
    }

    public Money multiply(int multiplier) {
        return of(amount.multiply(BigDecimal.valueOf(multiplier)));
    }
}
