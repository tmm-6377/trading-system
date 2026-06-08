package com.trading.merchant.domain.model;

import com.trading.common.exception.BusinessException;
import com.trading.common.exception.InsufficientStockException;
import lombok.Builder;
import lombok.Getter;

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

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "库存数量必须大于0");
        }
        this.availableQuantity += quantity;
    }

    public void rollbackStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "回滚数量必须大于0");
        }
        this.availableQuantity += quantity;
        this.soldQuantity = Math.max(0, this.soldQuantity - quantity);
    }

    public Money calculateSoldValue() {
        return price == null ? Money.zero() : price.multiply(soldQuantity);
    }
}
