package com.trading.common.exception;

/**
 * 远程服务调用异常
 *
 * <p>当调用外部微服务（如商家服务）失败或返回错误响应时抛出此异常。</p>
 * <p>错误码固定为 "REMOTE_SERVICE_ERROR"，HTTP响应状态码映射为 400。</p>
 *
 * <p>常见场景：</p>
 * <ul>
 *   <li>Feign调用商家服务确认订单失败</li>
 *   <li>远程服务超时</li>
 *   <li>服务降级处理</li>
 * </ul>
 *
 * @author Trading System
 * @since 1.0.0
 */
public class RemoteServiceException extends BusinessException {

    /**
     * 创建远程服务异常
     *
     * @param message 描述远程服务失败的原因
     */
    public RemoteServiceException(String message) {
        super("REMOTE_SERVICE_ERROR", message);
    }
}
