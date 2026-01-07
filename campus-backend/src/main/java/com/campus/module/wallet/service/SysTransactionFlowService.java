package com.campus.module.wallet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.wallet.entity.SysTransactionFlow;

import java.math.BigDecimal;

/**
 * 资金流水 Service 接口
 */
public interface SysTransactionFlowService extends IService<SysTransactionFlow> {

    /**
     * 分页获取用户交易流水
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页流水记录
     */
    IPage<SysTransactionFlow> pageByUserId(Long userId, Integer page, Integer size);

    /**
     * 记录交易流水
     * @param userId 用户ID
     * @param amount 变动金额(正数收入, 负数支出)
     * @param balanceAfter 变动后余额
     * @param flowType 流水类型
     * @param orderId 关联订单ID(可为空)
     * @param remark 备注
     */
    void recordFlow(Long userId, BigDecimal amount, BigDecimal balanceAfter, Integer flowType, Long orderId, String remark);
}
