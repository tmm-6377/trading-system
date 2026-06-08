package com.trading.merchant.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * 商家账户领域模型。
 *
 * <p>该模型用于维护商家的结算账户余额和版本号信息，承担订单成交后资金入账的核心职责。
 * 账户初始余额为零，后续所有入账操作都通过该模型统一完成，便于在领域层收敛资金累加规则。</p>
 */
@Getter
@Builder
public class MerchantAccount {
    private final String merchantId;
    private Money balance;
    private Integer version;

    /**
     * 创建商家账户。
     *
     * <p>新建账户时会以零金额作为初始余额，并将版本号初始化为 0，便于后续持久化层执行乐观锁控制。</p>
     *
     * @param merchantId 商家标识
     * @return 新创建的商家账户对象
     */
    public static MerchantAccount create(String merchantId) {
        return MerchantAccount.builder()
            .merchantId(merchantId)
            .balance(Money.zero())
            .version(0)
            .build();
    }

    /**
     * 为商家账户入账。
     *
     * <p>该方法会将传入金额累加到当前余额中，适用于订单支付成功后的商家收益入账场景。</p>
     *
     * @param amount 本次入账金额
     */
    public void credit(Money amount) {
        this.balance = this.balance.add(amount);
    }
}
