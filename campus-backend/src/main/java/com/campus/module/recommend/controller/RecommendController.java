package com.campus.module.recommend.controller;

import com.campus.common.result.Result;
import com.campus.module.recommend.dto.SimilarTutorDTO;
import com.campus.module.recommend.service.CollaborativeFilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "推荐服务", description = "协同过滤推荐相关接口")
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final CollaborativeFilterService collaborativeFilterService;

    @Operation(summary = "获取相似教员推荐")
    @GetMapping("/similar/{tutorId}")
    public Result<List<SimilarTutorDTO>> getSimilarTutors(
            @PathVariable Long tutorId,
            @RequestParam(defaultValue = "6") int limit) {
        List<SimilarTutorDTO> similarTutors = collaborativeFilterService.getSimilarTutors(tutorId, limit);
        return Result.success(similarTutors);
    }

    @Operation(summary = "获取个性化推荐")
    @GetMapping("/personalized")
    public Result<List<SimilarTutorDTO>> getPersonalizedRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = com.campus.common.context.UserContext.getUserId();
        List<SimilarTutorDTO> recommendations = collaborativeFilterService.getRecommendationsForUser(userId, limit);
        return Result.success(recommendations);
    }
}
