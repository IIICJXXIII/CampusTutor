package com.campus.module.recommend.controller;

import com.campus.common.result.Result;
import com.campus.common.utils.JwtUtils;
import com.campus.module.recommend.dto.SimilarTutorDTO;
import com.campus.module.recommend.service.CollaborativeFilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐服务控制器
 * 提供协同过滤推荐 API
 */
@Tag(name = "推荐服务", description = "协同过滤推荐相关接口")
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final CollaborativeFilterService collaborativeFilterService;
    private final JwtUtils jwtUtils;

    /**
     * 获取相似教员推荐
     * "看过这个教员的用户还看过..."
     */
    @Operation(summary = "获取相似教员推荐")
    @GetMapping("/similar/{tutorId}")
    public Result<List<SimilarTutorDTO>> getSimilarTutors(
            @PathVariable Long tutorId,
            @RequestParam(defaultValue = "6") int limit) {
        List<SimilarTutorDTO> similarTutors = collaborativeFilterService.getSimilarTutors(tutorId, limit);
        return Result.success(similarTutors);
    }

    /**
     * 获取个性化推荐
     * 基于用户历史行为的推荐
     */
    @Operation(summary = "获取个性化推荐")
    @GetMapping("/personalized")
    public Result<List<SimilarTutorDTO>> getPersonalizedRecommendations(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
        List<SimilarTutorDTO> recommendations = collaborativeFilterService.getRecommendationsForUser(userId, limit);
        return Result.success(recommendations);
    }

    /**
     * 清除推荐缓存（管理员接口）
     */
    @Operation(summary = "清除推荐缓存")
    @DeleteMapping("/cache/{tutorId}")
    public Result<Void> clearCache(@PathVariable(required = false) Long tutorId) {
        collaborativeFilterService.clearSimilarityCache(tutorId);
        return Result.success();
    }

    /**
     * 清除所有推荐缓存（管理员接口）
     */
    @Operation(summary = "清除所有推荐缓存")
    @DeleteMapping("/cache")
    public Result<Void> clearAllCache() {
        collaborativeFilterService.clearSimilarityCache(null);
        return Result.success();
    }
}
