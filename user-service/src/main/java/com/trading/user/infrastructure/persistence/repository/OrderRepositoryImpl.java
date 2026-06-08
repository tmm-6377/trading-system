package com.trading.user.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trading.user.domain.model.Money;
import com.trading.user.domain.model.Order;
import com.trading.user.domain.repository.OrderRepository;
import com.trading.user.infrastructure.persistence.entity.OrderPO;
import com.trading.user.infrastructure.persistence.mapper.OrderMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderMapper orderMapper;

    @Override
    public Order save(Order order) {
        OrderPO po = toPO(order);
        if (po.getId() == null) {
            orderMapper.insert(po);
        } else {
            orderMapper.updateById(po);
        }
        return toDomain(po);
    }

    @Override
    public Optional<Order> findByOrderNo(String orderNo) {
        return Optional.ofNullable(orderMapper.selectOne(new LambdaQueryWrapper<OrderPO>()
            .eq(OrderPO::getOrderNo, orderNo)
            .last("limit 1")))
            .map(this::toDomain);
    }

    @Override
    public List<Order> findByUserId(String userId) {
        return orderMapper.selectList(new LambdaQueryWrapper<OrderPO>()
                .eq(OrderPO::getUserId, userId)
                .orderByDesc(OrderPO::getCreatedAt))
            .stream()
            .map(this::toDomain)
            .toList();
    }

    private Order toDomain(OrderPO po) {
        return Order.builder()
            .id(po.getId())
            .orderNo(po.getOrderNo())
            .userId(po.getUserId())
            .merchantId(po.getMerchantId())
            .sku(po.getSku())
            .quantity(po.getQuantity())
            .unitPrice(Money.of(po.getUnitPrice()))
            .totalAmount(Money.of(po.getTotalAmount()))
            .status(po.getStatus())
            .createdAt(po.getCreatedAt())
            .build();
    }

    private OrderPO toPO(Order order) {
        OrderPO po = new OrderPO();
        po.setId(order.getId());
        po.setOrderNo(order.getOrderNo());
        po.setUserId(order.getUserId());
        po.setMerchantId(order.getMerchantId());
        po.setSku(order.getSku());
        po.setQuantity(order.getQuantity());
        po.setUnitPrice(order.getUnitPrice().getAmount());
        po.setTotalAmount(order.getTotalAmount().getAmount());
        po.setStatus(order.getStatus());
        po.setCreatedAt(order.getCreatedAt());
        return po;
    }
}
