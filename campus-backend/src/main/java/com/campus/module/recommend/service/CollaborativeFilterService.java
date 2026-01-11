package com.campus.module.recommend.service;

import com.campus.module.recommend.dto.SimilarTutorDTO;

import java.util.List;

/**
 * 协同过滤推荐服务接口
 */
public interface CollaborativeFilterService {

    /**
     * 获取与目标教员相似的教员列表
     * 基于 Item-Based 协同过滤，使用余弦相似度
     *
     * @param tutorId 目标教员ID
     * @param limit   返回数量限制
     * @return 相似教员列表，按相似度降序排列
     */
    List<SimilarTutorDTO> getSimilarTutors(Long tutorId, int limit);

    /**
     * 获取用户可能感兴趣的教员（基于用户历史行为）
     * "看过这些教员的用户还看过..."
     *
     * @param userId 用户ID
     * @param limit  返回数量限制
     * @return 推荐教员列表
     */
    List<SimilarTutorDTO> getRecommendationsForUser(Long userId, int limit);

    /**
     * 清除相似度缓存
     *
     * @param tutorId 教员ID，null则清除所有缓存
     */
    void clearSimilarityCache(Long tutorId);
}
