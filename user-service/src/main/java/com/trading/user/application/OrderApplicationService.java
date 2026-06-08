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

/**
 * 订单应用服务
 *
 * <p>负责订单的创建、查询等业务操作。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>协调用户服务和商家服务完成分布式下单流程</li>
 *   <li>通过 Redis 分布式锁防止重复下单</li>
 *   <li>通过 Seata 保证分布式事务的一致性</li>
 *   <li>提供订单查询功能</li>
 * </ul>
 *
 * <p>分布式事务说明：</p>
 * <p>下单流程涉及用户服务（扣减余额）和商家服务（扣减库存、收款）两个服务，
 * 通过 Seata AT 模式保证最终一致性。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    /** 用户账户仓储 */
    private final UserAccountRepository userAccountRepository;

    /** 订单仓储 */
    private final OrderRepository orderRepository;

    /** 账户流水仓储 */
    private final AccountTransactionRepository accountTransactionRepository;

    /** 商家服务 Feign 客户端 */
    private final MerchantServiceApi merchantServiceApi;

    /** Redis 分布式锁服务 */
    private final RedisLockService redisLockService;

    /**
     * 创建订单（分布式事务）
     *
     * <p>下单流程（分布式事务保护）：</p>
     * <ol>
     *   <li>获取 Redis 分布式锁（防重复下单）</li>
     *   <li>Step 1: 调用商家服务检查库存（Feign调用）</li>
     *   <li>Step 2: 查询用户账户并扣减余额（本地事务）</li>
     *   <li>Step 3: 调用商家服务确认订单（Feign调用，扣库存+收款）</li>
     *   <li>Step 4: 保存订单记录</li>
     *   <li>Step 5: 记录账户扣款流水</li>
     * </ol>
     *
     * @param command 创建订单命令，包含userId、merchantId、sku、quantity
     * @return 创建成功的订单信息
     * @throws com.trading.common.exception.LockAcquireException 如果获取分布式锁失败（重复下单）
     * @throws InsufficientStockException                        如果商品库存不足
     * @throws NotFoundException                                 如果用户账户不存在
     * @throws com.trading.common.exception.InsufficientBalanceException 如果账户余额不足
     * @throws RemoteServiceException                            如果商家服务调用失败
     */
    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class, timeoutMills = 60000)
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO createOrder(CreateOrderCommand command) {
        // 通过分布式锁防止同一用户对同一商品重复下单
        return redisLockService.executeWithLock(
            "order:%s:%s".formatted(command.getUserId(), command.getSku()),
            () -> doCreateOrder(command)
        );
    }

    /**
     * 查询订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     * @throws NotFoundException 如果订单不存在
     */
    public OrderDTO getOrder(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
            .map(this::toDTO)
            .orElseThrow(() -> new NotFoundException("订单不存在"));
    }

    /**
     * 查询用户订单列表
     *
     * @param userId 用户ID
     * @return 用户的所有订单列表（按创建时间倒序）
     */
    public List<OrderDTO> listUserOrders(String userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toDTO).toList();
    }

    /**
     * 执行下单核心逻辑
     *
     * @param command 创建订单命令
     * @return 创建成功的订单DTO
     */
    private OrderDTO doCreateOrder(CreateOrderCommand command) {
        // Step 1: 检查商品库存（Feign调用商家服务）
        CheckStockResponse stockResponse = merchantServiceApi.checkStock(CheckStockRequest.builder()
            .merchantId(command.getMerchantId())
            .sku(command.getSku())
            .quantity(command.getQuantity())
            .build());
        if (!stockResponse.isAvailable()) {
            throw new InsufficientStockException(stockResponse.getErrorMessage() == null ? "库存不足" : stockResponse.getErrorMessage());
        }

        // Step 2: 扣减用户余额（本地事务）
        UserAccount account = userAccountRepository.findByUserId(command.getUserId())
            .orElseThrow(() -> new NotFoundException("用户账户不存在"));
        Money unitPrice = Money.of(stockResponse.getUnitPrice());
        Money totalAmount = unitPrice.multiply(command.getQuantity());
        account.deduct(totalAmount);
        UserAccount savedAccount = userAccountRepository.save(account);

        // Step 3: 调用商家服务确认订单（Feign调用，扣库存+收款）
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

        // Step 4: 保存订单记录
        Order order = Order.create(orderNo, command.getUserId(), command.getMerchantId(), command.getSku(), command.getQuantity(), unitPrice);
        Order savedOrder = orderRepository.save(order);

        // Step 5: 记录账户扣款流水
        accountTransactionRepository.save(AccountTransaction.purchase(savedAccount.getUserId(), orderNo, totalAmount, savedAccount.getBalance()));
        return toDTO(savedOrder);
    }

    /**
     * 将领域对象转换为DTO
     *
     * @param order 订单领域对象
     * @return 订单DTO
     */
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
