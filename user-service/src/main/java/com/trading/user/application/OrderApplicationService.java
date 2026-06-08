package com.trading.user.application;

import com.trading.api.merchant.MerchantServiceApi;
import com.trading.api.merchant.dto.CheckStockRequest;
import com.trading.api.merchant.dto.CheckStockResponse;
import com.trading.api.merchant.dto.ConfirmOrderRequest;
import com.trading.api.merchant.dto.ConfirmOrderResponse;
import com.trading.common.exception.InsufficientStockException;
import com.trading.common.exception.NotFoundException;
import com.trading.common.exception.RemoteServiceException;
import com.trading.user.application.dto.CreateOrderCommand;
import com.trading.user.application.dto.OrderDTO;
import com.trading.user.domain.model.AccountTransaction;
import com.trading.user.domain.model.Money;
import com.trading.user.domain.model.Order;
import com.trading.user.domain.model.UserAccount;
import com.trading.user.domain.repository.AccountTransactionRepository;
import com.trading.user.domain.repository.OrderRepository;
import com.trading.user.domain.repository.UserAccountRepository;
import com.trading.user.infrastructure.redis.RedisLockService;
import io.seata.spring.annotation.GlobalTransactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {
    private final UserAccountRepository userAccountRepository;
    private final OrderRepository orderRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final MerchantServiceApi merchantServiceApi;
    private final RedisLockService redisLockService;

    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class, timeoutMills = 60000)
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO createOrder(CreateOrderCommand command) {
        return redisLockService.executeWithLock(
            "order:%s:%s".formatted(command.getUserId(), command.getSku()),
            () -> doCreateOrder(command)
        );
    }

    public OrderDTO getOrder(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
            .map(this::toDTO)
            .orElseThrow(() -> new NotFoundException("订单不存在"));
    }

    public List<OrderDTO> listUserOrders(String userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toDTO).toList();
    }

    private OrderDTO doCreateOrder(CreateOrderCommand command) {
        CheckStockResponse stockResponse = merchantServiceApi.checkStock(CheckStockRequest.builder()
            .merchantId(command.getMerchantId())
            .sku(command.getSku())
            .quantity(command.getQuantity())
            .build());
        if (!stockResponse.isAvailable()) {
            throw new InsufficientStockException(stockResponse.getErrorMessage() == null ? "库存不足" : stockResponse.getErrorMessage());
        }

        UserAccount account = userAccountRepository.findByUserId(command.getUserId())
            .orElseThrow(() -> new NotFoundException("用户账户不存在"));
        Money unitPrice = Money.of(stockResponse.getUnitPrice());
        Money totalAmount = unitPrice.multiply(command.getQuantity());
        account.deduct(totalAmount);
        UserAccount savedAccount = userAccountRepository.save(account);

        String orderNo = "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        ConfirmOrderResponse confirmResponse = merchantServiceApi.confirmOrder(ConfirmOrderRequest.builder()
            .orderNo(orderNo)
            .merchantId(command.getMerchantId())
            .sku(command.getSku())
            .quantity(command.getQuantity())
            .build());
        if (!confirmResponse.isSuccess()) {
            throw new RemoteServiceException(confirmResponse.getMessage() == null ? "商家服务确认订单失败" : confirmResponse.getMessage());
        }

        Order order = Order.create(orderNo, command.getUserId(), command.getMerchantId(), command.getSku(), command.getQuantity(), unitPrice);
        Order savedOrder = orderRepository.save(order);
        accountTransactionRepository.save(AccountTransaction.purchase(savedAccount.getUserId(), orderNo, totalAmount, savedAccount.getBalance()));
        return toDTO(savedOrder);
    }

    private OrderDTO toDTO(Order order) {
        return OrderDTO.builder()
            .orderNo(order.getOrderNo())
            .userId(order.getUserId())
            .merchantId(order.getMerchantId())
            .sku(order.getSku())
            .quantity(order.getQuantity())
            .unitPrice(order.getUnitPrice().getAmount())
            .totalAmount(order.getTotalAmount().getAmount())
            .status(order.getStatus())
            .createdAt(order.getCreatedAt())
            .build();
    }
}
