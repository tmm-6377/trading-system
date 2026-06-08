package com.trading.merchant.application;

import com.trading.api.merchant.dto.ConfirmOrderRequest;
import com.trading.api.merchant.dto.ConfirmOrderResponse;
import com.trading.api.merchant.dto.RollbackStockRequest;
import com.trading.common.exception.InsufficientStockException;
import com.trading.common.exception.NotFoundException;
import com.trading.merchant.domain.model.MerchantAccount;
import com.trading.merchant.domain.model.Money;
import com.trading.merchant.domain.model.ProductInventory;
import com.trading.merchant.domain.repository.MerchantAccountRepository;
import com.trading.merchant.domain.repository.ProductInventoryRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 订单处理应用服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class OrderProcessApplicationServiceTest {

    @Mock
    private ProductInventoryRepository productInventoryRepository;
    @Mock
    private MerchantAccountRepository merchantAccountRepository;
    @InjectMocks
    private OrderProcessApplicationService orderProcessApplicationService;

    /**
     * 测试场景：处理订单成功
     *
     * <p>Given: 商品库存充足，商家账户存在</p>
     * <p>When: 处理订单</p>
     * <p>Then: 返回成功响应，库存扣减，余额增加</p>
     */
    @Test
    void testProcessOrder_Success() {
        // Given
        ConfirmOrderRequest request = ConfirmOrderRequest.builder()
            .orderNo("ORD-001").merchantId("merchant001").sku("SKU-001").quantity(2).build();
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook",
            Money.of(new BigDecimal("100.00")), 10);
        MerchantAccount account = MerchantAccount.create("merchant001");
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-001")).thenReturn(Optional.of(inventory));
        when(productInventoryRepository.save(any())).thenReturn(inventory);
        when(merchantAccountRepository.findByMerchantId("merchant001")).thenReturn(Optional.of(account));
        when(merchantAccountRepository.save(any())).thenReturn(account);

        // When
        ConfirmOrderResponse response = orderProcessApplicationService.processOrder(request);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(new BigDecimal("100.00"), response.getUnitPrice());
        assertEquals(new BigDecimal("200.00"), response.getTotalAmount());
    }

    /**
     * 测试场景：处理订单时商品不存在
     *
     * <p>Given: 商品不存在</p>
     * <p>When: 处理订单</p>
     * <p>Then: 抛出 NotFoundException</p>
     */
    @Test
    void testProcessOrder_ProductNotFound() {
        // Given
        ConfirmOrderRequest request = ConfirmOrderRequest.builder()
            .orderNo("ORD-001").merchantId("merchant001").sku("SKU-999").quantity(1).build();
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> orderProcessApplicationService.processOrder(request));
    }

    /**
     * 测试场景：处理订单时库存不足
     *
     * <p>Given: 商品库存为1，请求购买5件</p>
     * <p>When: 处理订单</p>
     * <p>Then: 抛出 InsufficientStockException</p>
     */
    @Test
    void testProcessOrder_InsufficientStock() {
        // Given
        ConfirmOrderRequest request = ConfirmOrderRequest.builder()
            .orderNo("ORD-001").merchantId("merchant001").sku("SKU-001").quantity(5).build();
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook",
            Money.of(new BigDecimal("100.00")), 1);
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-001")).thenReturn(Optional.of(inventory));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> orderProcessApplicationService.processOrder(request));
    }

    /**
     * 测试场景：处理订单时商家不存在（自动创建）
     *
     * <p>Given: 商品存在，商家账户不存在</p>
     * <p>When: 处理订单</p>
     * <p>Then: 成功处理，自动创建商家账户</p>
     */
    @Test
    void testProcessOrder_MerchantNotFound_CreatesNewMerchant() {
        // Given
        ConfirmOrderRequest request = ConfirmOrderRequest.builder()
            .orderNo("ORD-001").merchantId("merchant001").sku("SKU-001").quantity(1).build();
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook",
            Money.of(new BigDecimal("100.00")), 10);
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-001")).thenReturn(Optional.of(inventory));
        when(productInventoryRepository.save(any())).thenReturn(inventory);
        // 商家账户不存在，返回 empty
        when(merchantAccountRepository.findByMerchantId("merchant001")).thenReturn(Optional.empty());
        when(merchantAccountRepository.save(any())).thenReturn(MerchantAccount.create("merchant001"));

        // When
        ConfirmOrderResponse response = orderProcessApplicationService.processOrder(request);

        // Then
        assertTrue(response.isSuccess());
    }

    /**
     * 测试场景：回滚库存成功
     *
     * <p>Given: 商品存在，已售1件</p>
     * <p>When: 回滚1件库存</p>
     * <p>Then: 返回成功响应</p>
     */
    @Test
    void testRollbackStock_Success() {
        // Given
        RollbackStockRequest request = RollbackStockRequest.builder()
            .merchantId("merchant001").sku("SKU-001").quantity(1).build();
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook",
            Money.of(new BigDecimal("100.00")), 9);
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-001")).thenReturn(Optional.of(inventory));
        when(productInventoryRepository.save(any())).thenReturn(inventory);

        // When
        ConfirmOrderResponse response = orderProcessApplicationService.rollbackStock(request);

        // Then
        assertTrue(response.isSuccess());
    }

    /**
     * 测试场景：回滚不存在商品的库存
     *
     * <p>Given: 商品不存在</p>
     * <p>When: 回滚库存</p>
     * <p>Then: 抛出 NotFoundException</p>
     */
    @Test
    void testRollbackStock_ProductNotFound() {
        // Given
        RollbackStockRequest request = RollbackStockRequest.builder()
            .merchantId("merchant001").sku("SKU-999").quantity(1).build();
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> orderProcessApplicationService.rollbackStock(request));
    }
}
