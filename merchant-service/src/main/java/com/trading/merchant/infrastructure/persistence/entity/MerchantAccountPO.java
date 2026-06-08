package com.trading.merchant.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.math.BigDecimal;
import lombok.Data;

@Data
@TableName("merchant_account")
public class MerchantAccountPO {
    @TableId
    private String merchantId;
    private BigDecimal balance;
    @Version
    private Integer version;
}
