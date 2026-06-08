package com.trading.user.domain.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 订单聚合根
 *
 * <p>表示用户的一次购买行为，记录订单的完整信息。</p>
 *
 * <p>订单生命周期：</p>
 * <ul>
 *   <li>CREATED - 订单已创建（初始状态）</li>
 *   <li>PAID - 已付款（未来扩展）</li>
 *   <li>CANCELLED - 已取消（未来扩展）</li>
 * </ul>
 *
 * <p>订单号规则：格式为 ORD-{16位大写十六进制UUID}</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Getter
@Builder
public class Order {

    /** 数据库主键ID（自增） */
    private Long id;

    /** 订单号（业务主键，格式: ORD-XXXXXXXXXXXXXXXX） */
    private String orderNo;

    /** 用户ID */
    private String userId;

    /** 商家ID */
    private String merchantId;

    /** 商品SKU */
    private String sku;

    /** 购买数量 */
    private Integer quantity;

    /** 商品单价 */
    private Money unitPrice;

    /** 订单总金额（= 单价 × 数量） */
    private Money totalAmount;

    /** 订单状态（CREATED/PAID/CANCELLED） */
    private String status;

    /** 订单创建时间 */
    private LocalDateTime createdAt;

    /**
     * 工厂方法：创建新订单
     *
     * <p>创建规则：</p>
     * <ul>
     *   <li>总金额自动计算：totalAmount = unitPrice × quantity</li>
     *   <li>初始状态为 CREATED</li>
     *   <li>创建时间设置为当前时间</li>
     * </ul>
     *
     * @param orderNo    订单号
     * @param userId     用户ID
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @param quantity   购买数量
     * @param unitPrice  商品单价
     * @return 新创建的订单
     */
    public static Order create(String orderNo, String userId, String merchantId, String sku, int quantity, Money unitPrice) {
        return Order.builder()
            .orderNo(orderNo)
            .userId(userId)
            .merchantId(merchantId)
            .sku(sku)
            .quantity(quantity)
            .unitPrice(unitPrice)
            // 总金额 = 单价 × 数量
            .totalAmount(unitPrice.multiply(quantity))
            .status("CREATED")
            .createdAt(LocalDateTime.now())
            .build();
    }
}
