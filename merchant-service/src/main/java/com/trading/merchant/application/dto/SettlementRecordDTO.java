package com.trading.merchant.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SettlementRecordDTO {
    LocalDate settlementDate;
    BigDecimal expectedAmount;
    BigDecimal actualAmount;
    String status;
    String remark;
    LocalDateTime createdAt;
}
