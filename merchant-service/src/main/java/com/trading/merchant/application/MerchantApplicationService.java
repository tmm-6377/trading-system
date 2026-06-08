package com.trading.merchant.application;

import com.trading.common.exception.NotFoundException;
import com.trading.merchant.application.dto.MerchantAccountDTO;
import com.trading.merchant.domain.repository.MerchantAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantApplicationService {
    private final MerchantAccountRepository merchantAccountRepository;

    public MerchantAccountDTO getAccount(String merchantId) {
        return merchantAccountRepository.findByMerchantId(merchantId)
            .map(account -> MerchantAccountDTO.builder()
                .merchantId(account.getMerchantId())
                .balance(account.getBalance().getAmount())
                .build())
            .orElseThrow(() -> new NotFoundException("商家账户不存在"));
    }
}
