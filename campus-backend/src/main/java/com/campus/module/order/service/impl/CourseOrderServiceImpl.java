package com.campus.module.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.ResultCode;
import com.campus.module.order.dto.AcceptDemandRequest;
import com.campus.module.order.dto.CreateOrderRequest;
import com.campus.module.order.dto.PayOrderRequest;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.demand.mapper.DemandPostMapper;
import com.campus.module.order.entity.CourseOrder;
import com.campus.module.order.mapper.CourseOrderMapper;
import com.campus.module.order.service.CourseOrderService;
import com.campus.module.teaching.entity.TeachingRecord;
import com.campus.module.teaching.mapper.TeachingRecordMapper;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import com.campus.module.wallet.service.SysWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 课程订单Service实现
 */
@Service
@RequiredArgsConstructor
public class CourseOrderServiceImpl extends ServiceImpl<CourseOrderMapper, CourseOrder>
        implements CourseOrderService {

    private final TutorProfileMapper tutorProfileMapper;
    private final SysWalletService walletService;
    private final TeachingRecordMapper teachingRecordMapper;
    private final DemandPostMapper demandPostMapper;

    /**
     * 平台服务费比例(10%)
     */
    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.10");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long parentId, CreateOrderRequest request) {
        // 查询教员档案
        TutorProfile tutorProfile = tutorProfileMapper.selectById(request.getTutorProfileId());
        if (tutorProfile == null || tutorProfile.getCertStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "教员不存在或未认证");
        }

        // 计算金额
        BigDecimal unitPrice = request.getUnitPrice();
        BigDecimal totalHours = new BigDecimal(request.getTotalHours());
        BigDecimal totalAmount = unitPrice.multiply(totalHours);
        BigDecimal serviceFee = totalAmount.multiply(SERVICE_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tutorAmount = totalAmount.subtract(serviceFee);

        // 创建订单
        CourseOrder order = new CourseOrder();
        order.setOrderNo(generateOrderNo());
        order.setParentId(parentId);
        order.setStudentId(request.getStudentId());
        order.setTutorId(tutorProfile.getUserId());
        order.setTutorProfileId(tutorProfile.getId());
        order.setDemandId(request.getDemandId());
        order.setSubject(request.getSubject());
        order.setGrade(request.getGrade());
        order.setTeachMode(request.getTeachMode());
        order.setUnitPrice(unitPrice);
        order.setTotalHours(request.getTotalHours());
        order.setTotalAmount(totalAmount);
        order.setServiceFee(serviceFee);
        order.setTutorAmount(tutorAmount);
        order.setUsedHours(0);
        order.setStatus(0); // 待支付
        order.setRemark(request.getRemark());
        save(order);

        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, PayOrderRequest request) {
        CourseOrder order = getById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        if (!order.getParentId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无权操作此订单");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单状态不正确");
        }

        // 钱包支付
        if (request.getPayType() == 1) {
            // 扣减钱包余额
            boolean success = walletService.deduct(userId, order.getTotalAmount());
            if (!success) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "余额不足");
            }
            // 冻结教员收益(待完课后解冻)
            walletService.freeze(order.getTutorId(), order.getTutorAmount());
        } else {
            // 模拟第三方支付成功
            order.setPayTradeNo("MOCK_" + IdUtil.simpleUUID());
        }

        // 更新订单状态
        order.setStatus(1); // 已支付待上课
        order.setPayTime(LocalDateTime.now());
        order.setPayType(request.getPayType());
        updateById(order);

        // 支付成功后生成课程记录（课表）
        generateTeachingRecords(order);
    }

    /**
     * 根据订单生成课程记录(课表)
     */
    private void generateTeachingRecords(CourseOrder order) {
        // 解析首课时间 (格式: "首课时间: 01-09 14:00 - 15:00")
        LocalDateTime firstLessonStart = parseFirstLessonTime(order.getRemark());
        if (firstLessonStart == null) {
            firstLessonStart = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
        }

        int totalHours = order.getTotalHours();

        for (int i = 1; i <= totalHours; i++) {
            TeachingRecord record = new TeachingRecord();
            record.setOrderId(order.getId());
            record.setLessonIndex(i);
            // 每周一节课，从首课时间开始
            LocalDateTime lessonStart = firstLessonStart.plusWeeks(i - 1);
            record.setStartTime(lessonStart);
            record.setEndTime(lessonStart.plusHours(1)); // 每节课1小时
            record.setStatus(0); // 待确认
            // createTime/updateTime 由 MyBatis-Plus 自动填充
            teachingRecordMapper.insert(record);
        }
    }

    /**
     * 解析首课时间
     * 格式: "首课时间: 01-09 14:00 - 15:00"
     */
    private LocalDateTime parseFirstLessonTime(String remark) {
        if (remark == null || remark.isEmpty()) {
            return null;
        }
        try {
            // 匹配 "01-09 14:00" 这样的格式
            Pattern pattern = Pattern.compile("(\\d{2})-(\\d{2})\\s+(\\d{2}):(\\d{2})");
            Matcher matcher = pattern.matcher(remark);
            if (matcher.find()) {
                int month = Integer.parseInt(matcher.group(1));
                int day = Integer.parseInt(matcher.group(2));
                int hour = Integer.parseInt(matcher.group(3));
                int minute = Integer.parseInt(matcher.group(4));

                // 使用当前年份
                int year = LocalDateTime.now().getYear();
                return LocalDateTime.of(year, month, day, hour, minute);
            }
        } catch (Exception e) {
            // 解析失败，返回null
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId, String reason) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        if (!order.getParentId().equals(userId) && !order.getTutorId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无权操作此订单");
        }
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前状态无法取消");
        }

        // 如果已支付，需要退款
        if (order.getStatus() == 1) {
            // 解冻教员冻结金额
            walletService.unfreeze(order.getTutorId(), order.getTutorAmount());
            // 退还家长金额
            walletService.recharge(order.getParentId(), order.getTotalAmount());
        }

        order.setStatus(4); // 已取消
        order.setCancelReason(reason);
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmStart(Long tutorId, Long orderId) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        if (!order.getTutorId().equals(tutorId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无权操作此订单");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单状态不正确");
        }

        order.setStatus(2); // 进行中
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单状态不正确");
        }

        // 解冻并转入教员余额
        walletService.unfreeze(order.getTutorId(), order.getTutorAmount());
        walletService.recharge(order.getTutorId(), order.getTutorAmount());

        // 更新教员完成订单数
        TutorProfile profile = tutorProfileMapper.selectById(order.getTutorProfileId());
        if (profile != null) {
            profile.setOrderCount(profile.getOrderCount() + 1);
            tutorProfileMapper.updateById(profile);
        }

        order.setStatus(3); // 已完成
        order.setUsedHours(order.getTotalHours());
        updateById(order);
    }

    @Override
    public IPage<CourseOrder> listParentOrders(Long parentId, Integer status, Integer page, Integer size) {
        Page<CourseOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CourseOrder> wrapper = new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getParentId, parentId);
        if (status != null) {
            wrapper.eq(CourseOrder::getStatus, status);
        }
        wrapper.orderByDesc(CourseOrder::getCreateTime);
        return page(pageParam, wrapper);
    }

    @Override
    public IPage<CourseOrder> listTutorOrders(Long tutorId, Integer status, Integer page, Integer size) {
        Page<CourseOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CourseOrder> wrapper = new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getTutorId, tutorId);
        if (status != null) {
            wrapper.eq(CourseOrder::getStatus, status);
        }
        wrapper.orderByDesc(CourseOrder::getCreateTime);
        return page(pageParam, wrapper);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "CT" + System.currentTimeMillis() + IdUtil.simpleUUID().substring(0, 6).toUpperCase();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long acceptDemand(Long tutorId, AcceptDemandRequest request) {
        // 1. 查询需求帖
        DemandPost demand = demandPostMapper.selectById(request.getDemandId());
        if (demand == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "需求不存在");
        }
        if (demand.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "需求已下架或已被接单");
        }
        if (demand.getMatchedTutorId() != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该需求已被其他教员接单");
        }

        // 2. 查询教员档案
        LambdaQueryWrapper<TutorProfile> profileWrapper = new LambdaQueryWrapper<TutorProfile>()
                .eq(TutorProfile::getUserId, tutorId)
                .eq(TutorProfile::getCertStatus, 2); // 已认证
        TutorProfile tutorProfile = tutorProfileMapper.selectOne(profileWrapper);
        if (tutorProfile == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "教员未认证或档案不存在");
        }

        // 3. 计算金额
        BigDecimal unitPrice = demand.getExpectPrice();
        int totalHours = request.getTotalHours() != null ? request.getTotalHours() : 10; // 默认10课时
        BigDecimal totalAmount = unitPrice.multiply(new BigDecimal(totalHours));
        BigDecimal serviceFee = totalAmount.multiply(SERVICE_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tutorAmount = totalAmount.subtract(serviceFee);

        // 4. 创建订单
        CourseOrder order = new CourseOrder();
        order.setOrderNo(generateOrderNo());
        order.setParentId(demand.getPublisherId());
        order.setStudentId(demand.getStudentId());
        order.setTutorId(tutorId);
        order.setTutorProfileId(tutorProfile.getId());
        order.setDemandId(demand.getId());
        order.setSubject(demand.getSubject());
        order.setGrade(demand.getGrade());
        order.setTeachMode(demand.getTeachMode() == 3 ? 1 : demand.getTeachMode()); // 均可时默认上门
        order.setUnitPrice(unitPrice);
        order.setTotalHours(totalHours);
        order.setTotalAmount(totalAmount);
        order.setServiceFee(serviceFee);
        order.setTutorAmount(tutorAmount);
        order.setUsedHours(0);
        order.setStatus(-1); // 待确认（需家长确认后才能支付）
        order.setRemark(request.getRemark());
        save(order);

        // 5. 更新需求帖状态
        demand.setStatus(2); // 已匹配
        demand.setMatchedTutorId(tutorId);
        demandPostMapper.updateById(demand);

        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long parentId, Long orderId) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单不存在");
        }
        if (!order.getParentId().equals(parentId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无权操作此订单");
        }
        if (order.getStatus() != -1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "订单状态不正确，仅待确认订单可确认");
        }

        // 确认后状态变为待支付
        order.setStatus(0);
        updateById(order);
    }
}
