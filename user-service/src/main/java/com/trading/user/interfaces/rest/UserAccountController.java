package com.trading.user.interfaces.rest;

import com.trading.common.dto.ApiResponse;
import com.trading.user.application.UserAccountApplicationService;
import com.trading.user.application.dto.AccountDTO;
import com.trading.user.application.dto.RechargeCommand;
import com.trading.user.application.dto.TransactionDTO;
import com.trading.user.interfaces.dto.request.RechargeRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户账户 REST 控制器
 *
 * <p>提供用户账户相关的 HTTP API，基础路径：/api/v1/users</p>
 *
 * <p>接口列表：</p>
 * <ul>
 *   <li>POST /api/v1/users/{userId}/account/recharge - 充值</li>
 *   <li>GET  /api/v1/users/{userId}/account - 查询账户</li>
 *   <li>GET  /api/v1/users/{userId}/transactions - 查询账户流水</li>
 * </ul>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserAccountController {

    /** 用户账户应用服务 */
    private final UserAccountApplicationService userAccountApplicationService;

    /**
     * 充值接口
     *
     * <p>为指定用户充值，如果账户不存在会自动创建。</p>
     *
     * @param userId  路径参数，用户ID
     * @param request 充值请求体，包含金额（最小0.01）
     * @return 201 Created，包含充值后的账户信息
     */
    @PostMapping("/{userId}/account/recharge")
    public ResponseEntity<ApiResponse<AccountDTO>> recharge(@PathVariable String userId, @RequestBody @Valid RechargeRequest request) {
        AccountDTO accountDTO = userAccountApplicationService.recharge(new RechargeCommand(userId, request.getAmount()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(accountDTO));
    }

    /**
     * 查询账户接口
     *
     * @param userId 路径参数，用户ID
     * @return 账户信息，账户不存在时返回 404
     */
    @GetMapping("/{userId}/account")
    public ApiResponse<AccountDTO> getAccount(@PathVariable String userId) {
        return ApiResponse.success(userAccountApplicationService.getAccount(userId));
    }

    /**
     * 查询账户流水接口
     *
     * @param userId 路径参数，用户ID
     * @return 账户流水列表（包含充值和扣款记录），账户不存在时返回 404
     */
    @GetMapping("/{userId}/transactions")
    public ApiResponse<List<TransactionDTO>> getTransactions(@PathVariable String userId) {
        return ApiResponse.success(userAccountApplicationService.getTransactions(userId));
    }
}
