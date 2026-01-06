package com.campus.module.teaching.controller;

import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.module.teaching.dto.CheckInRequest;
import com.campus.module.teaching.dto.TeachingRecordDTO;
import com.campus.module.teaching.service.TeachingRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课时打卡控制器
 */
@Tag(name = "课时打卡", description = "教师打卡、家长确认相关接口")
@RestController
@RequestMapping("/api/teaching")
@RequiredArgsConstructor
public class TeachingController {

    private final TeachingRecordService teachingRecordService;

    @Operation(summary = "教师打卡上课", description = "教师开始上课时打卡，需要GPS定位和拍照")
    @PostMapping("/check-in")
    public Result<Long> checkIn(@Valid @RequestBody CheckInRequest request) {
        Long userId = UserContext.getUserId();
        Long recordId = teachingRecordService.checkIn(userId, request);
        return Result.success("打卡成功", recordId);
    }

    @Operation(summary = "教师打卡下课", description = "教师结束上课时打卡，可填写教学内容和作业")
    @PostMapping("/check-out/{recordId}")
    public Result<Void> checkOut(
            @Parameter(description = "课时记录ID") @PathVariable Long recordId,
            @Parameter(description = "教学内容摘要") @RequestParam(required = false) String contentSummary,
            @Parameter(description = "布置作业") @RequestParam(required = false) String homeworkAssigned) {
        Long userId = UserContext.getUserId();
        teachingRecordService.checkOut(userId, recordId, contentSummary, homeworkAssigned);
        return Result.success("下课打卡成功");
    }

    @Operation(summary = "家长确认课时", description = "家长确认教师完成了本节课")
    @PostMapping("/confirm/{recordId}")
    public Result<Void> confirm(
            @Parameter(description = "课时记录ID") @PathVariable Long recordId) {
        Long userId = UserContext.getUserId();
        teachingRecordService.confirmByParent(userId, recordId);
        return Result.success("确认成功");
    }

    @Operation(summary = "家长申诉课时", description = "家长对课时有异议时发起申诉")
    @PostMapping("/dispute/{recordId}")
    public Result<Void> dispute(
            @Parameter(description = "课时记录ID") @PathVariable Long recordId,
            @Parameter(description = "申诉原因") @RequestParam String reason) {
        Long userId = UserContext.getUserId();
        teachingRecordService.disputeByParent(userId, recordId, reason);
        return Result.success("申诉已提交");
    }

    @Operation(summary = "获取订单课时记录", description = "获取指定订单的所有课时记录")
    @GetMapping("/records/{orderId}")
    public Result<List<TeachingRecordDTO>> getRecords(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        List<TeachingRecordDTO> records = teachingRecordService.getRecordsByOrderId(orderId);
        return Result.success(records);
    }

    @Operation(summary = "获取课时记录详情", description = "获取单条课时记录详情")
    @GetMapping("/record/{recordId}")
    public Result<TeachingRecordDTO> getRecord(
            @Parameter(description = "课时记录ID") @PathVariable Long recordId) {
        TeachingRecordDTO record = teachingRecordService.getRecordById(recordId);
        return Result.success(record);
    }
}
