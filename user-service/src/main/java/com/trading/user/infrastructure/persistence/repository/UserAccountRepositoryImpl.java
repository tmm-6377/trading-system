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

/**
 * 用户账户仓储实现
 *
 * <p>基于 MyBatis-Plus 实现用户账户的持久化操作。</p>
 *
 * <p>并发控制：</p>
 * <p>使用 MyBatis-Plus 的乐观锁插件（@Version 注解），当并发更新冲突时
 * updateById 返回0，此时抛出 BusinessException。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class UserAccountRepositoryImpl implements UserAccountRepository {

    /** MyBatis-Plus 数据库操作 Mapper */
    private final UserAccountMapper userAccountMapper;

    /**
     * 根据用户ID查询账户
     *
     * @param userId 用户ID（数据库主键）
     * @return 用户账户Optional，不存在时返回 Optional.empty()
     */
    @Override
    public Optional<UserAccount> findByUserId(String userId) {
        return Optional.ofNullable(userAccountMapper.selectById(userId)).map(this::toDomain);
    }

    /**
     * 保存用户账户（新增或更新）
     *
     * <p>逻辑：</p>
     * <ul>
     *   <li>如果账户不存在（selectById 返回 null），执行 insert</li>
     *   <li>如果账户已存在，执行 update（包含乐观锁版本号检查）</li>
     *   <li>如果 update 影响行数为0，说明发生并发冲突，抛出异常</li>
     * </ul>
     *
     * @param userAccount 待保存的用户账户领域对象
     * @return 保存后的最新账户信息
     * @throws BusinessException 如果乐观锁冲突导致更新失败
     */
    @Override
    public UserAccount save(UserAccount userAccount) {
        UserAccountPO po = toPO(userAccount);
        if (userAccountMapper.selectById(po.getUserId()) == null) {
            // 新账户：执行插入
            userAccountMapper.insert(po);
            userAccount = toDomain(po);
        } else {
            // 已有账户：执行乐观锁更新
            int updated = userAccountMapper.updateById(po);
            if (updated == 0) {
                // 乐观锁冲突：版本号不匹配
                throw new BusinessException("CONCURRENT_MODIFICATION", "用户账户更新失败，请稍后重试");
            }
            userAccount = toDomain(userAccountMapper.selectById(po.getUserId()));
        }
        return userAccount;
    }

    /**
     * 将持久化对象转换为领域对象
     *
     * @param po 持久化对象
     * @return 用户账户领域对象
     */
    private UserAccount toDomain(UserAccountPO po) {
        return UserAccount.builder()
            .userId(po.getUserId())
            .balance(Money.of(po.getBalance()))
            .version(po.getVersion())
            .build();
    }

    /**
     * 将领域对象转换为持久化对象
     *
     * @param account 用户账户领域对象
     * @return 持久化对象
     */
    private UserAccountPO toPO(UserAccount account) {
        UserAccountPO po = new UserAccountPO();
        po.setUserId(account.getUserId());
        po.setBalance(account.getBalance().getAmount());
        po.setVersion(account.getVersion());
        return po;
    }
}
