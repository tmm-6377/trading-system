package com.trading.merchant.interfaces.rest;

import com.trading.common.dto.ApiResponse;
import com.trading.merchant.application.MerchantApplicationService;
import com.trading.merchant.application.SettlementApplicationService;
import com.trading.merchant.application.dto.MerchantAccountDTO;
import com.trading.merchant.application.dto.SettlementRecordDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {
    private final MerchantApplicationService merchantApplicationService;
    private final SettlementApplicationService settlementApplicationService;

    @GetMapping("/{merchantId}/account")
    public ApiResponse<MerchantAccountDTO> getAccount(@PathVariable String merchantId) {
        return ApiResponse.success(merchantApplicationService.getAccount(merchantId));
    }

    @GetMapping("/{merchantId}/settlements")
    public ApiResponse<List<SettlementRecordDTO>> listSettlements(@PathVariable String merchantId) {
        return ApiResponse.success(settlementApplicationService.listSettlements(merchantId));
    }
}
