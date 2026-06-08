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
public class CheckStockResponse {
    private boolean available;
    private BigDecimal unitPrice;
    private String errorMessage;
}
