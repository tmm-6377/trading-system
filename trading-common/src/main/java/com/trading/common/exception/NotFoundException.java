package com.trading.common.exception;

/**
 * 资源不存在异常
 *
 * <p>当查询的资源（如用户账户、订单、商品等）不存在时抛出此异常。</p>
 * <p>错误码固定为 "NOT_FOUND"，HTTP响应状态码映射为 404。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
public class NotFoundException extends BusinessException {

    /**
     * 创建资源不存在异常
     *
     * @param message 描述哪个资源不存在的信息，例如 "用户账户不存在"
     */
    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
