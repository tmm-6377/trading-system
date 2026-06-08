package com.trading.user.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderDTO {
    String orderNo;
    String userId;
    String merchantId;
    String sku;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal totalAmount;
    String status;
    LocalDateTime createdAt;
}
