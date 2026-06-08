package com.trading.merchant.application.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProductCommand {
    private String merchantId;
    private String sku;
    private String productName;
    private BigDecimal price;
    private int quantity;
}
