package com.trading.merchant.application;

import com.trading.api.merchant.dto.ConfirmOrderRequest;
import com.trading.api.merchant.dto.ConfirmOrderResponse;
import com.trading.api.merchant.dto.RollbackStockRequest;
import com.trading.common.exception.NotFoundException;
import com.trading.merchant.domain.model.MerchantAccount;
import com.trading.merchant.domain.model.Money;
import com.trading.merchant.domain.model.ProductInventory;
import com.trading.merchant.domain.repository.MerchantAccountRepository;
import com.trading.merchant.domain.repository.ProductInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderProcessApplicationService {
    private final ProductInventoryRepository productInventoryRepository;
    private final MerchantAccountRepository merchantAccountRepository;

    @Transactional
    public ConfirmOrderResponse processOrder(ConfirmOrderRequest request) {
        ProductInventory inventory = productInventoryRepository.findByMerchantIdAndSku(request.getMerchantId(), request.getSku())
            .orElseThrow(() -> new NotFoundException("商品不存在"));
        inventory.checkAndDeductStock(request.getQuantity());
        ProductInventory savedInventory = productInventoryRepository.save(inventory);
        Money totalAmount = savedInventory.getPrice().multiply(request.getQuantity());
        MerchantAccount merchantAccount = merchantAccountRepository.findByMerchantId(request.getMerchantId())
            .orElse(MerchantAccount.create(request.getMerchantId()));
        merchantAccount.credit(totalAmount);
        merchantAccountRepository.save(merchantAccount);
        return ConfirmOrderResponse.builder()
            .success(true)
            .unitPrice(savedInventory.getPrice().getAmount())
            .totalAmount(totalAmount.getAmount())
            .message("订单确认成功")
            .build();
    }

    @Transactional
    public ConfirmOrderResponse rollbackStock(RollbackStockRequest request) {
        ProductInventory inventory = productInventoryRepository.findByMerchantIdAndSku(request.getMerchantId(), request.getSku())
            .orElseThrow(() -> new NotFoundException("商品不存在"));
        inventory.rollbackStock(request.getQuantity());
        productInventoryRepository.save(inventory);
        return ConfirmOrderResponse.builder().success(true).message("库存回滚成功").build();
    }
}
