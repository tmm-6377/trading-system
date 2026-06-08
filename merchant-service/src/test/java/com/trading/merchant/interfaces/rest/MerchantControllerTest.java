package com.trading.merchant.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.common.exception.NotFoundException;
import com.trading.merchant.application.MerchantApplicationService;
import com.trading.merchant.application.SettlementApplicationService;
import com.trading.merchant.application.dto.MerchantAccountDTO;
import com.trading.merchant.application.dto.SettlementRecordDTO;
import com.trading.merchant.interfaces.exception.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MerchantControllerTest {

    @Mock
    private MerchantApplicationService merchantApplicationService;
    @Mock
    private SettlementApplicationService settlementApplicationService;
    @InjectMocks
    private MerchantController merchantController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(merchantController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
    }

    @Test
    void testGetAccount_Success() throws Exception {
        MerchantAccountDTO accountDTO = MerchantAccountDTO.builder()
            .merchantId("merchant001")
            .balance(new BigDecimal("88.88"))
            .build();
        when(merchantApplicationService.getAccount("merchant001")).thenReturn(accountDTO);

        mockMvc.perform(get("/api/v1/merchants/merchant001/account"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.balance").value(88.88));
    }

    @Test
    void testGetAccount_NotFound_Returns404() throws Exception {
        when(merchantApplicationService.getAccount("merchant404")).thenThrow(new NotFoundException("商家账户不存在"));

        mockMvc.perform(get("/api/v1/merchants/merchant404/account"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void testListSettlements_Success() throws Exception {
        List<SettlementRecordDTO> settlements = List.of(SettlementRecordDTO.builder()
            .settlementDate(LocalDate.of(2026, 6, 8))
            .expectedAmount(new BigDecimal("20.00"))
            .actualAmount(new BigDecimal("20.00"))
            .status("MATCH")
            .remark("结算正常")
            .createdAt(LocalDateTime.of(2026, 6, 8, 2, 0))
            .build());
        when(settlementApplicationService.listSettlements("merchant001")).thenReturn(settlements);

        mockMvc.perform(get("/api/v1/merchants/merchant001/settlements"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].status").value("MATCH"));
    }
}
