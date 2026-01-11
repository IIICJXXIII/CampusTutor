package com.campus.module.behavior.service;

import com.campus.module.behavior.dto.TutorBehaviorStats;

/**
 * 用户行为服务接口
 */
public interface BehaviorService {

    /**
     * 记录用户行为
     *
     * @param userId     用户ID（家长）
     * @param targetId   目标ID（教员ID）
     * @param actionType 行为类型：1-查看详情, 2-搜索, 3-收藏, 4-聊天, 5-下单
     * @param duration   停留时长(秒)，可为null
     */
    void recordAction(Long userId, Long targetId, Integer actionType, Integer duration);

    /**
     * 获取教员的行为统计信息
     *
     * @param tutorId 教员ID
     * @return 行为统计结果
     */
    TutorBehaviorStats getTutorStats(Long tutorId);

    /**
     * 获取用户的搜索次数
     *
     * @param userId 用户ID
     * @return 搜索次数
     */
    int getUserSearchCount(Long userId);

    /**
     * 计算教员热度分
     *
     * @param stats 行为统计
     * @return 热度分(0-100)
     */
    double calculateHotnessScore(TutorBehaviorStats stats);
}
