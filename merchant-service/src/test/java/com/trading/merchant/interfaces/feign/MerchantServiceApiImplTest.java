package com.trading.merchant.interfaces.feign;

import com.trading.api.merchant.dto.CheckStockRequest;
import com.trading.api.merchant.dto.CheckStockResponse;
import com.trading.api.merchant.dto.ConfirmOrderRequest;
import com.trading.api.merchant.dto.ConfirmOrderResponse;
import com.trading.api.merchant.dto.RollbackStockRequest;
import com.trading.merchant.application.InventoryApplicationService;
import com.trading.merchant.application.OrderProcessApplicationService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantServiceApiImplTest {

    @Mock
    private InventoryApplicationService inventoryApplicationService;
    @Mock
    private OrderProcessApplicationService orderProcessApplicationService;
    @InjectMocks
    private MerchantServiceApiImpl merchantServiceApi;

    @Test
    void testCheckStock_Available() {
        CheckStockRequest request = CheckStockRequest.builder().merchantId("merchant001").sku("SKU-001").quantity(1).build();
        CheckStockResponse response = CheckStockResponse.builder().available(true).unitPrice(new BigDecimal("99.99")).build();
        when(inventoryApplicationService.checkStock(request)).thenReturn(response);

        CheckStockResponse result = merchantServiceApi.checkStock(request);

        assertTrue(result.isAvailable());
        assertEquals(new BigDecimal("99.99"), result.getUnitPrice());
    }

    @Test
    void testCheckStock_Insufficient() {
        CheckStockRequest request = CheckStockRequest.builder().merchantId("merchant001").sku("SKU-001").quantity(10).build();
        CheckStockResponse response = CheckStockResponse.builder().available(false).errorMessage("库存不足").build();
        when(inventoryApplicationService.checkStock(request)).thenReturn(response);

        CheckStockResponse result = merchantServiceApi.checkStock(request);

        assertFalse(result.isAvailable());
        assertEquals("库存不足", result.getErrorMessage());
    }

    @Test
    void testConfirmOrder_Success() {
        ConfirmOrderRequest request = ConfirmOrderRequest.builder().orderNo("ORDER-1").merchantId("merchant001").sku("SKU-001").quantity(2).build();
        ConfirmOrderResponse response = ConfirmOrderResponse.builder()
            .success(true)
            .unitPrice(new BigDecimal("20.00"))
            .totalAmount(new BigDecimal("40.00"))
            .message("订单确认成功")
            .build();
        when(orderProcessApplicationService.processOrder(request)).thenReturn(response);

        ConfirmOrderResponse result = merchantServiceApi.confirmOrder(request);

        assertTrue(result.isSuccess());
        assertEquals(new BigDecimal("40.00"), result.getTotalAmount());
    }

    @Test
    void testConfirmOrder_Failed() {
        ConfirmOrderRequest request = ConfirmOrderRequest.builder().orderNo("ORDER-1").merchantId("merchant001").sku("SKU-001").quantity(2).build();
        ConfirmOrderResponse response = ConfirmOrderResponse.builder()
            .success(false)
            .message("订单确认失败")
            .build();
        when(orderProcessApplicationService.processOrder(request)).thenReturn(response);

        ConfirmOrderResponse result = merchantServiceApi.confirmOrder(request);

        assertFalse(result.isSuccess());
        assertEquals("订单确认失败", result.getMessage());
    }

    @Test
    void testRollbackStock_Success() {
        RollbackStockRequest request = RollbackStockRequest.builder().merchantId("merchant001").sku("SKU-001").quantity(2).build();
        ConfirmOrderResponse response = ConfirmOrderResponse.builder().success(true).message("库存回滚成功").build();
        when(orderProcessApplicationService.rollbackStock(request)).thenReturn(response);

        ConfirmOrderResponse result = merchantServiceApi.rollbackStock(request);

        assertTrue(result.isSuccess());
        assertEquals("库存回滚成功", result.getMessage());
    }
}
