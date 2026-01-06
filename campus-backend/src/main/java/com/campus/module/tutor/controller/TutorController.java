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

    @Operation(summary = "根据ID获取教员档案(公开)")
    @GetMapping("/public/{id}")
    public Result<TutorProfile> getById(@PathVariable Long id) {
        TutorProfile profile = tutorProfileService.getById(id);
        return Result.success(profile);
    }
}
