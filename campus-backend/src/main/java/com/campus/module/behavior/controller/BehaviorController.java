package com.campus.module.behavior.controller;

import com.campus.common.result.Result;
import com.campus.common.utils.JwtUtils;
import com.campus.module.behavior.dto.BehaviorRecordRequest;
import com.campus.module.behavior.dto.TutorBehaviorStats;
import com.campus.module.behavior.service.BehaviorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户行为上报接口
 */
@Tag(name = "行为追踪", description = "用户行为数据上报接口")
@RestController
@RequestMapping("/api/behavior")
@RequiredArgsConstructor
public class BehaviorController {

    private final BehaviorService behaviorService;
    private final JwtUtils jwtUtils;

    /**
     * 记录查看教员详情行为
     */
    @Operation(summary = "记录查看行为")
    @PostMapping("/view")
    public Result<Void> recordView(@RequestBody BehaviorRecordRequest request,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        behaviorService.recordAction(userId, request.getTargetId(), 1, request.getDuration());
        return Result.success();
    }

    /**
     * 记录收藏教员行为
     */
    @Operation(summary = "记录收藏行为")
    @PostMapping("/favorite")
    public Result<Void> recordFavorite(@RequestBody BehaviorRecordRequest request,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        behaviorService.recordAction(userId, request.getTargetId(), 3, null);
        return Result.success();
    }

    /**
     * 记录发起聊天行为
     */
    @Operation(summary = "记录聊天行为")
    @PostMapping("/chat")
    public Result<Void> recordChat(@RequestBody BehaviorRecordRequest request,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        behaviorService.recordAction(userId, request.getTargetId(), 4, null);
        return Result.success();
    }

    /**
     * 记录搜索行为
     */
    @Operation(summary = "记录搜索行为")
    @PostMapping("/search")
    public Result<Void> recordSearch(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        behaviorService.recordAction(userId, null, 2, null);
        return Result.success();
    }

    /**
     * 获取教员热度统计（管理员/调试用）
     */
    @Operation(summary = "获取教员热度统计")
    @GetMapping("/tutor-stats/{tutorId}")
    public Result<TutorBehaviorStats> getTutorStats(@PathVariable Long tutorId) {
        return Result.success(behaviorService.getTutorStats(tutorId));
    }
}
