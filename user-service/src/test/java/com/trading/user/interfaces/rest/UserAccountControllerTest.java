package com.trading.user.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.exception.NotFoundException;
import com.trading.user.application.UserAccountApplicationService;
import com.trading.user.application.dto.AccountDTO;
import com.trading.user.interfaces.exception.GlobalExceptionHandler;
import com.trading.user.interfaces.dto.request.RechargeRequest;
import java.math.BigDecimal;
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
class UserAccountControllerTest {
    @Mock
    private UserAccountApplicationService userAccountApplicationService;
    @InjectMocks
    private UserAccountController userAccountController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userAccountController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
    }

    @Test
    void testRecharge_Success() throws Exception {
        // Given
        RechargeRequest request = new RechargeRequest();
        request.setAmount(new BigDecimal("100.00"));
        when(userAccountApplicationService.recharge(any())).thenReturn(AccountDTO.builder()
            .userId("user001")
            .balance(new BigDecimal("100.00"))
            .build());

        // When
        // Then
        mockMvc.perform(post("/api/v1/users/user001/account/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value("user001"))
            .andExpect(jsonPath("$.data.balance").value(100.00));
    }

    @Test
    void testRecharge_InvalidRequest() throws Exception {
        // Given
        RechargeRequest request = new RechargeRequest();

        // When
        // Then
        mockMvc.perform(post("/api/v1/users/user001/account/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void testGetAccount_Success() throws Exception {
        // Given
        when(userAccountApplicationService.getAccount("user001")).thenReturn(AccountDTO.builder()
            .userId("user001")
            .balance(new BigDecimal("50.00"))
            .build());

        // When
        // Then
        mockMvc.perform(get("/api/v1/users/user001/account"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value("user001"))
            .andExpect(jsonPath("$.data.balance").value(50.00));
    }

    @Test
    void testGetAccount_NotFound_Returns404() throws Exception {
        // Given
        when(userAccountApplicationService.getAccount("missing-user")).thenThrow(new NotFoundException("用户账户不存在"));

        // When
        // Then
        mockMvc.perform(get("/api/v1/users/missing-user/account"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
