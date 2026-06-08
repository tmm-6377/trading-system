package com.trading.merchant.interfaces.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateStockRequest {
    @Min(1)
    private int quantity;
}
