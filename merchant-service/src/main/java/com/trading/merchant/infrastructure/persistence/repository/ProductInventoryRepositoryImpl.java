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

/**
 * 商品库存仓储实现。
 *
 * <p>基于 MyBatis-Plus 实现商品库存领域对象与数据库持久化对象之间的转换，
 * 同时负责在更新失败时抛出并发修改异常，保证库存写入的业务语义清晰可见。</p>
 */
@Repository
@RequiredArgsConstructor
public class ProductInventoryRepositoryImpl implements ProductInventoryRepository {
    private final ProductInventoryMapper productInventoryMapper;

    /**
     * 保存商品库存。
     *
     * <p>当库存记录尚未持久化时执行插入；否则按主键更新并在更新失败时抛出并发修改异常。</p>
     *
     * @param inventory 商品库存领域对象
     * @return 保存后的最新库存对象
     */
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

    /**
     * 根据商家和 SKU 查询库存。
     *
     * @param merchantId 商家标识
     * @param sku 商品 SKU
     * @return 匹配到的库存对象，若不存在则返回空
     */
    @Override
    public Optional<ProductInventory> findByMerchantIdAndSku(String merchantId, String sku) {
        return Optional.ofNullable(productInventoryMapper.selectOne(new LambdaQueryWrapper<ProductInventoryPO>()
            .eq(ProductInventoryPO::getMerchantId, merchantId)
            .eq(ProductInventoryPO::getSku, sku)
            .last("limit 1"))).map(this::toDomain);
    }

    /**
     * 查询商家名下全部库存。
     *
     * @param merchantId 商家标识
     * @return 按 SKU 升序排列的库存列表
     */
    @Override
    public List<ProductInventory> findByMerchantId(String merchantId) {
        return productInventoryMapper.selectList(new LambdaQueryWrapper<ProductInventoryPO>()
                .eq(ProductInventoryPO::getMerchantId, merchantId)
                .orderByAsc(ProductInventoryPO::getSku))
            .stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * 将持久化对象转换为领域对象。
     *
     * @param po 持久化对象
     * @return 领域对象
     */
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

    /**
     * 将领域对象转换为持久化对象。
     *
     * @param inventory 领域对象
     * @return 持久化对象
     */
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
