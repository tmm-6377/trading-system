package com.trading.merchant.domain.repository;

import com.trading.merchant.domain.model.MerchantAccount;
import java.util.List;
import java.util.Optional;

public interface MerchantAccountRepository {
    Optional<MerchantAccount> findByMerchantId(String merchantId);
    MerchantAccount save(MerchantAccount merchantAccount);
    List<String> findAllMerchantIds();
}
