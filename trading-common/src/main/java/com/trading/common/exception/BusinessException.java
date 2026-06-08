package com.trading.common.exception;

/**
 * 业务异常基类
 *
 * <p>所有业务相关异常的父类，携带错误码和错误信息，便于统一异常处理。</p>
 *
 * <p>错误码规范：</p>
 * <ul>
 *   <li>NOT_FOUND - 资源不存在</li>
 *   <li>INSUFFICIENT_BALANCE - 账户余额不足</li>
 *   <li>INSUFFICIENT_STOCK - 商品库存不足</li>
 *   <li>INVALID_AMOUNT - 金额无效</li>
 *   <li>INVALID_QUANTITY - 数量无效</li>
 *   <li>CONCURRENT_MODIFICATION - 并发更新冲突</li>
 *   <li>LOCK_ACQUIRE_FAILED - 获取分布式锁失败</li>
 *   <li>REMOTE_SERVICE_ERROR - 远程服务调用失败</li>
 * </ul>
 *
 * @author Trading System
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    /** 错误码，用于客户端识别异常类型 */
    private final String code;

    /**
     * 创建业务异常
     *
     * @param code    错误码，不能为空
     * @param message 错误描述信息
     */
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取错误码
     *
     * @return 错误码字符串
     */
    public String getCode() {
        return code;
    }
}
