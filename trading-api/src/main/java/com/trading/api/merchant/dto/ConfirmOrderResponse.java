package com.trading.api.merchant.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 确认订单响应 DTO
 *
 * <p>返回订单确认处理结果，包含成功标识、单价、总金额和消息。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmOrderResponse {

    /** 是否处理成功 */
    private boolean success;

    /** 商品单价（处理成功时返回） */
    private BigDecimal unitPrice;

    /** 订单总金额（处理成功时返回） */
    private BigDecimal totalAmount;

    /** 处理结果描述或错误信息 */
    private String message;
}
