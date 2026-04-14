package com.campus.module.tutor.controller;

import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.module.tutor.dto.TutorCertRequest;
import com.campus.module.tutor.dto.TutorProfileUpdateRequest;
import com.campus.module.tutor.dto.TutorScheduleRequest;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.service.TutorProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 教员模块控制器
 */
@Tag(name = "教员模块", description = "教员认证、档案、时间配置")
@RestController
@RequestMapping("/api/tutor")
@RequiredArgsConstructor
public class TutorController {

    private final TutorProfileService tutorProfileService;

    @Operation(summary = "提交教员认证")
    @PostMapping("/certification")
    public Result<Void> submitCertification(@Valid @RequestBody TutorCertRequest request) {
        Long userId = UserContext.getUserId();
        tutorProfileService.submitCertification(userId, request);
        return Result.success();
    }

    @Operation(summary = "获取教员认证状态")
    @GetMapping("/certification")
    public Result<TutorProfile> getCertification() {
        Long userId = UserContext.getUserId();
        TutorProfile profile = tutorProfileService.getByUserId(userId);
        return Result.success(profile);
    }

    @Operation(summary = "获取当前教员档案")
    @GetMapping("/profile")
    public Result<TutorProfile> getProfile() {
        Long userId = UserContext.getUserId();
        TutorProfile profile = tutorProfileService.getByUserId(userId);
        return Result.success(profile);
    }

    @Operation(summary = "更新教员档案")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody TutorProfileUpdateRequest request) {
        Long userId = UserContext.getUserId();
        tutorProfileService.updateProfile(userId, request);
        return Result.success();
    }

    @Operation(summary = "保存时间配置")
    @PostMapping("/schedule")
    public Result<Void> saveSchedule(@RequestBody TutorScheduleRequest request) {
        Long userId = UserContext.getUserId();
        tutorProfileService.saveScheduleConfig(userId, request);
        return Result.success();
    }

    @Operation(summary = "获取时间配置")
    @GetMapping("/schedule")
    public Result<?> getSchedule() {
        Long userId = UserContext.getUserId();
        TutorProfile profile = tutorProfileService.getByUserId(userId);
        if (profile == null) {
            return Result.success(null);
        }
        return Result.success(tutorProfileService.getScheduleConfig(profile.getId()));
    }

    @Operation(summary = "获取指定教师的时间配置（公开）")
    @GetMapping("/public/schedule/{tutorId}")
    public Result<?> getPublicSchedule(@PathVariable Long tutorId) {
        try {
            var scheduleList = tutorProfileService.getScheduleConfig(tutorId);
            // 即使为空也返回空列表，不返回404
            return Result.success(scheduleList != null ? scheduleList : java.util.Collections.emptyList());
        } catch (Exception e) {
            // 捕获任何异常，返回空列表
            return Result.success(java.util.Collections.emptyList());
        }
    }

    @Operation(summary = "根据用户ID获取教员档案(公开)")
    @GetMapping("/public/{userId}")
    public Result<TutorProfile> getPublicProfileByUserId(@PathVariable Long userId) {
        TutorProfile profile = tutorProfileService.getByUserId(userId);
        return Result.success(profile);
    }

    @Operation(summary = "根据ID获取教员档案(公开)")
    @GetMapping("/public/profile/{id}")
    public Result<TutorProfile> getById(@PathVariable Long id) {
        TutorProfile profile = tutorProfileService.getById(id);
        return Result.success(profile);
    }

    @Operation(summary = "获取认证状态")
    @GetMapping("/certification/status")
    public Result<Map<String, Object>> getCertificationStatus() {
        Long userId = UserContext.getUserId();
        Map<String, Object> status = tutorProfileService.getCertificationStatus(userId);
        return Result.success(status);
    }

    @Operation(summary = "获取认证进度")
    @GetMapping("/certification/progress")
    public Result<Map<String, Object>> getCertificationProgress() {
        Long userId = UserContext.getUserId();
        Map<String, Object> progress = tutorProfileService.getCertificationProgress(userId);
        return Result.success(progress);
    }
}
