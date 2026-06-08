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

/**
 * 商家服务内部 Feign API 实现。
 *
 * <p>该控制器实现 {@link MerchantServiceApi} 定义的内部调用契约，供订单等其他微服务通过 Feign 远程调用，
 * 完成库存检查、订单确认和库存回滚等跨服务协作流程。</p>
 */
@Validated
@RestController
@RequiredArgsConstructor
public class MerchantServiceApiImpl implements MerchantServiceApi {
    private final InventoryApplicationService inventoryApplicationService;
    private final OrderProcessApplicationService orderProcessApplicationService;

    /**
     * 校验商品库存是否满足下单要求。
     *
     * @param request 库存校验请求
     * @return 库存校验结果
     */
    @Override
    public CheckStockResponse checkStock(@RequestBody @Valid CheckStockRequest request) {
        return inventoryApplicationService.checkStock(request);
    }

    /**
     * 确认订单并扣减库存。
     *
     * @param request 订单确认请求
     * @return 订单确认结果
     */
    @Override
    public ConfirmOrderResponse confirmOrder(@RequestBody @Valid ConfirmOrderRequest request) {
        return orderProcessApplicationService.processOrder(request);
    }

    /**
     * 回滚订单占用库存。
     *
     * @param request 库存回滚请求
     * @return 回滚结果
     */
    @Override
    public ConfirmOrderResponse rollbackStock(@RequestBody @Valid RollbackStockRequest request) {
        return orderProcessApplicationService.rollbackStock(request);
    }
}
