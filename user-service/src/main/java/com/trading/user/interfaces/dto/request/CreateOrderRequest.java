package com.trading.user.interfaces.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建订单请求 DTO
 *
 * <p>用户下单的请求参数。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Data
public class CreateOrderRequest {

    /**
     * 用户ID
     *
     * <p>校验规则：不能为空白字符串</p>
     */
    @NotBlank
    private String userId;

    /**
     * 商家ID
     *
     * <p>校验规则：不能为空白字符串</p>
     */
    @NotBlank
    private String merchantId;

    /**
     * 商品SKU
     *
     * <p>校验规则：不能为空白字符串</p>
     */
    @NotBlank
    private String sku;

    /**
     * 购买数量
     *
     * <p>校验规则：最小为1</p>
     */
    @Min(1)
    private int quantity;
}
