package com.trading.user.domain.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Order {
    private Long id;
    private String orderNo;
    private String userId;
    private String merchantId;
    private String sku;
    private Integer quantity;
    private Money unitPrice;
    private Money totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public static Order create(String orderNo, String userId, String merchantId, String sku, int quantity, Money unitPrice) {
        return Order.builder()
            .orderNo(orderNo)
            .userId(userId)
            .merchantId(merchantId)
            .sku(sku)
            .quantity(quantity)
            .unitPrice(unitPrice)
            .totalAmount(unitPrice.multiply(quantity))
            .status("CREATED")
            .createdAt(LocalDateTime.now())
            .build();
    }
}
