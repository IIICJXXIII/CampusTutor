package com.campus.module.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现申请实体类
 */
@Data
@TableName("sys_withdrawal")
public class SysWithdrawal implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 提现金额 */
    private BigDecimal amount;

    /** 渠道: 1-微信, 2-支付宝, 3-银行卡 */
    private Integer channel;

    /** 收款账号 */
    private String accountNo;

    /** 状态: 0-审核中, 1-已打款, 2-驳回 */
    private Integer status;

    /** 审核备注 */
    private String auditRemark;

    /** 创建时间 */
    private LocalDateTime createTime;

    // 渠道常量
    public static final int CHANNEL_WECHAT = 1;     // 微信
    public static final int CHANNEL_ALIPAY = 2;     // 支付宝
    public static final int CHANNEL_BANK = 3;       // 银行卡

    // 状态常量
    public static final int STATUS_PENDING = 0;     // 审核中
    public static final int STATUS_APPROVED = 1;    // 已打款
    public static final int STATUS_REJECTED = 2;    // 驳回
}
