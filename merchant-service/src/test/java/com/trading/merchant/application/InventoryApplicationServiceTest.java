package com.trading.merchant.application;

import com.trading.api.merchant.dto.CheckStockRequest;
import com.trading.api.merchant.dto.CheckStockResponse;
import com.trading.common.exception.NotFoundException;
import com.trading.merchant.application.dto.AddProductCommand;
import com.trading.merchant.application.dto.InventoryView;
import com.trading.merchant.domain.model.MerchantAccount;
import com.trading.merchant.domain.model.Money;
import com.trading.merchant.domain.model.ProductInventory;
import com.trading.merchant.domain.repository.MerchantAccountRepository;
import com.trading.merchant.domain.repository.ProductInventoryRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 库存应用服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class InventoryApplicationServiceTest {

    @Mock
    private ProductInventoryRepository productInventoryRepository;
    @Mock
    private MerchantAccountRepository merchantAccountRepository;
    @InjectMocks
    private InventoryApplicationService inventoryApplicationService;

    /**
     * 测试场景：添加新商品成功
     *
     * <p>Given: 商品不存在，商家账户不存在</p>
     * <p>When: 添加新商品</p>
     * <p>Then: 商品创建成功，同时创建商家账户</p>
     */
    @Test
    void testAddProduct_Success_NewProduct() {
        // Given
        AddProductCommand command = new AddProductCommand("merchant001", "SKU-001", "MacBook", new BigDecimal("9999.00"), 10);
        ProductInventory savedInventory = ProductInventory.create("merchant001", "SKU-001", "MacBook",
            Money.of(new BigDecimal("9999.00")), 10);
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-001")).thenReturn(Optional.empty());
        when(merchantAccountRepository.findByMerchantId("merchant001")).thenReturn(Optional.empty());
        when(merchantAccountRepository.save(any())).thenReturn(MerchantAccount.create("merchant001"));
        when(productInventoryRepository.save(any())).thenReturn(savedInventory);

        // When
        InventoryView result = inventoryApplicationService.addProduct(command);

        // Then
        assertEquals("merchant001", result.getMerchantId());
        assertEquals("SKU-001", result.getSku());
        assertEquals(10, result.getAvailableQuantity());
    }

    /**
     * 测试场景：添加已存在SKU时追加库存
     *
     * <p>Given: 商品已存在（库存10件），商家账户已存在</p>
     * <p>When: 再次添加同一SKU商品（5件）</p>
     * <p>Then: 库存增加到15件</p>
     */
    @Test
    void testAddProduct_ExistingSKU_AddStock() {
        // Given
        AddProductCommand command = new AddProductCommand("merchant001", "SKU-001", "MacBook", new BigDecimal("9999.00"), 5);
        ProductInventory existing = ProductInventory.create("merchant001", "SKU-001", "MacBook",
            Money.of(new BigDecimal("9999.00")), 10);
        ProductInventory updated = ProductInventory.create("merchant001", "SKU-001", "MacBook",
            Money.of(new BigDecimal("9999.00")), 15);
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-001")).thenReturn(Optional.of(existing));
        when(merchantAccountRepository.findByMerchantId("merchant001")).thenReturn(Optional.of(MerchantAccount.create("merchant001")));
        when(productInventoryRepository.save(any())).thenReturn(updated);

        // When
        InventoryView result = inventoryApplicationService.addProduct(command);

        // Then
        assertEquals(15, result.getAvailableQuantity());
    }

    /**
     * 测试场景：更新库存成功
     *
     * <p>Given: 商品存在（库存10件）</p>
     * <p>When: 增加5件库存</p>
     * <p>Then: 调用 save 保存</p>
     */
    @Test
    void testUpdateStock_Success() {
        // Given
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook",
            Money.of(new BigDecimal("9999.00")), 10);
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-001")).thenReturn(Optional.of(inventory));
        when(productInventoryRepository.save(any())).thenReturn(inventory);

        // When
        inventoryApplicationService.updateStock("merchant001", "SKU-001", 5);

        // Then
        verify(productInventoryRepository).save(any());
    }

    /**
     * 测试场景：更新不存在商品的库存
     *
     * <p>Given: 商品不存在</p>
     * <p>When: 尝试更新库存</p>
     * <p>Then: 抛出 NotFoundException</p>
     */
    @Test
    void testUpdateStock_ProductNotFound() {
        // Given
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
            () -> inventoryApplicationService.updateStock("merchant001", "SKU-999", 5));
    }

    /**
     * 测试场景：检查库存充足
     *
     * <p>Given: 商品库存10件，请求购买3件</p>
     * <p>When: 检查库存</p>
     * <p>Then: 返回 available=true，包含单价</p>
     */
    @Test
    void testCheckStock_Available() {
        // Given
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook",
            Money.of(new BigDecimal("9999.00")), 10);
        CheckStockRequest request = CheckStockRequest.builder()
            .merchantId("merchant001").sku("SKU-001").quantity(3).build();
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-001")).thenReturn(Optional.of(inventory));

        // When
        CheckStockResponse response = inventoryApplicationService.checkStock(request);

        // Then
        assertTrue(response.isAvailable());
        assertEquals(new BigDecimal("9999.00"), response.getUnitPrice());
    }

    /**
     * 测试场景：检查库存不足
     *
     * <p>Given: 商品库存2件，请求购买5件</p>
     * <p>When: 检查库存</p>
     * <p>Then: 返回 available=false，包含错误消息</p>
     */
    @Test
    void testCheckStock_Insufficient() {
        // Given
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook",
            Money.of(new BigDecimal("9999.00")), 2);
        CheckStockRequest request = CheckStockRequest.builder()
            .merchantId("merchant001").sku("SKU-001").quantity(5).build();
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-001")).thenReturn(Optional.of(inventory));

        // When
        CheckStockResponse response = inventoryApplicationService.checkStock(request);

        // Then
        assertFalse(response.isAvailable());
        assertEquals("库存不足", response.getErrorMessage());
    }

    /**
     * 测试场景：检查不存在商品的库存
     *
     * <p>Given: 商品不存在</p>
     * <p>When: 检查库存</p>
     * <p>Then: 返回 available=false，错误消息为"商品不存在"</p>
     */
    @Test
    void testCheckStock_ProductNotFound() {
        // Given
        CheckStockRequest request = CheckStockRequest.builder()
            .merchantId("merchant001").sku("SKU-999").quantity(1).build();
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-999")).thenReturn(Optional.empty());

        // When
        CheckStockResponse response = inventoryApplicationService.checkStock(request);

        // Then
        assertFalse(response.isAvailable());
        assertEquals("商品不存在", response.getErrorMessage());
    }

    /**
     * 测试场景：查询库存列表
     *
     * <p>Given: 商家有两件商品</p>
     * <p>When: 查询库存列表</p>
     * <p>Then: 返回两条记录</p>
     */
    @Test
    void testListInventory_Success() {
        // Given
        List<ProductInventory> inventories = List.of(
            ProductInventory.create("merchant001", "SKU-001", "MacBook", Money.of(new BigDecimal("9999.00")), 10),
            ProductInventory.create("merchant001", "SKU-002", "iPhone", Money.of(new BigDecimal("5999.00")), 20)
        );
        when(productInventoryRepository.findByMerchantId("merchant001")).thenReturn(inventories);

        // When
        List<InventoryView> result = inventoryApplicationService.listInventory("merchant001");

        // Then
        assertEquals(2, result.size());
    }

    /**
     * 测试场景：查询单个商品库存
     *
     * <p>Given: 商品存在</p>
     * <p>When: 查询单个商品</p>
     * <p>Then: 返回正确的库存信息</p>
     */
    @Test
    void testGetInventory_Success() {
        // Given
        ProductInventory inventory = ProductInventory.create("merchant001", "SKU-001", "MacBook",
            Money.of(new BigDecimal("9999.00")), 10);
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-001")).thenReturn(Optional.of(inventory));

        // When
        InventoryView result = inventoryApplicationService.getInventory("merchant001", "SKU-001");

        // Then
        assertEquals("SKU-001", result.getSku());
        assertEquals(10, result.getAvailableQuantity());
    }

    /**
     * 测试场景：查询不存在的商品
     *
     * <p>Given: 商品不存在</p>
     * <p>When: 查询单个商品</p>
     * <p>Then: 抛出 NotFoundException</p>
     */
    @Test
    void testGetInventory_NotFound() {
        // Given
        when(productInventoryRepository.findByMerchantIdAndSku("merchant001", "SKU-999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
            () -> inventoryApplicationService.getInventory("merchant001", "SKU-999"));
    }
}
