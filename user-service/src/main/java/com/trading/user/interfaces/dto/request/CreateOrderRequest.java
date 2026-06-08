package com.trading.user.interfaces.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String merchantId;
    @NotBlank
    private String sku;
    @Min(1)
    private int quantity;
}
