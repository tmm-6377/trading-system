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

/**
 * 库存应用服务
 *
 * <p>负责商品库存的管理操作，包括添加商品、更新库存、查询库存和库存检查。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>管理商品库存的增删改查</li>
 *   <li>为用户服务提供库存检查接口（Feign内部调用）</li>
 *   <li>自动初始化商家账户</li>
 * </ul>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class InventoryApplicationService {

    /** 商品库存仓储 */
    private final ProductInventoryRepository productInventoryRepository;

    /** 商家账户仓储 */
    private final MerchantAccountRepository merchantAccountRepository;

    /**
     * 添加商品（新增或追加库存）
     *
     * <p>逻辑：</p>
     * <ul>
     *   <li>如果该商家+SKU的商品已存在，则追加库存</li>
     *   <li>如果商品不存在，则创建新商品</li>
     *   <li>如果商家账户不存在，则自动创建</li>
     * </ul>
     *
     * @param command 添加商品命令，包含商家ID、SKU、名称、价格和数量
     * @return 库存视图
     */
    @Transactional
    public InventoryView addProduct(AddProductCommand command) {
        // 查询商品是否已存在，存在则追加库存，否则新建
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
        // 确保商家账户存在，不存在则自动创建
        merchantAccountRepository.findByMerchantId(command.getMerchantId())
            .orElseGet(() -> merchantAccountRepository.save(MerchantAccount.create(command.getMerchantId())));
        return toView(productInventoryRepository.save(inventory));
    }

    /**
     * 更新商品库存（增加）
     *
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @param quantity   增加的数量，必须大于0
     * @return 更新后的库存视图
     * @throws NotFoundException 如果商品不存在
     */
    @Transactional
    public InventoryView updateStock(String merchantId, String sku, int quantity) {
        ProductInventory inventory = productInventoryRepository.findByMerchantIdAndSku(merchantId, sku)
            .orElseThrow(() -> new NotFoundException("商品不存在"));
        inventory.addStock(quantity);
        return toView(productInventoryRepository.save(inventory));
    }

    /**
     * 查询商家的所有商品库存
     *
     * @param merchantId 商家ID
     * @return 库存视图列表
     */
    public List<InventoryView> listInventory(String merchantId) {
        return productInventoryRepository.findByMerchantId(merchantId).stream().map(this::toView).toList();
    }

    /**
     * 查询单个商品库存
     *
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @return 库存视图
     * @throws NotFoundException 如果商品不存在
     */
    public InventoryView getInventory(String merchantId, String sku) {
        return productInventoryRepository.findByMerchantIdAndSku(merchantId, sku)
            .map(this::toView)
            .orElseThrow(() -> new NotFoundException("商品不存在"));
    }

    /**
     * 检查库存是否充足（供用户服务 Feign 调用）
     *
     * <p>检查逻辑：</p>
     * <ul>
     *   <li>商品不存在：返回不可用，错误消息"商品不存在"</li>
     *   <li>库存不足：返回不可用，错误消息"库存不足"</li>
     *   <li>库存充足：返回可用，并携带商品单价</li>
     * </ul>
     *
     * @param request 库存检查请求
     * @return 库存检查结果
     */
    public CheckStockResponse checkStock(CheckStockRequest request) {
        return productInventoryRepository.findByMerchantIdAndSku(request.getMerchantId(), request.getSku())
            .map(inventory -> CheckStockResponse.builder()
                .available(inventory.getAvailableQuantity() >= request.getQuantity())
                .unitPrice(inventory.getPrice().getAmount())
                .errorMessage(inventory.getAvailableQuantity() >= request.getQuantity() ? null : "库存不足")
                .build())
            .orElse(CheckStockResponse.builder().available(false).errorMessage("商品不存在").build());
    }

    /**
     * 将领域对象转换为视图对象
     *
     * @param inventory 商品库存领域对象
     * @return 库存视图
     */
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
