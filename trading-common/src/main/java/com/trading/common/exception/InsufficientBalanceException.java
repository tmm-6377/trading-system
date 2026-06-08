package com.trading.common.exception;

/**
 * 账户余额不足异常
 *
 * <p>当用户账户余额不足以完成扣款操作时抛出此异常。</p>
 * <p>错误码固定为 "INSUFFICIENT_BALANCE"，HTTP响应状态码映射为 400。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
public class InsufficientBalanceException extends BusinessException {

    /**
     * 创建余额不足异常
     *
     * @param message 描述余额不足的详细信息，例如 "账户余额不足，当前余额: 50.00，需要: 100.00"
     */
    public InsufficientBalanceException(String message) {
        super("INSUFFICIENT_BALANCE", message);
    }
}
