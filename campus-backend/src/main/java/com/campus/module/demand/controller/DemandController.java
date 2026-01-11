package com.campus.module.demand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.module.demand.dto.DemandPostRequest;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.demand.service.DemandPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 需求模块控制器
 */
@Tag(name = "需求模块", description = "需求发布、管理、搜索")
@RestController
@RequestMapping("/api/demand")
@RequiredArgsConstructor
public class DemandController {

    private final DemandPostService demandPostService;

    @Operation(summary = "发布需求")
    @PostMapping("/publish")
    public Result<Long> publish(@Valid @RequestBody DemandPostRequest request) {
        Long publisherId = UserContext.getUserId();
        Long demandId = demandPostService.publishDemand(publisherId, request);
        return Result.success(demandId);
    }

    @Operation(summary = "更新需求")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody DemandPostRequest request) {
        Long publisherId = UserContext.getUserId();
        demandPostService.updateDemand(publisherId, request);
        return Result.success();
    }

    @Operation(summary = "上架需求")
    @PostMapping("/{id}/online")
    public Result<Void> online(@PathVariable Long id) {
        Long publisherId = UserContext.getUserId();
        demandPostService.changeStatus(publisherId, id, 1);
        return Result.success();
    }

    @Operation(summary = "下架需求")
    @PostMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        Long publisherId = UserContext.getUserId();
        demandPostService.changeStatus(publisherId, id, 0);
        return Result.success();
    }

    @Operation(summary = "删除需求")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long publisherId = UserContext.getUserId();
        demandPostService.deleteDemand(publisherId, id);
        return Result.success();
    }

    @Operation(summary = "我发布的需求列表")
    @GetMapping("/my")
    public Result<List<DemandPost>> myDemands() {
        Long publisherId = UserContext.getUserId();
        List<DemandPost> list = demandPostService.listMyDemands(publisherId);
        return Result.success(list);
    }

    @Operation(summary = "需求详情")
    @GetMapping("/detail/{id}")
    public Result<DemandPost> detail(@PathVariable Long id) {
        DemandPost demand = demandPostService.getById(id);
        return Result.success(demand);
    }

    @Operation(summary = "分页查询需求列表(公开)")
    @GetMapping("/list")
    public Result<IPage<DemandPost>> list(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<DemandPost> result = demandPostService.pageList(subject, grade, page, size);
        return Result.success(result);
    }

    @Operation(summary = "附近需求搜索(LBS)")
    @GetMapping("/nearby")
    public Result<List<DemandPost>> nearby(
            @RequestParam Double longitude,
            @RequestParam Double latitude,
            @RequestParam(defaultValue = "10") Double radius) {
        List<DemandPost> list = demandPostService.searchNearby(longitude, latitude, radius);
        return Result.success(list);
    }

    @Operation(summary = "获取带有匹配度的需求列表（教师端专用）")
    @GetMapping("/list-with-match")
    public Result<IPage<DemandPost>> listWithMatchScore(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double latitude,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        Long tutorId = UserContext.getUserId();
        IPage<DemandPost> result = demandPostService.pageListWithMatchScore(
                tutorId, subject, grade, longitude, latitude, page, size, sortBy, sortOrder);
        return Result.success(result);
    }
}
