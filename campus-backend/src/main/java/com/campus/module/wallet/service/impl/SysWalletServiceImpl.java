package com.campus.module.wallet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.ResultCode;
import com.campus.module.wallet.entity.SysWallet;
import com.campus.module.wallet.mapper.SysWalletMapper;
import com.campus.module.wallet.service.SysWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 用户钱包 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class SysWalletServiceImpl extends ServiceImpl<SysWalletMapper, SysWallet> implements SysWalletService {

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
        SysWallet wallet = getByUserId(userId);
        if (wallet == null) {
            throw new BusinessException("钱包不存在");
        }
        if (wallet.getBalance().compareTo(amount) < 0) {
            return false; // 余额不足
        }
        // 减少余额，增加冻结金额
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setFrozenAmount(wallet.getFrozenAmount().add(amount));
        return updateById(wallet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreeze(Long userId, BigDecimal amount) {
        SysWallet wallet = getByUserId(userId);
        if (wallet == null) {
            throw new BusinessException("钱包不存在");
        }
        if (wallet.getFrozenAmount().compareTo(amount) < 0) {
            throw new BusinessException("冻结金额不足");
        }
        // 减少冻结金额，增加回余额
        wallet.setFrozenAmount(wallet.getFrozenAmount().subtract(amount));
        wallet.setBalance(wallet.getBalance().add(amount));
        return updateById(wallet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deduct(Long userId, BigDecimal amount) {
        SysWallet wallet = getByUserId(userId);
        if (wallet == null) {
            throw new BusinessException("钱包不存在");
        }
        if (wallet.getBalance().compareTo(amount) < 0) {
            return false; // 余额不足
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        return updateById(wallet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recharge(Long userId, BigDecimal amount) {
        SysWallet wallet = getByUserId(userId);
        if (wallet == null) {
            // 自动创建钱包
            createWallet(userId);
            wallet = getByUserId(userId);
        }
        wallet.setBalance(wallet.getBalance().add(amount));
        return updateById(wallet);
    }
}
