package com.trading.merchant.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trading.common.exception.BusinessException;
import com.trading.merchant.domain.model.MerchantAccount;
import com.trading.merchant.domain.model.Money;
import com.trading.merchant.domain.repository.MerchantAccountRepository;
import com.trading.merchant.infrastructure.persistence.entity.MerchantAccountPO;
import com.trading.merchant.infrastructure.persistence.mapper.MerchantAccountMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MerchantAccountRepositoryImpl implements MerchantAccountRepository {
    private final MerchantAccountMapper merchantAccountMapper;

    @Override
    public Optional<MerchantAccount> findByMerchantId(String merchantId) {
        return Optional.ofNullable(merchantAccountMapper.selectById(merchantId)).map(this::toDomain);
    }

    @Override
    public MerchantAccount save(MerchantAccount merchantAccount) {
        MerchantAccountPO po = toPO(merchantAccount);
        if (merchantAccountMapper.selectById(po.getMerchantId()) == null) {
            merchantAccountMapper.insert(po);
            return toDomain(po);
        }
        if (merchantAccountMapper.updateById(po) == 0) {
            throw new BusinessException("CONCURRENT_MODIFICATION", "商家账户更新失败，请稍后重试");
        }
        return toDomain(merchantAccountMapper.selectById(po.getMerchantId()));
    }

    @Override
    public List<String> findAllMerchantIds() {
        return merchantAccountMapper.selectList(new LambdaQueryWrapper<>()).stream().map(MerchantAccountPO::getMerchantId).toList();
    }

    private MerchantAccount toDomain(MerchantAccountPO po) {
        return MerchantAccount.builder()
            .merchantId(po.getMerchantId())
            .balance(Money.of(po.getBalance()))
            .version(po.getVersion())
            .build();
    }

    private MerchantAccountPO toPO(MerchantAccount merchantAccount) {
        MerchantAccountPO po = new MerchantAccountPO();
        po.setMerchantId(merchantAccount.getMerchantId());
        po.setBalance(merchantAccount.getBalance().getAmount());
        po.setVersion(merchantAccount.getVersion());
        return po;
    }
}
