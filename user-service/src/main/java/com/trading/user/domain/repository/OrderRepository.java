package com.trading.user.domain.repository;

import com.trading.user.domain.model.Order;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findByOrderNo(String orderNo);
    List<Order> findByUserId(String userId);
}
