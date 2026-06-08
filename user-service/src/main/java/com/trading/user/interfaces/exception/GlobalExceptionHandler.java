package com.trading.user.interfaces.exception;

import com.trading.common.dto.ApiResponse;
import com.trading.common.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * <p>统一处理所有 Controller 层抛出的异常，转换为标准的 {@link ApiResponse} 格式返回。</p>
 *
 * <p>异常处理映射：</p>
 * <ul>
 *   <li>{@link BusinessException}（NOT_FOUND）→ 404 Not Found</li>
 *   <li>{@link BusinessException}（其他）→ 400 Bad Request</li>
 *   <li>{@link MethodArgumentNotValidException} → 400 Bad Request（参数校验失败）</li>
 *   <li>{@link ConstraintViolationException} → 400 Bad Request（约束校验失败）</li>
 *   <li>其他异常 → 500 Internal Server Error</li>
 * </ul>
 *
 * @author Trading System
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * <p>将 NOT_FOUND 错误码映射为 404，其他业务异常映射为 400。</p>
     *
     * @param exception 业务异常
     * @return 标准错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        // NOT_FOUND 错误码对应 404 状态码，其他业务异常对应 400
        HttpStatus status = "NOT_FOUND".equals(exception.getCode()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(ApiResponse.error(exception.getCode(), exception.getMessage()));
    }

    /**
     * 处理请求体参数校验失败异常（@RequestBody @Valid）
     *
     * <p>取第一个字段校验错误的描述信息作为响应消息。</p>
     *
     * @param exception 参数校验异常
     * @return 400 Bad Request 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        // 取第一个字段错误的描述
        FieldError error = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", error == null ? "请求参数错误" : error.getDefaultMessage()));
    }

    /**
     * 处理路径参数/查询参数校验失败异常（@PathVariable @Valid 等）
     *
     * @param exception 约束校验异常
     * @return 400 Bad Request 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", exception.getMessage()));
    }

    /**
     * 处理未预期的系统异常
     *
     * <p>捕获所有未被其他处理器处理的异常，避免泄露系统内部信息。</p>
     *
     * @param exception 系统异常
     * @return 500 Internal Server Error 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_SERVER_ERROR", exception.getMessage()));
    }
}
