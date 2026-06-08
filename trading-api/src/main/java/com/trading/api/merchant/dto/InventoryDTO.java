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
public class InventoryDTO {
    private String merchantId;
    private String sku;
    private String productName;
    private BigDecimal price;
    private Integer availableQuantity;
    private Integer soldQuantity;
}
