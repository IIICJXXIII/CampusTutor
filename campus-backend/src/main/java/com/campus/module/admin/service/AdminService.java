package com.campus.module.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.admin.dto.*;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.teaching.entity.TeachingRecord;
import com.campus.module.user.entity.SysUser;
import com.campus.module.wallet.entity.SysWallet;

/**
 * 管理后台服务接口
 */
public interface AdminService {

    /**
     * 获取仪表盘统计数据
     */
    DashboardVO getDashboardStats();

    // ==================== 用户管理 ====================

    /**
     * 分页查询用户列表
     */
    PageVO<UserVO> getUserList(Integer page, Integer size, String keyword, Integer role, Integer status);

    /**
     * 获取用户详情
     */
    UserVO getUserById(Long id);

    /**
     * 更新用户信息
     */
    void updateUser(Long id, SysUser user);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    // ==================== 教师管理 ====================

    /**
     * 分页查询教师列表
     */
    PageVO<TutorVO> getTutorList(Integer page, Integer size, String keyword, Integer certStatus, Integer education);

    /**
     * 获取教师详情
     */
    TutorVO getTutorById(Long id);

    /**
     * 获取待审核认证列表
     */
    PageVO<TutorVO> getPendingTutorList(Integer page, Integer size);

    /**
     * 通过认证
     */
    void approveTutor(Long id);

    /**
     * 拒绝认证
     */
    void rejectTutor(Long id, String reason);

    // ==================== 家长管理 ====================

    /**
     * 分页查询家长列表
     */
    PageVO<UserVO> getParentList(Integer page, Integer size, String keyword);

    /**
     * 获取家长详情
     */
    UserVO getParentById(Long id);

    // ==================== 需求管理 ====================

    /**
     * 分页查询需求列表
     */
    Page<DemandPost> getDemandList(Integer page, Integer size, String keyword, Integer status);

    /**
     * 更新需求状态
     */
    void updateDemandStatus(Long id, Integer status);

    /**
     * 删除需求
     */
    void deleteDemand(Long id);

    // ==================== 订单管理 ====================

    /**
     * 分页查询订单列表
     */
    PageVO<OrderVO> getOrderList(Integer page, Integer size, String keyword, Integer status);

    /**
     * 获取订单详情
     */
    OrderVO getOrderById(Long id);

    /**
     * 更新订单状态
     */
    void updateOrderStatus(Long id, Integer status);

    /**
     * 释放托管资金
     */
    void releaseEscrow(Long id);

    /**
     * 退款
     */
    void refundOrder(Long id, String reason);

    // ==================== 课时管理 ====================

    /**
     * 分页查询课时列表
     */
    Page<TeachingRecord> getLessonList(Integer page, Integer size, Long orderId, Integer status);

    /**
     * 管理员确认课时
     */
    void confirmLesson(Long id);

    /**
     * 管理员拒绝课时
     */
    void rejectLesson(Long id, String reason);

    // ==================== 钱包管理 ====================

    /**
     * 分页查询钱包列表
     */
    Page<SysWallet> getWalletList(Integer page, Integer size, String keyword);

    /**
     * 获取钱包详情
     */
    SysWallet getWalletById(Long id);

    /**
     * 调整余额
     */
    void adjustBalance(Long id, java.math.BigDecimal amount, String reason);
}
