package com.trading.merchant.application;

import com.trading.merchant.application.dto.SettlementRecordDTO;
import com.trading.merchant.domain.model.MerchantAccount;
import com.trading.merchant.domain.model.Money;
import com.trading.merchant.domain.model.ProductInventory;
import com.trading.merchant.domain.model.SettlementRecord;
import com.trading.merchant.domain.repository.MerchantAccountRepository;
import com.trading.merchant.domain.repository.ProductInventoryRepository;
import com.trading.merchant.domain.repository.SettlementRecordRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 结算应用服务单元测试
 */
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

    /**
     * 测试场景：生成结算记录
     *
     * <p>Given: 商家有库存记录和账户余额</p>
     * <p>When: 执行结算</p>
     * <p>Then: 生成一条结算记录</p>
     */
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

    /**
     * 测试场景：结算匹配（预期金额等于实际余额）
     *
     * <p>Given: 售出2件，每件20元，余额40元（匹配）</p>
     * <p>When: 执行结算</p>
     * <p>Then: 结算状态为 MATCH</p>
     */
    @Test
    void testExecuteSettlement_Match() {
        // Given
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook",
            Money.of(new BigDecimal("20.00")), 10);
        inventory.checkAndDeductStock(2); // 售出2件，预期=40
        MerchantAccount account = MerchantAccount.create("merchant001");
        account.credit(Money.of(new BigDecimal("40.00"))); // 实际余额=40
        when(merchantAccountRepository.findAllMerchantIds()).thenReturn(List.of("merchant001"));
        when(productInventoryRepository.findByMerchantId("merchant001")).thenReturn(List.of(inventory));
        when(merchantAccountRepository.findByMerchantId("merchant001")).thenReturn(Optional.of(account));
        ArgumentCaptor<SettlementRecord> captor = ArgumentCaptor.forClass(SettlementRecord.class);
        when(settlementRecordRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // When
        settlementApplicationService.executeSettlement(LocalDate.of(2026, 6, 8));

        // Then
        assertEquals("MATCH", captor.getValue().getStatus());
    }

    /**
     * 测试场景：结算不匹配（预期金额不等于实际余额）
     *
     * <p>Given: 售出2件，每件20元（预期40），但余额只有30元</p>
     * <p>When: 执行结算</p>
     * <p>Then: 结算状态为 MISMATCH</p>
     */
    @Test
    void testExecuteSettlement_Mismatch() {
        // Given
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook",
            Money.of(new BigDecimal("20.00")), 10);
        inventory.checkAndDeductStock(2); // 预期 = 40
        MerchantAccount account = MerchantAccount.create("merchant001");
        account.credit(Money.of(new BigDecimal("30.00"))); // 实际余额 = 30（不匹配）
        when(merchantAccountRepository.findAllMerchantIds()).thenReturn(List.of("merchant001"));
        when(productInventoryRepository.findByMerchantId("merchant001")).thenReturn(List.of(inventory));
        when(merchantAccountRepository.findByMerchantId("merchant001")).thenReturn(Optional.of(account));
        ArgumentCaptor<SettlementRecord> captor = ArgumentCaptor.forClass(SettlementRecord.class);
        when(settlementRecordRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // When
        settlementApplicationService.executeSettlement(LocalDate.of(2026, 6, 8));

        // Then
        assertEquals("MISMATCH", captor.getValue().getStatus());
    }

    /**
     * 测试场景：商家没有库存记录
     *
     * <p>Given: 商家没有任何商品库存，账户余额为0</p>
     * <p>When: 执行结算</p>
     * <p>Then: 预期金额为0，结算为 MATCH</p>
     */
    @Test
    void testExecuteSettlement_NoInventory() {
        // Given
        MerchantAccount account = MerchantAccount.create("merchant001");
        when(merchantAccountRepository.findAllMerchantIds()).thenReturn(List.of("merchant001"));
        when(productInventoryRepository.findByMerchantId("merchant001")).thenReturn(List.of());
        when(merchantAccountRepository.findByMerchantId("merchant001")).thenReturn(Optional.of(account));
        ArgumentCaptor<SettlementRecord> captor = ArgumentCaptor.forClass(SettlementRecord.class);
        when(settlementRecordRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // When
        settlementApplicationService.executeSettlement(LocalDate.of(2026, 6, 8));

        // Then
        assertEquals("MATCH", captor.getValue().getStatus());
        assertEquals(new BigDecimal("0.00"), captor.getValue().getExpectedAmount().getAmount());
    }

    /**
     * 测试场景：多个商家各自生成结算记录
     *
     * <p>Given: 两个商家</p>
     * <p>When: 执行结算</p>
     * <p>Then: 各生成一条结算记录，共2条</p>
     */
    @Test
    void testExecuteSettlement_MultipleMerchants() {
        // Given
        MerchantAccount account1 = MerchantAccount.create("merchant001");
        MerchantAccount account2 = MerchantAccount.create("merchant002");
        when(merchantAccountRepository.findAllMerchantIds()).thenReturn(List.of("merchant001", "merchant002"));
        when(productInventoryRepository.findByMerchantId("merchant001")).thenReturn(List.of());
        when(productInventoryRepository.findByMerchantId("merchant002")).thenReturn(List.of());
        when(merchantAccountRepository.findByMerchantId("merchant001")).thenReturn(Optional.of(account1));
        when(merchantAccountRepository.findByMerchantId("merchant002")).thenReturn(Optional.of(account2));
        when(settlementRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        settlementApplicationService.executeSettlement(LocalDate.of(2026, 6, 8));

        // Then
        verify(settlementRecordRepository, times(2)).save(any());
    }

    /**
     * 测试场景：查询结算记录列表
     *
     * <p>Given: 商家有2条结算记录</p>
     * <p>When: 查询结算列表</p>
     * <p>Then: 返回2条记录</p>
     */
    @Test
    void testListSettlements_Success() {
        // Given
        List<SettlementRecord> records = List.of(
            SettlementRecord.create("merchant001", LocalDate.of(2026, 6, 7),
                Money.of(new BigDecimal("100.00")), Money.of(new BigDecimal("100.00"))),
            SettlementRecord.create("merchant001", LocalDate.of(2026, 6, 8),
                Money.of(new BigDecimal("50.00")), Money.of(new BigDecimal("50.00")))
        );
        when(settlementRecordRepository.findByMerchantId("merchant001")).thenReturn(records);

        // When
        List<SettlementRecordDTO> result = settlementApplicationService.listSettlements("merchant001");

        // Then
        assertEquals(2, result.size());
    }
}
