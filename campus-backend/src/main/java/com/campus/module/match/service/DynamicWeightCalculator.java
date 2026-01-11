package com.campus.module.match.service;

import com.campus.module.behavior.service.BehaviorService;
import com.campus.module.match.dto.WeightConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 动态权重计算器
 * 根据用户成熟度（搜索次数）动态调整推荐权重
 */
@Component
@RequiredArgsConstructor
public class DynamicWeightCalculator {

    private final BehaviorService behaviorService;

    // 用户成熟度阈值
    private static final int NEW_USER_THRESHOLD = 3; // 搜索次数 < 3 为新用户
    private static final int ACTIVE_USER_THRESHOLD = 10; // 搜索次数 >= 10 为活跃用户

    /**
     * 根据用户ID获取动态权重配置
     *
     * @param userId 用户ID
     * @return 权重配置
     */
    public WeightConfig getWeightsForUser(Long userId) {
        if (userId == null) {
            return WeightConfig.defaultConfig();
        }

        int searchCount = behaviorService.getUserSearchCount(userId);
        return getWeightsBySearchCount(searchCount);
    }

    /**
     * 根据搜索次数获取权重配置
     *
     * @param searchCount 搜索次数
     * @return 权重配置
     */
    public WeightConfig getWeightsBySearchCount(int searchCount) {
        if (searchCount < NEW_USER_THRESHOLD) {
            // 新用户：更依赖评分和学历
            // 理由：新用户缺乏历史行为数据，需要依靠教员的硬指标
            return WeightConfig.newUserConfig();
        } else if (searchCount >= ACTIVE_USER_THRESHOLD) {
            // 活跃用户：更依赖热度和行为数据
            // 理由：活跃用户有足够的行为数据支撑推荐
            return WeightConfig.activeUserConfig();
        } else {
            // 普通用户：使用平衡配置
            return WeightConfig.defaultConfig();
        }
    }

    /**
     * 获取用户成熟度级别描述
     *
     * @param searchCount 搜索次数
     * @return 成熟度描述
     */
    public String getUserMaturityLevel(int searchCount) {
        if (searchCount < NEW_USER_THRESHOLD) {
            return "NEW";
        } else if (searchCount >= ACTIVE_USER_THRESHOLD) {
            return "ACTIVE";
        } else {
            return "NORMAL";
        }
    }
}
