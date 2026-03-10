package com.campus.module.match.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 实时意图追踪系统配置
 * 基于 Redis ZSET + 指数时间衰减模型
 * 通过 application.properties 中 campus.intent.* 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "campus.intent")
public class IntentConfig {

    /**
     * 是否启用实时意图追踪
     */
    private boolean enabled = true;

    // ============ 行为基础权重 (W_action) ============

    /** 点击查看详情的基础权重 */
    private double weightView = 1.0;

    /** 收藏/关注教员的基础权重 */
    private double weightFavorite = 3.0;

    /** 点击发送私信/聊天的基础权重 */
    private double weightChat = 5.0;

    /** 下单的基础权重 */
    private double weightOrder = 8.0;

    // ============ 衰减参数 ============

    /**
     * 兴趣半衰期（分钟）
     * 即经过此时间后，行为权重衰减到一半
     * 对应衰减常数 lambda = ln(2) / halfLifeMinutes
     */
    private double halfLifeMinutes = 5.0;

    /**
     * Session 意图记录的物理过期时间（分钟）
     * 超过此时间无操作，整个 ZSET 过期清零（宏观暴力衰减兜底）
     */
    private int sessionTtlMinutes = 30;

    // ============ 精排参数 ============

    /**
     * 读取的 Top-K 意图标签数量
     * 搜索时只读取权重最高的 K 个标签进行匹配
     */
    private int topKTags = 5;

    /**
     * 意图加分的 beta 放大因子
     * 最终分数 = alpha * baseScore + beta * intentBoost
     * alpha 固定为 1.0（保持原有评分不变）
     */
    private double betaFactor = 1.5;

    /**
     * 意图加分的最大上限
     * 防止意图加分过大导致排序完全被意图主导
     */
    private double maxIntentBoost = 15.0;

    // ============ 标签缓存 ============

    /**
     * 教员静态标签缓存过期时间（秒）
     * 教员标签不常变化，可以较长时间缓存
     */
    private int tagCacheExpireSeconds = 3600;

    // ============ Redis Streams 异步配置 ============

    /** Redis Stream 名称 */
    private String streamKey = "intent:actions";

    /** 消费者组名称 */
    private String consumerGroup = "intent-group";

    /** 消费者名称 */
    private String consumerName = "consumer-1";

    /** Stream 拉取超时（毫秒） */
    private long streamPollTimeout = 2000;

    /**
     * 根据行为类型获取对应的基础权重
     *
     * @param actionType 行为类型: 1=查看, 3=收藏, 4=聊天, 5=下单
     * @return 基础权重值
     */
    public double getActionWeight(int actionType) {
        switch (actionType) {
            case 1: return weightView;
            case 3: return weightFavorite;
            case 4: return weightChat;
            case 5: return weightOrder;
            default: return 0;
        }
    }

    /**
     * 计算指数衰减常数 lambda
     * lambda = ln(2) / halfLifeMinutes
     */
    public double getDecayLambda() {
        return Math.log(2) / halfLifeMinutes;
    }
}
