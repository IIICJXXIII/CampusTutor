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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final com.campus.module.teaching.mapper.TeachingFeedbackMapper feedbackMapper;
    private final com.campus.module.user.mapper.SysUserMapper sysUserMapper;
    private final com.campus.module.tutor.mapper.TutorProfileMapper tutorProfileMapper;
    private final com.campus.module.parent.mapper.ParentStudentMapper parentStudentMapper;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long checkIn(Long tutorId, CheckInRequest request) {
        return checkIn(tutorId, request, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long checkIn(Long tutorId, CheckInRequest request, MultipartFile photo) {
        // 分布式锁：防止前端连点导致重复打卡
        String lockKey = "checkin:order:" + request.getOrderId();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (acquired == null || !acquired) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "操作过于频繁，请稍后再试");
        }
        try {
            // 处理照片上传
            if (photo != null && !photo.isEmpty()) {
                String photoUrl = saveCheckinPhoto(photo, request.getOrderId());
                request.setPhotoUrl(photoUrl);
                // GPS 校验：打卡位置不能为空
                if (request.getLatitude() == null || request.getLongitude() == null
                        || request.getLatitude().compareTo(BigDecimal.ZERO) == 0
                        || request.getLongitude().compareTo(BigDecimal.ZERO) == 0) {
                    throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无法获取位置信息，请开启GPS后重试");
                }
            }
            return doCheckIn(tutorId, request);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 保存打卡照片到本地，返回访问URL。
     * 存储路径: {file.upload.path}/checkin/，由 WebMvcConfig 映射为 /uploads/checkin/**。
     * 生产环境应将 file.upload.path 指向持久化卷或挂载 OSS。
     */
    private String saveCheckinPhoto(MultipartFile photo, Long orderId) {
        try {
            // 预先校验文件类型
            String contentType = photo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "只允许上传图片文件");
            }

            // 解析上传根目录为绝对路径
            Path basePath = Paths.get(uploadPath);
            if (!basePath.isAbsolute()) {
                basePath = Paths.get(System.getProperty("user.dir")).resolve(uploadPath);
            }
            Path checkinDir = basePath.resolve("checkin");
            if (!Files.exists(checkinDir)) {
                Files.createDirectories(checkinDir);
            }

            String originalName = photo.getOriginalFilename();
            String ext = ".jpg";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf('.'));
            }
            String filename = orderId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
            Path filePath = checkinDir.resolve(filename);
            photo.transferTo(filePath.toFile());

            log.info("打卡照片已保存: orderId={}, path={}", orderId, filePath.toAbsolutePath());
            return "/uploads/checkin/" + filename;
        } catch (IOException e) {
            log.error("保存打卡照片失败: orderId={}", orderId, e);
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "照片保存失败，请重试");
        }
    }

    private Long doCheckIn(Long tutorId, CheckInRequest request) {
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

        // 4. 【关键修复】校验是否还有已结课但未确认的课时记录
        // 防止教员连续点击打卡，必须等家长确认上一节课后，才能打下一节
        Long pendingCount = teachingRecordMapper.selectCount(new LambdaQueryWrapper<TeachingRecord>()
                .eq(TeachingRecord::getOrderId, order.getId())
                .eq(TeachingRecord::getStatus, 2) // 2 = 待确认
                .and(wrapper -> wrapper
                        .isNotNull(TeachingRecord::getClockInLat)
                        .or()
                        .isNotNull(TeachingRecord::getClockInImg)));

        if (pendingCount > 0) {
            throw new BusinessException("上一节课家长尚未确认，请等待确认后再打卡");
        }

        // 5. 校验课时是否已用完
        if (order.getUsedHours() >= order.getTotalHours()) {
            throw new BusinessException("该订单课时已全部完成，无法继续打卡");
        }

        // 6. 【关键修复】查找第一个待上课的预排课时记录
        // generateTeachingRecords() 已预先创建了所有课时记录，打卡时应更新而非新建
        TeachingRecord nextLesson = teachingRecordMapper.selectOne(new LambdaQueryWrapper<TeachingRecord>()
                .eq(TeachingRecord::getOrderId, order.getId())
                .eq(TeachingRecord::getStatus, 0) // 0 = 待上课
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
            record.setStatus(1); // 上课中
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
            record.setStatus(1); // 上课中
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
        record.setStatus(2); // 待确认
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
        CourseOrder order = courseOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        List<TeachingRecord> records = teachingRecordMapper.selectByOrderId(orderId);

        int totalLessons = order.getTotalHours() != null ? order.getTotalHours() : 0;
        int confirmedCount = 0;
        int inProgressCount = 0;
        int pendingConfirmCount = 0;
        int disputedCount = 0;
        long totalDurationMinutes = 0;

        for (TeachingRecord r : records) {
            Integer s = r.getStatus();
            if (s != null) {
                if (s == 3 || s == 6) confirmedCount++;       // 已确认 or 已过期(自动确认)
                else if (s == 1) inProgressCount++;            // 上课中
                else if (s == 2) pendingConfirmCount++;        // 待确认
                else if (s == 4) disputedCount++;              // 申诉中
            }
            if (r.getStartTime() != null && r.getEndTime() != null) {
                totalDurationMinutes += java.time.Duration.between(r.getStartTime(), r.getEndTime()).toMinutes();
            }
        }

        double completionRate = totalLessons > 0 ? (double) confirmedCount / totalLessons * 100 : 0;
        double totalDurationHours = totalDurationMinutes / 60.0;

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalLessons", totalLessons);
        stats.put("completedLessons", confirmedCount);
        stats.put("inProgressLessons", inProgressCount);
        stats.put("pendingConfirmLessons", pendingConfirmCount);
        stats.put("disputedLessons", disputedCount);
        stats.put("completionRate", Math.round(completionRate * 10.0) / 10.0);
        stats.put("totalDurationHours", Math.round(totalDurationHours * 10.0) / 10.0);
        stats.put("paidHours", order.getPaidHours() != null ? order.getPaidHours() : 0);
        stats.put("orderStatus", order.getStatus());

        return stats;
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
        if (record.getStatus() != null && record.getStatus() == 3) {
            throw new BusinessException("该课时已确认，请勿重复操作");
        }

        // 4. 更新记录状态
        record.setStatus(3); // 已确认
        updateById(record);

        // 5. 逐节结算：释放对应比例资金给教师
        courseOrderService.releasePerLessonPayment(order, record);

        // 6. 检查是否所有课时都已确认并结算，完成订单
        CourseOrder refreshed = courseOrderMapper.selectById(order.getId());
        if (refreshed.getConfirmedHours() >= refreshed.getTotalHours()
                && refreshed.getPaidHours() >= refreshed.getConfirmedHours()) {
            courseOrderService.completeOrder(order.getTutorId(), order.getId());
        }
    }

    @Override
    public void disputeByParent(Long parentId, Long recordId, String reason) {
        TeachingRecord record = getById(recordId);
        if (record == null)
            throw new BusinessException("记录不存在");

        CourseOrder order = courseOrderMapper.selectById(record.getOrderId());
        if (order == null || !order.getParentId().equals(parentId)) {
            throw new BusinessException("无权操作");
        }

        // 只能申诉"待确认"状态的课时
        if (record.getStatus() != null && record.getStatus() != 2) {
            if (record.getStatus() == 3) {
                throw new BusinessException("该课时已确认，无法申诉");
            } else if (record.getStatus() == 4) {
                throw new BusinessException("该课时已在申诉中，请勿重复操作");
            } else if (record.getStatus() == 6) {
                throw new BusinessException("该课时已超时自动确认，无法申诉");
            } else {
                throw new BusinessException("当前课时状态无法申诉");
            }
        }

        record.setStatus(4); // 申诉中（资金保持冻结，不会被自动确认）
        updateById(record);

        log.info("家长 {} 申诉课时: recordId={}, orderId={}, reason={}",
                parentId, recordId, order.getId(), reason);
    }

    @Override
    public List<TeachingRecordDTO> getRecordsByOrderId(Long orderId) {
        List<TeachingRecord> records = teachingRecordMapper.selectByOrderId(orderId);
        CourseOrder order = courseOrderMapper.selectById(orderId);
        return enrichDTOs(records, order != null ? java.util.Collections.singletonList(order) : List.of());
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

        return enrichDTOs(records, orders);
    }

    @Override
    public TeachingRecordDTO getRecordById(Long recordId) {
        TeachingRecord record = getById(recordId);
        if (record == null) {
            return null;
        }
        CourseOrder order = courseOrderMapper.selectById(record.getOrderId());
        List<TeachingRecordDTO> enriched = enrichDTOs(List.of(record),
                order != null ? java.util.Collections.singletonList(order) : List.of());
        return enriched.isEmpty() ? null : enriched.get(0);
    }

    /**
     * 批量丰富 DTO：加载关联的订单、用户、档案数据并填充到 DTO 中
     */
    private List<TeachingRecordDTO> enrichDTOs(List<TeachingRecord> records, List<CourseOrder> orders) {
        if (records.isEmpty()) return List.of();

        // 建立 orderId -> CourseOrder 的索引
        Map<Long, CourseOrder> orderMap = orders.stream()
                .collect(Collectors.toMap(CourseOrder::getId, o -> o, (a, b) -> a));

        // 收集所有关联的用户 ID
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        for (CourseOrder o : orders) {
            userIds.add(o.getTutorId());
            userIds.add(o.getParentId());
        }

        // 批量加载用户信息
        final Map<Long, com.campus.module.user.entity.SysUser> userMap;
        if (!userIds.isEmpty()) {
            List<com.campus.module.user.entity.SysUser> users = sysUserMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(com.campus.module.user.entity.SysUser::getId, u -> u, (a, b) -> a));
        } else {
            userMap = Map.of();
        }

        // 批量加载教员档案
        final Map<Long, com.campus.module.tutor.entity.TutorProfile> tutorProfileMap;
        {
            List<Long> tutorUserIds = orders.stream().map(CourseOrder::getTutorId).distinct().collect(Collectors.toList());
            if (!tutorUserIds.isEmpty()) {
                List<com.campus.module.tutor.entity.TutorProfile> profiles = tutorProfileMapper.selectList(
                        new LambdaQueryWrapper<com.campus.module.tutor.entity.TutorProfile>()
                                .in(com.campus.module.tutor.entity.TutorProfile::getUserId, tutorUserIds));
                tutorProfileMap = profiles.stream()
                        .collect(Collectors.toMap(com.campus.module.tutor.entity.TutorProfile::getUserId, p -> p, (a, b) -> a));
            } else {
                tutorProfileMap = Map.of();
            }
        }

        // 批量加载学生信息
        final Map<Long, String> studentNameMap;
        {
            List<Long> studentIds = orders.stream()
                    .map(CourseOrder::getStudentId).filter(id -> id != null).distinct()
                    .collect(Collectors.toList());
            if (!studentIds.isEmpty()) {
                List<com.campus.module.parent.entity.ParentStudent> students = parentStudentMapper.selectBatchIds(studentIds);
                studentNameMap = students.stream()
                        .collect(Collectors.toMap(com.campus.module.parent.entity.ParentStudent::getId,
                                s -> s.getStudentName() != null ? s.getStudentName() : "", (a, b) -> a));
            } else {
                studentNameMap = Map.of();
            }
        }

        // 构建富化的 DTO
        return records.stream().map(record -> {
            CourseOrder order = orderMap.get(record.getOrderId());
            if (order == null) {
                return buildBasicDTO(record);
            }
            return buildEnrichedDTO(record, order, userMap, tutorProfileMap, studentNameMap);
        }).collect(Collectors.toList());
    }

    private TeachingRecordDTO buildEnrichedDTO(TeachingRecord record, CourseOrder order,
            Map<Long, com.campus.module.user.entity.SysUser> userMap,
            Map<Long, com.campus.module.tutor.entity.TutorProfile> tutorProfileMap,
            Map<Long, String> studentNameMap) {

        com.campus.module.user.entity.SysUser tutorUser = userMap.get(order.getTutorId());
        com.campus.module.user.entity.SysUser parentUser = userMap.get(order.getParentId());
        com.campus.module.tutor.entity.TutorProfile tutorProfile = tutorProfileMap.get(order.getTutorId());

        // 每课时费用
        java.math.BigDecimal fee = java.math.BigDecimal.ZERO;
        if (order.getTotalAmount() != null && order.getTotalHours() != null && order.getTotalHours() > 0) {
            fee = order.getTotalAmount().divide(
                    java.math.BigDecimal.valueOf(order.getTotalHours()), 2, java.math.RoundingMode.HALF_UP);
        }

        // 时长文字
        String duration = "--";
        if (record.getStartTime() != null && record.getEndTime() != null) {
            long minutes = java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMinutes();
            duration = minutes + "分钟";
        }

        // 签到位置
        String checkInLocation = null;
        if (record.getClockInLat() != null && record.getClockInLng() != null) {
            checkInLocation = record.getClockInLat() + ", " + record.getClockInLng();
        }

        String statusText;
        switch (record.getStatus() != null ? record.getStatus() : -1) {
            case 0: statusText = "待上课"; break;
            case 1: statusText = "上课中"; break;
            case 2: statusText = "待确认"; break;
            case 3: statusText = "已确认"; break;
            case 4: statusText = "申诉中"; break;
            case 5: statusText = "已解决"; break;
            case 6: statusText = "已过期"; break;
            default: statusText = "未知";
        }

        return TeachingRecordDTO.builder()
                .id(record.getId()).orderId(record.getOrderId()).lessonIndex(record.getLessonIndex())
                .startTime(record.getStartTime()).endTime(record.getEndTime())
                .scheduledStartTime(record.getScheduledStartTime()).scheduledEndTime(record.getScheduledEndTime())
                .clockInLat(record.getClockInLat()).clockInLng(record.getClockInLng())
                .clockInImg(record.getClockInImg())
                .contentSummary(record.getContentSummary()).homeworkAssigned(record.getHomeworkAssigned())
                .status(record.getStatus()).statusText(statusText)
                .payStatus(record.getPayStatus()).payTime(record.getPayTime()).createTime(record.getCreateTime())
                // 关联订单字段
                .subject(order.getSubject()).grade(order.getGrade())
                .teachingMode(order.getTeachMode()).address(order.getAddress())
                .unitPrice(order.getUnitPrice()).fee(fee)
                .lessonDate(record.getStartTime() != null ? record.getStartTime() : record.getScheduledStartTime())
                // 用户信息
                .tutorUserId(order.getTutorId())
                .tutorName(tutorUser != null ? (tutorUser.getNickname() != null ? tutorUser.getNickname() : "") : "")
                .tutorAvatar(tutorUser != null ? tutorUser.getAvatarUrl() : null)
                .tutorUniversity(tutorProfile != null ? tutorProfile.getUniversityName() : null)
                .parentId(order.getParentId())
                .studentName(order.getStudentId() != null ? studentNameMap.getOrDefault(order.getStudentId(), "") : "")
                .studentAvatar(parentUser != null ? parentUser.getAvatarUrl() : null)
                // 前端兼容字段
                .checkInTime(record.getStartTime()).checkOutTime(record.getEndTime())
                .checkInLocation(checkInLocation).checkOutLocation(null)
                .checkOutContent(record.getContentSummary()).duration(duration)
                .build();
    }

    private TeachingRecordDTO buildBasicDTO(TeachingRecord record) {
        String statusText;
        switch (record.getStatus() != null ? record.getStatus() : -1) {
            case 0: statusText = "待上课"; break;
            case 1: statusText = "上课中"; break;
            case 2: statusText = "待确认"; break;
            case 3: statusText = "已确认"; break;
            case 4: statusText = "申诉中"; break;
            case 5: statusText = "已解决"; break;
            case 6: statusText = "已过期"; break;
            default: statusText = "未知";
        }
        return TeachingRecordDTO.builder()
                .id(record.getId()).orderId(record.getOrderId()).lessonIndex(record.getLessonIndex())
                .startTime(record.getStartTime()).endTime(record.getEndTime())
                .scheduledStartTime(record.getScheduledStartTime()).scheduledEndTime(record.getScheduledEndTime())
                .clockInLat(record.getClockInLat()).clockInLng(record.getClockInLng())
                .clockInImg(record.getClockInImg())
                .contentSummary(record.getContentSummary()).homeworkAssigned(record.getHomeworkAssigned())
                .status(record.getStatus()).statusText(statusText)
                .payStatus(record.getPayStatus()).payTime(record.getPayTime()).createTime(record.getCreateTime())
                .build();
    }

    /**
     * 定时任务：自动确认超时未确认的课时记录（结课后72小时）
     * 每小时执行一次
     */
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 * * * *")
    @Transactional(rollbackFor = Exception.class)
    public void autoConfirmExpiredRecords() {
        LocalDateTime expiry = LocalDateTime.now().minusHours(72);
        List<TeachingRecord> expired = teachingRecordMapper.selectList(
                new LambdaQueryWrapper<TeachingRecord>()
                        .eq(TeachingRecord::getStatus, 2) // 待确认
                        .lt(TeachingRecord::getEndTime, expiry));

        if (expired.isEmpty()) return;

        log.info("开始自动确认超时课时: 共{}条", expired.size());
        for (TeachingRecord record : expired) {
            try {
                CourseOrder order = courseOrderMapper.selectById(record.getOrderId());
                if (order == null) continue;

                record.setStatus(6); // 已过期
                updateById(record);

                // 自动确认后释放资金给教师
                courseOrderService.releasePerLessonPayment(order, record);

                log.info("课时已超时自动确认: orderId={}, lessonIndex={}, recordId={}",
                        record.getOrderId(), record.getLessonIndex(), record.getId());
            } catch (Exception e) {
                log.error("自动确认课时失败: recordId={}, error={}", record.getId(), e.getMessage());
            }
        }
    }

    @Override
    public void submitFeedback(Long userId, Long recordId, Integer rating, String tags, String content) {
        TeachingRecord record = getById(recordId);
        if (record == null) throw new BusinessException("课时记录不存在");

        // 只能对已确认的课时提交反馈
        if (record.getStatus() == null || record.getStatus() < 3) {
            throw new BusinessException("该课时尚未确认，无法提交反馈");
        }

        // 检查是否已评价
        Long existingCount = feedbackMapper.selectCount(new LambdaQueryWrapper<com.campus.module.teaching.entity.TeachingFeedback>()
                .eq(com.campus.module.teaching.entity.TeachingFeedback::getRecordId, recordId)
                .eq(com.campus.module.teaching.entity.TeachingFeedback::getFromUserId, userId));
        if (existingCount > 0) {
            throw new BusinessException("您已对该课时提交过反馈");
        }

        com.campus.module.teaching.entity.TeachingFeedback feedback = new com.campus.module.teaching.entity.TeachingFeedback();
        feedback.setRecordId(recordId);
        feedback.setOrderId(record.getOrderId());
        feedback.setFromUserId(userId);
        feedback.setRating(rating != null ? rating : 5);
        feedback.setTags(tags);
        feedback.setContent(content);
        feedbackMapper.insert(feedback);
    }

    @Override
    public Object getFeedbackByRecordId(Long recordId) {
        List<com.campus.module.teaching.entity.TeachingFeedback> feedbacks = feedbackMapper.selectList(
                new LambdaQueryWrapper<com.campus.module.teaching.entity.TeachingFeedback>()
                        .eq(com.campus.module.teaching.entity.TeachingFeedback::getRecordId, recordId)
                        .orderByDesc(com.campus.module.teaching.entity.TeachingFeedback::getCreateTime));
        return feedbacks;
    }
}