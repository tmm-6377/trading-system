package com.trading.common.exception;

/**
 * 分布式锁获取失败异常
 *
 * <p>当尝试获取 Redis 分布式锁失败时抛出此异常。</p>
 * <p>错误码固定为 "LOCK_ACQUIRE_FAILED"，HTTP响应状态码映射为 400。</p>
 *
 * <p>常见场景：</p>
 * <ul>
 *   <li>同一用户对同一商品重复下单（防重复下单保护）</li>
 *   <li>分布式锁竞争激烈时获取超时</li>
 * </ul>
 *
 * @author Trading System
 * @since 1.0.0
 */
public class LockAcquireException extends BusinessException {

    /**
     * 创建分布式锁获取失败异常
     *
     * @param message 描述锁获取失败的原因，例如 "重复下单，请稍后重试"
     */
    public LockAcquireException(String message) {
        super("LOCK_ACQUIRE_FAILED", message);
    }
}
