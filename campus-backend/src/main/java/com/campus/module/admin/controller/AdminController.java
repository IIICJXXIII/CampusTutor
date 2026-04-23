package com.campus.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.result.Result;
import com.campus.module.admin.dto.*;
import com.campus.module.admin.service.AdminService;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.match.service.DeepFMInferenceService;
import com.campus.module.recommend.service.CollaborativeFilterService;
import com.campus.module.teaching.entity.TeachingRecord;
import com.campus.module.user.entity.SysUser;
import com.campus.module.wallet.entity.SysWallet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台 API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "管理后台", description = "管理后台各模块管理接口")
public class AdminController {

    private final AdminService adminService;
    private final DeepFMInferenceService deepFMInferenceService;
    private final CollaborativeFilterService collaborativeFilterService;

    // ==================== 仪表盘 ====================

    @GetMapping("/stats/dashboard")
    @Operation(summary = "获取仪表盘统计", description = "获取首页统计数据")
    public Result<DashboardVO> getDashboardStats() {
        DashboardVO stats = adminService.getDashboardStats();
        return Result.success(stats);
    }

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    @Operation(summary = "用户列表", description = "分页查询用户列表")
    public Result<PageVO<UserVO>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminService.getUserList(page, size, keyword, role, status));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "用户详情")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        return Result.success(adminService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "更新用户")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        adminService.updateUser(id, user);
        return Result.success();
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "更新用户状态")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateUserStatus(id, body.get("status"));
        return Result.success();
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.success();
    }

    // ==================== 教师管理 ====================

    @GetMapping("/tutors")
    @Operation(summary = "教师列表", description = "分页查询教师列表")
    public Result<PageVO<TutorVO>> getTutorList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer certStatus,
            @RequestParam(required = false) Integer education) {
        return Result.success(adminService.getTutorList(page, size, keyword, certStatus, education));
    }

    @GetMapping("/tutors/{id}")
    @Operation(summary = "教师详情")
    public Result<TutorVO> getTutorById(@PathVariable Long id) {
        return Result.success(adminService.getTutorById(id));
    }

    @GetMapping("/tutors/pending")
    @Operation(summary = "待审核列表", description = "获取待审核认证列表")
    public Result<PageVO<TutorVO>> getPendingTutorList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminService.getPendingTutorList(page, size));
    }

    @PostMapping("/tutors/{id}/approve")
    @Operation(summary = "通过认证")
    public Result<Void> approveTutor(@PathVariable Long id) {
        adminService.approveTutor(id);
        return Result.success();
    }

    @PostMapping("/tutors/{id}/reject")
    @Operation(summary = "拒绝认证")
    public Result<Void> rejectTutor(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.rejectTutor(id, body.get("reason"));
        return Result.success();
    }

    // ==================== 家长管理 ====================

    @GetMapping("/parents")
    @Operation(summary = "家长列表")
    public Result<PageVO<UserVO>> getParentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminService.getParentList(page, size, keyword));
    }

    @GetMapping("/parents/{id}")
    @Operation(summary = "家长详情")
    public Result<UserVO> getParentById(@PathVariable Long id) {
        return Result.success(adminService.getParentById(id));
    }

    // ==================== 需求管理 ====================

    @GetMapping("/demands")
    @Operation(summary = "需求列表")
    public Result<Page<DemandPost>> getDemandList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminService.getDemandList(page, size, keyword, status));
    }

    @PutMapping("/demands/{id}/status")
    @Operation(summary = "更新需求状态")
    public Result<Void> updateDemandStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateDemandStatus(id, body.get("status"));
        return Result.success();
    }

    @DeleteMapping("/demands/{id}")
    @Operation(summary = "删除需求")
    public Result<Void> deleteDemand(@PathVariable Long id) {
        adminService.deleteDemand(id);
        return Result.success();
    }

    // ==================== 订单管理 ====================

    @GetMapping("/orders")
    @Operation(summary = "订单列表")
    public Result<PageVO<OrderVO>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminService.getOrderList(page, size, keyword, status));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "订单详情")
    public Result<OrderVO> getOrderById(@PathVariable Long id) {
        return Result.success(adminService.getOrderById(id));
    }

    @PutMapping("/orders/{id}/status")
    @Operation(summary = "更新订单状态")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateOrderStatus(id, body.get("status"));
        return Result.success();
    }

    @PostMapping("/orders/{id}/release")
    @Operation(summary = "释放托管资金")
    public Result<Void> releaseEscrow(@PathVariable Long id) {
        adminService.releaseEscrow(id);
        return Result.success();
    }

    @PostMapping("/orders/{id}/refund")
    @Operation(summary = "订单退款")
    public Result<Void> refundOrder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.refundOrder(id, body.get("reason"));
        return Result.success();
    }

    // ==================== 课时管理 ====================

    @GetMapping("/lessons")
    @Operation(summary = "课时列表")
    public Result<Page<TeachingRecord>> getLessonList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminService.getLessonList(page, size, orderId, status));
    }

    @PostMapping("/lessons/{id}/confirm")
    @Operation(summary = "确认课时")
    public Result<Void> confirmLesson(@PathVariable Long id) {
        adminService.confirmLesson(id);
        return Result.success();
    }

    @PostMapping("/lessons/{id}/reject")
    @Operation(summary = "拒绝课时")
    public Result<Void> rejectLesson(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.rejectLesson(id, body.get("reason"));
        return Result.success();
    }

    // ==================== 钱包管理 ====================

    @GetMapping("/wallets")
    @Operation(summary = "钱包列表")
    public Result<Page<SysWallet>> getWalletList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminService.getWalletList(page, size, keyword));
    }

    @GetMapping("/wallets/{id}")
    @Operation(summary = "钱包详情")
    public Result<SysWallet> getWalletById(@PathVariable Long id) {
        return Result.success(adminService.getWalletById(id));
    }

    @PostMapping("/wallets/{id}/adjust")
    @Operation(summary = "调整余额")
    public Result<Void> adjustBalance(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String reason = (String) body.get("reason");
        adminService.adjustBalance(id, amount, reason);
        return Result.success();
    }

    // ==================== 系统设置 ====================

    @GetMapping("/settings")
    @Operation(summary = "获取系统设置")
    public Result<Map<String, Object>> getSettings() {
        return Result.success(Map.of(
                "siteName", "家教平台管理后台",
                "serviceFeeRate", 0.1,
                "minWithdrawAmount", 100,
                "maxWithdrawPerDay", 5000));
    }

    @PutMapping("/settings")
    @Operation(summary = "更新系统设置")
    public Result<Void> updateSettings(@RequestBody Map<String, Object> settings) {
        log.info("更新系统设置: {}", settings);
        return Result.success();
    }

    // ==================== 推荐算法监控 ====================

    @GetMapping("/deepfm/status")
    @Operation(summary = "DeepFM模型状态检查(管理员)")
    public Result<Map<String, Object>> deepfmStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("modelReady", deepFMInferenceService.isModelReady());
        status.put("modelPath", deepFMInferenceService.getModelPath());
        status.put("featureDim", deepFMInferenceService.getFeatureDim());
        return Result.success(status);
    }

    @DeleteMapping("/recommend/cache/{tutorId}")
    @Operation(summary = "清除推荐缓存(管理员)")
    public Result<Void> clearRecommendCache(@PathVariable(required = false) Long tutorId) {
        collaborativeFilterService.clearSimilarityCache(tutorId);
        return Result.success();
    }

    @DeleteMapping("/recommend/cache")
    @Operation(summary = "清除所有推荐缓存(管理员)")
    public Result<Void> clearAllRecommendCache() {
        collaborativeFilterService.clearSimilarityCache(null);
        return Result.success();
    }
}
