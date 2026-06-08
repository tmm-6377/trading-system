package com.trading.merchant.application;

import com.trading.merchant.domain.model.MerchantAccount;
import com.trading.merchant.domain.model.Money;
import com.trading.merchant.domain.model.ProductInventory;
import com.trading.merchant.domain.repository.MerchantAccountRepository;
import com.trading.merchant.domain.repository.ProductInventoryRepository;
import com.trading.merchant.domain.repository.SettlementRecordRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementApplicationServiceTest {
    @Mock
    private MerchantAccountRepository merchantAccountRepository;
    @Mock
    private ProductInventoryRepository productInventoryRepository;
    @Mock
    private SettlementRecordRepository settlementRecordRepository;
    @InjectMocks
    private SettlementApplicationService settlementApplicationService;

    @Test
    void should_generate_settlement_record() {
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-002", "MacBook", Money.of(new BigDecimal("20.00")), 10);
        inventory.checkAndDeductStock(2);
        MerchantAccount account = MerchantAccount.create("merchant001");
        account.credit(Money.of(new BigDecimal("40.00")));
        when(merchantAccountRepository.findAllMerchantIds()).thenReturn(List.of("merchant001"));
        when(productInventoryRepository.findByMerchantId("merchant001")).thenReturn(List.of(inventory));
        when(merchantAccountRepository.findByMerchantId("merchant001")).thenReturn(Optional.of(account));

        settlementApplicationService.executeSettlement(LocalDate.of(2026, 6, 8));

        verify(settlementRecordRepository, times(1)).save(any());
    }
}
