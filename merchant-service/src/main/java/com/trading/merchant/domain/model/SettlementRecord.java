package com.trading.merchant.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 商家结算记录领域模型。
 *
 * <p>结算记录用于保存某个商家在指定结算日的应结金额、实结金额以及对账结果。
 * 通过比较应结金额与实结金额是否一致，系统能够自动生成结算状态与备注，辅助财务人员进行对账审核。</p>
 */
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

    /**
     * 创建结算记录。
     *
     * <p>当应结金额与实结金额完全一致时，记录会被标记为 {@code MATCH}，备注为“结算正常”；
     * 否则标记为 {@code MISMATCH}，提示人工核对差异。记录创建时间取当前系统时间。</p>
     *
     * @param merchantId 商家标识
     * @param settlementDate 结算日期
     * @param expectedAmount 根据销售数据计算出的应结金额
     * @param actualAmount 商家账户中实际入账金额
     * @return 生成后的结算记录对象
     */
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
