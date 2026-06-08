package com.trading.merchant.application;

import com.trading.merchant.application.dto.SettlementRecordDTO;
import com.trading.merchant.domain.model.Money;
import com.trading.merchant.domain.model.SettlementRecord;
import com.trading.merchant.domain.repository.MerchantAccountRepository;
import com.trading.merchant.domain.repository.ProductInventoryRepository;
import com.trading.merchant.domain.repository.SettlementRecordRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 结算应用服务。
 *
 * <p>负责按商家汇总销售数据与账户余额，生成每日结算记录，并提供结算历史查询能力。
 * 该服务将领域对象计算结果转换为面向接口层的结算展示数据。</p>
 */
@Service
@RequiredArgsConstructor
public class SettlementApplicationService {
    private final MerchantAccountRepository merchantAccountRepository;
    private final ProductInventoryRepository productInventoryRepository;
    private final SettlementRecordRepository settlementRecordRepository;

    /**
     * 执行指定日期的商家结算。
     *
     * <p>系统会遍历全部商家账户，汇总该商家名下商品的已售金额作为应结金额，
     * 再将商家账户余额作为实结金额，最终生成结算记录并持久化保存。</p>
     *
     * @param date 需要执行结算的业务日期
     */
    @Transactional
    public void executeSettlement(LocalDate date) {
        for (String merchantId : merchantAccountRepository.findAllMerchantIds()) {
            BigDecimal expected = productInventoryRepository.findByMerchantId(merchantId).stream()
                .map(inventory -> inventory.getPrice().getAmount().multiply(BigDecimal.valueOf(inventory.getSoldQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal actual = merchantAccountRepository.findByMerchantId(merchantId)
                .map(account -> account.getBalance().getAmount())
                .orElse(BigDecimal.ZERO);
            settlementRecordRepository.save(SettlementRecord.create(
                merchantId,
                date,
                Money.of(expected),
                Money.of(actual)
            ));
        }
    }

    /**
     * 查询商家结算记录列表。
     *
     * @param merchantId 商家标识
     * @return 结算记录展示对象列表
     */
    public List<SettlementRecordDTO> listSettlements(String merchantId) {
        return settlementRecordRepository.findByMerchantId(merchantId).stream()
            .map(record -> SettlementRecordDTO.builder()
                .settlementDate(record.getSettlementDate())
                .expectedAmount(record.getExpectedAmount().getAmount())
                .actualAmount(record.getActualAmount().getAmount())
                .status(record.getStatus())
                .remark(record.getRemark())
                .createdAt(record.getCreatedAt())
                .build())
            .toList();
    }
}
