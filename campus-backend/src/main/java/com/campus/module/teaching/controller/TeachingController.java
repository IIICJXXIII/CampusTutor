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
import com.campus.module.order.service.CourseOrderService;
import com.campus.module.order.entity.CourseOrder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.ResultCode;

@Tag(name = "课时打卡", description = "教师打卡、家长确认相关接口")
@RestController
@RequestMapping("/api/teaching")
@RequiredArgsConstructor
public class TeachingController {

    private final TeachingRecordService teachingRecordService;
    private final CourseOrderService orderService;

    private Long resolveOrderId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单ID不能为空");
        }
        if (id.startsWith("CT")) {
            CourseOrder order = orderService.getOne(new LambdaQueryWrapper<CourseOrder>()
                    .eq(CourseOrder::getOrderNo, id));
            if (order == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单不存在");
            }
            return order.getId();
        }
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无效的订单ID格式");
        }
    }

    @Operation(summary = "教师打卡上课")
    @PostMapping("/check-in")
    public Result<Long> checkIn(@Valid @RequestBody CheckInRequest request) { // <--- 关键：必须有 @RequestBody
        Long userId = UserContext.getUserId();
        Long recordId = teachingRecordService.checkIn(userId, request);
        return Result.success("打卡成功", recordId);
    }

    @Operation(summary = "教师打卡下课")
    @PostMapping("/check-out/{recordId}")
    public Result<Void> checkOut(
            @PathVariable Long recordId,
            @RequestParam(required = false) String contentSummary,
            @RequestParam(required = false) String homeworkAssigned) {
        Long userId = UserContext.getUserId();
        teachingRecordService.checkOut(userId, recordId, contentSummary, homeworkAssigned);
        return Result.success();
    }

    @Operation(summary = "家长确认课时")
    @PostMapping("/confirm/{recordId}")
    public Result<Void> confirm(@PathVariable Long recordId) {
        Long userId = UserContext.getUserId();
        teachingRecordService.confirmByParent(userId, recordId);
        return Result.success("课时已确认");
    }

    @Operation(summary = "家长申诉")
    @PostMapping("/dispute/{recordId}")
    public Result<Void> dispute(
            @PathVariable Long recordId,
            @RequestParam String reason) {
        Long userId = UserContext.getUserId();
        teachingRecordService.disputeByParent(userId, recordId, reason);
        return Result.success("申诉已提交");
    }

    @Operation(summary = "获取订单课时记录")
    @GetMapping("/records/{orderId}")
    public Result<List<TeachingRecordDTO>> getRecordsByOrderId(@PathVariable String orderId) {
        List<TeachingRecordDTO> records = teachingRecordService.getRecordsByOrderId(resolveOrderId(orderId));
        return Result.success(records);
    }

    @Operation(summary = "获取课时详情")
    @GetMapping("/record/{recordId}")
    public Result<TeachingRecordDTO> getRecordById(@PathVariable Long recordId) {
        TeachingRecordDTO record = teachingRecordService.getRecordById(recordId);
        return Result.success(record);
    }

    @Operation(summary = "获取我的课时记录")
    @GetMapping("/my-records")
    public Result<List<TeachingRecordDTO>> getMyRecords() {
        Long userId = UserContext.getUserId();
        Integer role = UserContext.getRole();
        List<TeachingRecordDTO> records = teachingRecordService.getRecordsByUserId(userId, role);
        return Result.success(records);
    }
    
    @Operation(summary = "更新课时进度")
    @PostMapping("/update-progress/{recordId}")
    public Result<Void> updateProgress(
            @PathVariable Long recordId,
            @RequestParam Integer progress,
            @RequestParam(required = false) String notes) {
        Long userId = UserContext.getUserId();
        teachingRecordService.updateLessonProgress(userId, recordId, progress, notes);
        return Result.success("进度更新成功");
    }
    
    @Operation(summary = "获取课程统计信息")
    @GetMapping("/statistics/{orderId}")
    public Result<java.util.Map<String, Object>> getCourseStatistics(@PathVariable String orderId) {
        Long userId = UserContext.getUserId();
        // TODO: 权限校验
        java.util.Map<String, Object> statistics = teachingRecordService.getCourseStatistics(resolveOrderId(orderId));
        return Result.success(statistics);
    }
}