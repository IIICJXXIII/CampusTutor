package com.campus.module.recommend.service.impl;

import com.campus.module.behavior.mapper.UserActionLogMapper;
import com.campus.module.recommend.dto.SimilarTutorDTO;
import com.campus.module.recommend.service.CollaborativeFilterService;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 协同过滤推荐服务实现
 * 基于 Item-Based 协同过滤，使用余弦相似度
 */
@Service
@RequiredArgsConstructor
public class CollaborativeFilterServiceImpl implements CollaborativeFilterService {

    private static final Logger log = LoggerFactory.getLogger(CollaborativeFilterServiceImpl.class);

    private final UserActionLogMapper actionLogMapper;
    private final TutorProfileMapper tutorProfileMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis 缓存 Key 前缀
    private static final String SIMILAR_TUTORS_KEY = "recommend:similar:";
    private static final int CACHE_TTL_HOURS = 1;

    // 默认返回数量
    private static final int DEFAULT_CANDIDATE_LIMIT = 50;

    @Override
    public List<SimilarTutorDTO> getSimilarTutors(Long tutorId, int limit) {
        if (tutorId == null || limit <= 0) {
            return Collections.emptyList();
        }

        // 尝试从缓存获取
        String cacheKey = SIMILAR_TUTORS_KEY + tutorId;
        @SuppressWarnings("unchecked")
        List<SimilarTutorDTO> cached = (List<SimilarTutorDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            log.debug("从缓存获取相似教员: tutorId={}", tutorId);
            return cached.stream().limit(limit).collect(Collectors.toList());
        }

        // 计算相似教员
        List<SimilarTutorDTO> similarTutors = calculateSimilarTutors(tutorId, DEFAULT_CANDIDATE_LIMIT);

        // 缓存结果
        if (!similarTutors.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(cacheKey, similarTutors, CACHE_TTL_HOURS, TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("缓存相似教员失败: {}", e.getMessage());
            }
        }

        return similarTutors.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<SimilarTutorDTO> getRecommendationsForUser(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return Collections.emptyList();
        }

        // 获取用户交互过的教员
        List<Long> interactedTutors = actionLogMapper.findTutorsInteractedByUser(userId);
        if (interactedTutors.isEmpty()) {
            return Collections.emptyList();
        }

        // 聚合所有交互教员的相似教员
        Map<Long, Double> recommendScores = new HashMap<>();
        Set<Long> excludeSet = new HashSet<>(interactedTutors);

        for (Long tutorId : interactedTutors) {
            List<SimilarTutorDTO> similarTutors = getSimilarTutors(tutorId, 10);
            for (SimilarTutorDTO similar : similarTutors) {
                if (!excludeSet.contains(similar.getTutorId())) {
                    // 累加相似度分数
                    recommendScores.merge(similar.getTutorId(),
                            similar.getSimilarityScore(), Double::sum);
                }
            }
        }

        // 按累计分数排序并返回 Top-K
        List<Long> topTutorIds = recommendScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (topTutorIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取教员详情
        return buildSimilarTutorDTOs(topTutorIds, recommendScores);
    }

    @Override
    public void clearSimilarityCache(Long tutorId) {
        if (tutorId != null) {
            redisTemplate.delete(SIMILAR_TUTORS_KEY + tutorId);
            log.info("清除教员相似度缓存: tutorId={}", tutorId);
        } else {
            // 清除所有相似度缓存
            Set<String> keys = redisTemplate.keys(SIMILAR_TUTORS_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("清除所有相似度缓存: count={}", keys.size());
            }
        }
    }

    /**
     * 计算与目标教员相似的教员列表
     * 使用余弦相似度：sim(A,B) = |A∩B| / sqrt(|A| * |B|)
     */
    private List<SimilarTutorDTO> calculateSimilarTutors(Long tutorId, int limit) {
        // 获取与目标教员有共同用户的其他教员
        List<Map<String, Object>> coInteracted = actionLogMapper.findCoInteractedTutors(tutorId, limit);
        if (coInteracted.isEmpty()) {
            log.debug("未找到与教员{}有共同用户的其他教员", tutorId);
            return Collections.emptyList();
        }

        // 获取目标教员的用户数
        int targetUserCount = actionLogMapper.countInteractionUsers(tutorId);
        if (targetUserCount == 0) {
            return Collections.emptyList();
        }

        // 计算相似度
        Map<Long, Double> similarityScores = new HashMap<>();
        Map<Long, Integer> coInteractionCounts = new HashMap<>();

        for (Map<String, Object> row : coInteracted) {
            Long otherTutorId = ((Number) row.get("tutor_id")).longValue();
            int coCount = ((Number) row.get("co_count")).intValue();

            // 获取另一个教员的用户数
            int otherUserCount = actionLogMapper.countInteractionUsers(otherTutorId);
            if (otherUserCount == 0)
                continue;

            // 余弦相似度
            double similarity = coCount / Math.sqrt((double) targetUserCount * otherUserCount);
            similarityScores.put(otherTutorId, similarity);
            coInteractionCounts.put(otherTutorId, coCount);
        }

        // 按相似度排序
        List<Long> sortedTutorIds = similarityScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 构建返回结果
        return buildSimilarTutorDTOs(sortedTutorIds, similarityScores, coInteractionCounts);
    }

    /**
     * 构建相似教员 DTO 列表
     */
    private List<SimilarTutorDTO> buildSimilarTutorDTOs(
            List<Long> tutorIds,
            Map<Long, Double> similarityScores,
            Map<Long, Integer> coInteractionCounts) {

        if (tutorIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询教员信息
        List<TutorProfile> profiles = tutorProfileMapper.selectBatchIds(tutorIds);
        Map<Long, TutorProfile> profileMap = profiles.stream()
                .collect(Collectors.toMap(TutorProfile::getId, p -> p));

        List<SimilarTutorDTO> result = new ArrayList<>();
        for (Long tutorId : tutorIds) {
            TutorProfile profile = profileMap.get(tutorId);
            if (profile == null)
                continue;

            SimilarTutorDTO dto = new SimilarTutorDTO();
            dto.setTutorId(tutorId);
            dto.setUserId(profile.getUserId());

            // 姓名脱敏
            String name = profile.getRealName();
            if (name != null && name.length() > 1) {
                dto.setRealName(name.charAt(0) + "**");
            } else {
                dto.setRealName(name);
            }

            dto.setAvatarUrl(null); // 头像在 SysUser 表中，此处暂不查询
            dto.setUniversityName(profile.getUniversityName());
            dto.setMajor(profile.getMajor());
            dto.setExpectPrice(profile.getExpectPrice());
            dto.setRating(profile.getRating());
            dto.setTeachSubjects(profile.getTeachSubjects());
            dto.setSimilarityScore(similarityScores.getOrDefault(tutorId, 0.0));
            dto.setCoInteractionCount(coInteractionCounts != null ? coInteractionCounts.getOrDefault(tutorId, 0) : 0);

            result.add(dto);
        }

        return result;
    }

    /**
     * 构建相似教员 DTO 列表（无共同交互数）
     */
    private List<SimilarTutorDTO> buildSimilarTutorDTOs(
            List<Long> tutorIds,
            Map<Long, Double> scores) {
        return buildSimilarTutorDTOs(tutorIds, scores, null);
    }
}
