package com.trading.common.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 统一API响应包装类
 *
 * <p>所有REST API接口的统一响应格式，包含成功/失败标识、错误码、消息内容、业务数据和时间戳。</p>
 *
 * <p>使用示例：</p>
 * <ul>
 *   <li>成功响应：{@code ApiResponse.success(data)}</li>
 *   <li>错误响应：{@code ApiResponse.error("NOT_FOUND", "资源不存在")}</li>
 * </ul>
 *
 * <p>响应格式示例：</p>
 * <pre>
 * {
 *   "success": true,
 *   "code": "200",
 *   "message": "操作成功",
 *   "data": { ... },
 *   "timestamp": "2026-06-08T12:00:00Z"
 * }
 * </pre>
 *
 * @param <T> 业务数据类型
 * @author Trading System
 * @since 1.0.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** 是否成功 */
    private boolean success;

    /** 响应码：成功为"200"，失败为对应错误码 */
    private String code;

    /** 响应消息 */
    private String message;

    /** 业务数据，失败时为null */
    private T data;

    /** 响应时间戳 */
    private Instant timestamp;

    /**
     * 创建成功响应
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 包含业务数据的成功响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .code("200")
            .message("操作成功")
            .data(data)
            .timestamp(Instant.now())
            .build();
    }

    /**
     * 创建错误响应
     *
     * @param code    错误码
     * @param message 错误描述信息
     * @param <T>     业务数据类型（错误响应中为Void）
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .code(code)
            .message(message)
            .timestamp(Instant.now())
            .build();
    }
}
