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
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 钱包控制器
 */
@Tag(name = "钱包模块", description = "钱包信息、交易流水、提现管理")
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Validated
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
            walletService.createWallet(userId);
            wallet = walletService.getByUserId(userId);
        }
        if (wallet == null) {
            return Result.fail("钱包创建失败，请稍后重试");
        }
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

    /**
     * 充值 (Mock - 实际生产环境需要对接支付网关)
     */
    @Operation(summary = "充值", description = "模拟充值接口，实际需要对接支付网关")
    @PostMapping("/recharge")
    public Result<Long> recharge(
            @Parameter(description = "充值金额") @RequestParam @DecimalMin(value = "0.01", message = "充值金额必须大于0") java.math.BigDecimal amount,
            @Parameter(description = "支付方式: wechat/alipay") @RequestParam(defaultValue = "wechat") String paymentMethod) {
        Long userId = UserContext.getUserId();
        // Mock: 直接增加余额
        Long flowId = walletService.recharge(userId, amount, paymentMethod);
        return Result.success("充值成功", flowId);
    }
}
