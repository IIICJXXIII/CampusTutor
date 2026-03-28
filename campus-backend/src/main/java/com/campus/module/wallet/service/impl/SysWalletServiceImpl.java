package com.campus.module.wallet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.ResultCode;
import com.campus.module.wallet.entity.SysWallet;
import com.campus.module.wallet.mapper.SysWalletMapper;
import com.campus.module.wallet.service.SysTransactionFlowService;
import com.campus.module.wallet.service.SysWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 用户钱包 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysWalletServiceImpl extends ServiceImpl<SysWalletMapper, SysWallet> implements SysWalletService {

    private static final int MAX_RETRY = 3;
    private final SysTransactionFlowService transactionFlowService;

    @Override
    public SysWallet getByUserId(Long userId) {
        return getById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createWallet(Long userId) {
        SysWallet wallet = new SysWallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setFrozenAmount(BigDecimal.ZERO);
        save(wallet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freeze(Long userId, BigDecimal amount) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            SysWallet wallet = getByUserId(userId);
            if (wallet == null) {
                createWallet(userId);
                wallet = getByUserId(userId);
            }
            wallet.setFrozenAmount(wallet.getFrozenAmount().add(amount));
            if (updateById(wallet)) {
                return true;
            }
            log.warn("freeze 乐观锁冲突，重试 {}/{}: userId={}", attempt + 1, MAX_RETRY, userId);
        }
        throw new BusinessException("更新钱包失败，请稍后重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeFromBalance(Long userId, BigDecimal amount) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            SysWallet wallet = getByUserId(userId);
            if (wallet == null) {
                throw new BusinessException("钱包不存在");
            }
            if (wallet.getBalance().compareTo(amount) < 0) {
                return false; // 余额不足
            }
            wallet.setBalance(wallet.getBalance().subtract(amount));
            wallet.setFrozenAmount(wallet.getFrozenAmount().add(amount));
            if (updateById(wallet)) {
                return true;
            }
            log.warn("freezeFromBalance 乐观锁冲突，重试 {}/{}: userId={}", attempt + 1, MAX_RETRY, userId);
        }
        throw new BusinessException("更新钱包失败，请稍后重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreeze(Long userId, BigDecimal amount) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            SysWallet wallet = getByUserId(userId);
            if (wallet == null) {
                createWallet(userId);
                wallet = getByUserId(userId);
            }

            BigDecimal actualUnfreezeAmount = amount;
            if (wallet.getFrozenAmount().compareTo(amount) < 0) {
                log.warn("冻结金额不足，将解冻全部冻结金额: userId={}, frozenAmount={}, requestedAmount={}",
                        userId, wallet.getFrozenAmount(), amount);
                actualUnfreezeAmount = wallet.getFrozenAmount();
            }

            wallet.setFrozenAmount(wallet.getFrozenAmount().subtract(actualUnfreezeAmount));
            wallet.setBalance(wallet.getBalance().add(actualUnfreezeAmount));
            if (updateById(wallet)) {
                return true;
            }
            log.warn("unfreeze 乐观锁冲突，重试 {}/{}: userId={}", attempt + 1, MAX_RETRY, userId);
        }
        throw new BusinessException("更新钱包失败，请稍后重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deduct(Long userId, BigDecimal amount) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            SysWallet wallet = getByUserId(userId);
            if (wallet == null) {
                createWallet(userId);
                wallet = getByUserId(userId);
            }
            if (wallet.getBalance().compareTo(amount) < 0) {
                return false; // 余额不足
            }
            wallet.setBalance(wallet.getBalance().subtract(amount));
            if (updateById(wallet)) {
                return true;
            }
            log.warn("deduct 乐观锁冲突，重试 {}/{}: userId={}", attempt + 1, MAX_RETRY, userId);
        }
        throw new BusinessException("更新钱包失败，请稍后重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recharge(Long userId, BigDecimal amount) {
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            SysWallet wallet = getByUserId(userId);
            if (wallet == null) {
                createWallet(userId);
                wallet = getByUserId(userId);
            }
            wallet.setBalance(wallet.getBalance().add(amount));
            if (updateById(wallet)) {
                return true;
            }
            log.warn("recharge 乐观锁冲突，重试 {}/{}: userId={}", attempt + 1, MAX_RETRY, userId);
        }
        throw new BusinessException("更新钱包失败，请稍后重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recharge(Long userId, BigDecimal amount, String paymentMethod) {
        // 执行充值
        boolean success = recharge(userId, amount);
        if (!success) {
            throw new BusinessException("充值失败");
        }
        // 记录交易流水
        SysWallet wallet = getByUserId(userId);
        BigDecimal balanceAfter = wallet != null ? wallet.getBalance() : amount;
        transactionFlowService.recordFlow(userId, amount, balanceAfter, 1, null,
                "充值 (" + paymentMethod + ")");
        return 0L;
    }
}
