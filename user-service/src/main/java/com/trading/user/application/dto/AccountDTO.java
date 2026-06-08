package com.trading.user.application.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccountDTO {
    String userId;
    BigDecimal balance;
}
