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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitCertification(Long userId, TutorCertRequest request) {
        // 检查是否已有档案
        TutorProfile existing = getByUserId(userId);
        
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
            // 开发阶段：直接通过审核
            existing.setCertStatus(2); // 已通过
            existing.setRejectReason(null);
            updateById(existing);
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
            // 开发阶段：直接通过审核
            profile.setCertStatus(2); // 已通过
            profile.setRating(new BigDecimal("5.0"));
            profile.setOrderCount(0);
            save(profile);
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
}
