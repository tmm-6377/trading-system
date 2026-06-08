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

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryApplicationService inventoryApplicationService;

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

    @PutMapping("/{sku}")
    public ApiResponse<InventoryView> updateStock(@PathVariable String merchantId, @PathVariable String sku, @RequestBody @Valid UpdateStockRequest request) {
        return ApiResponse.success(inventoryApplicationService.updateStock(merchantId, sku, request.getQuantity()));
    }

    @GetMapping
    public ApiResponse<List<InventoryView>> listInventory(@PathVariable String merchantId) {
        return ApiResponse.success(inventoryApplicationService.listInventory(merchantId));
    }

    @GetMapping("/{sku}")
    public ApiResponse<InventoryView> getInventory(@PathVariable String merchantId, @PathVariable String sku) {
        return ApiResponse.success(inventoryApplicationService.getInventory(merchantId, sku));
    }
}
