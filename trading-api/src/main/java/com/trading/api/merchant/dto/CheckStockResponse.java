package com.trading.api.merchant.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存检查响应 DTO
 *
 * <p>返回库存检查结果，包含可用状态、商品单价和错误信息。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckStockResponse {

    /** 库存是否充足 */
    private boolean available;

    /** 商品单价（库存充足时返回，用于计算订单总金额） */
    private BigDecimal unitPrice;

    /** 错误信息（库存不足或商品不存在时返回） */
    private String errorMessage;
}
