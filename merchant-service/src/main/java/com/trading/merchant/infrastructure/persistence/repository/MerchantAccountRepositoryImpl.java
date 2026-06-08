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

/**
 * 商家账户仓储实现。
 *
 * <p>负责完成商家账户领域对象与数据库记录之间的映射，并对账户新增、更新、查询等操作进行统一封装。</p>
 */
@Repository
@RequiredArgsConstructor
public class MerchantAccountRepositoryImpl implements MerchantAccountRepository {
    private final MerchantAccountMapper merchantAccountMapper;

    /**
     * 根据商家标识查询账户。
     *
     * @param merchantId 商家标识
     * @return 商家账户，若不存在则返回空
     */
    @Override
    public Optional<MerchantAccount> findByMerchantId(String merchantId) {
        return Optional.ofNullable(merchantAccountMapper.selectById(merchantId)).map(this::toDomain);
    }

    /**
     * 保存商家账户。
     *
     * <p>若数据库中不存在该商家账户则执行插入，否则执行更新；更新失败时抛出并发修改异常。</p>
     *
     * @param merchantAccount 商家账户领域对象
     * @return 保存后的账户对象
     */
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

    /**
     * 查询全部商家标识。
     *
     * @return 系统中所有商家账户对应的商家标识列表
     */
    @Override
    public List<String> findAllMerchantIds() {
        return merchantAccountMapper.selectList(new LambdaQueryWrapper<>()).stream().map(MerchantAccountPO::getMerchantId).toList();
    }

    /**
     * 将持久化对象转换为领域对象。
     *
     * @param po 持久化对象
     * @return 领域对象
     */
    private MerchantAccount toDomain(MerchantAccountPO po) {
        return MerchantAccount.builder()
            .merchantId(po.getMerchantId())
            .balance(Money.of(po.getBalance()))
            .version(po.getVersion())
            .build();
    }

    /**
     * 将领域对象转换为持久化对象。
     *
     * @param merchantAccount 领域对象
     * @return 持久化对象
     */
    private MerchantAccountPO toPO(MerchantAccount merchantAccount) {
        MerchantAccountPO po = new MerchantAccountPO();
        po.setMerchantId(merchantAccount.getMerchantId());
        po.setBalance(merchantAccount.getBalance().getAmount());
        po.setVersion(merchantAccount.getVersion());
        return po;
    }
}
