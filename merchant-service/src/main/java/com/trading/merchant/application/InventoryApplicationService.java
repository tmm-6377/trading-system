package com.trading.merchant.application;

import com.trading.api.merchant.dto.CheckStockRequest;
import com.trading.api.merchant.dto.CheckStockResponse;
import com.trading.common.exception.NotFoundException;
import com.trading.merchant.application.dto.AddProductCommand;
import com.trading.merchant.application.dto.InventoryView;
import com.trading.merchant.domain.model.MerchantAccount;
import com.trading.merchant.domain.model.Money;
import com.trading.merchant.domain.model.ProductInventory;
import com.trading.merchant.domain.repository.MerchantAccountRepository;
import com.trading.merchant.domain.repository.ProductInventoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryApplicationService {
    private final ProductInventoryRepository productInventoryRepository;
    private final MerchantAccountRepository merchantAccountRepository;

    @Transactional
    public InventoryView addProduct(AddProductCommand command) {
        ProductInventory inventory = productInventoryRepository.findByMerchantIdAndSku(command.getMerchantId(), command.getSku())
            .map(existing -> {
                existing.addStock(command.getQuantity());
                return existing;
            })
            .orElse(ProductInventory.create(
                command.getMerchantId(),
                command.getSku(),
                command.getProductName(),
                Money.of(command.getPrice()),
                command.getQuantity()
            ));
        merchantAccountRepository.findByMerchantId(command.getMerchantId())
            .orElseGet(() -> merchantAccountRepository.save(MerchantAccount.create(command.getMerchantId())));
        return toView(productInventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryView updateStock(String merchantId, String sku, int quantity) {
        ProductInventory inventory = productInventoryRepository.findByMerchantIdAndSku(merchantId, sku)
            .orElseThrow(() -> new NotFoundException("商品不存在"));
        inventory.addStock(quantity);
        return toView(productInventoryRepository.save(inventory));
    }

    public List<InventoryView> listInventory(String merchantId) {
        return productInventoryRepository.findByMerchantId(merchantId).stream().map(this::toView).toList();
    }

    public InventoryView getInventory(String merchantId, String sku) {
        return productInventoryRepository.findByMerchantIdAndSku(merchantId, sku)
            .map(this::toView)
            .orElseThrow(() -> new NotFoundException("商品不存在"));
    }

    public CheckStockResponse checkStock(CheckStockRequest request) {
        return productInventoryRepository.findByMerchantIdAndSku(request.getMerchantId(), request.getSku())
            .map(inventory -> CheckStockResponse.builder()
                .available(inventory.getAvailableQuantity() >= request.getQuantity())
                .unitPrice(inventory.getPrice().getAmount())
                .errorMessage(inventory.getAvailableQuantity() >= request.getQuantity() ? null : "库存不足")
                .build())
            .orElse(CheckStockResponse.builder().available(false).errorMessage("商品不存在").build());
    }

    private InventoryView toView(ProductInventory inventory) {
        return InventoryView.builder()
            .merchantId(inventory.getMerchantId())
            .sku(inventory.getSku().getValue())
            .productName(inventory.getProductName())
            .price(inventory.getPrice().getAmount())
            .availableQuantity(inventory.getAvailableQuantity())
            .soldQuantity(inventory.getSoldQuantity())
            .build();
    }
}
