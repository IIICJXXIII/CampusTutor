package com.campus.module.wallet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.wallet.dto.WithdrawRequest;
import com.campus.module.wallet.entity.SysWithdrawal;

/**
 * 提现申请 Service 接口
 */
public interface SysWithdrawalService extends IService<SysWithdrawal> {

    /**
     * 发起提现申请
     * @param userId 用户ID
     * @param request 提现请求
     * @return 提现申请ID
     */
    Long applyWithdraw(Long userId, WithdrawRequest request);

    /**
     * 分页获取用户提现记录
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页提现记录
     */
    IPage<SysWithdrawal> pageByUserId(Long userId, Integer page, Integer size);

    /**
     * 审核提现申请
     * @param id 提现申请ID
     * @param approved 是否通过
     * @param auditRemark 审核备注
     */
    void audit(Long id, boolean approved, String auditRemark);
}
