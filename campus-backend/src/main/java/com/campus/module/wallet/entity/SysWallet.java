package com.campus.module.wallet.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户钱包实体类
 */
@TableName("sys_wallet")
public class SysWallet implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关联用户ID */
    @TableId
    private Long userId;

    /** 可用余额 */
    private BigDecimal balance;

    /** 冻结金额(担保交易中) */
    private BigDecimal frozenAmount;

    /** 支付密码(加密) */
    private String payPassword;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // 显式的getter和setter方法
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(BigDecimal frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
