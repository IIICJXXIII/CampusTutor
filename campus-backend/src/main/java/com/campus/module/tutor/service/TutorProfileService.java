package com.campus.module.tutor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.tutor.dto.TutorCertRequest;
import com.campus.module.tutor.dto.TutorProfileUpdateRequest;
import com.campus.module.tutor.dto.TutorScheduleRequest;
import com.campus.module.tutor.entity.TutorProfile;

import java.util.List;
import java.util.Map;

/**
 * 教员档案Service
 */
public interface TutorProfileService extends IService<TutorProfile> {

    /**
     * 提交教员认证
     * @param userId 用户ID
     * @param request 认证请求
     */
    void submitCertification(Long userId, TutorCertRequest request);

    /**
     * 获取当前用户的教员档案
     * @param userId 用户ID
     * @return 教员档案
     */
    TutorProfile getByUserId(Long userId);

    /**
     * 更新教员档案
     * @param userId 用户ID
     * @param request 更新请求
     */
    void updateProfile(Long userId, TutorProfileUpdateRequest request);

    /**
     * 保存时间配置
     * @param userId 用户ID
     * @param request 时间配置请求
     */
    void saveScheduleConfig(Long userId, TutorScheduleRequest request);

    /**
     * 获取教员时间配置
     * @param tutorId 教员档案ID
     * @return 时间配置列表
     */
    List<?> getScheduleConfig(Long tutorId);

    /**
     * 获取认证状态
     * @param userId 用户ID
     * @return 认证状态信息
     */
    Map<String, Object> getCertificationStatus(Long userId);

    /**
     * 获取认证进度
     * @param userId 用户ID
     * @return 认证进度信息
     */
    Map<String, Object> getCertificationProgress(Long userId);
}
