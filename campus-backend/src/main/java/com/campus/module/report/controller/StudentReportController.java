package com.campus.module.report.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.module.report.entity.StudentReport;
import com.campus.module.report.service.StudentReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学生报告")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class StudentReportController {

    private final StudentReportService studentReportService;

    @Operation(summary = "获取学生报告列表")
    @GetMapping("/list")
    public Result<IPage<StudentReport>> list(
            @RequestParam Long studentId,
            @RequestParam(required = false) Integer reportType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<StudentReport> result = studentReportService.listByStudentId(studentId, reportType, page, size);
        return Result.success(result);
    }

    @Operation(summary = "获取报告详情")
    @GetMapping("/{id}")
    public Result<StudentReport> getDetail(@PathVariable Long id) {
        StudentReport report = studentReportService.getById(id);
        return Result.success(report);
    }

    @Operation(summary = "根据订单获取报告")
    @GetMapping("/order/{orderId}")
    public Result<StudentReport> getByOrder(@PathVariable Long orderId) {
        StudentReport report = studentReportService.getByOrderId(orderId);
        return Result.success(report);
    }

    @Operation(summary = "创建学生报告(教师端)")
    @PostMapping
    public Result<StudentReport> create(@RequestBody StudentReport report) {
        StudentReport created = studentReportService.createReport(report);
        return Result.success(created);
    }
}
