package com.trading.common.exception;

/**
 * 商品库存不足异常
 *
 * <p>当商品可用库存不足以满足购买数量时抛出此异常。</p>
 * <p>错误码固定为 "INSUFFICIENT_STOCK"，HTTP响应状态码映射为 400。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
public class InsufficientStockException extends BusinessException {

    /**
     * 创建库存不足异常
     *
     * @param message 描述库存不足的详细信息，例如 "库存不足，当前库存: 5，需要: 10"
     */
    public InsufficientStockException(String message) {
        super("INSUFFICIENT_STOCK", message);
    }
}
