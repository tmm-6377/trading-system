package com.trading.api.merchant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 确认订单请求 DTO
 *
 * <p>用于确认订单并扣减库存的请求参数。在用户余额扣减成功后调用。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmOrderRequest {

    /** 订单号，不能为空 */
    @NotBlank
    private String orderNo;

    /** 商家ID，不能为空 */
    @NotBlank
    private String merchantId;

    /** 商品SKU，不能为空 */
    @NotBlank
    private String sku;

    /** 购买数量，最小为1 */
    @Min(1)
    private int quantity;
}
