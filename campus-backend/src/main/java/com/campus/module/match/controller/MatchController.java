package com.campus.module.match.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.module.match.dto.TutorSearchRequest;
import com.campus.module.match.dto.TutorSearchResult;
import com.campus.module.match.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 匹配搜索控制器
 */
@Tag(name = "匹配模块", description = "教员搜索、匹配")
@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @Operation(summary = "搜索教员")
    @PostMapping("/tutors")
    public Result<IPage<TutorSearchResult>> searchTutors(@RequestBody TutorSearchRequest request) {
        // 设置当前用户ID，用于个性化推荐(协同过滤)
        request.setUserId(UserContext.getUserId());
        IPage<TutorSearchResult> result = matchService.searchTutors(request);
        return Result.success(result);
    }

    @Operation(summary = "搜索教员(GET简化版)")
    @GetMapping("/tutors")
    public Result<IPage<TutorSearchResult>> searchTutorsGet(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double radius,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        TutorSearchRequest request = new TutorSearchRequest();
        // 设置当前用户ID
        request.setUserId(UserContext.getUserId());

        request.setSubject(subject);
        request.setGrade(grade);
        request.setLongitude(longitude);
        request.setLatitude(latitude);
        request.setRadius(radius);
        request.setSortBy(sortBy);
        request.setSortOrder(sortOrder);
        request.setPage(page);
        request.setSize(size);

        IPage<TutorSearchResult> result = matchService.searchTutors(request);
        return Result.success(result);
    }
}
