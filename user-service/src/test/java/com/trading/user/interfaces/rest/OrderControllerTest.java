package com.trading.user.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.exception.NotFoundException;
import com.trading.user.application.OrderApplicationService;
import com.trading.user.application.dto.OrderDTO;
import com.trading.user.interfaces.dto.request.CreateOrderRequest;
import com.trading.user.interfaces.exception.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {
    @Mock
    private OrderApplicationService orderApplicationService;
    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
    }

    @Test
    void testCreateOrder_Success_Returns201() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("user001");
        request.setMerchantId("merchant001");
        request.setSku("SKU-001");
        request.setQuantity(2);
        when(orderApplicationService.createOrder(any())).thenReturn(buildOrderDTO("ORD-001", "SKU-001", 2, new BigDecimal("20.00"), new BigDecimal("40.00")));

        // When
        // Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.orderNo").value("ORD-001"))
            .andExpect(jsonPath("$.data.totalAmount").value(40.00));
    }

    @Test
    void testCreateOrder_ValidationError_Returns400() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("user001");
        request.setQuantity(0);

        // When
        // Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void testGetOrder_Success() throws Exception {
        // Given
        when(orderApplicationService.getOrder("ORD-001")).thenReturn(buildOrderDTO("ORD-001", "SKU-001", 1, new BigDecimal("10.00"), new BigDecimal("10.00")));

        // When
        // Then
        mockMvc.perform(get("/api/v1/orders/ORD-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.orderNo").value("ORD-001"));
    }

    @Test
    void testGetOrder_NotFound_Returns404() throws Exception {
        // Given
        when(orderApplicationService.getOrder("ORD-404")).thenThrow(new NotFoundException("订单不存在"));

        // When
        // Then
        mockMvc.perform(get("/api/v1/orders/ORD-404"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void testListUserOrders_Success() throws Exception {
        // Given
        when(orderApplicationService.listUserOrders("user001")).thenReturn(List.of(
            buildOrderDTO("ORD-001", "SKU-001", 1, new BigDecimal("10.00"), new BigDecimal("10.00")),
            buildOrderDTO("ORD-002", "SKU-002", 2, new BigDecimal("15.00"), new BigDecimal("30.00"))
        ));

        // When
        // Then
        mockMvc.perform(get("/api/v1/users/user001/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].orderNo").value("ORD-001"))
            .andExpect(jsonPath("$.data[1].orderNo").value("ORD-002"));
    }

    private OrderDTO buildOrderDTO(String orderNo, String sku, int quantity, BigDecimal unitPrice, BigDecimal totalAmount) {
        return OrderDTO.builder()
            .orderNo(orderNo)
            .userId("user001")
            .merchantId("merchant001")
            .sku(sku)
            .quantity(quantity)
            .unitPrice(unitPrice)
            .totalAmount(totalAmount)
            .status("CREATED")
            .createdAt(LocalDateTime.now())
            .build();
    }
}
