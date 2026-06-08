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

/**
 * 商家信息 REST 控制器。
 *
 * <p>用于提供商家账户查询和结算记录查询接口，面向商家后台或运营平台输出统一结构的响应数据。</p>
 */
@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {
    private final MerchantApplicationService merchantApplicationService;
    private final SettlementApplicationService settlementApplicationService;

    /**
     * 查询商家账户信息。
     *
     * @param merchantId 商家标识
     * @return 商家账户余额信息
     */
    @GetMapping("/{merchantId}/account")
    public ApiResponse<MerchantAccountDTO> getAccount(@PathVariable String merchantId) {
        return ApiResponse.success(merchantApplicationService.getAccount(merchantId));
    }

    /**
     * 查询商家结算记录列表。
     *
     * @param merchantId 商家标识
     * @return 商家结算记录列表
     */
    @GetMapping("/{merchantId}/settlements")
    public ApiResponse<List<SettlementRecordDTO>> listSettlements(@PathVariable String merchantId) {
        return ApiResponse.success(settlementApplicationService.listSettlements(merchantId));
    }
}
