package com.trading.merchant.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 金额值对象。
 *
 * <p>该对象统一封装系统中的金额计算逻辑，所有金额在创建时都会按两位小数、四舍五入规则进行标准化处理，
 * 以避免不同业务模块各自处理精度带来的不一致问题。</p>
 */
@Getter
@EqualsAndHashCode
public class Money {
    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 根据指定金额创建金额对象。
     *
     * <p>当传入金额为 {@code null} 时会自动按零金额处理，避免调用方在创建前重复进行空值判断。</p>
     *
     * @param amount 原始金额
     * @return 标准化后的金额对象
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount == null ? BigDecimal.ZERO : amount);
    }

    /**
     * 获取零金额对象。
     *
     * @return 值为 0.00 的金额对象
     */
    public static Money zero() {
        return of(BigDecimal.ZERO);
    }

    /**
     * 执行金额相加。
     *
     * @param other 需要累加的金额
     * @return 累加后的新金额对象
     */
    public Money add(Money other) {
        return of(amount.add(other.amount));
    }

    /**
     * 按整数倍数计算金额。
     *
     * <p>常用于单价乘数量的业务场景，例如计算订单总价或累计销售金额。</p>
     *
     * @param multiplier 乘数
     * @return 乘法计算后的新金额对象
     */
    public Money multiply(int multiplier) {
        return of(amount.multiply(BigDecimal.valueOf(multiplier)));
    }
}
