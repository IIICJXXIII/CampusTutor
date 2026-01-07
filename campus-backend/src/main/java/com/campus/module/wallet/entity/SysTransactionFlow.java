package com.campus.module.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金流水记录实体类
 */
@Data
@TableName("sys_transaction_flow")
public class SysTransactionFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 变动金额 (正数收入, 负数支出) */
    private BigDecimal amount;

    /** 变动后余额 (快照) */
    private BigDecimal balanceAfter;

    /** 类型: 1-充值, 2-支付订单, 3-课时费解冻收入, 4-提现, 5-退款 */
    private Integer flowType;

    /** 关联订单ID (可为空) */
    private Long orderId;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    // 流水类型常量
    public static final int TYPE_RECHARGE = 1;      // 充值
    public static final int TYPE_PAY_ORDER = 2;     // 支付订单
    public static final int TYPE_LESSON_INCOME = 3; // 课时费解冻收入
    public static final int TYPE_WITHDRAW = 4;      // 提现
    public static final int TYPE_REFUND = 5;        // 退款
}
