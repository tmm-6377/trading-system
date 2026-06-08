package com.trading.merchant.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("product_inventory")
public class ProductInventoryPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String merchantId;
    private String sku;
    private String productName;
    private BigDecimal price;
    private Integer availableQuantity;
    private Integer soldQuantity;
    @Version
    private Integer version;
}
