package com.trading.user.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 金额值对象
 *
 * <p>不可变的金额封装类，所有货币计算操作均返回新实例。</p>
 *
 * <p>精度规则：</p>
 * <ul>
 *   <li>所有金额统一保留2位小数</li>
 *   <li>使用 HALF_UP 四舍五入模式</li>
 *   <li>null 输入自动转换为0</li>
 * </ul>
 *
 * <p>不可变性：</p>
 * <p>所有计算方法（add、subtract、multiply）均返回新的 Money 实例，
 * 原实例的值不会被修改，保证线程安全。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Getter
@EqualsAndHashCode
public class Money {

    /** 金额数值，保留2位小数 */
    private final BigDecimal amount;

    /**
     * 私有构造函数，统一设置精度
     *
     * @param amount 金额原始值
     */
    private Money(BigDecimal amount) {
        // 统一四舍五入到2位小数
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 工厂方法：创建指定金额
     *
     * @param amount 金额，null时返回0
     * @return Money 实例
     */
    public static Money of(BigDecimal amount) {
        return new Money(amount == null ? BigDecimal.ZERO : amount);
    }

    /**
     * 工厂方法：创建零金额
     *
     * @return 金额为0的 Money 实例
     */
    public static Money zero() {
        return of(BigDecimal.ZERO);
    }

    /**
     * 金额相加
     *
     * @param other 加数，不能为null
     * @return 相加后的新 Money 实例
     */
    public Money add(Money other) {
        return of(amount.add(other.amount));
    }

    /**
     * 金额相减
     *
     * @param other 减数，不能为null
     * @return 相减后的新 Money 实例（可能为负数）
     */
    public Money subtract(Money other) {
        return of(amount.subtract(other.amount));
    }

    /**
     * 金额乘以整数倍
     *
     * @param multiplier 乘数
     * @return 相乘后的新 Money 实例
     */
    public Money multiply(int multiplier) {
        return of(amount.multiply(BigDecimal.valueOf(multiplier)));
    }

    /**
     * 判断当前金额是否小于另一金额
     *
     * @param other 比较的金额
     * @return 如果当前金额小于 other 返回 true
     */
    public boolean lessThan(Money other) {
        return amount.compareTo(other.amount) < 0;
    }

    /**
     * 判断金额是否为负数或零
     *
     * @return 如果金额 &lt;= 0 返回 true
     */
    public boolean isNegativeOrZero() {
        return amount.compareTo(BigDecimal.ZERO) <= 0;
    }
}
