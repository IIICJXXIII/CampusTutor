package com.campus.module.parent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.demand.mapper.DemandPostMapper;
import com.campus.module.order.entity.CourseOrder;
import com.campus.module.order.mapper.CourseOrderMapper;
import com.campus.module.parent.dto.StudentRequest;
import com.campus.module.parent.entity.ParentStudent;
import com.campus.module.parent.service.ParentStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "家长模块", description = "学生信息管理")
@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ParentController {

    private final ParentStudentService parentStudentService;
    private final DemandPostMapper demandPostMapper;
    private final CourseOrderMapper courseOrderMapper;

    @Operation(summary = "添加学生")
    @PostMapping("/student")
    public Result<Long> addStudent(@Valid @RequestBody StudentRequest request) {
        Long parentId = UserContext.getUserId();
        Long studentId = parentStudentService.addStudent(parentId, request);
        return Result.success(studentId);
    }

    @Operation(summary = "更新学生信息")
    @PutMapping("/student")
    public Result<Void> updateStudent(@Valid @RequestBody StudentRequest request) {
        Long parentId = UserContext.getUserId();
        parentStudentService.updateStudent(parentId, request);
        return Result.success();
    }

    @Operation(summary = "删除学生")
    @DeleteMapping("/student/{id}")
    public Result<Void> deleteStudent(@PathVariable Long id) {
        Long parentId = UserContext.getUserId();
        parentStudentService.deleteStudent(parentId, id);
        return Result.success();
    }

    @Operation(summary = "获取我的学生列表")
    @GetMapping("/students")
    public Result<List<ParentStudent>> listStudents() {
        Long parentId = UserContext.getUserId();
        List<ParentStudent> list = parentStudentService.listByParentId(parentId);
        return Result.success(list);
    }

    @Operation(summary = "获取学生详情")
    @GetMapping("/student/{id}")
    public Result<ParentStudent> getStudent(@PathVariable Long id) {
        ParentStudent student = parentStudentService.getById(id);
        return Result.success(student);
    }

    @Operation(summary = "获取家长端统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Long parentId = UserContext.getUserId();
        Map<String, Object> stats = new HashMap<>();

        Long studentCount = parentStudentService.count(
            new LambdaQueryWrapper<ParentStudent>().eq(ParentStudent::getParentId, parentId)
        );
        stats.put("studentCount", studentCount);

        Long demandCount = demandPostMapper.selectCount(
            new LambdaQueryWrapper<DemandPost>().eq(DemandPost::getPublisherId, parentId)
        );
        stats.put("demandCount", demandCount);

        Long orderCount = courseOrderMapper.selectCount(
            new LambdaQueryWrapper<CourseOrder>().eq(CourseOrder::getParentId, parentId)
        );
        stats.put("orderCount", orderCount);

        return Result.success(stats);
    }
}
