package com.campus.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.exception.BusinessException;
import com.campus.module.admin.dto.*;
import com.campus.module.admin.service.AdminService;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.demand.mapper.DemandPostMapper;
import com.campus.module.order.entity.CourseOrder;
import com.campus.module.order.mapper.CourseOrderMapper;
import com.campus.module.teaching.entity.TeachingRecord;
import com.campus.module.teaching.mapper.TeachingRecordMapper;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.mapper.SysUserMapper;
import com.campus.module.wallet.entity.SysWallet;
import com.campus.module.wallet.entity.SysTransactionFlow;
import com.campus.module.wallet.mapper.SysWalletMapper;
import com.campus.module.wallet.mapper.SysTransactionFlowMapper;
import com.campus.module.wallet.service.SysWalletService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SysUserMapper sysUserMapper;
    private final TutorProfileMapper tutorProfileMapper;
    private final DemandPostMapper demandPostMapper;
    private final CourseOrderMapper courseOrderMapper;
    private final TeachingRecordMapper teachingRecordMapper;
    private final SysWalletMapper sysWalletMapper;
    private final SysTransactionFlowMapper transactionFlowMapper;
    private final SysWalletService walletService;
    private final ObjectMapper objectMapper;

    @Override
    public DashboardVO getDashboardStats() {
        DashboardVO vo = new DashboardVO();

        // 用户统计
        Long totalUsers = sysUserMapper.selectCount(null);
        Long tutorCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, 1));
        Long parentCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, 2));

        vo.setTotalUsers(totalUsers.intValue());
        vo.setTotalTutors(tutorCount.intValue());
        vo.setTotalParents(parentCount.intValue());
        vo.setTutorCount(tutorCount.intValue());
        vo.setParentCount(parentCount.intValue());

        // 订单统计
        Long totalOrders = courseOrderMapper.selectCount(null);
        vo.setTotalOrders(totalOrders.intValue());

        // 总交易额 (已支付订单)
        List<CourseOrder> paidOrders = courseOrderMapper.selectList(
                new LambdaQueryWrapper<CourseOrder>().ge(CourseOrder::getStatus, 1));
        BigDecimal totalRevenue = paidOrders.stream()
                .map(CourseOrder::getTotalAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalRevenue(totalRevenue);

        // 待办事项统计
        Long pendingAudit = tutorProfileMapper.selectCount(
                new LambdaQueryWrapper<TutorProfile>().eq(TutorProfile::getCertStatus, 1));
        vo.setPendingAudit(pendingAudit.intValue());

        Long pendingOrders = courseOrderMapper.selectCount(
                new LambdaQueryWrapper<CourseOrder>().eq(CourseOrder::getStatus, 0));
        vo.setPendingOrders(pendingOrders.intValue());

        Long pendingLessons = teachingRecordMapper.selectCount(
                new LambdaQueryWrapper<TeachingRecord>().eq(TeachingRecord::getStatus, 0));
        vo.setPendingLessons(pendingLessons.intValue());

        Long pendingRefunds = courseOrderMapper.selectCount(
                new LambdaQueryWrapper<CourseOrder>().eq(CourseOrder::getStatus, 5));
        vo.setPendingRefunds(pendingRefunds.intValue());

        // 最新用户
        List<SysUser> recentUserList = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .orderByDesc(SysUser::getCreateTime)
                        .last("LIMIT 5"));
        vo.setRecentUsers(recentUserList.stream().map(this::convertToUserVO).collect(Collectors.toList()));

        // 最新订单
        List<CourseOrder> recentOrderList = courseOrderMapper.selectList(
                new LambdaQueryWrapper<CourseOrder>()
                        .orderByDesc(CourseOrder::getCreateTime)
                        .last("LIMIT 5"));
        vo.setRecentOrders(recentOrderList.stream().map(this::convertToOrderVO).collect(Collectors.toList()));

        return vo;
    }

    // ==================== 用户管理 ====================

    @Override
    public PageVO<UserVO> getUserList(Integer page, Integer size, String keyword, Integer role, Integer status) {
        Page<SysUser> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword));
        }
        if (role != null) {
            wrapper.eq(SysUser::getRole, role);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> result = sysUserMapper.selectPage(pageParam, wrapper);

        List<UserVO> records = result.getRecords().stream()
                .map(this::convertToUserVO)
                .collect(Collectors.toList());

        return PageVO.of(records, result.getTotal(), page, size);
    }

    @Override
    public UserVO getUserById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, SysUser user) {
        SysUser existing = sysUserMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        user.setId(id);
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, Integer status) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        sysUserMapper.deleteById(id);
    }

    // ==================== 教师管理 ====================

    @Override
    public PageVO<TutorVO> getTutorList(Integer page, Integer size, String keyword, Integer certStatus,
            Integer education) {
        Page<TutorProfile> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TutorProfile> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(TutorProfile::getRealName, keyword)
                    .or().like(TutorProfile::getUniversityName, keyword)
                    .or().like(TutorProfile::getMajor, keyword));
        }
        if (certStatus != null) {
            wrapper.eq(TutorProfile::getCertStatus, certStatus);
        }
        if (education != null) {
            wrapper.eq(TutorProfile::getEducation, education);
        }
        wrapper.orderByDesc(TutorProfile::getCreateTime);

        Page<TutorProfile> result = tutorProfileMapper.selectPage(pageParam, wrapper);

        List<TutorVO> records = result.getRecords().stream()
                .map(this::convertToTutorVO)
                .collect(Collectors.toList());

        return PageVO.of(records, result.getTotal(), page, size);
    }

    @Override
    public TutorVO getTutorById(Long id) {
        TutorProfile tutor = tutorProfileMapper.selectById(id);
        if (tutor == null) {
            throw new BusinessException("教师不存在");
        }
        return convertToTutorVO(tutor);
    }

    @Override
    public PageVO<TutorVO> getPendingTutorList(Integer page, Integer size) {
        Page<TutorProfile> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TutorProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TutorProfile::getCertStatus, 1);
        wrapper.orderByAsc(TutorProfile::getCreateTime);

        Page<TutorProfile> result = tutorProfileMapper.selectPage(pageParam, wrapper);

        List<TutorVO> records = result.getRecords().stream()
                .map(this::convertToTutorVO)
                .collect(Collectors.toList());

        return PageVO.of(records, result.getTotal(), page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTutor(Long id) {
        TutorProfile tutor = tutorProfileMapper.selectById(id);
        if (tutor == null) {
            throw new BusinessException("教师不存在");
        }
        if (tutor.getCertStatus() != 1) {
            throw new BusinessException("当前状态不支持审核操作");
        }
        tutor.setCertStatus(2);
        tutor.setRejectReason(null);
        tutorProfileMapper.updateById(tutor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectTutor(Long id, String reason) {
        TutorProfile tutor = tutorProfileMapper.selectById(id);
        if (tutor == null) {
            throw new BusinessException("教师不存在");
        }
        if (tutor.getCertStatus() != 1) {
            throw new BusinessException("当前状态不支持审核操作");
        }
        tutor.setCertStatus(3);
        tutor.setRejectReason(reason);
        tutorProfileMapper.updateById(tutor);
    }

    // ==================== 家长管理 ====================

    @Override
    public PageVO<UserVO> getParentList(Integer page, Integer size, String keyword) {
        return getUserList(page, size, keyword, 2, null);
    }

    @Override
    public UserVO getParentById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || user.getRole() != 2) {
            throw new BusinessException("家长不存在");
        }
        return convertToUserVO(user);
    }

    // ==================== 需求管理 ====================

    @Override
    public Page<DemandPost> getDemandList(Integer page, Integer size, String keyword, Integer status) {
        Page<DemandPost> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DemandPost> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(DemandPost::getSubject, keyword)
                    .or().like(DemandPost::getTitle, keyword));
        }
        if (status != null) {
            wrapper.eq(DemandPost::getStatus, status);
        }
        wrapper.orderByDesc(DemandPost::getCreateTime);

        return demandPostMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDemandStatus(Long id, Integer status) {
        DemandPost demand = demandPostMapper.selectById(id);
        if (demand == null) {
            throw new BusinessException("需求不存在");
        }
        demand.setStatus(status);
        demandPostMapper.updateById(demand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDemand(Long id) {
        demandPostMapper.deleteById(id);
    }

    // ==================== 订单管理 ====================

    @Override
    public PageVO<OrderVO> getOrderList(Integer page, Integer size, String keyword, Integer status) {
        Page<CourseOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CourseOrder> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(CourseOrder::getOrderNo, keyword);
        }
        if (status != null) {
            wrapper.eq(CourseOrder::getStatus, status);
        }
        wrapper.orderByDesc(CourseOrder::getCreateTime);

        Page<CourseOrder> result = courseOrderMapper.selectPage(pageParam, wrapper);

        List<OrderVO> records = result.getRecords().stream()
                .map(this::convertToOrderVO)
                .collect(Collectors.toList());

        return PageVO.of(records, result.getTotal(), page, size);
    }

    @Override
    public OrderVO getOrderById(Long id) {
        CourseOrder order = courseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return convertToOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long id, Integer status) {
        CourseOrder order = courseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(status);
        courseOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseEscrow(Long id) {
        CourseOrder order = courseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 实际资金释放：解冻教员收益
        boolean success = walletService.unfreeze(order.getTutorId(), order.getTutorAmount());
        if (!success) {
            throw new BusinessException("解冻教员收益失败");
        }

        order.setStatus(3); // 已完成
        courseOrderMapper.updateById(order);
        log.info("订单 {} 托管资金已释放，教员 {} 收到 {}", id, order.getTutorId(), order.getTutorAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long id, String reason) {
        CourseOrder order = courseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (order.getStatus() != 5) {
            throw new BusinessException("仅退款中状态的订单可处理退款");
        }

        // 从 cancelReason 中解析退款金额
        java.math.BigDecimal refundAmount = order.getTotalAmount(); // 默认全额
        String cancelReason = order.getCancelReason();
        if (cancelReason != null && cancelReason.contains("申请退款金额: ")) {
            try {
                String amountStr = cancelReason.substring(cancelReason.lastIndexOf("申请退款金额: ") + 8).trim();
                refundAmount = new java.math.BigDecimal(amountStr);
            } catch (Exception e) {
                log.warn("解析退款金额失败，使用订单总金额: {}", e.getMessage());
            }
        }

        // 计算退款比例和教员应解冻金额
        java.math.BigDecimal refundRatio = refundAmount.divide(order.getTotalAmount(), 4, java.math.RoundingMode.HALF_UP);
        java.math.BigDecimal refundToTutor = order.getTutorAmount().multiply(refundRatio)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // 解冻教员冻结金额
        boolean unfreezeSuccess = walletService.unfreeze(order.getTutorId(), refundToTutor);
        if (!unfreezeSuccess) {
            log.error("管理员退款解冻教员冻结金额失败: orderId={}, tutorId={}, amount={}",
                    id, order.getTutorId(), refundToTutor);
            throw new BusinessException("退款处理失败：解冻教员金额失败");
        }

        // 退还家长金额
        walletService.recharge(order.getParentId(), refundAmount);

        // 更新订单状态
        order.setStatus(6); // 已退款
        order.setCancelReason(reason != null ? reason : cancelReason);
        courseOrderMapper.updateById(order);

        log.info("管理员处理退款成功: orderId={}, refundAmount={}, refundToTutor={}, reason={}",
                id, refundAmount, refundToTutor, reason);
    }

    // ==================== 课时管理 ====================

    @Override
    public Page<TeachingRecord> getLessonList(Integer page, Integer size, Long orderId, Integer status) {
        Page<TeachingRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TeachingRecord> wrapper = new LambdaQueryWrapper<>();

        if (orderId != null) {
            wrapper.eq(TeachingRecord::getOrderId, orderId);
        }
        if (status != null) {
            wrapper.eq(TeachingRecord::getStatus, status);
        }
        wrapper.orderByDesc(TeachingRecord::getCreateTime);

        return teachingRecordMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmLesson(Long id) {
        TeachingRecord record = teachingRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("课时记录不存在");
        }
        record.setStatus(1); // 已确认
        teachingRecordMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectLesson(Long id, String reason) {
        TeachingRecord record = teachingRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("课时记录不存在");
        }
        record.setStatus(2); // 异常/申诉
        teachingRecordMapper.updateById(record);
    }

    // ==================== 钱包管理 ====================

    @Override
    public Page<SysWallet> getWalletList(Integer page, Integer size, String keyword) {
        Page<SysWallet> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysWallet> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysWallet::getUpdateTime);
        return sysWalletMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public SysWallet getWalletById(Long id) {
        SysWallet wallet = sysWalletMapper.selectById(id);
        if (wallet == null) {
            throw new BusinessException("钱包不存在");
        }
        return wallet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustBalance(Long id, BigDecimal amount, String reason) {
        SysWallet wallet = sysWalletMapper.selectById(id);
        if (wallet == null) {
            throw new BusinessException("钱包不存在");
        }

        BigDecimal newBalance = wallet.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("调整后余额不能为负");
        }

        wallet.setBalance(newBalance);
        sysWalletMapper.updateById(wallet);

        // 记录流水
        SysTransactionFlow flow = new SysTransactionFlow();
        flow.setUserId(id);
        flow.setAmount(amount);
        flow.setFlowType(amount.compareTo(BigDecimal.ZERO) > 0 ? SysTransactionFlow.TYPE_RECHARGE
                : SysTransactionFlow.TYPE_WITHDRAW);
        flow.setBalanceAfter(newBalance);
        flow.setRemark("管理员调整: " + reason);
        flow.setCreateTime(LocalDateTime.now());
        transactionFlowMapper.insert(flow);

        log.info("用户 {} 余额已调整 {}, 原因: {}", id, amount, reason);
    }

    // ==================== 转换方法 ====================

    private UserVO convertToUserVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setPhone(user.getUsername()); // username is phone
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    private OrderVO convertToOrderVO(CourseOrder order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setSubject(order.getSubject());
        vo.setAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setParentId(order.getParentId());
        vo.setTutorId(order.getTutorId());
        vo.setCreateTime(order.getCreateTime());

        // 获取用户昵称
        if (order.getParentId() != null) {
            SysUser parent = sysUserMapper.selectById(order.getParentId());
            if (parent != null) {
                vo.setParentName(parent.getNickname());
            }
        }
        if (order.getTutorId() != null) {
            SysUser tutor = sysUserMapper.selectById(order.getTutorId());
            if (tutor != null) {
                vo.setTutorName(tutor.getNickname());
            }
        }

        return vo;
    }

    private TutorVO convertToTutorVO(TutorProfile tutor) {
        TutorVO vo = new TutorVO();
        vo.setId(tutor.getId());
        vo.setUserId(tutor.getUserId());
        vo.setRealName(tutor.getRealName());
        vo.setUniversityName(tutor.getUniversityName());
        vo.setMajor(tutor.getMajor());
        vo.setEducation(tutor.getEducation());
        vo.setEnrollYear(tutor.getEnrollYear());
        vo.setCertStatus(tutor.getCertStatus());
        vo.setRating(tutor.getRating());
        vo.setOrderCount(tutor.getOrderCount());
        vo.setExpectPrice(tutor.getExpectPrice());
        vo.setCanVisit(tutor.getCanVisit() != null && tutor.getCanVisit() == 1);
        vo.setCanOnline(tutor.getCanOnline() != null && tutor.getCanOnline() == 1);
        vo.setIntroduction(tutor.getIntroduction());
        vo.setIdCardFrontUrl(tutor.getIdCardFrontUrl());
        vo.setIdCardBackUrl(tutor.getIdCardBackUrl());
        vo.setStudentCardUrl(tutor.getStudentCardUrl());
        vo.setCreateTime(tutor.getCreateTime());

        // 脱敏身份证
        if (tutor.getIdCard() != null && tutor.getIdCard().length() > 10) {
            vo.setIdCard(tutor.getIdCard().substring(0, 6) + "********"
                    + tutor.getIdCard().substring(tutor.getIdCard().length() - 4));
        }

        // 解析 JSON 数组
        try {
            if (StringUtils.hasText(tutor.getTeachSubjects())) {
                vo.setTeachSubjects(objectMapper.readValue(tutor.getTeachSubjects(), new TypeReference<List<String>>() {
                }));
            } else {
                vo.setTeachSubjects(Collections.emptyList());
            }
            if (StringUtils.hasText(tutor.getTeachGrades())) {
                vo.setTeachGrades(objectMapper.readValue(tutor.getTeachGrades(), new TypeReference<List<String>>() {
                }));
            } else {
                vo.setTeachGrades(Collections.emptyList());
            }
        } catch (Exception e) {
            log.error("解析教师科目/年级 JSON 失败", e);
            vo.setTeachSubjects(Collections.emptyList());
            vo.setTeachGrades(Collections.emptyList());
        }

        return vo;
    }
}
