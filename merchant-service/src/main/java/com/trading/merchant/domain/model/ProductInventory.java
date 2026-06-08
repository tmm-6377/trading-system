package com.trading.merchant.domain.model;

import com.trading.common.exception.BusinessException;
import com.trading.common.exception.InsufficientStockException;
import lombok.Builder;
import lombok.Getter;

/**
 * 商品库存领域模型。
 *
 * <p>该模型描述商家维度下某个商品 SKU 的库存状态，统一封装商品名称、销售单价、可售库存、
 * 已售数量以及并发控制版本号等信息。所有库存变更操作都应通过该对象完成，以确保数量校验、
 * 库存不足判断和销售金额统计等业务规则在领域层保持一致。</p>
 */
@Getter
@Builder
public class ProductInventory {
    private Long id;
    private String merchantId;
    private SKU sku;
    private String productName;
    private Money price;
    private Integer availableQuantity;
    private Integer soldQuantity;
    private Integer version;

    /**
     * 创建新的商品库存对象。
     *
     * <p>创建时会初始化可售库存、已售数量和版本号，其中已售数量默认为 0，版本号默认为 0。
     * 当传入库存数量小于等于 0 时会抛出业务异常，防止生成非法库存数据。</p>
     *
     * @param merchantId 商家标识
     * @param sku 商品 SKU 编码
     * @param productName 商品名称
     * @param price 商品单价
     * @param quantity 初始库存数量，必须大于 0
     * @return 初始化完成的商品库存对象
     * @throws BusinessException 当库存数量非法时抛出
     */
    public static ProductInventory create(String merchantId, String sku, String productName, Money price, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "库存数量必须大于0");
        }
        return ProductInventory.builder()
            .merchantId(merchantId)
            .sku(SKU.of(sku))
            .productName(productName)
            .price(price)
            .availableQuantity(quantity)
            .soldQuantity(0)
            .version(0)
            .build();
    }

    /**
     * 校验库存并执行扣减。
     *
     * <p>该方法用于订单确认场景，会先校验购买数量是否合法，再判断当前可售库存是否足够。
     * 校验通过后同步减少可售库存并增加已售数量，保证库存和销量的变更原子一致。</p>
     *
     * @param quantity 本次需要扣减的数量，必须大于 0
     * @throws BusinessException 当购买数量小于等于 0 时抛出
     * @throws InsufficientStockException 当可售库存不足时抛出
     */
    public void checkAndDeductStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "购买数量必须大于0");
        }
        if (availableQuantity < quantity) {
            throw new InsufficientStockException("库存不足");
        }
        this.availableQuantity -= quantity;
        this.soldQuantity += quantity;
    }

    /**
     * 为商品补充库存。
     *
     * <p>该方法仅增加可售库存，不影响已售数量，可用于商家上架补货或人工盘点修正库存。
     * 传入数量必须为正数，否则会拒绝更新。</p>
     *
     * @param quantity 本次新增的库存数量，必须大于 0
     * @throws BusinessException 当库存数量非法时抛出
     */
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "库存数量必须大于0");
        }
        this.availableQuantity += quantity;
    }

    /**
     * 回滚已经扣减的库存。
     *
     * <p>通常用于订单取消或下单失败补偿场景。回滚时会恢复可售库存，并按数量回退已售数量；
     * 若回滚数量大于当前已售数量，则已售数量会被重置为 0，避免出现负数。</p>
     *
     * @param quantity 本次回滚数量，必须大于 0
     * @throws BusinessException 当回滚数量非法时抛出
     */
    public void rollbackStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "回滚数量必须大于0");
        }
        this.availableQuantity += quantity;
        this.soldQuantity = Math.max(0, this.soldQuantity - quantity);
    }

    /**
     * 计算累计已售商品金额。
     *
     * <p>当商品价格为空时返回零金额，避免空指针异常；否则按单价乘以已售数量返回累计销售额。</p>
     *
     * @return 累计已售金额
     */
    public Money calculateSoldValue() {
        return price == null ? Money.zero() : price.multiply(soldQuantity);
    }
}
