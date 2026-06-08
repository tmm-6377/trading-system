package com.trading.user.domain.repository;

import com.trading.user.domain.model.UserAccount;
import java.util.Optional;

public interface UserAccountRepository {
    Optional<UserAccount> findByUserId(String userId);
    UserAccount save(UserAccount userAccount);
}
