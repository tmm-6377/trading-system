package com.trading.user.domain.repository;

import com.trading.user.domain.model.AccountTransaction;
import java.util.List;

public interface AccountTransactionRepository {
    AccountTransaction save(AccountTransaction transaction);
    List<AccountTransaction> findByUserId(String userId);
}
