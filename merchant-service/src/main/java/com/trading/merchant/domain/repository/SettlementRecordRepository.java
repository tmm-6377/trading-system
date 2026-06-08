package com.trading.merchant.domain.repository;

import com.trading.merchant.domain.model.SettlementRecord;
import java.util.List;

public interface SettlementRecordRepository {
    SettlementRecord save(SettlementRecord settlementRecord);
    List<SettlementRecord> findByMerchantId(String merchantId);
}
