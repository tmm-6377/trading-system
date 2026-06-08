package com.trading.merchant.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MerchantAccount {
    private final String merchantId;
    private Money balance;
    private Integer version;

    public static MerchantAccount create(String merchantId) {
        return MerchantAccount.builder()
            .merchantId(merchantId)
            .balance(Money.zero())
            .version(0)
            .build();
    }

    public void credit(Money amount) {
        this.balance = this.balance.add(amount);
    }
}
