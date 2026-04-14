package com.campus.module.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户钱包实体类
 */
@Data
@TableName("sys_wallet")
public class SysWallet implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关联用户ID */
    @TableId(type = IdType.INPUT)
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
}
