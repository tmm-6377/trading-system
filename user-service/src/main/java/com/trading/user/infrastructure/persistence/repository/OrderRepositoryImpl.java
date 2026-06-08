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

/**
 * 订单仓储实现
 *
 * <p>基于 MyBatis-Plus 实现订单的持久化操作。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    /** MyBatis-Plus 数据库操作 Mapper */
    private final OrderMapper orderMapper;

    /**
     * 保存订单（新增或更新）
     *
     * <p>根据主键ID判断：</p>
     * <ul>
     *   <li>id 为 null：新订单，执行 insert（由数据库自动生成ID）</li>
     *   <li>id 不为 null：已有订单，执行 update</li>
     * </ul>
     *
     * @param order 待保存的订单领域对象
     * @return 保存后的订单（包含数据库生成的ID）
     */
    @Override
    public Order save(Order order) {
        OrderPO po = toPO(order);
        if (po.getId() == null) {
            // 新订单：insert 并获取自增ID
            orderMapper.insert(po);
        } else {
            orderMapper.updateById(po);
        }
        return toDomain(po);
    }

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号（格式：ORD-XXXXXXXXXXXXXXXX）
     * @return 订单Optional，不存在时返回 Optional.empty()
     */
    @Override
    public Optional<Order> findByOrderNo(String orderNo) {
        return Optional.ofNullable(orderMapper.selectOne(new LambdaQueryWrapper<OrderPO>()
            .eq(OrderPO::getOrderNo, orderNo)
            .last("limit 1")))
            .map(this::toDomain);
    }

    /**
     * 查询用户的所有订单
     *
     * @param userId 用户ID
     * @return 订单列表（按创建时间倒序）
     */
    @Override
    public List<Order> findByUserId(String userId) {
        return orderMapper.selectList(new LambdaQueryWrapper<OrderPO>()
                .eq(OrderPO::getUserId, userId)
                .orderByDesc(OrderPO::getCreatedAt))
            .stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * 将持久化对象转换为领域对象
     *
     * @param po 持久化对象
     * @return 订单领域对象
     */
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

    /**
     * 将领域对象转换为持久化对象
     *
     * @param order 订单领域对象
     * @return 持久化对象
     */
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
