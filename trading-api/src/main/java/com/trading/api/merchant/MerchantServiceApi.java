package com.trading.api.merchant;

import com.trading.api.merchant.dto.CheckStockRequest;
import com.trading.api.merchant.dto.CheckStockResponse;
import com.trading.api.merchant.dto.ConfirmOrderRequest;
import com.trading.api.merchant.dto.ConfirmOrderResponse;
import com.trading.api.merchant.dto.RollbackStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 商家服务 Feign 客户端接口
 *
 * <p>定义用户服务调用商家服务的 RPC 接口，通过 Nacos 注册中心进行服务发现。</p>
 *
 * <p>接口功能：</p>
 * <ul>
 *   <li>查库存 - 下单前验证商品库存是否充足</li>
 *   <li>确认订单 - 扣减库存并记录商家收款</li>
 *   <li>回滚库存 - Seata 分布式事务补偿操作</li>
 * </ul>
 *
 * <p>降级策略：通过 {@link MerchantServiceFallbackFactory} 实现，当商家服务不可用时返回默认响应。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@FeignClient(
    name = "merchant-service",
    path = "/internal",
    fallbackFactory = MerchantServiceFallbackFactory.class
)
public interface MerchantServiceApi {

    /**
     * 检查商品库存是否充足
     *
     * <p>在下单前调用，验证指定商品的可用库存是否满足购买数量要求。</p>
     * <p>如果库存充足，同时返回商品单价用于后续扣款计算。</p>
     *
     * @param request 库存检查请求，包含商家ID、SKU和购买数量
     * @return 库存检查结果，包含是否可用、单价和错误信息
     */
    @PostMapping("/inventory/check-stock")
    CheckStockResponse checkStock(@RequestBody CheckStockRequest request);

    /**
     * 确认订单并扣减库存
     *
     * <p>在用户余额扣减成功后调用，扣减商品库存并增加商家账户余额。</p>
     * <p>此操作参与 Seata 分布式事务，失败时需要通过 rollbackStock 回滚。</p>
     *
     * @param request 订单确认请求，包含订单号、商家ID、SKU和数量
     * @return 确认结果，包含是否成功、单价、总金额和消息
     */
    @PostMapping("/orders/confirm")
    ConfirmOrderResponse confirmOrder(@RequestBody ConfirmOrderRequest request);

    /**
     * 回滚库存（Seata 分布式事务补偿）
     *
     * <p>当分布式事务需要回滚时，将已扣减的库存归还给商品，同时减少已售数量。</p>
     * <p>由 Seata 框架在分支事务回滚时自动调用。</p>
     *
     * @param request 库存回滚请求，包含商家ID、SKU和回滚数量
     * @return 回滚结果，包含是否成功和消息
     */
    @PostMapping("/inventory/rollback")
    ConfirmOrderResponse rollbackStock(@RequestBody RollbackStockRequest request);
}
