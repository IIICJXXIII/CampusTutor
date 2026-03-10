package com.campus.module.match.service;

import cn.hutool.json.JSONUtil;
import com.campus.module.match.config.IntentConfig;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 实时意图追踪服务
 *
 * 核心机制（来自 MVP 推荐架构方案文档 §6）：
 * 1. 家长点击/收藏/私信教员时，提取教员身上的核心标签（科目、大学、价格带等）
 * 2. 使用 Redis ZSET 的 ZINCRBY 原子操作，将行为权重增量累加到家长专属的意图 ZSET
 * 3. 搜索时读取 Top-K 意图标签，对命中标签的候选教员加分
 * 4. ZSET 设置 Session TTL 作为宏观衰减兜底
 *
 * Redis Key 设计：
 * - realtime_intent:parent:{parentId}  — ZSET，member=标签名，score=累加权重分
 * - tutor_static_tags:{tutorId}        — Hash，缓存教员核心标签
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeIntentService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TutorProfileMapper tutorProfileMapper;
    private final IntentConfig intentConfig;

    // Redis Key 前缀
    private static final String INTENT_KEY_PREFIX = "realtime_intent:parent:";
    private static final String TUTOR_TAGS_KEY_PREFIX = "tutor_static_tags:";

    /**
     * 处理用户实时行为，更新意图标签偏好分
     *
     * 流程：
     * 1. 获取行为对应的基础权重 W_action
     * 2. 获取被操作教员的核心标签列表
     * 3. 对每个标签执行 ZINCRBY 增量累加
     * 4. 刷新 Session TTL
     *
     * @param parentId   家长用户ID
     * @param tutorId    被交互的教员ID
     * @param actionType 行为类型: 1=查看详情, 3=收藏, 4=聊天, 5=下单
     */
    public void handleUserAction(Long parentId, Long tutorId, int actionType) {
        if (!intentConfig.isEnabled() || parentId == null || tutorId == null) {
            return;
        }

        try {
            // 1. 获取行为基础权重
            double weight = intentConfig.getActionWeight(actionType);
            if (weight <= 0) {
                return;
            }

            // 2. 获取教员身上的核心标签
            List<String> tags = getTutorTags(tutorId);
            if (tags.isEmpty()) {
                log.debug("教员 {} 无可用标签，跳过意图更新", tutorId);
                return;
            }

            // 3. 增量更新家长的实时标签偏好分
            String intentKey = INTENT_KEY_PREFIX + parentId;
            for (String tag : tags) {
                redisTemplate.opsForZSet().incrementScore(intentKey, tag, weight);
            }

            // 4. 刷新 Session TTL
            redisTemplate.expire(intentKey, intentConfig.getSessionTtlMinutes(), TimeUnit.MINUTES);

            log.info("实时意图更新: parentId={}, tutorId={}, action={}, weight={}, tags={}",
                    parentId, tutorId, actionType, weight, tags);

        } catch (Exception e) {
            // 意图追踪失败不应影响主流程
            log.warn("实时意图更新失败: parentId={}, tutorId={}, error={}", parentId, tutorId, e.getMessage());
        }
    }

    /**
     * 获取家长当前最高权重的 Top-K 意图标签及其分数
     *
     * @param parentId 家长用户ID
     * @param topK     返回的标签数量
     * @return Map<标签名, 累加分数>，按分数降序
     */
    public Map<String, Double> getTopIntentTags(Long parentId, int topK) {
        if (parentId == null) {
            return Collections.emptyMap();
        }

        String intentKey = INTENT_KEY_PREFIX + parentId;
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(intentKey, 0, topK - 1);

        if (tuples == null || tuples.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Double> result = new LinkedHashMap<>();
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            if (tuple.getValue() != null && tuple.getScore() != null) {
                result.put(tuple.getValue().toString(), tuple.getScore());
            }
        }
        return result;
    }

    /**
     * 计算候选教员的实时意图加分
     *
     * 算法：遍历家长的 Top-K 意图标签，检查候选教员是否也拥有该标签。
     * 如果命中，将该标签的衰减后分数 × beta 因子 累加为加分。
     * 加分有上限，防止意图完全主导排序。
     *
     * @param parentId  家长用户ID
     * @param candidate 候选教员
     * @return 意图加分值（0 ~ maxIntentBoost）
     */
    public double calculateIntentBoost(Long parentId, TutorProfile candidate) {
        if (!intentConfig.isEnabled() || parentId == null || candidate == null) {
            return 0.0;
        }

        // 1. 获取家长 Top-K 意图标签
        Map<String, Double> topIntentTags = getTopIntentTags(parentId, intentConfig.getTopKTags());
        if (topIntentTags.isEmpty()) {
            return 0.0;
        }

        // 2. 获取候选教员的标签
        List<String> candidateTags = getTutorTags(candidate.getId());
        if (candidateTags.isEmpty()) {
            return 0.0;
        }

        // 3. 标签命中检测 & 加分计算
        Set<String> candidateTagSet = new HashSet<>(candidateTags);
        double totalBoost = 0.0;

        for (Map.Entry<String, Double> entry : topIntentTags.entrySet()) {
            String intentTag = entry.getKey();
            double rawScore = entry.getValue();

            if (candidateTagSet.contains(intentTag)) {
                // 命中！加分 = 原始累加分 × beta 放大因子
                totalBoost += rawScore * intentConfig.getBetaFactor();
            }
        }

        // 4. 应用上限
        double cappedBoost = Math.min(totalBoost, intentConfig.getMaxIntentBoost());

        if (cappedBoost > 0) {
            log.debug("意图加分: parentId={}, tutorId={}, boost={} (capped from {})",
                    parentId, candidate.getId(), cappedBoost, totalBoost);
        }

        return cappedBoost;
    }

    /**
     * 获取教员的核心特征标签
     *
     * 标签提取规则：
     * - 科目标签：每个教授科目各作为一个标签（如 "科目:数学", "科目:物理"）
     * - 大学标签：大学名称作为标签（如 "大学:悉尼大学"）
     * - 价格带标签：离散化到价格区间（如 "价格带:100-150"）
     * - 学历标签：学历等级（如 "学历:本科"）
     *
     * 优先从 Redis Hash 缓存读取，缓存未命中则从数据库构建并回写缓存。
     *
     * @param tutorId 教员ID
     * @return 标签列表
     */
    public List<String> getTutorTags(Long tutorId) {
        if (tutorId == null) {
            return Collections.emptyList();
        }

        String cacheKey = TUTOR_TAGS_KEY_PREFIX + tutorId;

        // 尝试从缓存获取
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> cachedTags = (List<String>) cached;
                return cachedTags;
            }
        } catch (Exception e) {
            log.debug("读取教员标签缓存失败: {}", e.getMessage());
        }

        // 从数据库构建标签
        TutorProfile tutor = tutorProfileMapper.selectById(tutorId);
        if (tutor == null) {
            return Collections.emptyList();
        }

        List<String> tags = buildTutorTags(tutor);

        // 回写缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, tags,
                    intentConfig.getTagCacheExpireSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.debug("写入教员标签缓存失败: {}", e.getMessage());
        }

        return tags;
    }

    /**
     * 从教员档案构建标签列表
     *
     * @param tutor 教员档案
     * @return 标签列表
     */
    public List<String> buildTutorTags(TutorProfile tutor) {
        List<String> tags = new ArrayList<>();

        // 1. 科目标签
        if (StringUtils.hasText(tutor.getTeachSubjects())) {
            try {
                List<String> subjects = JSONUtil.toList(tutor.getTeachSubjects(), String.class);
                for (String subject : subjects) {
                    tags.add("科目:" + subject);
                }
            } catch (Exception e) {
                log.debug("解析科目JSON失败: {}", e.getMessage());
            }
        }

        // 2. 大学标签
        if (StringUtils.hasText(tutor.getUniversityName())) {
            tags.add("大学:" + tutor.getUniversityName());
        }

        // 3. 价格带标签（离散化）
        if (tutor.getExpectPrice() != null) {
            tags.add("价格带:" + getPriceBand(tutor.getExpectPrice()));
        }

        // 4. 学历标签
        if (tutor.getEducation() != null) {
            tags.add("学历:" + getEducationLabel(tutor.getEducation()));
        }

        // 5. 授课方式标签
        if (tutor.getCanVisit() != null && tutor.getCanVisit() == 1) {
            tags.add("授课方式:上门");
        }
        if (tutor.getCanOnline() != null && tutor.getCanOnline() == 1) {
            tags.add("授课方式:网课");
        }

        // 6. 年级标签
        if (StringUtils.hasText(tutor.getTeachGrades())) {
            try {
                List<String> grades = JSONUtil.toList(tutor.getTeachGrades(), String.class);
                for (String grade : grades) {
                    tags.add("年级:" + grade);
                }
            } catch (Exception e) {
                log.debug("解析年级JSON失败: {}", e.getMessage());
            }
        }

        return tags;
    }

    /**
     * 将连续价格离散化为价格带标签
     * 基于 MVP 架构文档 §3.1.2 的分箱设计
     */
    private String getPriceBand(BigDecimal price) {
        double p = price.doubleValue();
        if (p < 70) {
            return "40-70";
        } else if (p < 100) {
            return "70-100";
        } else if (p < 150) {
            return "100-150";
        } else if (p < 200) {
            return "150-200";
        } else {
            return "200+";
        }
    }

    /**
     * 学历等级转标签
     */
    private String getEducationLabel(Integer education) {
        if (education == null) return "未知";
        switch (education) {
            case 5: return "博士";
            case 4: return "硕士";
            case 3: return "本科";
            case 2: return "大专";
            default: return "其他";
        }
    }

    /**
     * 清除指定家长的实时意图缓存
     *
     * @param parentId 家长ID，null 则清除所有
     */
    public void clearIntentCache(Long parentId) {
        if (parentId != null) {
            redisTemplate.delete(INTENT_KEY_PREFIX + parentId);
        }
        // 注意：全量清除需要 SCAN，MVP 阶段不实现
    }

    /**
     * 清除指定教员的标签缓存（教员信息更新时调用）
     *
     * @param tutorId 教员ID
     */
    public void clearTutorTagCache(Long tutorId) {
        if (tutorId != null) {
            redisTemplate.delete(TUTOR_TAGS_KEY_PREFIX + tutorId);
        }
    }
}
