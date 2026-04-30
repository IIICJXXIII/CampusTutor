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
import com.campus.module.demand.entity.TutorApplication;
import com.campus.module.demand.mapper.DemandPostMapper;
import com.campus.module.demand.mapper.TutorApplicationMapper;
import com.campus.module.order.entity.CourseOrder;
import com.campus.module.order.mapper.CourseOrderMapper;
import com.campus.module.order.service.CourseOrderService;
import com.campus.module.teaching.entity.TeachingRecord;
import com.campus.module.teaching.mapper.TeachingRecordMapper;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import com.campus.module.wallet.service.SysWalletService;
import com.campus.module.wallet.entity.SysWallet;
import com.campus.module.wallet.service.SysTransactionFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 课程订单Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseOrderServiceImpl extends ServiceImpl<CourseOrderMapper, CourseOrder>
        implements CourseOrderService {

    private final TutorProfileMapper tutorProfileMapper;
    private final SysWalletService walletService;
    private final TeachingRecordMapper teachingRecordMapper;
    private final DemandPostMapper demandPostMapper;
    private final SysTransactionFlowService transactionFlowService;
    private final com.campus.module.demand.service.GeoService geoService;
    private final com.campus.module.booking.mapper.BookingRequestMapper bookingRequestMapper;
    private final TutorApplicationMapper tutorApplicationMapper;

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
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "教员不存在或未认证");
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
        order.setStatus(-1);
        order.setRemark(request.getRemark());
        if (request.getDemandId() != null) {
            DemandPost demand = demandPostMapper.selectById(request.getDemandId());
            if (demand != null) {
                order.setLongitude(demand.getLongitude());
                order.setLatitude(demand.getLatitude());
                order.setAddress(demand.getAddress());

                if (demand.getStatus() == 1 && demand.getMatchedTutorId() == null) {
                    demand.setMatchedTutorId(tutorProfile.getUserId());
                    demand.setStatus(2);
                    demandPostMapper.updateById(demand);
                    geoService.removeDemandLocation(demand.getId());
                    log.info("[订单创建] 家长 {} 创建订单 {}，需求 {} 标记为已匹配教师 {}",
                            parentId, order.getId(), demand.getId(), tutorProfile.getUserId());
                } else {
                    log.warn("[订单创建] 需求 {} 状态异常(status={}, matchedTutorId={})，未更新匹配状态",
                            demand.getId(), demand.getStatus(), demand.getMatchedTutorId());
                }
            }
        }
        save(order);

        log.info("[订单创建] 家长 {} 创建订单 {}, 教师 {}, 金额 {}", parentId, order.getId(), tutorProfile.getUserId(), totalAmount);

        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> payOrder(Long userId, PayOrderRequest request) {
        CourseOrder order = getById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }
        if (!order.getParentId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此订单");
        }

        String paymentMode = request.getPaymentMode();
        if (paymentMode == null) {
            paymentMode = "per_lesson";
        }

        if ("per_lesson".equals(paymentMode)) {
            return payPerLesson(userId, order, request);
        } else {
            return payFull(userId, order, request);
        }
    }

    private Map<String, String> payPerLesson(Long userId, CourseOrder order, PayOrderRequest request) {
        int lessonCount = request.getLessonCount() != null ? request.getLessonCount() : 1;
        BigDecimal unitPrice = order.getUnitPrice();
        BigDecimal payAmount = unitPrice.multiply(new BigDecimal(lessonCount));
        BigDecimal serviceFeePerLesson = unitPrice.multiply(SERVICE_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tutorAmountPerLesson = unitPrice.subtract(serviceFeePerLesson);
        BigDecimal totalServiceFee = serviceFeePerLesson.multiply(new BigDecimal(lessonCount));
        BigDecimal totalTutorAmount = tutorAmountPerLesson.multiply(new BigDecimal(lessonCount));

        if (request.getPayType() == 1) {
            boolean success = walletService.deduct(userId, payAmount);
            if (!success) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "余额不足");
            }

            try {
                BigDecimal parentBalanceAfter = BigDecimal.ZERO;
                SysWallet parentWallet = walletService.getByUserId(userId);
                if (parentWallet != null) {
                    parentBalanceAfter = parentWallet.getBalance();
                }
                transactionFlowService.recordFlow(
                        userId, payAmount.negate(), parentBalanceAfter, 2,
                        order.getId(), "按课时支付(" + lessonCount + "节): " + order.getOrderNo());
            } catch (Exception e) {
                log.error("记录家长支付流水失败", e);
            }

            boolean freezeSuccess = walletService.freeze(order.getTutorId(), totalTutorAmount);
            if (!freezeSuccess) {
                walletService.recharge(userId, payAmount);
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "支付失败，请重试");
            }

            try {
                transactionFlowService.recordFlow(
                        order.getTutorId(), totalTutorAmount, totalTutorAmount, 3,
                        order.getId(), "课时费冻结(" + lessonCount + "节): " + order.getOrderNo());
            } catch (Exception e) {
                log.error("记录教员收入流水失败", e);
            }

            int newPaidHours = (order.getPaidHours() != null ? order.getPaidHours() : 0) + lessonCount;
            order.setPaidHours(newPaidHours);
            order.setPaymentMode("per_lesson");

            if (order.getStatus() == 0) {
                order.setStatus(1);
                order.setPayTime(LocalDateTime.now());
                order.setPayType(request.getPayType());
                order.setPayTradeNo("WALLET_" + IdUtil.simpleUUID());

                if (order.getDemandId() != null) {
                    DemandPost demand = demandPostMapper.selectById(order.getDemandId());
                    if (demand != null && demand.getStatus() == 1) {
                        demand.setStatus(2);
                        demandPostMapper.updateById(demand);
                    }
                }

                generateTeachingRecords(order);
            }

            if (request.getLessonId() != null) {
                TeachingRecord record = teachingRecordMapper.selectById(request.getLessonId());
                if (record != null && record.getOrderId().equals(order.getId())) {
                    record.setPayStatus(1);
                    record.setPayTime(LocalDateTime.now());
                    teachingRecordMapper.updateById(record);
                }
            } else {
                LambdaQueryWrapper<TeachingRecord> wrapper = new LambdaQueryWrapper<TeachingRecord>()
                        .eq(TeachingRecord::getOrderId, order.getId())
                        .eq(TeachingRecord::getPayStatus, 0)
                        .orderByAsc(TeachingRecord::getLessonIndex)
                        .last("LIMIT " + lessonCount);
                java.util.List<TeachingRecord> unpaidLessons = teachingRecordMapper.selectList(wrapper);
                for (TeachingRecord record : unpaidLessons) {
                    record.setPayStatus(1);
                    record.setPayTime(LocalDateTime.now());
                    teachingRecordMapper.updateById(record);
                }
            }

            updateById(order);

            log.info("按课时支付成功: userId={}, orderId={}, lessonCount={}, amount={}", userId, order.getId(), lessonCount,
                    payAmount);
            return Collections.singletonMap("status", "success");
        }

        throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "当前仅支持钱包支付");
    }

    private Map<String, String> payFull(Long userId, CourseOrder order, PayOrderRequest request) {
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单状态不正确");
        }

        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单金额无效");
        }

        if (request.getPayType() == 1) {
            boolean success = walletService.deduct(userId, order.getTotalAmount());
            if (!success) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "余额不足");
            }

            try {
                BigDecimal parentBalanceAfter = BigDecimal.ZERO;
                SysWallet parentWallet = walletService.getByUserId(userId);
                if (parentWallet != null) {
                    parentBalanceAfter = parentWallet.getBalance();
                }
                transactionFlowService.recordFlow(
                        userId, order.getTotalAmount().negate(), parentBalanceAfter, 2,
                        order.getId(), "全额支付订单: " + order.getOrderNo());
            } catch (Exception e) {
                log.error("记录家长支付流水失败", e);
            }

            boolean freezeSuccess = walletService.freeze(order.getTutorId(), order.getTutorAmount());
            if (!freezeSuccess) {
                walletService.recharge(userId, order.getTotalAmount());
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "支付失败，请重试");
            }

            try {
                transactionFlowService.recordFlow(
                        order.getTutorId(), order.getTutorAmount(), order.getTutorAmount(), 3,
                        order.getId(), "订单支付，课时费已冻结: " + order.getOrderNo());
            } catch (Exception e) {
                log.error("记录教员收入流水失败", e);
            }

            order.setStatus(1);
            order.setPayTime(LocalDateTime.now());
            order.setPayType(request.getPayType());
            order.setPayTradeNo("WALLET_" + IdUtil.simpleUUID());
            order.setPaymentMode("full");
            order.setPaidHours(order.getTotalHours());
            updateById(order);

            if (order.getDemandId() != null) {
                DemandPost demand = demandPostMapper.selectById(order.getDemandId());
                if (demand != null && demand.getStatus() == 1) {
                    demand.setStatus(2);
                    demandPostMapper.updateById(demand);
                }
            }

            generateTeachingRecords(order);

            log.info("全额支付成功: userId={}, orderId={}, amount={}", userId, order.getId(), order.getTotalAmount());
            return Collections.singletonMap("status", "success");
        }

        throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "当前仅支持钱包支付");
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

    private void restoreDemand(Long orderId, Long tutorId, String trigger) {
        CourseOrder order = getById(orderId);
        if (order == null || order.getDemandId() == null) {
            log.warn("[需求恢复] 订单 {} 无关联需求，跳过恢复 (触发: {})", orderId, trigger);
            return;
        }

        DemandPost demand = demandPostMapper.selectById(order.getDemandId());
        if (demand == null) {
            log.warn("[需求恢复] 订单 {} 关联的需求 {} 不存在 (触发: {})", orderId, order.getDemandId(), trigger);
            return;
        }

        log.info("[需求恢复] 开始恢复需求 {} (当前status={}, matchedTutorId={}, 触发: {}, 订单: {}, 教师: {})",
                demand.getId(), demand.getStatus(), demand.getMatchedTutorId(), trigger, orderId, tutorId);

        if (demand.getStatus() == 1 && demand.getMatchedTutorId() == null) {
            log.info("[需求恢复] 需求 {} 已处于上架且未匹配状态，无需恢复 (触发: {})", demand.getId(), trigger);
            return;
        }

        if (demand.getStatus() == 3) {
            log.warn("[需求恢复] 需求 {} 已完成，不应恢复 (触发: {})", demand.getId(), trigger);
            return;
        }

        if (demand.getStatus() == 0) {
            log.warn("[需求恢复] 需求 {} 已被家长主动下架，不应恢复 (触发: {})", demand.getId(), trigger);
            return;
        }

        if (demand.getStatus() != 2) {
            log.warn("[需求恢复] 需求 {} 状态异常(status={})，不恢复 (触发: {})", demand.getId(), demand.getStatus(), trigger);
            return;
        }

        if (demand.getMatchedTutorId() != null && !demand.getMatchedTutorId().equals(tutorId)) {
            log.warn("[需求恢复] 需求 {} 已被其他教师 {} 匹配，当前教师 {}，不恢复 (触发: {})",
                    demand.getId(), demand.getMatchedTutorId(), tutorId, trigger);
            return;
        }

        // 恢复需求状态：上架 + 清除匹配教师
        demand.setMatchedTutorId(null);
        demand.setStatus(1);
        demandPostMapper.updateById(demand);
        log.info("[需求恢复] 已执行 updateById，需求 {} 状态变更: status=1, matchedTutorId=null (触发: {}, 订单: {}, 教师: {})",
                demand.getId(), trigger, orderId, tutorId);

        // 验证恢复结果
        DemandPost verifyDemand = demandPostMapper.selectById(demand.getId());
        if (verifyDemand != null) {
            if (verifyDemand.getMatchedTutorId() != null || verifyDemand.getStatus() != 1) {
                log.error("[需求恢复] 验证失败！需求 {} 更新后 status={}, matchedTutorId={}，使用 UpdateWrapper 强制修正",
                        demand.getId(), verifyDemand.getStatus(), verifyDemand.getMatchedTutorId());
                // 使用 UpdateWrapper 强制将 matchedTutorId 设置为 NULL
                com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<DemandPost> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
                updateWrapper.eq(DemandPost::getId, demand.getId())
                        .set(DemandPost::getStatus, 1)
                        .set(DemandPost::getMatchedTutorId, null);
                demandPostMapper.update(null, updateWrapper);
                log.info("[需求恢复] UpdateWrapper 强制修正完成，需求 {} 已恢复", demand.getId());
            } else {
                log.info("[需求恢复] 验证通过，需求 {} 已正确恢复为上架可匹配状态", demand.getId());
            }
        }

        // 恢复GEO索引
        if (demand.getLongitude() != null && demand.getLatitude() != null) {
            try {
                geoService.addDemandLocation(demand.getId(),
                        demand.getLongitude().doubleValue(), demand.getLatitude().doubleValue());
                log.info("[需求恢复] 需求 {} 已重新添加到Redis GEO索引 ({}, {})",
                        demand.getId(), demand.getLongitude(), demand.getLatitude());
            } catch (Exception e) {
                log.error("[需求恢复] 需求 {} 添加到GEO索引失败", demand.getId(), e);
            }
        } else {
            log.warn("[需求恢复] 需求 {} 无经纬度信息，跳过GEO索引", demand.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId, String reason) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }
        if (!order.getParentId().equals(userId) && !order.getTutorId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此订单");
        }
        if (order.getStatus() != -1 && order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "当前状态无法取消");
        }

        log.info("[订单取消] 用户 {} 取消订单 {}, 原因: {}, 当前订单状态: {}, 关联需求ID: {}",
                userId, orderId, reason, order.getStatus(), order.getDemandId());

        if (order.getStatus() == 1) {
            walletService.unfreeze(order.getTutorId(), order.getTutorAmount());
            walletService.recharge(order.getParentId(), order.getTotalAmount());
            log.info("[订单取消] 已退款: 订单 {}, 退款金额 {}", orderId, order.getTotalAmount());
        }

        order.setStatus(4);
        order.setCancelReason(reason);
        updateById(order);

        restoreDemand(orderId, order.getTutorId(), "取消订单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmStart(Long tutorId, Long orderId) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }
        if (!order.getTutorId().equals(tutorId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "仅教师可确认开课");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单状态不正确，仅已支付待上课的订单可确认开课");
        }

        int paidHours = order.getPaidHours() != null ? order.getPaidHours() : 0;
        int usedHours = order.getUsedHours() != null ? order.getUsedHours() : 0;
        if (paidHours <= usedHours) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "家长尚未支付课时费用，无法确认开课。请等待家长完成支付后再操作。");
        }

        order.setStatus(2);
        updateById(order);
        log.info("教师 {} 确认开课: {}, 已支付课时: {}, 已上课时: {}", tutorId, orderId, paidHours, usedHours);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long userId, Long orderId) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }
        boolean isOwner = order.getTutorId().equals(userId) || order.getParentId().equals(userId);
        if (!isOwner) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此订单");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单状态不正确");
        }

        // 解冻并转入教员余额（失败则回滚整个事务）
        boolean unfreezeSuccess = walletService.unfreeze(order.getTutorId(), order.getTutorAmount());
        if (!unfreezeSuccess) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "解冻教员收益失败");
        }

        // 生成解冻收入的交易流水记录（非关键，不影响主流程）
        try {
            BigDecimal balanceAfter = BigDecimal.ZERO;
            var wallet = walletService.getByUserId(order.getTutorId());
            if (wallet != null) {
                balanceAfter = wallet.getBalance();
            }
            transactionFlowService.recordFlow(
                    order.getTutorId(),
                    order.getTutorAmount(),
                    balanceAfter,
                    3, // 课时费解冻收入
                    order.getId(),
                    "订单完成，课时费已解冻: " + order.getOrderNo());
        } catch (Exception e) {
            log.error("生成解冻交易流水记录失败: orderId={}, error={}", orderId, e.getMessage());
        }

        // 更新教员完成订单数
        try {
            TutorProfile profile = tutorProfileMapper.selectById(order.getTutorProfileId());
            if (profile != null) {
                profile.setOrderCount(profile.getOrderCount() + 1);
                tutorProfileMapper.updateById(profile);
            }
        } catch (Exception e) {
            log.error("更新教员订单数失败: orderId={}, error={}", orderId, e.getMessage());
            // 不影响主流程，但记录错误
        }

        // 更新订单状态
        order.setStatus(3); // 已完成
        order.setUsedHours(order.getTotalHours());
        updateById(order);

        // 更新需求状态为已完成
        if (order.getDemandId() != null) {
            try {
                DemandPost demand = demandPostMapper.selectById(order.getDemandId());
                if (demand != null && demand.getStatus() != 3) {
                    demand.setStatus(3); // 已完成
                    demandPostMapper.updateById(demand);
                    log.info("需求状态更新为已完成: demandId={}, orderId={}", demand.getId(), orderId);
                }
            } catch (Exception e) {
                log.error("更新需求状态失败: demandId={}, orderId={}, error={}",
                        order.getDemandId(), orderId, e.getMessage());
                // 不影响主流程
            }
        }
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
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "需求不存在");
        }
        if (demand.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "需求已下架或已被接单");
        }
        if (demand.getMatchedTutorId() != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该需求已被其他教员接单");
        }

        // 2. 查询教员档案
        LambdaQueryWrapper<TutorProfile> profileWrapper = new LambdaQueryWrapper<TutorProfile>()
                .eq(TutorProfile::getUserId, tutorId)
                .eq(TutorProfile::getCertStatus, 2); // 已认证
        TutorProfile tutorProfile = tutorProfileMapper.selectOne(profileWrapper);
        if (tutorProfile == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "教员未认证或档案不存在");
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
        order.setStatus(-1);
        String remark = request.getRemark();
        if (demand.getDetail() != null && !demand.getDetail().trim().isEmpty()) {
            remark = (remark != null && !remark.trim().isEmpty())
                    ? demand.getDetail() + "\n教师备注: " + remark
                    : demand.getDetail();
        }
        order.setRemark(remark);
        order.setLongitude(demand.getLongitude());
        order.setLatitude(demand.getLatitude());
        order.setAddress(demand.getAddress());
        save(order);

        demand.setMatchedTutorId(tutorId);
        demand.setStatus(2);
        demandPostMapper.updateById(demand);

        geoService.removeDemandLocation(demand.getId());

        log.info("[教师接单] 教师 {} 接单成功，需求ID: {}, 订单ID: {}", tutorId, demand.getId(), order.getId());

        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long parentId, Long orderId) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }
        if (!order.getParentId().equals(parentId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此订单");
        }
        if (order.getStatus() != -1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单状态不正确，仅待确认订单可确认");
        }

        // 确认后状态变为待支付
        order.setStatus(0);
        updateById(order);

        log.info("家长 {} 确认订单: {}, 状态变更为待支付", parentId, orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void parentRejectOrder(Long parentId, Long orderId, String reason) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }
        if (!order.getParentId().equals(parentId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此订单");
        }
        if (order.getStatus() != -1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单状态不正确，仅待确认订单可拒绝");
        }

        order.setStatus(4);
        order.setCancelReason("家长拒绝接单申请: " + (reason != null ? reason : "未说明原因"));
        updateById(order);

        log.info("[家长拒绝] 家长 {} 拒绝接单申请: {}, 原因: {}", parentId, orderId, reason);

        restoreDemand(orderId, order.getTutorId(), "家长拒绝接单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void tutorConfirmOrder(Long tutorId, Long orderId) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }
        if (!order.getTutorId().equals(tutorId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此订单");
        }
        if (order.getStatus() != -1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单状态不正确，仅待确认订单可操作");
        }

        // 教师确认后，订单变为待支付
        order.setStatus(0);
        updateById(order);

        log.info("教师 {} 确认预约订单: {}, 状态变更为待支付", tutorId, orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void tutorRejectOrder(Long tutorId, Long orderId, String reason) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }
        if (!order.getTutorId().equals(tutorId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此订单");
        }
        if (order.getStatus() != -1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单状态不正确，仅待确认订单可拒绝");
        }

        order.setStatus(4);
        order.setCancelReason("教师拒绝: " + (reason != null ? reason : "未说明原因"));
        updateById(order);

        log.info("[教师拒绝] 教师 {} 拒绝预约订单: {}, 原因: {}", tutorId, orderId, reason);

        restoreDemand(orderId, tutorId, "教师拒绝接单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String applyRefund(Long userId, Long orderId, java.math.BigDecimal refundAmount, String reason) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }

        if (!order.getParentId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此订单");
        }

        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单状态不正确，仅已支付或进行中的订单可退款");
        }

        // 计算可退款金额（考虑已上课时）
        int totalHours = order.getTotalHours();
        int usedHours = order.getUsedHours() != null ? order.getUsedHours() : 0;
        int remainingHours = totalHours - usedHours;

        if (remainingHours <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无可退课时");
        }

        BigDecimal totalAmount = order.getTotalAmount();
        BigDecimal refundableAmount = totalAmount.multiply(new BigDecimal(remainingHours))
                .divide(new BigDecimal(totalHours), 2, RoundingMode.HALF_UP);

        // 检查退款金额
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "退款金额必须大于0");
        }

        if (refundAmount.compareTo(refundableAmount) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "退款金额不能大于可退金额");
        }

        if (refundAmount.compareTo(totalAmount) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "退款金额不能大于订单总金额");
        }

        // 生成退款单号
        String refundNo = "RF" + System.currentTimeMillis() + IdUtil.simpleUUID().substring(0, 6).toUpperCase();

        // 设置订单为退款中状态（等待管理员审批后实际执行退款）
        order.setStatus(5); // 退款中
        order.setCancelReason("退款申请: " + reason + " | 退款单号: " + refundNo + " | 申请退款金额: " + refundAmount);
        updateById(order);

        log.info("退款申请已提交: orderId={}, refundNo={}, refundAmount={}, reason={}",
                orderId, refundNo, refundAmount, reason);

        return refundNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApplication(Long tutorId, Long orderId, String reason) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }
        if (!order.getTutorId().equals(tutorId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此订单");
        }
        if (order.getApplicationId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该订单不是申请订单，无法取消申请");
        }
        if (order.getStatus() != -1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "当前状态无法取消申请");
        }

        // 更新申请状态为已拒绝(2)
        TutorApplication application = tutorApplicationMapper.selectById(order.getApplicationId());
        if (application != null) {
            application.setStatus(2);
            application.setRejectReason("教师取消申请: " + (reason != null ? reason : "未说明原因"));
            tutorApplicationMapper.updateById(application);
        }

        // 更新订单状态为已取消
        order.setStatus(4);
        order.setCancelReason("教师取消申请: " + (reason != null ? reason : "未说明原因"));
        updateById(order);

        log.info("[取消申请] 教师 {} 取消申请, 订单ID: {}, 申请ID: {}, 原因: {}",
                tutorId, orderId, order.getApplicationId(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrderFromBooking(Long parentId, Long bookingId, Integer totalHours, BigDecimal unitPrice) {
        // 查询预约请求
        com.campus.module.booking.entity.BookingRequest booking = bookingRequestMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "预约请求不存在");
        }
        if (!booking.getParentId().equals(parentId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此预约");
        }

        // 查询教员档案
        LambdaQueryWrapper<TutorProfile> profileWrapper = new LambdaQueryWrapper<TutorProfile>()
                .eq(TutorProfile::getUserId, booking.getTutorId())
                .eq(TutorProfile::getCertStatus, 2);
        TutorProfile tutorProfile = tutorProfileMapper.selectOne(profileWrapper);
        if (tutorProfile == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "教员未认证或档案不存在");
        }

        // 计算金额
        BigDecimal totalAmount = unitPrice.multiply(new BigDecimal(totalHours));
        BigDecimal serviceFee = totalAmount.multiply(SERVICE_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tutorAmount = totalAmount.subtract(serviceFee);

        // 创建订单
        CourseOrder order = new CourseOrder();
        order.setOrderNo(generateOrderNo());
        order.setParentId(parentId);
        order.setStudentId(booking.getStudentId());
        order.setTutorId(booking.getTutorId());
        order.setTutorProfileId(tutorProfile.getId());
        order.setSubject(booking.getSubject());
        order.setGrade(booking.getGrade());
        order.setTeachMode(1); // 默认上门
        order.setUnitPrice(unitPrice);
        order.setTotalHours(totalHours);
        order.setTotalAmount(totalAmount);
        order.setServiceFee(serviceFee);
        order.setTutorAmount(tutorAmount);
        order.setUsedHours(0);
        order.setStatus(0); // 待支付
        order.setRemark(booking.getRemark());
        save(order);

        log.info("[预约转订单] 家长 {} 从预约 {} 创建订单 {}, 教师 {}, 金额 {}",
                parentId, bookingId, order.getId(), booking.getTutorId(), totalAmount);

        return order.getId();
    }

}
