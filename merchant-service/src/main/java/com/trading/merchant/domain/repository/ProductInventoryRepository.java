package com.trading.merchant.domain.repository;

import com.trading.merchant.domain.model.ProductInventory;
import java.util.List;
import java.util.Optional;

public interface ProductInventoryRepository {
    ProductInventory save(ProductInventory inventory);
    Optional<ProductInventory> findByMerchantIdAndSku(String merchantId, String sku);
    List<ProductInventory> findByMerchantId(String merchantId);
}
