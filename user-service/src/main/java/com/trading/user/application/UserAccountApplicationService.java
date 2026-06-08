package com.trading.user.application;

import com.trading.common.exception.NotFoundException;
import com.trading.user.application.dto.AccountDTO;
import com.trading.user.application.dto.RechargeCommand;
import com.trading.user.application.dto.TransactionDTO;
import com.trading.user.domain.model.AccountTransaction;
import com.trading.user.domain.model.Money;
import com.trading.user.domain.model.UserAccount;
import com.trading.user.domain.repository.AccountTransactionRepository;
import com.trading.user.domain.repository.UserAccountRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户账户应用服务
 *
 * <p>负责用户账户相关的业务操作，包括充值、查询账户信息和查询账户流水。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>协调领域对象和仓储完成充值业务</li>
 *   <li>处理账户查询逻辑</li>
 *   <li>记录账户流水（充值记录）</li>
 * </ul>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserAccountApplicationService {

    /** 用户账户仓储 */
    private final UserAccountRepository userAccountRepository;

    /** 账户流水仓储 */
    private final AccountTransactionRepository accountTransactionRepository;

    /**
     * 充值
     *
     * <p>充值流程：</p>
     * <ol>
     *   <li>根据userId查询账户，如果不存在则自动创建</li>
     *   <li>执行充值操作（余额累加）</li>
     *   <li>保存账户信息</li>
     *   <li>记录充值流水</li>
     * </ol>
     *
     * @param command 充值命令，包含userId和充值金额
     * @return 充值后的账户信息
     * @throws com.trading.common.exception.BusinessException 如果充值金额无效
     */
    @Transactional
    public AccountDTO recharge(RechargeCommand command) {
        // 查询账户，不存在则自动创建
        UserAccount account = userAccountRepository.findByUserId(command.getUserId())
            .orElse(UserAccount.create(command.getUserId()));
        Money amount = Money.of(command.getAmount());
        // 执行充值
        account.recharge(amount);
        // 保存账户
        UserAccount saved = userAccountRepository.save(account);
        // 记录充值流水
        accountTransactionRepository.save(AccountTransaction.recharge(saved.getUserId(), amount, saved.getBalance()));
        return toAccountDTO(saved);
    }

    /**
     * 查询账户信息
     *
     * @param userId 用户ID
     * @return 账户信息DTO
     * @throws NotFoundException 如果账户不存在
     */
    public AccountDTO getAccount(String userId) {
        return userAccountRepository.findByUserId(userId)
            .map(this::toAccountDTO)
            .orElseThrow(() -> new NotFoundException("用户账户不存在"));
    }

    /**
     * 查询账户流水
     *
     * @param userId 用户ID
     * @return 账户流水列表
     * @throws NotFoundException 如果账户不存在
     */
    public List<TransactionDTO> getTransactions(String userId) {
        // 先验证用户账户存在
        if (userAccountRepository.findByUserId(userId).isEmpty()) {
            throw new NotFoundException("用户账户不存在");
        }
        return accountTransactionRepository.findByUserId(userId).stream()
            .map(transaction -> TransactionDTO.builder()
                .orderNo(transaction.getOrderNo())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount().getAmount())
                .balanceAfter(transaction.getBalanceAfter().getAmount())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build())
            .toList();
    }

    /**
     * 将领域对象转换为DTO
     *
     * @param account 用户账户领域对象
     * @return 账户DTO
     */
    private AccountDTO toAccountDTO(UserAccount account) {
        return AccountDTO.builder()
            .userId(account.getUserId())
            .balance(account.getBalance().getAmount())
            .build();
    }
}
