package com.trading.user.interfaces.rest;

import com.trading.common.dto.ApiResponse;
import com.trading.user.application.OrderApplicationService;
import com.trading.user.application.dto.CreateOrderCommand;
import com.trading.user.application.dto.OrderDTO;
import com.trading.user.interfaces.dto.request.CreateOrderRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单 REST 控制器
 *
 * <p>提供订单相关的 HTTP API，基础路径：/api/v1</p>
 *
 * <p>接口列表：</p>
 * <ul>
 *   <li>POST /api/v1/orders - 创建订单</li>
 *   <li>GET  /api/v1/orders/{orderNo} - 查询订单</li>
 *   <li>GET  /api/v1/users/{userId}/orders - 查询用户订单列表</li>
 * </ul>
 *
 * @author Trading System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    /** 订单应用服务 */
    private final OrderApplicationService orderApplicationService;

    /**
     * 创建订单接口
     *
     * <p>完整的下单流程，包括库存检查、余额扣减、商家确认等步骤。</p>
     *
     * @param request 创建订单请求体，包含userId、merchantId、sku、quantity
     * @return 201 Created，包含创建成功的订单信息
     */
    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        OrderDTO order = orderApplicationService.createOrder(new CreateOrderCommand(
            request.getUserId(),
            request.getMerchantId(),
            request.getSku(),
            request.getQuantity()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(order));
    }

    /**
     * 查询订单接口
     *
     * @param orderNo 路径参数，订单号
     * @return 订单信息，订单不存在时返回 404
     */
    @GetMapping("/orders/{orderNo}")
    public ApiResponse<OrderDTO> getOrder(@PathVariable String orderNo) {
        return ApiResponse.success(orderApplicationService.getOrder(orderNo));
    }

    /**
     * 查询用户订单列表接口
     *
     * @param userId 路径参数，用户ID
     * @return 用户的订单列表（按创建时间倒序）
     */
    @GetMapping("/users/{userId}/orders")
    public ApiResponse<List<OrderDTO>> listOrders(@PathVariable String userId) {
        return ApiResponse.success(orderApplicationService.listUserOrders(userId));
    }
}
