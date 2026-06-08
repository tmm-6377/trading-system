package com.trading.user.domain.model;

import com.trading.common.exception.BusinessException;
import com.trading.common.exception.InsufficientBalanceException;
import lombok.Builder;
import lombok.Getter;

/**
 * 用户账户聚合根
 *
 * <p>职责：</p>
 * <ul>
 *   <li>管理用户账户余额</li>
 *   <li>提供充值功能</li>
 *   <li>提供扣款功能</li>
 *   <li>保证账户余额的一致性</li>
 * </ul>
 *
 * <p>领域规则：</p>
 * <ul>
 *   <li>余额不能为负数</li>
 *   <li>充值和扣款金额必须大于0</li>
 *   <li>扣款金额不能超过当前余额</li>
 *   <li>金额精度保留2位小数</li>
 *   <li>使用乐观锁（version字段）处理并发更新</li>
 * </ul>
 *
 * <p>并发控制：</p>
 * <p>通过 version 字段实现乐观锁，防止并发更新导致的数据不一致</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Getter
@Builder
public class UserAccount {

    /**
     * 用户ID（业务主键，唯一）
     */
    private final String userId;

    /**
     * 账户余额（精度2位小数，不能为负）
     */
    private Money balance;

    /**
     * 乐观锁版本号（每次更新自动+1）
     */
    private Integer version;

    /**
     * 工厂方法：创建新用户账户
     *
     * <p>初始状态：</p>
     * <ul>
     *   <li>余额为0</li>
     *   <li>版本号为0</li>
     * </ul>
     *
     * @param userId 用户ID，不能为空
     * @return 新创建的用户账户
     */
    public static UserAccount create(String userId) {
        return UserAccount.builder()
            .userId(userId)
            .balance(Money.zero())
            .version(0)
            .build();
    }

    /**
     * 充值
     *
     * <p>业务规则：</p>
     * <ul>
     *   <li>充值金额必须大于0</li>
     *   <li>充值后余额自动累加</li>
     * </ul>
     *
     * @param amount 充值金额，必须大于0
     * @throws BusinessException 如果充值金额小于等于0
     */
    public void recharge(Money amount) {
        validatePositive(amount);
        // 累加余额
        this.balance = this.balance.add(amount);
    }

    /**
     * 扣款
     *
     * <p>业务规则：</p>
     * <ul>
     *   <li>扣款金额必须大于0</li>
     *   <li>扣款金额不能超过当前余额</li>
     *   <li>扣款失败时余额不变</li>
     * </ul>
     *
     * @param amount 扣款金额，必须大于0且不超过余额
     * @throws BusinessException              如果扣款金额小于等于0
     * @throws InsufficientBalanceException 如果余额不足
     */
    public void deduct(Money amount) {
        validatePositive(amount);
        // 检查余额是否充足
        if (this.balance.lessThan(amount)) {
            throw new InsufficientBalanceException("账户余额不足");
        }
        // 扣减余额
        this.balance = this.balance.subtract(amount);
    }

    /**
     * 验证金额必须大于0
     *
     * @param amount 待验证的金额
     * @throws BusinessException 如果金额为null或小于等于0
     */
    private void validatePositive(Money amount) {
        if (amount == null || amount.isNegativeOrZero()) {
            throw new BusinessException("INVALID_AMOUNT", "金额必须大于0");
        }
    }
}
