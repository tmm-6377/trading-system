package com.trading.user.infrastructure.persistence.repository;

import com.trading.user.domain.model.Money;
import com.trading.user.domain.model.Order;
import com.trading.user.infrastructure.persistence.entity.OrderPO;
import com.trading.user.infrastructure.persistence.mapper.OrderMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderRepositoryImplTest {
    @Mock
    private OrderMapper orderMapper;
    @InjectMocks
    private OrderRepositoryImpl orderRepository;

    @Test
    void testSave_Success() {
        // Given
        Order order = Order.create("ORD-001", "user001", "merchant001", "SKU-001", 2, Money.of(new BigDecimal("20.00")));
        when(orderMapper.insert(any(OrderPO.class))).thenAnswer(invocation -> {
            OrderPO po = invocation.getArgument(0);
            po.setId(1L);
            return 1;
        });

        // When
        Order result = orderRepository.save(order);

        // Then
        assertEquals(1L, result.getId());
        assertEquals("ORD-001", result.getOrderNo());
        verify(orderMapper).insert(any(OrderPO.class));
    }

    @Test
    void testFindByOrderNo_Found() {
        // Given
        OrderPO po = buildOrderPO(1L, "ORD-001", "user001", "SKU-001", 2, new BigDecimal("20.00"), new BigDecimal("40.00"));
        when(orderMapper.selectOne(any())).thenReturn(po);

        // When
        Optional<Order> result = orderRepository.findByOrderNo("ORD-001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("ORD-001", result.orElseThrow().getOrderNo());
    }

    @Test
    void testFindByOrderNo_NotFound() {
        // Given
        when(orderMapper.selectOne(any())).thenReturn(null);

        // When
        Optional<Order> result = orderRepository.findByOrderNo("ORD-404");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByUserId_MultipleOrders() {
        // Given
        OrderPO first = buildOrderPO(1L, "ORD-001", "user001", "SKU-001", 1, new BigDecimal("10.00"), new BigDecimal("10.00"));
        OrderPO second = buildOrderPO(2L, "ORD-002", "user001", "SKU-002", 2, new BigDecimal("15.00"), new BigDecimal("30.00"));
        when(orderMapper.selectList(any())).thenReturn(List.of(first, second));

        // When
        List<Order> result = orderRepository.findByUserId("user001");

        // Then
        assertEquals(2, result.size());
        assertEquals("ORD-001", result.get(0).getOrderNo());
        assertEquals("ORD-002", result.get(1).getOrderNo());
    }

    @Test
    void testFindByUserId_EmptyResult() {
        // Given
        when(orderMapper.selectList(any())).thenReturn(List.of());

        // When
        List<Order> result = orderRepository.findByUserId("user001");

        // Then
        assertTrue(result.isEmpty());
    }

    private OrderPO buildOrderPO(Long id, String orderNo, String userId, String sku, int quantity, BigDecimal unitPrice, BigDecimal totalAmount) {
        OrderPO po = new OrderPO();
        po.setId(id);
        po.setOrderNo(orderNo);
        po.setUserId(userId);
        po.setMerchantId("merchant001");
        po.setSku(sku);
        po.setQuantity(quantity);
        po.setUnitPrice(unitPrice);
        po.setTotalAmount(totalAmount);
        po.setStatus("CREATED");
        po.setCreatedAt(LocalDateTime.now());
        return po;
    }
}
