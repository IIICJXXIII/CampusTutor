package com.campus.module.tutor.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.ResultCode;
import com.campus.module.tutor.dto.TutorCertRequest;
import com.campus.module.tutor.dto.TutorProfileUpdateRequest;
import com.campus.module.tutor.dto.TutorScheduleRequest;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.entity.TutorScheduleConfig;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import com.campus.module.tutor.mapper.TutorScheduleConfigMapper;
import com.campus.module.tutor.service.TutorProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.util.List;

/**
 * 教员档案Service实现
 */
@Service
@RequiredArgsConstructor
public class TutorProfileServiceImpl extends ServiceImpl<TutorProfileMapper, TutorProfile>
        implements TutorProfileService {

    private final TutorScheduleConfigMapper scheduleConfigMapper;
    private final com.campus.module.demand.service.GeoService geoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitCertification(Long userId, TutorCertRequest request) {
        // 检查是否已有档案
        TutorProfile existing = getByUserId(userId);

        TutorProfile targetProfile;
        if (existing != null) {
            // 已有档案，允许重复提交并直接通过
            existing.setRealName(request.getRealName());
            existing.setIdCard(request.getIdCard());
            existing.setIdCardFrontUrl(request.getIdCardFrontUrl());
            existing.setIdCardBackUrl(request.getIdCardBackUrl());
            existing.setUniversityName(request.getUniversityName());
            existing.setMajor(request.getMajor());
            existing.setEducation(request.getEducation());
            existing.setEnrollYear(request.getEnrollYear());
            existing.setStudentCardUrl(request.getStudentCardUrl());
            if (request.getCertificateUrls() != null) {
                existing.setCertificateUrls(JSONUtil.toJsonStr(request.getCertificateUrls()));
            }
            // 处理教学设置字段
            if (request.getTeachGrades() != null) {
                existing.setTeachGrades(JSONUtil.toJsonStr(request.getTeachGrades()));
            }
            if (request.getTeachSubjects() != null) {
                existing.setTeachSubjects(JSONUtil.toJsonStr(request.getTeachSubjects()));
            }
            if (request.getExpectPrice() != null) {
                existing.setExpectPrice(request.getExpectPrice());
            }
            // 开发阶段：直接通过审核
            existing.setCertStatus(2); // 已通过
            existing.setRejectReason(null);
            updateById(existing);
            targetProfile = existing;
        } else {
            // 新建档案
            TutorProfile profile = new TutorProfile();
            profile.setUserId(userId);
            profile.setRealName(request.getRealName());
            profile.setIdCard(request.getIdCard());
            profile.setIdCardFrontUrl(request.getIdCardFrontUrl());
            profile.setIdCardBackUrl(request.getIdCardBackUrl());
            profile.setUniversityName(request.getUniversityName());
            profile.setMajor(request.getMajor());
            profile.setEducation(request.getEducation());
            profile.setEnrollYear(request.getEnrollYear());
            profile.setStudentCardUrl(request.getStudentCardUrl());
            if (request.getCertificateUrls() != null) {
                profile.setCertificateUrls(JSONUtil.toJsonStr(request.getCertificateUrls()));
            }
            // 处理教学设置字段
            if (request.getTeachGrades() != null) {
                profile.setTeachGrades(JSONUtil.toJsonStr(request.getTeachGrades()));
            }
            if (request.getTeachSubjects() != null) {
                profile.setTeachSubjects(JSONUtil.toJsonStr(request.getTeachSubjects()));
            }
            if (request.getExpectPrice() != null) {
                profile.setExpectPrice(request.getExpectPrice());
            }
            // 开发阶段：直接通过审核
            profile.setCertStatus(2); // 已通过
            profile.setRating(new BigDecimal("5.0"));
            profile.setOrderCount(0);
            save(profile);
            targetProfile = profile;
        }

        // 同步位置信息到Redis (如果已有位置信息)
        if (targetProfile.getLongitude() != null && targetProfile.getLatitude() != null) {
            geoService.addTutorLocation(
                    targetProfile.getId(),
                    targetProfile.getLongitude().doubleValue(),
                    targetProfile.getLatitude().doubleValue());
        }
    }

    @Override
    public TutorProfile getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<TutorProfile>()
                .eq(TutorProfile::getUserId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, TutorProfileUpdateRequest request) {
        TutorProfile profile = getByUserId(userId);
        if (profile == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请先完成教员认证");
        }
        if (profile.getCertStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "认证未通过，无法更新档案");
        }

        // 更新字段
        if (request.getRealName() != null) {
            profile.setRealName(request.getRealName());
        }
        if (request.getUniversityName() != null) {
            profile.setUniversityName(request.getUniversityName());
        }
        if (request.getMajor() != null) {
            profile.setMajor(request.getMajor());
        }
        if (request.getTeachSubjects() != null) {
            profile.setTeachSubjects(JSONUtil.toJsonStr(request.getTeachSubjects()));
        }
        if (request.getTeachGrades() != null) {
            profile.setTeachGrades(JSONUtil.toJsonStr(request.getTeachGrades()));
        }
        if (request.getTeachStyle() != null) {
            profile.setTeachStyle(request.getTeachStyle());
        }
        if (request.getIntroduction() != null) {
            profile.setIntroduction(request.getIntroduction());
        }
        if (request.getExpectPrice() != null) {
            profile.setExpectPrice(request.getExpectPrice());
        }
        if (request.getCanVisit() != null) {
            profile.setCanVisit(request.getCanVisit());
        }
        if (request.getCanOnline() != null) {
            profile.setCanOnline(request.getCanOnline());
        }
        if (request.getLongitude() != null) {
            profile.setLongitude(request.getLongitude());
        }
        if (request.getLatitude() != null) {
            profile.setLatitude(request.getLatitude());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }

        updateById(profile);

        // 同步位置信息到Redis
        if (profile.getLongitude() != null && profile.getLatitude() != null) {
            geoService.addTutorLocation(
                    profile.getId(),
                    profile.getLongitude().doubleValue(),
                    profile.getLatitude().doubleValue());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveScheduleConfig(Long userId, TutorScheduleRequest request) {
        TutorProfile profile = getByUserId(userId);
        if (profile == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请先完成教员认证");
        }

        // 删除旧配置
        scheduleConfigMapper.delete(new LambdaQueryWrapper<TutorScheduleConfig>()
                .eq(TutorScheduleConfig::getTutorId, profile.getId()));

        // 保存新配置
        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            for (TutorScheduleRequest.ScheduleItem item : request.getSchedules()) {
                TutorScheduleConfig config = new TutorScheduleConfig();
                config.setTutorId(profile.getId());
                config.setDayOfWeek(item.getDayOfWeek());
                config.setStartTime(item.getStartTime());
                config.setEndTime(item.getEndTime());
                config.setAvailable(item.getAvailable() != null ? item.getAvailable() : 1);
                scheduleConfigMapper.insert(config);
            }
        }
    }

    @Override
    public List<?> getScheduleConfig(Long tutorId) {
        return scheduleConfigMapper.selectList(new LambdaQueryWrapper<TutorScheduleConfig>()
                .eq(TutorScheduleConfig::getTutorId, tutorId)
                .orderByAsc(TutorScheduleConfig::getDayOfWeek)
                .orderByAsc(TutorScheduleConfig::getStartTime));
    }

    @Override
    public Map<String, Object> getCertificationStatus(Long userId) {
        Map<String, Object> status = new HashMap<>();
        TutorProfile profile = getByUserId(userId);

        if (profile == null) {
            status.put("status", 0); // 未提交
            status.put("statusText", "未提交认证");
            status.put("canTeach", false);
        } else {
            status.put("status", profile.getCertStatus());
            status.put("statusText", getCertStatusText(profile.getCertStatus()));
            status.put("canTeach", profile.getCertStatus() == 2); // 已通过
            status.put("profileId", profile.getId());
            status.put("lastUpdateTime", profile.getUpdateTime());
        }

        return status;
    }

    @Override
    public Map<String, Object> getCertificationProgress(Long userId) {
        Map<String, Object> progress = new HashMap<>();
        TutorProfile profile = getByUserId(userId);

        if (profile == null) {
            progress.put("step", 0);
            progress.put("totalSteps", 3);
            progress.put("currentStep", "填写基本信息");
            progress.put("progress", 0);
        } else {
            // 检查认证所需字段
            int completedFields = 0;
            int totalFields = 8; // 基本信息、身份证、学生证、学历、专业、学校、照片等

            if (profile.getRealName() != null)
                completedFields++;
            if (profile.getIdCard() != null)
                completedFields++;
            if (profile.getIdCardFrontUrl() != null)
                completedFields++;
            if (profile.getIdCardBackUrl() != null)
                completedFields++;
            if (profile.getUniversityName() != null)
                completedFields++;
            if (profile.getMajor() != null)
                completedFields++;
            if (profile.getEducation() != null)
                completedFields++;
            if (profile.getStudentCardUrl() != null)
                completedFields++;

            int progressPercent = (completedFields * 100) / totalFields;

            progress.put("step", 1);
            progress.put("totalSteps", 3);
            progress.put("currentStep", "审核中");
            progress.put("progress", progressPercent);
            progress.put("completedFields", completedFields);
            progress.put("totalFields", totalFields);
        }

        return progress;
    }

    /**
     * 获取认证状态文本
     */
    private String getCertStatusText(Integer status) {
        switch (status) {
            case 0:
                return "未提交";
            case 1:
                return "审核中";
            case 2:
                return "已通过";
            case 3:
                return "已拒绝";
            default:
                return "未知状态";
        }
    }
}
