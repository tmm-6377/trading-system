package com.trading.user.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("account_transaction")
public class AccountTransactionPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String orderNo;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private LocalDateTime createdAt;
}
