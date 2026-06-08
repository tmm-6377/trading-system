package com.trading.api.merchant;

import com.trading.api.merchant.dto.CheckStockRequest;
import com.trading.api.merchant.dto.CheckStockResponse;
import com.trading.api.merchant.dto.ConfirmOrderRequest;
import com.trading.api.merchant.dto.ConfirmOrderResponse;
import com.trading.api.merchant.dto.RollbackStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "merchant-service",
    path = "/internal",
    fallbackFactory = MerchantServiceFallbackFactory.class
)
public interface MerchantServiceApi {
    @PostMapping("/inventory/check-stock")
    CheckStockResponse checkStock(@RequestBody CheckStockRequest request);

    @PostMapping("/orders/confirm")
    ConfirmOrderResponse confirmOrder(@RequestBody ConfirmOrderRequest request);

    @PostMapping("/inventory/rollback")
    ConfirmOrderResponse rollbackStock(@RequestBody RollbackStockRequest request);
}
