package com.trading.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * 自定义异常类单元测试
 *
 * <p>测试目标：验证所有自定义异常的创建、错误码和消息内容</p>
 *
 * <p>测试范围：</p>
 * <ul>
 *   <li>BusinessException - 业务异常基类</li>
 *   <li>NotFoundException - 资源不存在异常</li>
 *   <li>InsufficientBalanceException - 余额不足异常</li>
 *   <li>InsufficientStockException - 库存不足异常</li>
 *   <li>RemoteServiceException - 远程服务异常</li>
 *   <li>LockAcquireException - 分布式锁获取失败异常</li>
 * </ul>
 */
class ExceptionTest {

    /**
     * 测试场景：创建 BusinessException
     *
     * <p>Given: 错误码 "CUSTOM_ERROR" 和错误消息 "自定义错误"</p>
     * <p>When: 创建 BusinessException</p>
     * <p>Then: getCode() 返回错误码，getMessage() 返回错误消息</p>
     */
    @Test
    void testBusinessException_Creation() {
        // Given
        String code = "CUSTOM_ERROR";
        String message = "自定义错误";

        // When
        BusinessException ex = new BusinessException(code, message);

        // Then
        assertEquals(code, ex.getCode());
        assertEquals(message, ex.getMessage());
    }

    /**
     * 测试场景：创建 NotFoundException
     *
     * <p>Given: 错误消息 "用户账户不存在"</p>
     * <p>When: 创建 NotFoundException</p>
     * <p>Then: 错误码为 "NOT_FOUND"，且是 BusinessException 的子类</p>
     */
    @Test
    void testNotFoundException_Creation() {
        // Given
        String message = "用户账户不存在";

        // When
        NotFoundException ex = new NotFoundException(message);

        // Then
        assertEquals("NOT_FOUND", ex.getCode());
        assertEquals(message, ex.getMessage());
        assertInstanceOf(BusinessException.class, ex);
    }

    /**
     * 测试场景：创建 InsufficientBalanceException
     *
     * <p>Given: 错误消息 "账户余额不足"</p>
     * <p>When: 创建 InsufficientBalanceException</p>
     * <p>Then: 错误码为 "INSUFFICIENT_BALANCE"</p>
     */
    @Test
    void testInsufficientBalanceException_Creation() {
        // Given
        String message = "账户余额不足";

        // When
        InsufficientBalanceException ex = new InsufficientBalanceException(message);

        // Then
        assertEquals("INSUFFICIENT_BALANCE", ex.getCode());
        assertEquals(message, ex.getMessage());
        assertInstanceOf(BusinessException.class, ex);
    }

    /**
     * 测试场景：创建 InsufficientStockException
     *
     * <p>Given: 错误消息 "商品库存不足"</p>
     * <p>When: 创建 InsufficientStockException</p>
     * <p>Then: 错误码为 "INSUFFICIENT_STOCK"</p>
     */
    @Test
    void testInsufficientStockException_Creation() {
        // Given
        String message = "商品库存不足";

        // When
        InsufficientStockException ex = new InsufficientStockException(message);

        // Then
        assertEquals("INSUFFICIENT_STOCK", ex.getCode());
        assertEquals(message, ex.getMessage());
        assertInstanceOf(BusinessException.class, ex);
    }

    /**
     * 测试场景：创建 RemoteServiceException
     *
     * <p>Given: 错误消息 "商家服务暂时不可用"</p>
     * <p>When: 创建 RemoteServiceException</p>
     * <p>Then: 错误码为 "REMOTE_SERVICE_ERROR"</p>
     */
    @Test
    void testRemoteServiceException_Creation() {
        // Given
        String message = "商家服务暂时不可用";

        // When
        RemoteServiceException ex = new RemoteServiceException(message);

        // Then
        assertEquals("REMOTE_SERVICE_ERROR", ex.getCode());
        assertEquals(message, ex.getMessage());
        assertInstanceOf(BusinessException.class, ex);
    }

    /**
     * 测试场景：创建 LockAcquireException
     *
     * <p>Given: 错误消息 "重复下单，请稍后重试"</p>
     * <p>When: 创建 LockAcquireException</p>
     * <p>Then: 错误码为 "LOCK_ACQUIRE_FAILED"</p>
     */
    @Test
    void testLockAcquireException_Creation() {
        // Given
        String message = "重复下单，请稍后重试";

        // When
        LockAcquireException ex = new LockAcquireException(message);

        // Then
        assertEquals("LOCK_ACQUIRE_FAILED", ex.getCode());
        assertEquals(message, ex.getMessage());
        assertInstanceOf(BusinessException.class, ex);
    }

    /**
     * 测试场景：BusinessException 继承关系
     *
     * <p>Given: 所有自定义异常</p>
     * <p>When: 检查继承关系</p>
     * <p>Then: 所有自定义异常都是 RuntimeException 的子类</p>
     */
    @Test
    void testAllExceptionsExtendRuntimeException() {
        // Given & When & Then
        assertInstanceOf(RuntimeException.class, new BusinessException("CODE", "msg"));
        assertInstanceOf(RuntimeException.class, new NotFoundException("msg"));
        assertInstanceOf(RuntimeException.class, new InsufficientBalanceException("msg"));
        assertInstanceOf(RuntimeException.class, new InsufficientStockException("msg"));
        assertInstanceOf(RuntimeException.class, new RemoteServiceException("msg"));
        assertInstanceOf(RuntimeException.class, new LockAcquireException("msg"));
    }
}
