package com.trading.user.application.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RechargeCommand {
    private String userId;
    private BigDecimal amount;
}
