package com.trading.merchant.interfaces.feign;

import com.trading.api.merchant.MerchantServiceApi;
import com.trading.api.merchant.dto.CheckStockRequest;
import com.trading.api.merchant.dto.CheckStockResponse;
import com.trading.api.merchant.dto.ConfirmOrderRequest;
import com.trading.api.merchant.dto.ConfirmOrderResponse;
import com.trading.api.merchant.dto.RollbackStockRequest;
import com.trading.merchant.application.InventoryApplicationService;
import com.trading.merchant.application.OrderProcessApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class MerchantServiceApiImpl implements MerchantServiceApi {
    private final InventoryApplicationService inventoryApplicationService;
    private final OrderProcessApplicationService orderProcessApplicationService;

    @Override
    public CheckStockResponse checkStock(@RequestBody @Valid CheckStockRequest request) {
        return inventoryApplicationService.checkStock(request);
    }

    @Override
    public ConfirmOrderResponse confirmOrder(@RequestBody @Valid ConfirmOrderRequest request) {
        return orderProcessApplicationService.processOrder(request);
    }

    @Override
    public ConfirmOrderResponse rollbackStock(@RequestBody @Valid RollbackStockRequest request) {
        return orderProcessApplicationService.rollbackStock(request);
    }
}
