package com.trading.merchant.application;

import com.trading.common.exception.NotFoundException;
import com.trading.merchant.application.dto.MerchantAccountDTO;
import com.trading.merchant.domain.model.MerchantAccount;
import com.trading.merchant.domain.model.Money;
import com.trading.merchant.domain.repository.MerchantAccountRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantApplicationServiceTest {

    @Mock
    private MerchantAccountRepository merchantAccountRepository;
    @InjectMocks
    private MerchantApplicationService merchantApplicationService;

    @Test
    void testGetAccount_Success() {
        MerchantAccount account = MerchantAccount.builder()
            .merchantId("merchant001")
            .balance(Money.of(new BigDecimal("88.88")))
            .version(0)
            .build();
        when(merchantAccountRepository.findByMerchantId("merchant001")).thenReturn(Optional.of(account));

        MerchantAccountDTO result = merchantApplicationService.getAccount("merchant001");

        assertEquals("merchant001", result.getMerchantId());
        assertEquals(new BigDecimal("88.88"), result.getBalance());
    }

    @Test
    void testGetAccount_NotFound() {
        when(merchantAccountRepository.findByMerchantId("merchant404")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> merchantApplicationService.getAccount("merchant404"));
    }
}
