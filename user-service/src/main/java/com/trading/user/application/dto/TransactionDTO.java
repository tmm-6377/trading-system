package com.trading.user.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TransactionDTO {
    String orderNo;
    String transactionType;
    BigDecimal amount;
    BigDecimal balanceAfter;
    String description;
    LocalDateTime createdAt;
}
