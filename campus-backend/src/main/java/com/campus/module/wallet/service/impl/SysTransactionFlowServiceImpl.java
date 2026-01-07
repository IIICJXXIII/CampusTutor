package com.campus.module.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.module.wallet.entity.SysTransactionFlow;
import com.campus.module.wallet.mapper.SysTransactionFlowMapper;
import com.campus.module.wallet.service.SysTransactionFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金流水 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class SysTransactionFlowServiceImpl extends ServiceImpl<SysTransactionFlowMapper, SysTransactionFlow> implements SysTransactionFlowService {

    @Override
    public IPage<SysTransactionFlow> pageByUserId(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<SysTransactionFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTransactionFlow::getUserId, userId)
               .orderByDesc(SysTransactionFlow::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public void recordFlow(Long userId, BigDecimal amount, BigDecimal balanceAfter, Integer flowType, Long orderId, String remark) {
        SysTransactionFlow flow = new SysTransactionFlow();
        flow.setUserId(userId);
        flow.setAmount(amount);
        flow.setBalanceAfter(balanceAfter);
        flow.setFlowType(flowType);
        flow.setOrderId(orderId);
        flow.setRemark(remark);
        flow.setCreateTime(LocalDateTime.now());
        save(flow);
    }
}
