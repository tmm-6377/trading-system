package com.trading.merchant.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trading.merchant.domain.model.Money;
import com.trading.merchant.domain.model.SettlementRecord;
import com.trading.merchant.domain.repository.SettlementRecordRepository;
import com.trading.merchant.infrastructure.persistence.entity.SettlementRecordPO;
import com.trading.merchant.infrastructure.persistence.mapper.SettlementRecordMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SettlementRecordRepositoryImpl implements SettlementRecordRepository {
    private final SettlementRecordMapper settlementRecordMapper;

    @Override
    public SettlementRecord save(SettlementRecord settlementRecord) {
        SettlementRecordPO po = toPO(settlementRecord);
        settlementRecordMapper.insert(po);
        return toDomain(po);
    }

    @Override
    public List<SettlementRecord> findByMerchantId(String merchantId) {
        return settlementRecordMapper.selectList(new LambdaQueryWrapper<SettlementRecordPO>()
                .eq(SettlementRecordPO::getMerchantId, merchantId)
                .orderByDesc(SettlementRecordPO::getSettlementDate))
            .stream()
            .map(this::toDomain)
            .toList();
    }

    private SettlementRecordPO toPO(SettlementRecord record) {
        SettlementRecordPO po = new SettlementRecordPO();
        po.setId(record.getId());
        po.setMerchantId(record.getMerchantId());
        po.setSettlementDate(record.getSettlementDate());
        po.setExpectedAmount(record.getExpectedAmount().getAmount());
        po.setActualAmount(record.getActualAmount().getAmount());
        po.setStatus(record.getStatus());
        po.setRemark(record.getRemark());
        po.setCreatedAt(record.getCreatedAt());
        return po;
    }

    private SettlementRecord toDomain(SettlementRecordPO po) {
        return SettlementRecord.builder()
            .id(po.getId())
            .merchantId(po.getMerchantId())
            .settlementDate(po.getSettlementDate())
            .expectedAmount(Money.of(po.getExpectedAmount()))
            .actualAmount(Money.of(po.getActualAmount()))
            .status(po.getStatus())
            .remark(po.getRemark())
            .createdAt(po.getCreatedAt())
            .build();
    }
}
