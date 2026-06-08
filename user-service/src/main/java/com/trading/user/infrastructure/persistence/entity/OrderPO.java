package com.trading.user.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("trade_order")
public class OrderPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String userId;
    private String merchantId;
    private String sku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}
