package com.campus.module.teaching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.exception.BusinessException;
import com.campus.module.order.entity.CourseOrder;
import com.campus.module.order.mapper.CourseOrderMapper;
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
import java.util.stream.Collectors;

/**
 * 课时打卡服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeachingRecordServiceImpl implements TeachingRecordService {

    private final TeachingRecordMapper teachingRecordMapper;
    private final CourseOrderMapper courseOrderMapper;

    @Override
    @Transactional
    public Long checkIn(Long tutorId, CheckInRequest request) {
        // 1. 验证订单存在且属于该教员
        CourseOrder order = courseOrderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getTutorId().equals(tutorId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() < 1) {
            throw new BusinessException("订单未支付，无法打卡");
        }

        // 2. 检查是否有未完成的课时记录
        TeachingRecord latestRecord = teachingRecordMapper.selectLatestByOrderId(request.getOrderId());
        if (latestRecord != null && latestRecord.getEndTime() == null) {
            throw new BusinessException("存在未结束的课时记录，请先完成下课打卡");
        }

        // 3. 计算第几节课
        int lessonIndex = 1;
        if (latestRecord != null) {
            lessonIndex = latestRecord.getLessonIndex() + 1;
        }

        // 4. 检查是否超出课时包总数
        if (lessonIndex > order.getTotalHours()) {
            throw new BusinessException("已超出购买课时数，请续费后继续");
        }

        // 5. 创建打卡记录
        TeachingRecord record = new TeachingRecord();
        record.setOrderId(request.getOrderId());
        record.setLessonIndex(lessonIndex);
        record.setStartTime(LocalDateTime.now());
        record.setClockInLat(request.getLatitude());
        record.setClockInLng(request.getLongitude());
        record.setClockInImg(request.getPhotoUrl());
        record.setStatus(0); // 待确认

        teachingRecordMapper.insert(record);
        log.info("教师打卡成功: orderId={}, lessonIndex={}", request.getOrderId(), lessonIndex);

        return record.getId();
    }

    @Override
    @Transactional
    public void checkOut(Long tutorId, Long recordId, String contentSummary, String homeworkAssigned) {
        // 1. 验证记录存在
        TeachingRecord record = teachingRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("课时记录不存在");
        }

        // 2. 验证订单属于该教员
        CourseOrder order = courseOrderMapper.selectById(record.getOrderId());
        if (!order.getTutorId().equals(tutorId)) {
            throw new BusinessException("无权操作此记录");
        }

        // 3. 检查是否已下课
        if (record.getEndTime() != null) {
            throw new BusinessException("该课时已结束");
        }

        // 4. 更新下课时间和教学内容
        record.setEndTime(LocalDateTime.now());
        record.setContentSummary(contentSummary);
        record.setHomeworkAssigned(homeworkAssigned);

        teachingRecordMapper.updateById(record);
        log.info("教师下课打卡成功: recordId={}", recordId);
    }

    @Override
    @Transactional
    public void confirmByParent(Long parentId, Long recordId) {
        // 1. 验证记录存在
        TeachingRecord record = teachingRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("课时记录不存在");
        }

        // 2. 验证订单属于该家长
        CourseOrder order = courseOrderMapper.selectById(record.getOrderId());
        if (!order.getParentId().equals(parentId)) {
            throw new BusinessException("无权操作此记录");
        }

        // 3. 检查状态
        if (record.getStatus() != 0) {
            throw new BusinessException("该课时已处理过");
        }

        // 4. 检查是否已下课
        if (record.getEndTime() == null) {
            throw new BusinessException("课程尚未结束，无法确认");
        }

        // 5. 更新状态为已确认
        record.setStatus(1);
        teachingRecordMapper.updateById(record);

        // 6. 更新订单已用课时
        int confirmedCount = teachingRecordMapper.countConfirmedByOrderId(order.getId());
        order.setUsedHours(confirmedCount);
        
        // 如果所有课时都已完成，更新订单状态
        if (confirmedCount >= order.getTotalHours()) {
            order.setStatus(3); // 已完成
        }
        courseOrderMapper.updateById(order);

        log.info("家长确认课时成功: recordId={}, usedHours={}", recordId, order.getUsedHours());
    }

    @Override
    @Transactional
    public void disputeByParent(Long parentId, Long recordId, String reason) {
        // 1. 验证记录存在
        TeachingRecord record = teachingRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("课时记录不存在");
        }

        // 2. 验证订单属于该家长
        CourseOrder order = courseOrderMapper.selectById(record.getOrderId());
        if (!order.getParentId().equals(parentId)) {
            throw new BusinessException("无权操作此记录");
        }

        // 3. 更新状态为申诉中
        record.setStatus(2);
        teachingRecordMapper.updateById(record);

        log.info("家长申诉课时: recordId={}, reason={}", recordId, reason);
        // TODO: 可以记录申诉原因到单独的表，并通知管理员
    }

    @Override
    public List<TeachingRecordDTO> getRecordsByOrderId(Long orderId) {
        List<TeachingRecord> records = teachingRecordMapper.selectByOrderId(orderId);
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TeachingRecordDTO getRecordById(Long recordId) {
        TeachingRecord record = teachingRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("课时记录不存在");
        }
        return convertToDTO(record);
    }

    /**
     * 转换为 DTO
     */
    private TeachingRecordDTO convertToDTO(TeachingRecord record) {
        String statusText;
        switch (record.getStatus()) {
            case 0: statusText = "待确认"; break;
            case 1: statusText = "已确认"; break;
            case 2: statusText = "申诉中"; break;
            default: statusText = "未知";
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
                .createTime(record.getCreateTime())
                .build();
    }
}
