package com.trading.merchant.interfaces.exception;

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
 * 全局异常处理器。
 *
 * <p>负责将商家服务中抛出的业务异常、参数校验异常以及未预期异常统一转换为标准 API 响应格式，
 * 保证接口层返回稳定的错误码和 HTTP 状态码。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * <p>当错误码为 {@code NOT_FOUND} 时映射为 404，其余业务异常统一映射为 400。</p>
     *
     * @param exception 业务异常
     * @return 统一错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        HttpStatus status = "NOT_FOUND".equals(exception.getCode()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(ApiResponse.error(exception.getCode(), exception.getMessage()));
    }

    /**
     * 处理请求体参数校验异常。
     *
     * @param exception Spring MVC 参数校验异常
     * @return 400 错误响应，优先返回首个字段错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        FieldError error = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", error == null ? "请求参数错误" : error.getDefaultMessage()));
    }

    /**
     * 处理路径参数、查询参数等约束校验异常。
     *
     * @param exception 约束校验异常
     * @return 400 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", exception.getMessage()));
    }

    /**
     * 处理其他未捕获异常。
     *
     * @param exception 系统异常
     * @return 500 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_SERVER_ERROR", exception.getMessage()));
    }
}
