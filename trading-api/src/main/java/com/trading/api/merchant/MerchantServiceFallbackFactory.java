package com.trading.api.merchant;

import com.trading.api.merchant.dto.CheckStockRequest;
import com.trading.api.merchant.dto.CheckStockResponse;
import com.trading.api.merchant.dto.ConfirmOrderRequest;
import com.trading.api.merchant.dto.ConfirmOrderResponse;
import com.trading.api.merchant.dto.RollbackStockRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MerchantServiceFallbackFactory implements FallbackFactory<MerchantServiceApi> {
    @Override
    public MerchantServiceApi create(Throwable cause) {
        log.error("调用商家服务失败", cause);
        return new MerchantServiceApi() {
            @Override
            public CheckStockResponse checkStock(CheckStockRequest request) {
                return CheckStockResponse.builder()
                    .available(false)
                    .errorMessage("商家服务暂时不可用")
                    .build();
            }

            @Override
            public ConfirmOrderResponse confirmOrder(ConfirmOrderRequest request) {
                return ConfirmOrderResponse.builder()
                    .success(false)
                    .message("商家服务暂时不可用")
                    .build();
            }

            @Override
            public ConfirmOrderResponse rollbackStock(RollbackStockRequest request) {
                return ConfirmOrderResponse.builder()
                    .success(false)
                    .message("商家服务暂时不可用")
                    .build();
            }
        };
    }
}
