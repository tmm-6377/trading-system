package com.trading.merchant.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementRecord {
    private Long id;
    private String merchantId;
    private LocalDate settlementDate;
    private Money expectedAmount;
    private Money actualAmount;
    private String status;
    private String remark;
    private LocalDateTime createdAt;

    public static SettlementRecord create(String merchantId, LocalDate settlementDate, Money expectedAmount, Money actualAmount) {
        boolean matched = expectedAmount.getAmount().compareTo(actualAmount.getAmount()) == 0;
        return SettlementRecord.builder()
            .merchantId(merchantId)
            .settlementDate(settlementDate)
            .expectedAmount(expectedAmount)
            .actualAmount(actualAmount)
            .status(matched ? "MATCH" : "MISMATCH")
            .remark(matched ? "结算正常" : "结算差异，请人工核对")
            .createdAt(LocalDateTime.now())
            .build();
    }
}
