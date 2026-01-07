package com.campus.module.wallet.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.module.wallet.dto.WithdrawRequest;
import com.campus.module.wallet.entity.SysTransactionFlow;
import com.campus.module.wallet.entity.SysWallet;
import com.campus.module.wallet.entity.SysWithdrawal;
import com.campus.module.wallet.service.SysTransactionFlowService;
import com.campus.module.wallet.service.SysWalletService;
import com.campus.module.wallet.service.SysWithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 钱包控制器
 */
@Tag(name = "钱包模块", description = "钱包信息、交易流水、提现管理")
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final SysWalletService walletService;
    private final SysTransactionFlowService transactionFlowService;
    private final SysWithdrawalService withdrawalService;

    /**
     * 获取当前用户钱包信息
     */
    @Operation(summary = "获取当前用户钱包信息")
    @GetMapping
    public Result<SysWallet> getWallet() {
        Long userId = UserContext.getUserId();
        SysWallet wallet = walletService.getByUserId(userId);
        if (wallet == null) {
            // 如果钱包不存在，自动创建
            walletService.createWallet(userId);
            wallet = walletService.getByUserId(userId);
        }
        // 清空敏感信息
        wallet.setPayPassword(null);
        return Result.success(wallet);
    }

    /**
     * 分页获取交易流水
     */
    @Operation(summary = "分页获取交易流水")
    @GetMapping("/transactions")
    public Result<IPage<SysTransactionFlow>> getTransactions(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        IPage<SysTransactionFlow> result = transactionFlowService.pageByUserId(userId, page, size);
        return Result.success(result);
    }

    /**
     * 发起提现申请
     */
    @Operation(summary = "发起提现申请")
    @PostMapping("/withdraw")
    public Result<Long> withdraw(@Valid @RequestBody WithdrawRequest request) {
        Long userId = UserContext.getUserId();
        Long withdrawalId = withdrawalService.applyWithdraw(userId, request);
        return Result.success(withdrawalId);
    }

    /**
     * 获取提现记录列表
     */
    @Operation(summary = "获取提现记录列表")
    @GetMapping("/withdrawals")
    public Result<IPage<SysWithdrawal>> getWithdrawals(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        IPage<SysWithdrawal> result = withdrawalService.pageByUserId(userId, page, size);
        return Result.success(result);
    }
}
