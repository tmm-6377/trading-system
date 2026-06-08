package com.trading.api.merchant;

import com.trading.api.merchant.dto.CheckStockRequest;
import com.trading.api.merchant.dto.CheckStockResponse;
import com.trading.api.merchant.dto.ConfirmOrderRequest;
import com.trading.api.merchant.dto.ConfirmOrderResponse;
import com.trading.api.merchant.dto.RollbackStockRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 商家服务 Feign 降级工厂
 *
 * <p>当商家服务不可用（网络超时、服务宕机等）时，提供降级响应，避免雪崩效应。</p>
 *
 * <p>降级策略：</p>
 * <ul>
 *   <li>checkStock - 返回库存不可用，阻止下单</li>
 *   <li>confirmOrder - 返回确认失败，触发事务回滚</li>
 *   <li>rollbackStock - 返回回滚失败，需要人工介入</li>
 * </ul>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Slf4j
@Component
public class MerchantServiceFallbackFactory implements FallbackFactory<MerchantServiceApi> {

    /**
     * 创建降级实例
     *
     * @param cause 触发降级的异常原因
     * @return 降级的 MerchantServiceApi 实现
     */
    @Override
    public MerchantServiceApi create(Throwable cause) {
        log.error("调用商家服务失败", cause);
        return new MerchantServiceApi() {
            @Override
            public CheckStockResponse checkStock(CheckStockRequest request) {
                // 服务不可用时，返回库存不可用，阻止下单
                return CheckStockResponse.builder()
                    .available(false)
                    .errorMessage("商家服务暂时不可用")
                    .build();
            }

            @Override
            public ConfirmOrderResponse confirmOrder(ConfirmOrderRequest request) {
                // 服务不可用时，返回确认失败，触发分布式事务回滚
                return ConfirmOrderResponse.builder()
                    .success(false)
                    .message("商家服务暂时不可用")
                    .build();
            }

            @Override
            public ConfirmOrderResponse rollbackStock(RollbackStockRequest request) {
                // 库存回滚失败时记录日志，需要人工介入处理
                return ConfirmOrderResponse.builder()
                    .success(false)
                    .message("商家服务暂时不可用")
                    .build();
            }
        };
    }
}
