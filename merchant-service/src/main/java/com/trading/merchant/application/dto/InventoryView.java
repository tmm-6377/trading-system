package com.trading.merchant.application.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InventoryView {
    String merchantId;
    String sku;
    String productName;
    BigDecimal price;
    Integer availableQuantity;
    Integer soldQuantity;
}
