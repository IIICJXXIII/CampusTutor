package com.campus.module.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.wallet.entity.SysWallet;

import java.math.BigDecimal;

/**
 * 用户钱包 Service 接口
 */
public interface SysWalletService extends IService<SysWallet> {

    /**
     * 获取用户钱包
     */
    SysWallet getByUserId(Long userId);

    /**
     * 创建用户钱包
     */
    void createWallet(Long userId);

    /**
     * 冻结金额 (用于订单支付托管，直接增加冻结金额)
     */
    boolean freeze(Long userId, BigDecimal amount);
    
    /**
     * 从余额中冻结金额 (用于其他冻结场景)
     */
    boolean freezeFromBalance(Long userId, BigDecimal amount);

    /**
     * 解冻金额
     */
    boolean unfreeze(Long userId, BigDecimal amount);

    /**
     * 扣减余额
     */
    boolean deduct(Long userId, BigDecimal amount);

    /**
     * 充值/增加余额
     */
    boolean recharge(Long userId, BigDecimal amount);

    /**
     * 充值/增加余额(带支付方式，返回流水ID)
     */
    Long recharge(Long userId, BigDecimal amount, String paymentMethod);
}

