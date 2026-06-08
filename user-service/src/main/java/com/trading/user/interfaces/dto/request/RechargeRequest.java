package com.trading.user.interfaces.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 充值请求 DTO
 *
 * <p>用户账户充值的请求参数。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Data
public class RechargeRequest {

    /**
     * 充值金额
     *
     * <p>校验规则：不能为null，且最小值为0.01（必须大于0）</p>
     */
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
}
