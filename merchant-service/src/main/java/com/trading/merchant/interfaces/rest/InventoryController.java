package com.trading.merchant.interfaces.rest;

import com.trading.common.dto.ApiResponse;
import com.trading.merchant.application.InventoryApplicationService;
import com.trading.merchant.application.dto.AddProductCommand;
import com.trading.merchant.application.dto.InventoryView;
import com.trading.merchant.interfaces.dto.request.AddInventoryRequest;
import com.trading.merchant.interfaces.dto.request.UpdateStockRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存管理 REST 控制器。
 *
 * <p>向商家后台提供商品新增、库存调整、库存列表查询和库存详情查询等 HTTP 接口，
 * 负责接收请求参数、调用应用服务并统一包装响应结果。</p>
 */
@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryApplicationService inventoryApplicationService;

    /**
     * 新增商品库存。
     *
     * @param merchantId 商家标识
     * @param request 新增库存请求
     * @return 创建后的库存信息，HTTP 状态码为 201
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InventoryView>> addProduct(@PathVariable String merchantId, @RequestBody @Valid AddInventoryRequest request) {
        InventoryView inventory = inventoryApplicationService.addProduct(new AddProductCommand(
            merchantId,
            request.getSku(),
            request.getProductName(),
            request.getPrice(),
            request.getQuantity()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(inventory));
    }

    /**
     * 更新指定商品库存数量。
     *
     * @param merchantId 商家标识
     * @param sku 商品 SKU
     * @param request 库存调整请求
     * @return 更新后的库存信息
     */
    @PutMapping("/{sku}")
    public ApiResponse<InventoryView> updateStock(@PathVariable String merchantId, @PathVariable String sku, @RequestBody @Valid UpdateStockRequest request) {
        return ApiResponse.success(inventoryApplicationService.updateStock(merchantId, sku, request.getQuantity()));
    }

    /**
     * 查询商家全部库存。
     *
     * @param merchantId 商家标识
     * @return 库存列表
     */
    @GetMapping
    public ApiResponse<List<InventoryView>> listInventory(@PathVariable String merchantId) {
        return ApiResponse.success(inventoryApplicationService.listInventory(merchantId));
    }

    /**
     * 查询单个商品库存详情。
     *
     * @param merchantId 商家标识
     * @param sku 商品 SKU
     * @return 商品库存详情
     */
    @GetMapping("/{sku}")
    public ApiResponse<InventoryView> getInventory(@PathVariable String merchantId, @PathVariable String sku) {
        return ApiResponse.success(inventoryApplicationService.getInventory(merchantId, sku));
    }
}
