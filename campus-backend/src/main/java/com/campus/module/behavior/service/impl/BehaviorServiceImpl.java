package com.campus.module.behavior.service.impl;

import com.campus.module.behavior.dto.TutorBehaviorStats;
import com.campus.module.behavior.entity.UserActionLog;
import com.campus.module.behavior.mapper.UserActionLogMapper;
import com.campus.module.behavior.service.BehaviorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户行为服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BehaviorServiceImpl implements BehaviorService {

    private final UserActionLogMapper actionLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis 缓存 Key 前缀
    private static final String TUTOR_STATS_KEY = "behavior:tutor:stats:";
    private static final String USER_SEARCH_COUNT_KEY = "behavior:user:search:";
    private static final int CACHE_TTL_MINUTES = 10;

    // 行为类型常量
    public static final int ACTION_VIEW = 1;
    public static final int ACTION_SEARCH = 2;
    public static final int ACTION_FAVORITE = 3;
    public static final int ACTION_CHAT = 4;
    public static final int ACTION_ORDER = 5;

    @Override
    public void recordAction(Long userId, Long targetId, Integer actionType, Integer duration) {
        if (userId == null || actionType == null) {
            return;
        }

        UserActionLog actionLog = new UserActionLog();
        actionLog.setUserId(userId);
        actionLog.setTargetId(targetId);
        actionLog.setActionType(actionType);
        actionLog.setDuration(duration != null ? duration : 0);
        actionLog.setCreateTime(LocalDateTime.now());

        actionLogMapper.insert(actionLog);

        // 清除相关缓存
        if (targetId != null) {
            redisTemplate.delete(TUTOR_STATS_KEY + targetId);
        }
        if (actionType == ACTION_SEARCH) {
            redisTemplate.delete(USER_SEARCH_COUNT_KEY + userId);
        }

        log.info("记录用户行为: userId={}, targetId={}, actionType={}", userId, targetId, actionType);
    }

    @Override
    public TutorBehaviorStats getTutorStats(Long tutorId) {
        if (tutorId == null) {
            return new TutorBehaviorStats();
        }

        // 尝试从缓存获取
        String cacheKey = TUTOR_STATS_KEY + tutorId;
        TutorBehaviorStats cached = (TutorBehaviorStats) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 从数据库统计
        TutorBehaviorStats stats = new TutorBehaviorStats();
        stats.setTutorId(tutorId);

        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        List<Map<String, Object>> actionCounts = actionLogMapper.countTutorActions(tutorId, last24h);

        int viewCount = 0;
        int chatCount = 0;
        double totalDuration = 0;
        int durationCount = 0;

        for (Map<String, Object> row : actionCounts) {
            Integer actionType = ((Number) row.get("action_type")).intValue();
            Long count = ((Number) row.get("count")).longValue();
            Double avgDuration = row.get("avg_duration") != null ? ((Number) row.get("avg_duration")).doubleValue() : 0;

            switch (actionType) {
                case ACTION_VIEW:
                    viewCount = count.intValue();
                    totalDuration = avgDuration;
                    durationCount = viewCount;
                    break;
                case ACTION_CHAT:
                    chatCount = count.intValue();
                    break;
            }
        }

        stats.setViewCount24h(viewCount);
        stats.setChatCount24h(chatCount);
        stats.setFavoriteCount(actionLogMapper.countTutorFavorites(tutorId));
        stats.setAvgViewDuration(durationCount > 0 ? totalDuration : 0);

        // 计算聊天率
        if (viewCount > 0) {
            stats.setChatRate((double) chatCount / viewCount);
        }

        // 计算热度分
        stats.setHotnessScore(calculateHotnessScore(stats));

        // 缓存结果
        try {
            redisTemplate.opsForValue().set(cacheKey, stats, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存教员行为统计失败: {}", e.getMessage());
        }

        return stats;
    }

    @Override
    public int getUserSearchCount(Long userId) {
        if (userId == null) {
            return 0;
        }

        // 尝试从缓存获取
        String cacheKey = USER_SEARCH_COUNT_KEY + userId;
        Integer cached = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 从数据库统计
        int count = actionLogMapper.countUserSearches(userId);

        // 缓存结果
        try {
            redisTemplate.opsForValue().set(cacheKey, count, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存用户搜索次数失败: {}", e.getMessage());
        }

        return count;
    }

    @Override
    public double calculateHotnessScore(TutorBehaviorStats stats) {
        if (stats == null) {
            return 0;
        }

        // 热度计算公式：
        // 浏览分(30%) + 聊天分(40%) + 收藏分(30%)
        double viewScore = Math.min(stats.getViewCount24h() / 50.0, 1.0) * 30;
        double chatScore = Math.min(stats.getChatCount24h() / 10.0, 1.0) * 40;
        double favScore = Math.min(stats.getFavoriteCount() / 20.0, 1.0) * 30;

        return viewScore + chatScore + favScore;
    }
}
