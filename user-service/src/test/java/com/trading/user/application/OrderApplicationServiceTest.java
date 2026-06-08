package com.trading.user.application;

import com.trading.api.merchant.MerchantServiceApi;
import com.trading.api.merchant.dto.CheckStockResponse;
import com.trading.api.merchant.dto.ConfirmOrderResponse;
import com.trading.common.exception.InsufficientBalanceException;
import com.trading.common.exception.InsufficientStockException;
import com.trading.common.exception.LockAcquireException;
import com.trading.common.exception.NotFoundException;
import com.trading.common.exception.RemoteServiceException;
import com.trading.user.application.dto.CreateOrderCommand;
import com.trading.user.application.dto.OrderDTO;
import com.trading.user.domain.model.Money;
import com.trading.user.domain.model.Order;
import com.trading.user.domain.model.UserAccount;
import com.trading.user.domain.repository.AccountTransactionRepository;
import com.trading.user.domain.repository.OrderRepository;
import com.trading.user.domain.repository.UserAccountRepository;
import com.trading.user.infrastructure.redis.RedisLockService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AccountTransactionRepository accountTransactionRepository;
    @Mock
    private MerchantServiceApi merchantServiceApi;
    @Mock
    private RedisLockService redisLockService;
    @InjectMocks
    private OrderApplicationService orderApplicationService;

    @Test
    void should_create_order_successfully() {
        // Given
        UserAccount account = UserAccount.create("user001");
        account.recharge(Money.of(new BigDecimal("200.00")));
        mockRedisLockSuccess();
        when(userAccountRepository.findByUserId("user001")).thenReturn(Optional.of(account));
        when(userAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(merchantServiceApi.checkStock(any())).thenReturn(CheckStockResponse.builder().available(true).unitPrice(new BigDecimal("20.00")).build());
        when(merchantServiceApi.confirmOrder(any())).thenReturn(ConfirmOrderResponse.builder().success(true).build());
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountTransactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderDTO order = orderApplicationService.createOrder(new CreateOrderCommand("user001", "merchant001", "SKU-001", 2));

        // Then
        assertEquals(new BigDecimal("40.00"), order.getTotalAmount());
        assertEquals("SKU-001", order.getSku());
        assertEquals("CREATED", order.getStatus());
        assertNotNull(order.getOrderNo());
    }

    @Test
    void should_throw_when_stock_not_available() {
        // Given
        mockRedisLockSuccess();
        when(merchantServiceApi.checkStock(any())).thenReturn(CheckStockResponse.builder().available(false).errorMessage("库存不足").build());

        // When
        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
            () -> orderApplicationService.createOrder(new CreateOrderCommand("user001", "merchant001", "SKU-001", 2)));

        // Then
        assertEquals("INSUFFICIENT_STOCK", exception.getCode());
    }

    @Test
    void testCreateOrder_UserNotFound() {
        // Given
        mockRedisLockSuccess();
        when(merchantServiceApi.checkStock(any())).thenReturn(CheckStockResponse.builder().available(true).unitPrice(new BigDecimal("20.00")).build());
        when(userAccountRepository.findByUserId("user001")).thenReturn(Optional.empty());

        // When
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> orderApplicationService.createOrder(new CreateOrderCommand("user001", "merchant001", "SKU-001", 2)));

        // Then
        assertEquals("NOT_FOUND", exception.getCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testCreateOrder_InsufficientBalance() {
        // Given
        UserAccount account = UserAccount.create("user001");
        account.recharge(Money.of(new BigDecimal("10.00")));
        mockRedisLockSuccess();
        when(merchantServiceApi.checkStock(any())).thenReturn(CheckStockResponse.builder().available(true).unitPrice(new BigDecimal("20.00")).build());
        when(userAccountRepository.findByUserId("user001")).thenReturn(Optional.of(account));

        // When
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class,
            () -> orderApplicationService.createOrder(new CreateOrderCommand("user001", "merchant001", "SKU-001", 1)));

        // Then
        assertEquals("INSUFFICIENT_BALANCE", exception.getCode());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void testCreateOrder_MerchantServiceDown() {
        // Given
        mockRedisLockSuccess();
        when(merchantServiceApi.checkStock(any())).thenThrow(new RemoteServiceException("商家服务不可用"));

        // When
        RemoteServiceException exception = assertThrows(RemoteServiceException.class,
            () -> orderApplicationService.createOrder(new CreateOrderCommand("user001", "merchant001", "SKU-001", 2)));

        // Then
        assertEquals("REMOTE_SERVICE_ERROR", exception.getCode());
    }

    @Test
    void testCreateOrder_ConfirmOrderFailed() {
        // Given
        UserAccount account = UserAccount.create("user001");
        account.recharge(Money.of(new BigDecimal("100.00")));
        mockRedisLockSuccess();
        when(merchantServiceApi.checkStock(any())).thenReturn(CheckStockResponse.builder().available(true).unitPrice(new BigDecimal("20.00")).build());
        when(userAccountRepository.findByUserId("user001")).thenReturn(Optional.of(account));
        when(userAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(merchantServiceApi.confirmOrder(any())).thenReturn(ConfirmOrderResponse.builder().success(false).message("确认失败").build());

        // When
        RemoteServiceException exception = assertThrows(RemoteServiceException.class,
            () -> orderApplicationService.createOrder(new CreateOrderCommand("user001", "merchant001", "SKU-001", 2)));

        // Then
        assertEquals("REMOTE_SERVICE_ERROR", exception.getCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testCreateOrder_DuplicateLock() {
        // Given
        when(redisLockService.executeWithLock(anyString(), any())).thenThrow(new LockAcquireException("重复下单，请稍后重试"));

        // When
        LockAcquireException exception = assertThrows(LockAcquireException.class,
            () -> orderApplicationService.createOrder(new CreateOrderCommand("user001", "merchant001", "SKU-001", 2)));

        // Then
        assertEquals("LOCK_ACQUIRE_FAILED", exception.getCode());
    }

    @Test
    void testGetOrder_Success() {
        // Given
        Order order = Order.builder()
            .id(1L)
            .orderNo("ORD-001")
            .userId("user001")
            .merchantId("merchant001")
            .sku("SKU-001")
            .quantity(2)
            .unitPrice(Money.of(new BigDecimal("20.00")))
            .totalAmount(Money.of(new BigDecimal("40.00")))
            .status("CREATED")
            .createdAt(LocalDateTime.now())
            .build();
        when(orderRepository.findByOrderNo("ORD-001")).thenReturn(Optional.of(order));

        // When
        OrderDTO result = orderApplicationService.getOrder("ORD-001");

        // Then
        assertEquals("ORD-001", result.getOrderNo());
        assertEquals(new BigDecimal("40.00"), result.getTotalAmount());
    }

    @Test
    void testGetOrder_NotFound() {
        // Given
        when(orderRepository.findByOrderNo("ORD-404")).thenReturn(Optional.empty());

        // When
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> orderApplicationService.getOrder("ORD-404"));

        // Then
        assertEquals("NOT_FOUND", exception.getCode());
    }

    @Test
    void testListUserOrders_Success() {
        // Given
        Order first = Order.builder()
            .id(1L)
            .orderNo("ORD-001")
            .userId("user001")
            .merchantId("merchant001")
            .sku("SKU-001")
            .quantity(1)
            .unitPrice(Money.of(new BigDecimal("10.00")))
            .totalAmount(Money.of(new BigDecimal("10.00")))
            .status("CREATED")
            .createdAt(LocalDateTime.now())
            .build();
        Order second = Order.builder()
            .id(2L)
            .orderNo("ORD-002")
            .userId("user001")
            .merchantId("merchant001")
            .sku("SKU-002")
            .quantity(2)
            .unitPrice(Money.of(new BigDecimal("15.00")))
            .totalAmount(Money.of(new BigDecimal("30.00")))
            .status("CREATED")
            .createdAt(LocalDateTime.now())
            .build();
        when(orderRepository.findByUserId("user001")).thenReturn(List.of(first, second));

        // When
        List<OrderDTO> result = orderApplicationService.listUserOrders("user001");

        // Then
        assertEquals(2, result.size());
        assertEquals("ORD-001", result.get(0).getOrderNo());
        assertEquals("ORD-002", result.get(1).getOrderNo());
    }

    @Test
    void testListUserOrders_EmptyResult() {
        // Given
        when(orderRepository.findByUserId("user001")).thenReturn(List.of());

        // When
        List<OrderDTO> result = orderApplicationService.listUserOrders("user001");

        // Then
        assertEquals(0, result.size());
    }

    @SuppressWarnings("unchecked")
    private void mockRedisLockSuccess() {
        when(redisLockService.executeWithLock(anyString(), any())).thenAnswer(invocation -> {
            Supplier<OrderDTO> supplier = invocation.getArgument(1);
            return supplier.get();
        });
    }
}
