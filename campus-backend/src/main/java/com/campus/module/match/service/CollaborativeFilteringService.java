package com.campus.module.match.service;

import com.campus.module.match.dto.CFRecommendation;
import com.campus.module.match.dto.UserSimilarity;

import java.util.List;
import java.util.Map;

/**
 * 协同过滤服务接口
 * 基于用户的协同过滤(User-Based CF)算法实现
 */
public interface CollaborativeFilteringService {

    /**
     * 构建用户的隐式评分向量
     * 基于 user_action_log 和 course_order 数据
     *
     * @param userId 用户ID
     * @return Map<教员ID, 隐式评分>，评分范围 0.0 ~ 1.0
     */
    Map<Long, Double> buildUserRatingVector(Long userId);

    /**
     * 计算两个用户之间的相似度
     * 使用余弦相似度算法
     *
     * @param userA 用户A的ID
     * @param userB 用户B的ID
     * @return 相似度结果，包含相似度值和共同交互数
     */
    UserSimilarity calculateSimilarity(Long userA, Long userB);

    /**
     * 查找与目标用户最相似的K个用户
     *
     * @param userId 目标用户ID
     * @param topK   返回的相似用户数量
     * @return 按相似度降序排列的相似用户列表
     */
    List<UserSimilarity> findSimilarUsers(Long userId, int topK);

    /**
     * 预测用户对某教员的评分
     * 基于相似用户的加权平均
     *
     * @param userId  用户ID
     * @param tutorId 教员ID
     * @return 预测评分 (0.0 ~ 1.0)，如无法预测返回null
     */
    Double predictScore(Long userId, Long tutorId);

    /**
     * 获取用户的Top-N推荐教员列表
     *
     * @param userId 用户ID
     * @param n      推荐数量
     * @return 推荐结果列表
     */
    List<CFRecommendation> getRecommendations(Long userId, int n);

    /**
     * 判断用户是否满足CF条件（非冷启动）
     *
     * @param userId 用户ID
     * @return true表示有足够的历史数据支持CF
     */
    boolean hasEnoughHistory(Long userId);

    /**
     * 刷新用户相似度缓存
     *
     * @param userId 用户ID，如为null则刷新全部
     */
    void refreshSimilarityCache(Long userId);

    /**
     * 批量预测用户对多个教员的评分
     * 优化性能，避免重复计算相似用户
     *
     * @param userId   用户ID
     * @param tutorIds 教员ID列表
     * @return Map<教员ID, 预测评分>
     */
    Map<Long, Double> batchPredictScores(Long userId, List<Long> tutorIds);
}
