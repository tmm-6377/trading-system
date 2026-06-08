package com.trading.merchant.interfaces.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class AddInventoryRequest {
    @NotBlank
    private String sku;
    @NotBlank
    private String productName;
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal price;
    @Min(1)
    private int quantity;
}
