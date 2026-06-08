package com.trading.user.domain.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountTransaction {
    private Long id;
    private String userId;
    private String orderNo;
    private String transactionType;
    private Money amount;
    private Money balanceAfter;
    private String description;
    private LocalDateTime createdAt;

    public static AccountTransaction recharge(String userId, Money amount, Money balanceAfter) {
        return AccountTransaction.builder()
            .userId(userId)
            .transactionType("RECHARGE")
            .amount(amount)
            .balanceAfter(balanceAfter)
            .description("账户充值")
            .createdAt(LocalDateTime.now())
            .build();
    }

    public static AccountTransaction purchase(String userId, String orderNo, Money amount, Money balanceAfter) {
        return AccountTransaction.builder()
            .userId(userId)
            .orderNo(orderNo)
            .transactionType("PURCHASE")
            .amount(amount)
            .balanceAfter(balanceAfter)
            .description("下单扣款")
            .createdAt(LocalDateTime.now())
            .build();
    }
}
