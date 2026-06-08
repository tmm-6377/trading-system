package com.trading.api.merchant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmOrderRequest {
    @NotBlank
    private String orderNo;
    @NotBlank
    private String merchantId;
    @NotBlank
    private String sku;
    @Min(1)
    private int quantity;
}
