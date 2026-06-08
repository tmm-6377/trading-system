package com.trading.user.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trading.user.domain.model.AccountTransaction;
import com.trading.user.domain.model.Money;
import com.trading.user.domain.repository.AccountTransactionRepository;
import com.trading.user.infrastructure.persistence.entity.AccountTransactionPO;
import com.trading.user.infrastructure.persistence.mapper.AccountTransactionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountTransactionRepositoryImpl implements AccountTransactionRepository {
    private final AccountTransactionMapper accountTransactionMapper;

    @Override
    public AccountTransaction save(AccountTransaction transaction) {
        AccountTransactionPO po = toPO(transaction);
        accountTransactionMapper.insert(po);
        return toDomain(po);
    }

    @Override
    public List<AccountTransaction> findByUserId(String userId) {
        return accountTransactionMapper.selectList(new LambdaQueryWrapper<AccountTransactionPO>()
                .eq(AccountTransactionPO::getUserId, userId)
                .orderByDesc(AccountTransactionPO::getCreatedAt))
            .stream()
            .map(this::toDomain)
            .toList();
    }

    private AccountTransactionPO toPO(AccountTransaction transaction) {
        AccountTransactionPO po = new AccountTransactionPO();
        po.setId(transaction.getId());
        po.setUserId(transaction.getUserId());
        po.setOrderNo(transaction.getOrderNo());
        po.setTransactionType(transaction.getTransactionType());
        po.setAmount(transaction.getAmount().getAmount());
        po.setBalanceAfter(transaction.getBalanceAfter().getAmount());
        po.setDescription(transaction.getDescription());
        po.setCreatedAt(transaction.getCreatedAt());
        return po;
    }

    private AccountTransaction toDomain(AccountTransactionPO po) {
        return AccountTransaction.builder()
            .id(po.getId())
            .userId(po.getUserId())
            .orderNo(po.getOrderNo())
            .transactionType(po.getTransactionType())
            .amount(Money.of(po.getAmount()))
            .balanceAfter(Money.of(po.getBalanceAfter()))
            .description(po.getDescription())
            .createdAt(po.getCreatedAt())
            .build();
    }
}
