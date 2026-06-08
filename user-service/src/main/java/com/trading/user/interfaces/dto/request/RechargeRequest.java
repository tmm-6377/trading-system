package com.trading.user.interfaces.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class RechargeRequest {
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
}
