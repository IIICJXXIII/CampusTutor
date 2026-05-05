package com.campus.module.match.service.impl;

import com.campus.module.behavior.entity.UserActionLog;
import com.campus.module.behavior.mapper.UserActionLogMapper;
import cn.hutool.json.JSONUtil;
import com.campus.module.match.config.CFConfig;
import com.campus.module.match.dto.CFRecommendation;
import com.campus.module.match.dto.UserSimilarity;
import com.campus.module.match.service.CollaborativeFilteringService;
import com.campus.module.order.mapper.CourseOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 协同过滤服务实现类
 * 基于用户的协同过滤(User-Based CF)算法
 */
@Service
public class CollaborativeFilteringServiceImpl implements CollaborativeFilteringService {

    private static final Logger log = LoggerFactory.getLogger(CollaborativeFilteringServiceImpl.class);

    private static final String CACHE_KEY_PREFIX = "cf:similarity:";
    private static final String CACHE_RATINGS_PREFIX = "cf:ratings:";

    @Autowired
    private UserActionLogMapper actionLogMapper;

    @Autowired
    private CourseOrderMapper orderMapper;

    @Autowired
    private CFConfig cfConfig;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Qualifier("cfComputeExecutor")
    private ThreadPoolTaskExecutor cfComputeExecutor;

    /**
     * 构建用户的隐式评分向量
     * 合并行为日志与订单数据，按教员ID聚合评分
     */
    @Override
    public Map<Long, Double> buildUserRatingVector(Long userId) {
        Map<Long, Double> ratings = new HashMap<>();

        // 1. 从订单历史获取评分（权重最高）
        List<Map<String, Object>> orderHistory = orderMapper.getUserOrderHistory(userId);
        for (Map<String, Object> order : orderHistory) {
            Long tutorId = ((Number) order.get("tutor_id")).longValue();
            Double score = ((Number) order.get("score")).doubleValue();
            ratings.merge(tutorId, score, Math::max); // 取最高分
        }

        // 2. 从行为日志获取隐式评分
        List<Long> interactedTutors = actionLogMapper.findTutorsInteractedByUser(userId);
        for (Long tutorId : interactedTutors) {
            // 如果订单中已有更高评分，跳过
            if (ratings.containsKey(tutorId) && ratings.get(tutorId) >= 0.5) {
                continue;
            }
            // 否则给予基础交互分
            ratings.putIfAbsent(tutorId, 0.2);
        }

        // 3. 根据行为类型更新评分（收藏和聊天提升分数）
        updateRatingsWithActionTypes(userId, ratings);

        log.debug("Built rating vector for user {}: {} tutors", userId, ratings.size());
        return ratings;
    }

    /**
     * 根据行为类型权重更新评分
     */
    private void updateRatingsWithActionTypes(Long userId, Map<Long, Double> ratings) {
        // 查询用户的详细行为记录
        List<UserActionLog> logs = actionLogMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserActionLog>()
                        .eq("user_id", userId)
                        .isNotNull("target_id"));

        for (UserActionLog log : logs) {
            Long tutorId = log.getTargetId();
            if (tutorId == null)
                continue;

            double actionWeight = cfConfig.getActionWeight(log.getActionType());
            ratings.merge(tutorId, actionWeight, Math::max);
        }

        // 归一化到 0-1 范围
        if (!ratings.isEmpty()) {
            double maxRating = Collections.max(ratings.values());
            if (maxRating > 1.0) {
                ratings.replaceAll((k, v) -> v / maxRating);
            }
        }
    }

    /**
     * 计算两个用户之间的余弦相似度
     */
    @Override
    public UserSimilarity calculateSimilarity(Long userA, Long userB) {
        Map<Long, Double> ratingsA = buildUserRatingVector(userA);
        Map<Long, Double> ratingsB = buildUserRatingVector(userB);

        // 找出共同交互的教员
        Set<Long> commonItems = new HashSet<>(ratingsA.keySet());
        commonItems.retainAll(ratingsB.keySet());

        int commonCount = commonItems.size();
        if (commonCount < cfConfig.getMinCommonItems()) {
            return new UserSimilarity(userB, 0.0, commonCount);
        }

        // 计算余弦相似度
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (Long item : commonItems) {
            double rA = ratingsA.get(item);
            double rB = ratingsB.get(item);
            dotProduct += rA * rB;
        }

        for (Double r : ratingsA.values()) {
            normA += r * r;
        }
        for (Double r : ratingsB.values()) {
            normB += r * r;
        }

        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);

        double similarity = (normA > 0 && normB > 0) ? dotProduct / (normA * normB) : 0.0;

        return new UserSimilarity(userB, similarity, commonCount);
    }

    /**
     * 查找与目标用户最相似的K个用户
     */
    @Override
    public List<UserSimilarity> findSimilarUsers(Long userId, int topK) {
        // 尝试从缓存获取
        // 尝试从缓存获取
        String cacheKey = CACHE_KEY_PREFIX + userId;
        if (cfConfig.isEnableCache() && redisTemplate != null) {
            try {
                Object cachedObj = redisTemplate.opsForValue().get(cacheKey);
                if (cachedObj instanceof List) {
                    List<?> list = (List<?>) cachedObj;
                    if (!list.isEmpty()) {
                        // 检查第一个元素类型，如果是LinkedHashMap则需要转换
                        Object firstItem = list.get(0);
                        if (firstItem instanceof java.util.LinkedHashMap) {
                            List<UserSimilarity> convertedList = new ArrayList<>();
                            for (Object item : list) {
                                UserSimilarity similarity = JSONUtil.toBean(JSONUtil.toJsonStr(item),
                                        UserSimilarity.class);
                                convertedList.add(similarity);
                            }
                            log.debug("Cache hit for user {} similarity (converted)", userId);
                            return convertedList.stream().limit(topK).collect(Collectors.toList());
                        } else if (firstItem instanceof UserSimilarity) {
                            log.debug("Cache hit for user {} similarity", userId);
                            @SuppressWarnings("unchecked")
                            List<UserSimilarity> userSimilarities = (List<UserSimilarity>) list;
                            return userSimilarities.stream().limit(topK).collect(Collectors.toList());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to retrieve/convert similarity cache for user {}: {}", userId, e.getMessage());
                // 缓存出错不阻断，继续重新计算
            }
        }

        // 1. 找出有共同订单教员的用户
        List<Long> candidateUsers = orderMapper.findParentsWithCommonTutors(userId);

        // 2. 补充有行为交互的用户
        List<Long> interactedTutors = actionLogMapper.findTutorsInteractedByUser(userId);
        for (Long tutorId : interactedTutors) {
            List<Long> interactedUsers = actionLogMapper.findUsersWhoInteractedWith(tutorId);
            for (Long u : interactedUsers) {
                if (!u.equals(userId) && !candidateUsers.contains(u)) {
                    candidateUsers.add(u);
                }
            }
        }

        // 3. 并行计算相似度并过滤
        List<CompletableFuture<UserSimilarity>> simFutures = candidateUsers.stream()
                .map(cid -> CompletableFuture.supplyAsync(
                        () -> calculateSimilarity(userId, cid), cfComputeExecutor))
                .collect(Collectors.toList());

        List<UserSimilarity> similarities = simFutures.stream()
                .map(CompletableFuture::join)
                .filter(sim -> sim.getSimilarity() >= cfConfig.getMinSimilarity())
                .collect(Collectors.toList());

        // 按相似度降序排序
        similarities.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));

        // 缓存结果
        if (cfConfig.isEnableCache() && redisTemplate != null && !similarities.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(cacheKey, similarities,
                        cfConfig.getCacheExpireSeconds(), TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("Failed to write similarity cache for user {}: {}", userId, e.getMessage());
            }
        }

        return similarities.stream().limit(topK).collect(Collectors.toList());
    }

    /**
     * 预测用户对某教员的评分
     * 基于相似用户的加权平均
     */
    @Override
    public Double predictScore(Long userId, Long tutorId) {
        List<UserSimilarity> similarUsers = findSimilarUsers(userId, cfConfig.getTopKSimilarUsers());

        if (similarUsers.isEmpty()) {
            return null;
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (UserSimilarity sim : similarUsers) {
            Map<Long, Double> ratings = buildUserRatingVector(sim.getUserId());
            if (ratings.containsKey(tutorId)) {
                double rating = ratings.get(tutorId);
                weightedSum += sim.getSimilarity() * rating;
                totalWeight += Math.abs(sim.getSimilarity());
            }
        }

        if (totalWeight == 0) {
            return null;
        }

        return weightedSum / totalWeight;
    }

    /**
     * 获取用户的Top-N推荐教员列表
     */
    @Override
    public List<CFRecommendation> getRecommendations(Long userId, int n) {
        List<UserSimilarity> similarUsers = findSimilarUsers(userId, cfConfig.getTopKSimilarUsers());

        if (similarUsers.isEmpty()) {
            return Collections.emptyList();
        }

        // 用户已交互的教员（需排除）
        Map<Long, Double> userRatings = buildUserRatingVector(userId);
        Set<Long> excludedTutors = userRatings.keySet();

        // 收集所有候选教员及其加权评分
        Map<Long, Double> candidateScores = new HashMap<>();
        Map<Long, Integer> contributorCounts = new HashMap<>();

        for (UserSimilarity sim : similarUsers) {
            Map<Long, Double> ratings = buildUserRatingVector(sim.getUserId());
            for (Map.Entry<Long, Double> entry : ratings.entrySet()) {
                Long tutorId = entry.getKey();
                if (excludedTutors.contains(tutorId)) {
                    continue; // 跳过已交互的
                }

                double weighted = sim.getSimilarity() * entry.getValue();
                candidateScores.merge(tutorId, weighted, Double::sum);
                contributorCounts.merge(tutorId, 1, Integer::sum);
            }
        }

        // 归一化并排序
        List<CFRecommendation> recommendations = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : candidateScores.entrySet()) {
            Long tutorId = entry.getKey();
            int contributors = contributorCounts.get(tutorId);
            double normalizedScore = entry.getValue() / contributors;

            recommendations.add(new CFRecommendation(
                    tutorId,
                    Math.min(normalizedScore, 1.0),
                    contributors,
                    contributors + "位相似用户推荐"));
        }

        // 按分数排序并取Top-N
        recommendations.sort((a, b) -> Double.compare(b.getCfScore(), a.getCfScore()));
        return recommendations.stream().limit(n).collect(Collectors.toList());
    }

    /**
     * 判断用户是否满足CF条件
     */
    @Override
    public boolean hasEnoughHistory(Long userId) {
        // 检查订单数量
        List<Long> orderedTutors = orderMapper.findOrderedTutorsByParent(userId);
        // 检查行为记录数量
        List<Long> interactedTutors = actionLogMapper.findTutorsInteractedByUser(userId);

        int total = orderedTutors.size() + interactedTutors.size();
        return total >= cfConfig.getColdStartThreshold();
    }

    /**
     * 刷新用户相似度缓存
     */
    @Override
    public void refreshSimilarityCache(Long userId) {
        if (redisTemplate == null) {
            return;
        }

        try {
            if (userId != null) {
                String cacheKey = CACHE_KEY_PREFIX + userId;
                redisTemplate.delete(cacheKey);
            } else {
                // 刷新全部缓存
                Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            }
        } catch (Exception e) {
            // Redis操作失败不影响业务
        }
    }

    /**
     * 批量预测用户对多个教员的评分
     */
    @Override
    public Map<Long, Double> batchPredictScores(Long userId, List<Long> tutorIds) {
        // 一次性获取相似用户，避免重复计算
        List<UserSimilarity> similarUsers = findSimilarUsers(userId, cfConfig.getTopKSimilarUsers());

        if (similarUsers.isEmpty()) {
            return Collections.emptyMap();
        }

        // 并行构建相似用户的评分向量
        Map<Long, Map<Long, Double>> similarUserRatings = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> ratingFutures = similarUsers.stream()
                .map(sim -> CompletableFuture.runAsync(() ->
                        similarUserRatings.put(sim.getUserId(), buildUserRatingVector(sim.getUserId())),
                        cfComputeExecutor))
                .collect(Collectors.toList());
        ratingFutures.forEach(CompletableFuture::join);

        // 并行批量计算预测分数
        Map<Long, Double> predictions = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> predFutures = tutorIds.stream()
                .map(tutorId -> CompletableFuture.runAsync(() -> {
                    double weightedSum = 0.0;
                    double totalWeight = 0.0;
                    for (UserSimilarity sim : similarUsers) {
                        Map<Long, Double> ratings = similarUserRatings.get(sim.getUserId());
                        if (ratings.containsKey(tutorId)) {
                            double rating = ratings.get(tutorId);
                            weightedSum += sim.getSimilarity() * rating;
                            totalWeight += Math.abs(sim.getSimilarity());
                        }
                    }
                    if (totalWeight > 0) {
                        predictions.put(tutorId, weightedSum / totalWeight);
                    }
                }, cfComputeExecutor))
                .collect(Collectors.toList());
        predFutures.forEach(CompletableFuture::join);

        return predictions;
    }
}
