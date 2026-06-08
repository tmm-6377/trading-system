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

@Service
@RequiredArgsConstructor
public class UserAccountApplicationService {
    private final UserAccountRepository userAccountRepository;
    private final AccountTransactionRepository accountTransactionRepository;

    @Transactional
    public AccountDTO recharge(RechargeCommand command) {
        UserAccount account = userAccountRepository.findByUserId(command.getUserId())
            .orElse(UserAccount.create(command.getUserId()));
        Money amount = Money.of(command.getAmount());
        account.recharge(amount);
        UserAccount saved = userAccountRepository.save(account);
        accountTransactionRepository.save(AccountTransaction.recharge(saved.getUserId(), amount, saved.getBalance()));
        return toAccountDTO(saved);
    }

    public AccountDTO getAccount(String userId) {
        return userAccountRepository.findByUserId(userId)
            .map(this::toAccountDTO)
            .orElseThrow(() -> new NotFoundException("用户账户不存在"));
    }

    public List<TransactionDTO> getTransactions(String userId) {
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

    private AccountDTO toAccountDTO(UserAccount account) {
        return AccountDTO.builder()
            .userId(account.getUserId())
            .balance(account.getBalance().getAmount())
            .build();
    }
}
