package com.trading.merchant.application;

import com.trading.common.exception.NotFoundException;
import com.trading.merchant.application.dto.MerchantAccountDTO;
import com.trading.merchant.domain.repository.MerchantAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 商家应用服务。
 *
 * <p>对外提供商家账户相关查询能力，将领域模型转换为适合接口层返回的数据传输对象。</p>
 */
@Service
@RequiredArgsConstructor
public class MerchantApplicationService {
    private final MerchantAccountRepository merchantAccountRepository;

    /**
     * 查询商家账户信息。
     *
     * @param merchantId 商家标识
     * @return 商家账户展示对象
     * @throws NotFoundException 当商家账户不存在时抛出
     */
    public MerchantAccountDTO getAccount(String merchantId) {
        return merchantAccountRepository.findByMerchantId(merchantId)
            .map(account -> MerchantAccountDTO.builder()
                .merchantId(account.getMerchantId())
                .balance(account.getBalance().getAmount())
                .build())
            .orElseThrow(() -> new NotFoundException("商家账户不存在"));
    }
}
