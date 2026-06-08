package com.trading.api.merchant.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmOrderResponse {
    private boolean success;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String message;
}
