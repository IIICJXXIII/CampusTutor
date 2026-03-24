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
import com.campus.module.wallet.entity.SysWallet;
import com.campus.service.RealWechatPayService;
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
import org.apache.commons.lang3.StringUtils;

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
    private final RealWechatPayService wechatPayService;
    private final SysTransactionFlowService transactionFlowService;

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
        order.setStatus(0); // 待支付
        order.setRemark(request.getRemark());
        save(order);

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
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单状态不正确");
        }

        // 验证金额
        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单金额无效");
        }

        // 钱包支付
        if (request.getPayType() == 1) {
            // 扣减钱包余额
            boolean success = walletService.deduct(userId, order.getTotalAmount());
            if (!success) {
                log.error("余额不足: userId={}, orderId={}, amount={}", userId, order.getId(), order.getTotalAmount());
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "余额不足");
            }

            // 获取家长扣款后的余额
            BigDecimal parentBalanceAfter = BigDecimal.ZERO;
            try {
                SysWallet parentWallet = walletService.getByUserId(userId);
                if (parentWallet != null) {
                    parentBalanceAfter = parentWallet.getBalance();
                }
            } catch (Exception e) {
                log.error("获取家长余额失败: userId={}", userId, e);
            }

            // 记录家长支付交易流水
            try {
                transactionFlowService.recordFlow(
                        userId,
                        order.getTotalAmount().negate(), // 负数表示支出
                        parentBalanceAfter,
                        2, // 支付订单
                        order.getId(),
                        "支付订单: " + order.getOrderNo());
                log.info("家长支付流水记录成功: userId={}, orderId={}, amount={}", userId, order.getId(), order.getTotalAmount());
            } catch (Exception e) {
                log.error("记录家长支付流水失败", e);
                // 不影响支付流程
            }

            // 冻结教员收益(待完课后解冻)
            boolean freezeSuccess = walletService.freeze(order.getTutorId(), order.getTutorAmount());
            if (!freezeSuccess) {
                log.error("冻结教员收益失败: tutorId={}, amount={}", order.getTutorId(), order.getTutorAmount());
                // 回滚家长扣款
                walletService.recharge(userId, order.getTotalAmount());
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "支付失败，请重试");
            }

            // 记录教员收入流水（冻结状态）
            try {
                transactionFlowService.recordFlow(
                        order.getTutorId(),
                        order.getTutorAmount(), // 正数表示收入
                        order.getTutorAmount(), // 暂时用冻结金额
                        3, // 课时费收入
                        order.getId(),
                        "订单支付，课时费已冻结: " + order.getOrderNo());
                log.info("教员收入流水记录成功: tutorId={}, orderId={}, amount={}", order.getTutorId(), order.getId(),
                        order.getTutorAmount());
            } catch (Exception e) {
                log.error("记录教员收入流水失败", e);
                // 不影响支付流程
            }

            // 更新订单状态
            order.setStatus(1); // 已支付待上课
            order.setPayTime(LocalDateTime.now());
            order.setPayType(request.getPayType());
            order.setPayTradeNo("WALLET_" + IdUtil.simpleUUID());
            updateById(order);

            // 更新需求状态为已匹配（支付成功后）
            if (order.getDemandId() != null) {
                DemandPost demand = demandPostMapper.selectById(order.getDemandId());
                if (demand != null && demand.getStatus() == 1) {
                    demand.setStatus(2); // 已匹配
                    demandPostMapper.updateById(demand);
                    log.info("需求状态更新为已匹配: demandId={}", demand.getId());
                }
            }

            // 支付成功后生成课程记录（课表）
            generateTeachingRecords(order);

            log.info("钱包支付成功: userId={}, orderId={}, amount={}", userId, order.getId(), order.getTotalAmount());
            return Collections.singletonMap("status", "success");
        } else {
            // 微信支付
            if (StringUtils.isBlank(request.getOpenid())) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "缺少微信用户标识");
            }

            // 调用微信支付创建支付订单
            Map<String, String> payParams = wechatPayService.createJsapiPay(
                    order.getOrderNo(),
                    order.getTotalAmount().multiply(new BigDecimal(100)).intValue(),
                    "家教课程 - " + order.getSubject(),
                    request.getOpenid());

            // 记录支付预处理信息
            order.setPayType(request.getPayType());
            updateById(order);

            return payParams;
        }
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
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }
        if (!order.getParentId().equals(userId) && !order.getTutorId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此订单");
        }
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "当前状态无法取消");
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
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }
        if (!order.getTutorId().equals(tutorId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此订单");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单状态不正确");
        }

        order.setStatus(2); // 进行中
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long tutorId, Long orderId) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }
        if (!order.getTutorId().equals(tutorId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此订单");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单状态不正确");
        }

        // 解冻并转入教员余额
        try {
            boolean unfreezeSuccess = walletService.unfreeze(order.getTutorId(), order.getTutorAmount());
            if (!unfreezeSuccess) {
                log.error("解冻教员收益失败: orderId={}, tutorId={}, amount={}",
                        orderId, order.getTutorId(), order.getTutorAmount());
                // 不抛出异常，继续执行主流程
            }

            // 生成解冻收入的交易流水记录
            try {
                // 获取实际钱包余额作为balanceAfter
                BigDecimal balanceAfter = BigDecimal.ZERO;
                try {
                    var wallet = walletService.getByUserId(order.getTutorId());
                    if (wallet != null) {
                        balanceAfter = wallet.getBalance();
                    }
                } catch (Exception e) {
                    log.error("获取钱包余额失败: tutorId={}, error={}", order.getTutorId(), e.getMessage());
                }

                transactionFlowService.recordFlow(
                        order.getTutorId(),
                        order.getTutorAmount(),
                        balanceAfter, // 使用实际余额
                        3, // 课时费解冻收入
                        order.getId(),
                        "订单完成，课时费已解冻: " + order.getOrderNo());
                log.info("生成教员解冻收入记录成功: orderId={}, tutorId={}, amount={}",
                        orderId, order.getTutorId(), order.getTutorAmount());
            } catch (Exception e) {
                log.error("生成解冻交易流水记录失败: orderId={}, error={}", orderId, e.getMessage());
                // 不影响主流程，但记录错误
            }
        } catch (Exception e) {
            log.error("钱包操作失败: orderId={}, error={}", orderId, e.getMessage());
            // 不抛出异常，继续执行主流程
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
        order.setStatus(-1); // 待确认（需家长确认后才能支付）
        order.setRemark(request.getRemark());
        save(order);

        // 5. 标记需求已被接单（但保持status=1，等支付后才变为2）
        demand.setMatchedTutorId(tutorId);
        demandPostMapper.updateById(demand);

        log.info("教师 {} 接单成功，需求ID: {}, 订单ID: {}", tutorId, demand.getId(), order.getId());

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
    public Long createOrderFromBooking(Long parentId, Long bookingId, Integer totalHours, BigDecimal unitPrice) {
        // TODO: 实现从预约请求创建订单的逻辑
        // 1. 验证预约请求存在且状态为已确认
        // 2. 创建订单
        // 3. 更新预约请求状态
        // 4. 返回订单ID
        throw new BusinessException("功能开发中");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaySuccess(String orderNo, String transactionId) {
        // 根据订单号查询订单
        LambdaQueryWrapper<CourseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseOrder::getOrderNo, orderNo);
        CourseOrder order = getOne(wrapper);

        if (order == null) {
            log.error("订单不存在: orderNo={}", orderNo);
            return;
        }

        if (order.getStatus() != 0) {
            log.warn("订单状态不正确，无需处理: orderNo={}, status={}", orderNo, order.getStatus());
            return;
        }

        // 更新订单状态
        order.setStatus(1); // 已支付待上课
        order.setPayTime(LocalDateTime.now());
        order.setPayType(2); // 微信支付
        order.setPayTradeNo(transactionId);
        updateById(order);

        // 冻结教员收益(待完课后解冻)
        boolean freezeSuccess = walletService.freeze(order.getTutorId(), order.getTutorAmount());
        if (!freezeSuccess) {
            log.error("冻结教员收益失败: orderNo={}, tutorId={}, amount={}",
                    orderNo, order.getTutorId(), order.getTutorAmount());
            throw new BusinessException("冻结教员收益失败");
        }

        // 生成交易流水记录
        try {
            transactionFlowService.recordFlow(
                    order.getTutorId(),
                    order.getTutorAmount(),
                    order.getTutorAmount(), // 冻结金额作为收入
                    3, // 课时费解冻收入
                    order.getId(),
                    "订单支付成功，课时费已冻结: " + order.getOrderNo());
            log.info("生成教员收入记录成功: orderNo={}, tutorId={}, amount={}",
                    orderNo, order.getTutorId(), order.getTutorAmount());
        } catch (Exception e) {
            log.error("生成交易流水记录失败: orderNo={}, error={}", orderNo, e.getMessage());
            // 不影响主流程，但记录错误
        }

        // 生成课程记录
        generateTeachingRecords(order);

        log.info("订单支付成功处理完成: orderNo={}, transactionId={}", orderNo, transactionId);
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

        // 调用微信支付退款
        try {
            Integer totalAmountInt = totalAmount.multiply(new java.math.BigDecimal(100)).intValue();
            Integer refundAmountInt = refundAmount.multiply(new java.math.BigDecimal(100)).intValue();

            var refundResponse = wechatPayService.createRefund(
                    refundNo,
                    order.getOrderNo(),
                    totalAmountInt,
                    refundAmountInt);

            log.info("退款申请成功: orderId={}, refundNo={}, refundId={}",
                    orderId, refundNo, refundResponse.get("refundId"));
        } catch (Exception e) {
            log.error("退款申请失败: orderId={}, error={}", orderId, e.getMessage());
            throw new BusinessException(ResultCode.REFUND_FAILED.getCode(), "退款申请失败，请稍后重试");
        }

        // 处理退款逻辑
        if (refundAmount.compareTo(refundableAmount) == 0) {
            // 全额退款（按剩余课时）
            // 解冻教员冻结金额
            BigDecimal frozenAmount = order.getTutorAmount();
            BigDecimal refundRatio = refundAmount.divide(totalAmount, 4, RoundingMode.HALF_UP);
            BigDecimal refundToTutor = frozenAmount.multiply(refundRatio).setScale(2, RoundingMode.HALF_UP);

            boolean unfreezeSuccess = walletService.unfreeze(order.getTutorId(), refundToTutor);
            if (!unfreezeSuccess) {
                log.error("退款解冻教员冻结金额失败: orderId={}, tutorId={}, amount={}",
                        orderId, order.getTutorId(), refundToTutor);
                throw new BusinessException("退款处理失败");
            }

            // 生成退款交易流水记录
            try {
                transactionFlowService.recordFlow(
                        order.getTutorId(),
                        refundToTutor.negate(), // 负数表示支出/退款
                        BigDecimal.ZERO, // 退款后余额
                        5, // 退款
                        order.getId(),
                        "订单退款，已解冻: " + order.getOrderNo());
                log.info("生成教员退款记录成功: orderId={}, tutorId={}, amount={}",
                        orderId, order.getTutorId(), refundToTutor);
            } catch (Exception e) {
                log.error("生成退款交易流水记录失败: orderId={}, error={}", orderId, e.getMessage());
                // 不影响主流程，但记录错误
            }

            // 退还家长金额
            walletService.recharge(order.getParentId(), refundAmount);
            // 更新订单状态
            order.setStatus(4); // 已取消
            order.setCancelReason(reason);
        } else {
            // 部分退款
            // 计算退款比例
            BigDecimal refundRatio = refundAmount.divide(totalAmount, 4, RoundingMode.HALF_UP);
            // 解冻相应比例的冻结金额
            BigDecimal frozenAmount = order.getTutorAmount();
            BigDecimal refundToTutor = frozenAmount.multiply(refundRatio).setScale(2, RoundingMode.HALF_UP);

            boolean unfreezeSuccess = walletService.unfreeze(order.getTutorId(), refundToTutor);
            if (!unfreezeSuccess) {
                log.error("部分退款解冻教员冻结金额失败: orderId={}, tutorId={}, amount={}",
                        orderId, order.getTutorId(), refundToTutor);
                throw new BusinessException("退款处理失败");
            }

            // 生成部分退款交易流水记录
            try {
                transactionFlowService.recordFlow(
                        order.getTutorId(),
                        refundToTutor.negate(), // 负数表示支出/退款
                        order.getTutorAmount().subtract(refundToTutor), // 剩余冻结金额
                        5, // 退款
                        order.getId(),
                        "订单部分退款，已解冻: " + order.getOrderNo());
                log.info("生成教员部分退款记录成功: orderId={}, tutorId={}, amount={}",
                        orderId, order.getTutorId(), refundToTutor);
            } catch (Exception e) {
                log.error("生成部分退款交易流水记录失败: orderId={}, error={}", orderId, e.getMessage());
                // 不影响主流程，但记录错误
            }

            // 退还家长金额
            walletService.recharge(order.getParentId(), refundAmount);
            // 更新订单金额
            order.setTotalAmount(order.getTotalAmount().subtract(refundAmount));
            order.setTutorAmount(order.getTutorAmount().subtract(refundToTutor));
        }

        updateById(order);

        return refundNo;
    }

    @Override
    public java.util.Map<String, String> createWechatPayParams(Long userId, Long orderId, String openid) {
        CourseOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
        }

        if (!order.getParentId().equals(userId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此订单");
        }

        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单状态不正确，仅待支付订单可发起支付");
        }

        // 计算金额（分）
        Integer amount = order.getTotalAmount().multiply(new java.math.BigDecimal(100)).intValue();

        // 商品描述
        String description = "家教课程 - " + order.getSubject() + " - " + order.getGrade();

        // 创建微信支付参数
        return wechatPayService.createJsapiPay(
                order.getOrderNo(),
                amount,
                description,
                openid);
    }
}
