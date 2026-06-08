package com.trading.merchant.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("settlement_record")
public class SettlementRecordPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String merchantId;
    private LocalDate settlementDate;
    private BigDecimal expectedAmount;
    private BigDecimal actualAmount;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
}
