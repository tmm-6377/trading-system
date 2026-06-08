package com.trading.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderCommand {
    private String userId;
    private String merchantId;
    private String sku;
    private int quantity;
}
