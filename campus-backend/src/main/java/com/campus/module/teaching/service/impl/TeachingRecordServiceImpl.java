package com.campus.module.teaching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.ResultCode;
import com.campus.module.order.entity.CourseOrder;
import com.campus.module.order.mapper.CourseOrderMapper;
import com.campus.module.order.service.CourseOrderService;
import com.campus.module.teaching.dto.CheckInRequest;
import com.campus.module.teaching.dto.TeachingRecordDTO;
import com.campus.module.teaching.entity.TeachingRecord;
import com.campus.module.teaching.mapper.TeachingRecordMapper;
import com.campus.module.teaching.service.TeachingRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课时打卡服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeachingRecordServiceImpl extends ServiceImpl<TeachingRecordMapper, TeachingRecord>
        implements TeachingRecordService {

    private final TeachingRecordMapper teachingRecordMapper;
    private final CourseOrderMapper courseOrderMapper;
    private final CourseOrderService courseOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 【关键修复】加上 synchronized 关键字，防止前端手抖连点导致瞬间生成多条记录
    public synchronized Long checkIn(Long tutorId, CheckInRequest request) {
        // 1. 获取订单信息
        CourseOrder order = courseOrderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND.getCode(), ResultCode.ORDER_NOT_FOUND.getMsg());
        }

        // 2. 权限校验：必须是该订单的教员
        if (!order.getTutorId().equals(tutorId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "您不是该订单的执教老师");
        }

        // 3. 状态校验与自动流转 (兼容 1-待开课 和 2-进行中)
        if (order.getStatus() == 1) {
            // 如果是第一次打卡，自动将订单状态变更为 2 (进行中)
            order.setStatus(2);
            courseOrderMapper.updateById(order);
            log.info("订单 {} 首次打卡，状态自动流转为进行中", order.getId());
        } else if (order.getStatus() != 2) {
            // 其他状态（如待支付0、已完成3等）不允许打卡
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "当前订单状态无法打卡");
        }

        // 4. 【关键修复】校验是否还有真正开始但未确认的课时记录
        // 防止教员连续点击打卡，必须等家长确认上一节课后，才能打下一节
        // 只检查有实际打卡数据（GPS坐标或照片）的记录，不包括预排的课表记录
        Long pendingCount = teachingRecordMapper.selectCount(new LambdaQueryWrapper<TeachingRecord>()
                .eq(TeachingRecord::getOrderId, order.getId())
                .eq(TeachingRecord::getStatus, 0) // 0 = 待确认
                .and(wrapper -> wrapper
                        .isNotNull(TeachingRecord::getClockInLat)
                        .or()
                        .isNotNull(TeachingRecord::getClockInImg))); // 只计算有实际打卡数据的记录

        if (pendingCount > 0) {
            throw new BusinessException("上一节课家长尚未确认，请等待确认后再打卡");
        }

        // 5. 校验课时是否已用完
        if (order.getUsedHours() >= order.getTotalHours()) {
            throw new BusinessException("该订单课时已全部完成，无法继续打卡");
        }

        // 6. 【关键修复】查找第一个未打卡的预排课时记录
        // generateTeachingRecords() 已预先创建了所有课时记录，打卡时应更新而非新建
        TeachingRecord nextLesson = teachingRecordMapper.selectOne(new LambdaQueryWrapper<TeachingRecord>()
                .eq(TeachingRecord::getOrderId, order.getId())
                .eq(TeachingRecord::getStatus, 0) // 待确认
                .isNull(TeachingRecord::getClockInLat) // 未打卡（无GPS数据）
                .isNull(TeachingRecord::getClockInImg) // 未打卡（无照片）
                .orderByAsc(TeachingRecord::getLessonIndex)
                .last("LIMIT 1"));

        TeachingRecord record;
        if (nextLesson != null) {
            // 7a. 更新已存在的预排课时记录
            record = nextLesson;
            record.setStartTime(LocalDateTime.now());
            record.setEndTime(LocalDateTime.now().plusHours(2));
            record.setClockInLat(request.getLatitude());
            record.setClockInLng(request.getLongitude());
            record.setClockInImg(request.getPhotoUrl());
            record.setContentSummary(request.getContentSummary());
            record.setHomeworkAssigned(request.getHomeworkAssigned());
            // status 保持 0-待家长确认
            updateById(record);
        } else {
            // 7b. 没有预排记录时（兼容旧订单），创建新记录
            int currentLessonIndex = order.getUsedHours() + 1;
            if (currentLessonIndex > order.getTotalHours()) {
                throw new BusinessException("课时已满，无法继续打卡");
            }

            record = new TeachingRecord();
            record.setOrderId(order.getId());
            record.setLessonIndex(currentLessonIndex);
            record.setStartTime(LocalDateTime.now());
            record.setEndTime(LocalDateTime.now().plusHours(2));
            record.setClockInLat(request.getLatitude());
            record.setClockInLng(request.getLongitude());
            record.setClockInImg(request.getPhotoUrl());
            record.setContentSummary(request.getContentSummary());
            record.setHomeworkAssigned(request.getHomeworkAssigned());
            record.setStatus(0); // 0-待家长确认
            save(record);
        }

        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOut(Long tutorId, Long recordId, String contentSummary, String homeworkAssigned) {
        TeachingRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException("记录不存在");
        }

        CourseOrder order = courseOrderMapper.selectById(record.getOrderId());
        if (order == null || !order.getTutorId().equals(tutorId)) {
            throw new BusinessException("无权操作");
        }

        record.setEndTime(LocalDateTime.now());
        record.setContentSummary(contentSummary);
        record.setHomeworkAssigned(homeworkAssigned);
        updateById(record);

        log.info("教师 {} 完成课时打卡: {}, 内容摘要: {}, 作业: {}",
                tutorId, recordId, contentSummary, homeworkAssigned);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLessonProgress(Long tutorId, Long recordId, Integer progress, String notes) {
        TeachingRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException("记录不存在");
        }

        CourseOrder order = courseOrderMapper.selectById(record.getOrderId());
        if (order == null || !order.getTutorId().equals(tutorId)) {
            throw new BusinessException("无权操作");
        }

        // TODO: 实现课时进度更新逻辑
        // 1. 验证进度值 (0-100)
        // 2. 更新课时记录
        // 3. 记录操作日志

        log.info("教师 {} 更新课时进度: {}, 进度: {}, 备注: {}",
                tutorId, recordId, progress, notes);
    }

    @Override
    public Map<String, Object> getCourseStatistics(Long orderId) {
        // TODO: 实现课程统计功能
        // 1. 计算已完成课时数
        // 2. 计算课程完成率
        // 3. 统计教学内容分布
        // 4. 返回统计结果
        throw new BusinessException("功能开发中");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmByParent(Long parentId, Long recordId) {
        // 1. 获取记录
        TeachingRecord record = getById(recordId);
        if (record == null)
            throw new BusinessException("记录不存在");

        // 2. 校验权限
        CourseOrder order = courseOrderMapper.selectById(record.getOrderId());
        if (order == null || !order.getParentId().equals(parentId)) {
            throw new BusinessException("无权确认");
        }

        // 3. 防重复确认
        if (record.getStatus() == 1) {
            throw new BusinessException("该课时已确认，请勿重复操作");
        }

        // 4. 更新记录状态
        record.setStatus(1); // 已确认
        updateById(record);

        // 5. 更新订单已用课时
        int confirmedCount = teachingRecordMapper.countConfirmedByOrderId(order.getId());
        order.setUsedHours(confirmedCount);

        // 如果所有课时都已完成，调用订单完成方法（会解冻钱包金额）
        if (confirmedCount >= order.getTotalHours()) {
            courseOrderService.completeOrder(order.getTutorId(), order.getId());
        } else {
            order.setUsedHours(confirmedCount);
            courseOrderMapper.updateById(order);
        }
    }

    @Override
    public void disputeByParent(Long parentId, Long recordId, String reason) {
        TeachingRecord record = getById(recordId);
        if (record == null)
            throw new BusinessException("记录不存在");

        CourseOrder order = courseOrderMapper.selectById(record.getOrderId());
        if (!order.getParentId().equals(parentId)) {
            throw new BusinessException("无权操作");
        }

        record.setStatus(2); // 申诉中
        updateById(record);
    }

    @Override
    public List<TeachingRecordDTO> getRecordsByOrderId(Long orderId) {
        List<TeachingRecord> records = teachingRecordMapper.selectByOrderId(orderId);
        return records.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<TeachingRecordDTO> getRecordsByUserId(Long userId, Integer role) {
        LambdaQueryWrapper<CourseOrder> orderQuery = new LambdaQueryWrapper<>();
        if (role == 1) { // 教员
            orderQuery.eq(CourseOrder::getTutorId, userId);
        } else { // 家长
            orderQuery.eq(CourseOrder::getParentId, userId);
        }
        List<CourseOrder> orders = courseOrderMapper.selectList(orderQuery);

        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream().map(CourseOrder::getId).collect(Collectors.toList());

        LambdaQueryWrapper<TeachingRecord> recordQuery = new LambdaQueryWrapper<>();
        recordQuery.in(TeachingRecord::getOrderId, orderIds)
                .orderByDesc(TeachingRecord::getStartTime);
        List<TeachingRecord> records = teachingRecordMapper.selectList(recordQuery);

        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TeachingRecordDTO getRecordById(Long recordId) {
        TeachingRecord record = getById(recordId);
        if (record == null) {
            return null;
        }
        return convertToDTO(record);
    }

    private TeachingRecordDTO convertToDTO(TeachingRecord record) {
        if (record == null)
            return null;
        String statusText;
        switch (record.getStatus()) {
            case 0:
                statusText = "待确认";
                break;
            case 1:
                statusText = "已确认";
                break;
            case 2:
                statusText = "申诉中";
                break;
            default:
                statusText = "未知";
        }

        return TeachingRecordDTO.builder()
                .id(record.getId())
                .orderId(record.getOrderId())
                .lessonIndex(record.getLessonIndex())
                .startTime(record.getStartTime())
                .endTime(record.getEndTime())
                .clockInLat(record.getClockInLat())
                .clockInLng(record.getClockInLng())
                .clockInImg(record.getClockInImg())
                .contentSummary(record.getContentSummary())
                .homeworkAssigned(record.getHomeworkAssigned())
                .status(record.getStatus())
                .statusText(statusText)
                .build();
    }
}