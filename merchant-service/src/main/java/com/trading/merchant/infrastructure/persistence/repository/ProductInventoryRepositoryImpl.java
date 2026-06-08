package com.trading.merchant.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trading.common.exception.BusinessException;
import com.trading.merchant.domain.model.Money;
import com.trading.merchant.domain.model.ProductInventory;
import com.trading.merchant.domain.model.SKU;
import com.trading.merchant.domain.repository.ProductInventoryRepository;
import com.trading.merchant.infrastructure.persistence.entity.ProductInventoryPO;
import com.trading.merchant.infrastructure.persistence.mapper.ProductInventoryMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductInventoryRepositoryImpl implements ProductInventoryRepository {
    private final ProductInventoryMapper productInventoryMapper;

    @Override
    public ProductInventory save(ProductInventory inventory) {
        ProductInventoryPO po = toPO(inventory);
        if (po.getId() == null) {
            productInventoryMapper.insert(po);
            return toDomain(po);
        }
        if (productInventoryMapper.updateById(po) == 0) {
            throw new BusinessException("CONCURRENT_MODIFICATION", "库存更新失败，请稍后重试");
        }
        return toDomain(productInventoryMapper.selectById(po.getId()));
    }

    @Override
    public Optional<ProductInventory> findByMerchantIdAndSku(String merchantId, String sku) {
        return Optional.ofNullable(productInventoryMapper.selectOne(new LambdaQueryWrapper<ProductInventoryPO>()
            .eq(ProductInventoryPO::getMerchantId, merchantId)
            .eq(ProductInventoryPO::getSku, sku)
            .last("limit 1"))).map(this::toDomain);
    }

    @Override
    public List<ProductInventory> findByMerchantId(String merchantId) {
        return productInventoryMapper.selectList(new LambdaQueryWrapper<ProductInventoryPO>()
                .eq(ProductInventoryPO::getMerchantId, merchantId)
                .orderByAsc(ProductInventoryPO::getSku))
            .stream()
            .map(this::toDomain)
            .toList();
    }

    private ProductInventory toDomain(ProductInventoryPO po) {
        return ProductInventory.builder()
            .id(po.getId())
            .merchantId(po.getMerchantId())
            .sku(SKU.of(po.getSku()))
            .productName(po.getProductName())
            .price(Money.of(po.getPrice()))
            .availableQuantity(po.getAvailableQuantity())
            .soldQuantity(po.getSoldQuantity())
            .version(po.getVersion())
            .build();
    }

    private ProductInventoryPO toPO(ProductInventory inventory) {
        ProductInventoryPO po = new ProductInventoryPO();
        po.setId(inventory.getId());
        po.setMerchantId(inventory.getMerchantId());
        po.setSku(inventory.getSku().getValue());
        po.setProductName(inventory.getProductName());
        po.setPrice(inventory.getPrice().getAmount());
        po.setAvailableQuantity(inventory.getAvailableQuantity());
        po.setSoldQuantity(inventory.getSoldQuantity());
        po.setVersion(inventory.getVersion());
        return po;
    }
}
