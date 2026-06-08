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

@Service
@RequiredArgsConstructor
public class SettlementApplicationService {
    private final MerchantAccountRepository merchantAccountRepository;
    private final ProductInventoryRepository productInventoryRepository;
    private final SettlementRecordRepository settlementRecordRepository;

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
