package com.campus.module.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.module.wallet.dto.WithdrawRequest;
import com.campus.module.wallet.entity.SysTransactionFlow;
import com.campus.module.wallet.entity.SysWallet;
import com.campus.module.wallet.entity.SysWithdrawal;
import com.campus.module.wallet.mapper.SysWithdrawalMapper;
import com.campus.module.wallet.service.SysTransactionFlowService;
import com.campus.module.wallet.service.SysWalletService;
import com.campus.module.wallet.service.SysWithdrawalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 提现申请 Service 实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysWithdrawalServiceImpl extends ServiceImpl<SysWithdrawalMapper, SysWithdrawal> implements SysWithdrawalService {

    private final SysWalletService walletService;
    private final SysTransactionFlowService transactionFlowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyWithdraw(Long userId, WithdrawRequest request) {
        // 1. 获取用户钱包
        SysWallet wallet = walletService.getByUserId(userId);
        if (wallet == null) {
            throw new BusinessException("钱包不存在");
        }

        // 2. 检查余额是否充足
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("余额不足");
        }

        // 3. 校验提现渠道
        Integer channel = request.getChannel();
        if (channel == null || channel < 1 || channel > 3) {
            throw new BusinessException("提现渠道不正确");
        }

        // 4. 冻结提现金额
        boolean frozen = walletService.freeze(userId, request.getAmount());
        if (!frozen) {
            throw new BusinessException("冻结金额失败");
        }

        // 5. 扣减可用余额
        boolean deducted = walletService.deduct(userId, request.getAmount());
        if (!deducted) {
            throw new BusinessException("扣减余额失败");
        }

        // 6. 创建提现申请记录
        SysWithdrawal withdrawal = new SysWithdrawal();
        withdrawal.setUserId(userId);
        withdrawal.setAmount(request.getAmount());
        withdrawal.setChannel(request.getChannel());
        withdrawal.setAccountNo(request.getAccountNo());
        withdrawal.setStatus(SysWithdrawal.STATUS_PENDING);
        withdrawal.setCreateTime(LocalDateTime.now());
        save(withdrawal);

        return withdrawal.getId();
    }

    @Override
    public IPage<SysWithdrawal> pageByUserId(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<SysWithdrawal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysWithdrawal::getUserId, userId)
               .orderByDesc(SysWithdrawal::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, boolean approved, String auditRemark) {
        SysWithdrawal withdrawal = getById(id);
        if (withdrawal == null) {
            throw new BusinessException("提现记录不存在");
        }

        if (withdrawal.getStatus() != SysWithdrawal.STATUS_PENDING) {
            throw new BusinessException("该提现申请已处理");
        }

        Long userId = withdrawal.getUserId();
        SysWallet wallet = walletService.getByUserId(userId);

        if (approved) {
            // 审核通过：解冻金额，记录流水
            walletService.unfreeze(userId, withdrawal.getAmount());
            withdrawal.setStatus(SysWithdrawal.STATUS_APPROVED);

            // 调用微信支付提现
            if (withdrawal.getChannel() == SysWithdrawal.CHANNEL_WECHAT) {
                // 这里需要调用微信支付的企业付款功能
                // 实际项目中需要集成微信支付的企业付款API
                // 暂时模拟提现成功
                log.info("微信提现处理: userId={}, amount={}, accountNo={}", 
                        userId, withdrawal.getAmount(), withdrawal.getAccountNo());
            }

            // 记录提现流水
            transactionFlowService.recordFlow(
                userId,
                withdrawal.getAmount().negate(),
                wallet.getBalance(),
                SysTransactionFlow.TYPE_WITHDRAW,
                null,
                "提现到" + getChannelName(withdrawal.getChannel())
            );
        } else {
            // 审核驳回：解冻金额并返还余额
            walletService.unfreeze(userId, withdrawal.getAmount());
            walletService.recharge(userId, withdrawal.getAmount());
            withdrawal.setStatus(SysWithdrawal.STATUS_REJECTED);
        }

        withdrawal.setAuditRemark(auditRemark);
        updateById(withdrawal);
    }

    private String getChannelName(Integer channel) {
        return switch (channel) {
            case SysWithdrawal.CHANNEL_WECHAT -> "微信";
            case SysWithdrawal.CHANNEL_ALIPAY -> "支付宝";
            case SysWithdrawal.CHANNEL_BANK -> "银行卡";
            default -> "未知渠道";
        };
    }
}
