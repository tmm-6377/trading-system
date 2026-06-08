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

/**
 * 订单处理应用服务
 *
 * <p>负责处理来自用户服务的订单确认和库存回滚请求（Seata 分布式事务参与方）。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>处理订单确认：扣减库存并增加商家余额</li>
 *   <li>处理库存回滚：恢复已扣减的库存（分布式事务补偿）</li>
 * </ul>
 *
 * <p>事务说明：</p>
 * <p>作为 Seata 全局事务的分支事务参与方，本地操作通过本地事务保证，
 * Seata 框架负责全局事务的协调和回滚。</p>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class OrderProcessApplicationService {

    /** 商品库存仓储 */
    private final ProductInventoryRepository productInventoryRepository;

    /** 商家账户仓储 */
    private final MerchantAccountRepository merchantAccountRepository;

    /**
     * 处理订单（扣减库存 + 增加商家余额）
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>查询商品库存</li>
     *   <li>扣减库存（检查并减少可用数量）</li>
     *   <li>计算订单总金额</li>
     *   <li>查询商家账户（不存在则自动创建）</li>
     *   <li>增加商家账户余额</li>
     * </ol>
     *
     * @param request 订单确认请求，包含订单号、商家ID、SKU和数量
     * @return 处理结果，包含成功标识、单价、总金额
     * @throws NotFoundException              如果商品不存在
     * @throws com.trading.common.exception.InsufficientStockException 如果库存不足
     */
    @Transactional(rollbackFor = Exception.class)
    public ConfirmOrderResponse processOrder(ConfirmOrderRequest request) {
        // 查询商品并扣减库存
        ProductInventory inventory = productInventoryRepository.findByMerchantIdAndSku(request.getMerchantId(), request.getSku())
            .orElseThrow(() -> new NotFoundException("商品不存在"));
        inventory.checkAndDeductStock(request.getQuantity());
        ProductInventory savedInventory = productInventoryRepository.save(inventory);

        // 计算订单总金额并增加商家余额
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

    /**
     * 回滚库存（分布式事务补偿操作）
     *
     * <p>当 Seata 全局事务需要回滚时，将已扣减的库存归还给商品。</p>
     *
     * @param request 库存回滚请求，包含商家ID、SKU和回滚数量
     * @return 回滚结果
     * @throws NotFoundException 如果商品不存在
     */
    @Transactional
    public ConfirmOrderResponse rollbackStock(RollbackStockRequest request) {
        ProductInventory inventory = productInventoryRepository.findByMerchantIdAndSku(request.getMerchantId(), request.getSku())
            .orElseThrow(() -> new NotFoundException("商品不存在"));
        // 归还库存，同时减少已售数量
        inventory.rollbackStock(request.getQuantity());
        productInventoryRepository.save(inventory);
        return ConfirmOrderResponse.builder().success(true).message("库存回滚成功").build();
    }
}
