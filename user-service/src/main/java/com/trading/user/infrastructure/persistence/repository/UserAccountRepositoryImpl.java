package com.trading.user.infrastructure.persistence.repository;

import com.trading.common.exception.BusinessException;
import com.trading.user.domain.model.Money;
import com.trading.user.domain.model.UserAccount;
import com.trading.user.domain.repository.UserAccountRepository;
import com.trading.user.infrastructure.persistence.entity.UserAccountPO;
import com.trading.user.infrastructure.persistence.mapper.UserAccountMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserAccountRepositoryImpl implements UserAccountRepository {
    private final UserAccountMapper userAccountMapper;

    @Override
    public Optional<UserAccount> findByUserId(String userId) {
        return Optional.ofNullable(userAccountMapper.selectById(userId)).map(this::toDomain);
    }

    @Override
    public UserAccount save(UserAccount userAccount) {
        UserAccountPO po = toPO(userAccount);
        if (userAccountMapper.selectById(po.getUserId()) == null) {
            userAccountMapper.insert(po);
            userAccount = toDomain(po);
        } else {
            int updated = userAccountMapper.updateById(po);
            if (updated == 0) {
                throw new BusinessException("CONCURRENT_MODIFICATION", "用户账户更新失败，请稍后重试");
            }
            userAccount = toDomain(userAccountMapper.selectById(po.getUserId()));
        }
        return userAccount;
    }

    private UserAccount toDomain(UserAccountPO po) {
        return UserAccount.builder()
            .userId(po.getUserId())
            .balance(Money.of(po.getBalance()))
            .version(po.getVersion())
            .build();
    }

    private UserAccountPO toPO(UserAccount account) {
        UserAccountPO po = new UserAccountPO();
        po.setUserId(account.getUserId());
        po.setBalance(account.getBalance().getAmount());
        po.setVersion(account.getVersion());
        return po;
    }
}
