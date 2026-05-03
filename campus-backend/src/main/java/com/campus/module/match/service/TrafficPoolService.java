package com.campus.module.match.service;

import com.campus.module.behavior.dto.TutorBehaviorStats;
import com.campus.module.behavior.service.BehaviorService;
import com.campus.module.match.config.TrafficPoolConfig;
import com.campus.module.match.dto.TrafficPoolLevel;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficPoolService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TutorProfileMapper tutorProfileMapper;
    private final BehaviorService behaviorService;
    private final TrafficPoolConfig poolConfig;

    private static final String POOL_KEY_PREFIX = "traffic_pool:";

    /**
     * 获取教员当前流量池级别
     *
     * 优先从 Redis 缓存读取，缓存未命中则根据教员数据评估并回写。
     *
     * @param tutorId 教员ID
     * @return 流量池级别
     */
    public TrafficPoolLevel getPoolLevel(Long tutorId) {
        if (!poolConfig.isEnabled() || tutorId == null) {
            return TrafficPoolLevel.BASIC;
        }

        String cacheKey = POOL_KEY_PREFIX + tutorId;

        // 1. 尝试从 Redis 读取
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return TrafficPoolLevel.valueOf(cached.toString());
            }
        } catch (Exception e) {
            log.debug("读取流量池缓存失败: {}", e.getMessage());
        }

        // 2. 评估当前级别
        TrafficPoolLevel level = evaluatePoolLevel(tutorId);

        // 3. 回写缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, level.name(),
                    poolConfig.getCacheExpireSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.debug("写入流量池缓存失败: {}", e.getMessage());
        }

        return level;
    }

    /**
     * 根据教员的业务指标评估其流量池级别
     *
     * 评估逻辑：
     * - HOT: 订单数>=10 且 评分>=4.5
     * - WARM: 订单数>=3 或 (注册超过观察期且有聊天记录)
     * - BASIC: 默认（新注册或指标不达标）
     *
     * @param tutorId 教员ID
     * @return 评估后的流量池级别
     */
    private TrafficPoolLevel evaluatePoolLevel(Long tutorId) {
        TutorProfile tutor = tutorProfileMapper.selectById(tutorId);
        if (tutor == null) {
            return TrafficPoolLevel.BASIC;
        }

        int orderCount = tutor.getOrderCount() != null ? tutor.getOrderCount() : 0;
        double rating = tutor.getRating() != null ? tutor.getRating().doubleValue() : 0;

        // HOT: 多次成交 + 高评分
        if (orderCount >= 10 && rating >= 4.5) {
            log.info("教员 {} 晋级 HOT 池: orderCount={}, rating={}", tutorId, orderCount, rating);
            return TrafficPoolLevel.HOT;
        }

        // WARM: 有一定成交量 或 观察期过了且有互动
        if (orderCount >= 3) {
            log.info("教员 {} 晋级 WARM 池: orderCount={}", tutorId, orderCount);
            return TrafficPoolLevel.WARM;
        }

        // 检查是否已过观察期且有聊天记录
        if (tutor.getCreateTime() != null) {
            long daysSinceCreation = ChronoUnit.DAYS.between(tutor.getCreateTime(), LocalDateTime.now());
            if (daysSinceCreation > poolConfig.getBasicPeriodDays()) {
                // 超过观察期，检查是否有互动行为
                TutorBehaviorStats stats = behaviorService.getTutorStats(tutorId);
                if (stats != null && stats.getChatCount24h() > 0) {
                    log.info("教员 {} 晋级 WARM 池: 观察期结束且有互动", tutorId);
                    return TrafficPoolLevel.WARM;
                }
            }
        }

        // 默认 BASIC
        return TrafficPoolLevel.BASIC;
    }

    /**
     * 获取流量池对应的排序加分
     *
     * @param level 流量池级别
     * @return 加分值
     */
    public double getPoolBoostScore(TrafficPoolLevel level) {
        if (!poolConfig.isEnabled()) {
            return 0;
        }
        switch (level) {
            case HOT:
                return poolConfig.getHotBoost();
            case WARM:
                return poolConfig.getWarmBoost();
            case BASIC:
                return poolConfig.getBasicBoost();
            default:
                return 0;
        }
    }

    /**
     * 获取流量池对应的推荐标签
     *
     * @param level 流量池级别
     * @return 标签文本，WARM 级别无特殊标签则返回 null
     */
    public String getPoolTag(TrafficPoolLevel level) {
        switch (level) {
            case HOT:
                return level.getTag(); // "明星教员⭐"
            case BASIC:
                return level.getTag(); // "新晋教员"
            default:
                return null;
        }
    }

    /**
     * 强制刷新教员的流量池级别缓存
     *
     * @param tutorId 教员ID
     */
    public void refreshPoolLevel(Long tutorId) {
        if (tutorId != null) {
            String cacheKey = POOL_KEY_PREFIX + tutorId;
            redisTemplate.delete(cacheKey);
            // 触发重新评估
            getPoolLevel(tutorId);
        }
    }
}
