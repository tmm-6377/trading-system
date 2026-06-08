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

@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserAccountController {
    private final UserAccountApplicationService userAccountApplicationService;

    @PostMapping("/{userId}/account/recharge")
    public ResponseEntity<ApiResponse<AccountDTO>> recharge(@PathVariable String userId, @RequestBody @Valid RechargeRequest request) {
        AccountDTO accountDTO = userAccountApplicationService.recharge(new RechargeCommand(userId, request.getAmount()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(accountDTO));
    }

    @GetMapping("/{userId}/account")
    public ApiResponse<AccountDTO> getAccount(@PathVariable String userId) {
        return ApiResponse.success(userAccountApplicationService.getAccount(userId));
    }

    @GetMapping("/{userId}/transactions")
    public ApiResponse<List<TransactionDTO>> getTransactions(@PathVariable String userId) {
        return ApiResponse.success(userAccountApplicationService.getTransactions(userId));
    }
}
