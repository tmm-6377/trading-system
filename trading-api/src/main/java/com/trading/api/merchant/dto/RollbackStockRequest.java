package com.trading.api.merchant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存回滚请求 DTO
 *
 * <p>用于 Seata 分布式事务补偿时回滚已扣减库存的请求参数。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollbackStockRequest {

    /** 商家ID，不能为空 */
    @NotBlank
    private String merchantId;

    /** 商品SKU，不能为空 */
    @NotBlank
    private String sku;

    /** 需要回滚的数量，最小为1 */
    @Min(1)
    private int quantity;
}
