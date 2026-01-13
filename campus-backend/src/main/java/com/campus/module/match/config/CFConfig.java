package com.campus.module.match.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 协同过滤算法配置
 * 支持通过 application.yml 配置参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "campus.cf")
public class CFConfig {

    /**
     * 最小共同交互数量
     * 用户之间至少要有这么多共同交互的教员才计算相似度
     */
    private int minCommonItems = 2;

    /**
     * 相似用户Top-K
     * 选取最相似的K个用户进行推荐
     */
    private int topKSimilarUsers = 20;

    /**
     * 混合模型中CF权重 (0.0 ~ 1.0)
     * 最终分数 = cfWeight * cfScore + (1 - cfWeight) * contentScore
     */
    private double cfWeight = 0.3;

    /**
     * 最小相似度阈值
     * 低于此值不认为用户相似
     */
    private double minSimilarity = 0.1;

    /**
     * 冷启动阈值：最少行为数量
     * 用户至少有这么多行为记录才启用CF
     */
    private int coldStartThreshold = 5;

    /**
     * 各行为类型的权重
     * 1=查看详情, 2=搜索, 3=收藏, 4=聊天, 5=下单
     */
    private Map<Integer, Double> actionWeights = new HashMap<>() {
        {
            put(1, 0.1); // 查看详情
            put(2, 0.05); // 搜索
            put(3, 0.4); // 收藏
            put(4, 0.6); // 聊天
            put(5, 1.0); // 下单
        }
    };

    /**
     * 相似度缓存过期时间(秒)
     */
    private int cacheExpireSeconds = 3600;

    /**
     * 是否启用缓存
     */
    private boolean enableCache = true;

    /**
     * 获取行为权重，未配置的行为类型返回默认权重0.1
     */
    public double getActionWeight(Integer actionType) {
        return actionWeights.getOrDefault(actionType, 0.1);
    }
}
