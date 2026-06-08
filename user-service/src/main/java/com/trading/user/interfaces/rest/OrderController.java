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

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {
    private final OrderApplicationService orderApplicationService;

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

    @GetMapping("/orders/{orderNo}")
    public ApiResponse<OrderDTO> getOrder(@PathVariable String orderNo) {
        return ApiResponse.success(orderApplicationService.getOrder(orderNo));
    }

    @GetMapping("/users/{userId}/orders")
    public ApiResponse<List<OrderDTO>> listOrders(@PathVariable String userId) {
        return ApiResponse.success(orderApplicationService.listUserOrders(userId));
    }
}
