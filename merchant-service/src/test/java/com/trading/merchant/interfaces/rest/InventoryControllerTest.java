package com.trading.merchant.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.exception.NotFoundException;
import com.trading.merchant.application.InventoryApplicationService;
import com.trading.merchant.application.dto.InventoryView;
import com.trading.merchant.interfaces.dto.request.AddInventoryRequest;
import com.trading.merchant.interfaces.dto.request.UpdateStockRequest;
import com.trading.merchant.interfaces.exception.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryApplicationService inventoryApplicationService;
    @InjectMocks
    private InventoryController inventoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
    }

    @Test
    void testAddInventory_Success_Returns201() throws Exception {
        AddInventoryRequest request = new AddInventoryRequest();
        request.setSku("SKU-001");
        request.setProductName("MacBook");
        request.setPrice(new BigDecimal("99.99"));
        request.setQuantity(5);
        InventoryView view = InventoryView.builder()
            .merchantId("merchant001")
            .sku("SKU-001")
            .productName("MacBook")
            .price(new BigDecimal("99.99"))
            .availableQuantity(5)
            .soldQuantity(0)
            .build();
        when(inventoryApplicationService.addProduct(any())).thenReturn(view);

        mockMvc.perform(post("/api/v1/merchants/merchant001/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.sku").value("SKU-001"))
            .andExpect(jsonPath("$.data.availableQuantity").value(5));
    }

    @Test
    void testAddInventory_ValidationError_Returns400() throws Exception {
        AddInventoryRequest request = new AddInventoryRequest();
        request.setSku(" ");
        request.setProductName("");
        request.setPrice(BigDecimal.ZERO);
        request.setQuantity(0);

        mockMvc.perform(post("/api/v1/merchants/merchant001/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void testUpdateStock_Success() throws Exception {
        UpdateStockRequest request = new UpdateStockRequest();
        request.setQuantity(3);
        InventoryView view = InventoryView.builder()
            .merchantId("merchant001")
            .sku("SKU-001")
            .productName("MacBook")
            .price(new BigDecimal("99.99"))
            .availableQuantity(13)
            .soldQuantity(0)
            .build();
        when(inventoryApplicationService.updateStock("merchant001", "SKU-001", 3)).thenReturn(view);

        mockMvc.perform(put("/api/v1/merchants/merchant001/inventory/SKU-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.availableQuantity").value(13));
    }

    @Test
    void testListInventory_Success() throws Exception {
        List<InventoryView> inventories = List.of(InventoryView.builder()
            .merchantId("merchant001")
            .sku("SKU-001")
            .productName("MacBook")
            .price(new BigDecimal("99.99"))
            .availableQuantity(10)
            .soldQuantity(1)
            .build());
        when(inventoryApplicationService.listInventory("merchant001")).thenReturn(inventories);

        mockMvc.perform(get("/api/v1/merchants/merchant001/inventory"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].sku").value("SKU-001"));
    }

    @Test
    void testGetInventory_Success() throws Exception {
        InventoryView view = InventoryView.builder()
            .merchantId("merchant001")
            .sku("SKU-001")
            .productName("MacBook")
            .price(new BigDecimal("99.99"))
            .availableQuantity(10)
            .soldQuantity(1)
            .build();
        when(inventoryApplicationService.getInventory("merchant001", "SKU-001")).thenReturn(view);

        mockMvc.perform(get("/api/v1/merchants/merchant001/inventory/SKU-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.sku").value("SKU-001"));
    }

    @Test
    void testGetInventory_NotFound_Returns404() throws Exception {
        when(inventoryApplicationService.getInventory(eq("merchant001"), eq("SKU-404"))).thenThrow(new NotFoundException("商品不存在"));

        mockMvc.perform(get("/api/v1/merchants/merchant001/inventory/SKU-404"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
