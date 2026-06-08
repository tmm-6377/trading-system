package com.trading.user.application;

import com.trading.api.merchant.MerchantServiceApi;
import com.trading.api.merchant.dto.CheckStockResponse;
import com.trading.api.merchant.dto.ConfirmOrderResponse;
import com.trading.common.exception.InsufficientStockException;
import com.trading.user.application.dto.CreateOrderCommand;
import com.trading.user.application.dto.OrderDTO;
import com.trading.user.domain.model.Money;
import com.trading.user.domain.model.UserAccount;
import com.trading.user.domain.repository.AccountTransactionRepository;
import com.trading.user.domain.repository.OrderRepository;
import com.trading.user.domain.repository.UserAccountRepository;
import com.trading.user.infrastructure.redis.RedisLockService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        UserAccount account = UserAccount.create("user001");
        account.recharge(Money.of(new BigDecimal("200.00")));
        when(redisLockService.executeWithLock(any(), any())).thenAnswer(invocation -> invocation.<java.util.function.Supplier<OrderDTO>>getArgument(1).get());
        when(userAccountRepository.findByUserId("user001")).thenReturn(Optional.of(account));
        when(userAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(merchantServiceApi.checkStock(any())).thenReturn(CheckStockResponse.builder().available(true).unitPrice(new BigDecimal("20.00")).build());
        when(merchantServiceApi.confirmOrder(any())).thenReturn(ConfirmOrderResponse.builder().success(true).build());
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountTransactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO order = orderApplicationService.createOrder(new CreateOrderCommand("user001", "merchant001", "SKU-001", 2));

        assertEquals(new BigDecimal("40.00"), order.getTotalAmount());
        assertEquals("SKU-001", order.getSku());
    }

    @Test
    void should_throw_when_stock_not_available() {
        when(redisLockService.executeWithLock(any(), any())).thenAnswer(invocation -> invocation.<java.util.function.Supplier<OrderDTO>>getArgument(1).get());
        when(merchantServiceApi.checkStock(any())).thenReturn(CheckStockResponse.builder().available(false).errorMessage("库存不足").build());

        assertThrows(InsufficientStockException.class,
            () -> orderApplicationService.createOrder(new CreateOrderCommand("user001", "merchant001", "SKU-001", 2)));
    }
}
