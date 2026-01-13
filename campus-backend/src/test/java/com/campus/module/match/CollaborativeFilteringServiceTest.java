package com.campus.module.match;

import com.campus.module.behavior.mapper.UserActionLogMapper;
import com.campus.module.match.config.CFConfig;
import com.campus.module.match.dto.CFRecommendation;
import com.campus.module.match.dto.UserSimilarity;
import com.campus.module.match.service.CollaborativeFilteringService;
import com.campus.module.order.mapper.CourseOrderMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 协同过滤服务测试
 * 测试相似度计算、评分预测和推荐生成
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("协同过滤服务测试")
class CollaborativeFilteringServiceTest {

    @Autowired
    private CollaborativeFilteringService cfService;

    @Autowired
    private CFConfig cfConfig;

    @Autowired
    private UserActionLogMapper actionLogMapper;

    @Autowired
    private CourseOrderMapper orderMapper;

    // ============ 用户评分向量构建测试 ============

    @Test
    @Order(1)
    @DisplayName("1. 构建用户评分向量 - 有历史数据")
    void testBuildUserRatingVectorWithHistory() {
        // 使用一个可能有历史记录的用户ID
        Long userId = 1L;

        Map<Long, Double> ratings = cfService.buildUserRatingVector(userId);

        assertNotNull(ratings, "评分向量不应为null");
        System.out.println("✅ 用户 " + userId + " 的评分向量:");
        System.out.println("   教员数量: " + ratings.size());
        ratings.forEach(
                (tutorId, score) -> System.out.println("   教员 " + tutorId + ": " + String.format("%.2f", score)));
    }

    @Test
    @Order(2)
    @DisplayName("2. 构建用户评分向量 - 新用户（冷启动）")
    void testBuildUserRatingVectorColdStart() {
        // 使用一个不存在的用户ID
        Long userId = 999999L;

        Map<Long, Double> ratings = cfService.buildUserRatingVector(userId);

        assertNotNull(ratings, "评分向量不应为null");
        assertTrue(ratings.isEmpty() || ratings.size() == 0, "新用户应无历史数据");
        System.out.println("✅ 新用户评分向量为空，符合预期");
    }

    // ============ 用户相似度计算测试 ============

    @Test
    @Order(3)
    @DisplayName("3. 计算用户相似度")
    void testCalculateSimilarity() {
        Long userA = 1L;
        Long userB = 2L;

        UserSimilarity sim = cfService.calculateSimilarity(userA, userB);

        assertNotNull(sim, "相似度结果不应为null");
        assertNotNull(sim.getSimilarity(), "相似度值不应为null");
        assertTrue(sim.getSimilarity() >= 0 && sim.getSimilarity() <= 1,
                "相似度应在0-1范围内");

        System.out.println("✅ 用户相似度计算:");
        System.out.println("   用户 " + userA + " vs " + userB);
        System.out.println("   相似度: " + String.format("%.4f", sim.getSimilarity()));
        System.out.println("   共同教员: " + sim.getCommonItems());
    }

    @Test
    @Order(4)
    @DisplayName("4. 自相似度测试（应为1.0）")
    void testSelfSimilarity() {
        Long userId = 1L;

        UserSimilarity sim = cfService.calculateSimilarity(userId, userId);

        assertNotNull(sim);
        // 自身相似度应该是1.0（如果有历史数据）
        if (sim.getCommonItems() > 0) {
            assertEquals(1.0, sim.getSimilarity(), 0.01, "自相似度应为1.0");
        }
        System.out.println("✅ 自相似度: " + sim.getSimilarity());
    }

    // ============ 相似用户查找测试 ============

    @Test
    @Order(5)
    @DisplayName("5. 查找相似用户 Top-K")
    void testFindSimilarUsers() {
        Long userId = 1L;
        int topK = 5;

        List<UserSimilarity> similarUsers = cfService.findSimilarUsers(userId, topK);

        assertNotNull(similarUsers, "相似用户列表不应为null");
        assertTrue(similarUsers.size() <= topK, "返回数量不应超过K");

        // 验证降序排列
        for (int i = 1; i < similarUsers.size(); i++) {
            assertTrue(similarUsers.get(i - 1).getSimilarity() >= similarUsers.get(i).getSimilarity(),
                    "相似用户应按相似度降序排列");
        }

        System.out.println("✅ 用户 " + userId + " 的相似用户:");
        similarUsers.forEach(sim -> System.out.println("   用户 " + sim.getUserId() +
                ": 相似度=" + String.format("%.4f", sim.getSimilarity()) +
                ", 共同=" + sim.getCommonItems()));
    }

    // ============ 评分预测测试 ============

    @Test
    @Order(6)
    @DisplayName("6. 预测用户对教员的评分")
    void testPredictScore() {
        Long userId = 1L;
        Long tutorId = 5L; // 假设用户未直接交互过的教员

        Double predicted = cfService.predictScore(userId, tutorId);

        if (predicted != null) {
            assertTrue(predicted >= 0 && predicted <= 1, "预测分应在0-1范围内");
            System.out.println("✅ 预测分数: " + String.format("%.4f", predicted));
        } else {
            System.out.println("✅ 无法预测（无足够相似用户数据），返回null符合预期");
        }
    }

    @Test
    @Order(7)
    @DisplayName("7. 批量预测评分")
    void testBatchPredictScores() {
        Long userId = 1L;
        List<Long> tutorIds = List.of(1L, 2L, 3L, 4L, 5L);

        Map<Long, Double> predictions = cfService.batchPredictScores(userId, tutorIds);

        assertNotNull(predictions, "预测结果不应为null");

        System.out.println("✅ 批量预测结果:");
        tutorIds.forEach(tutorId -> {
            Double score = predictions.get(tutorId);
            if (score != null) {
                System.out.println("   教员 " + tutorId + ": " + String.format("%.4f", score));
            } else {
                System.out.println("   教员 " + tutorId + ": 无预测");
            }
        });
    }

    // ============ 推荐生成测试 ============

    @Test
    @Order(8)
    @DisplayName("8. 获取推荐教员列表")
    void testGetRecommendations() {
        Long userId = 1L;
        int n = 5;

        List<CFRecommendation> recommendations = cfService.getRecommendations(userId, n);

        assertNotNull(recommendations, "推荐列表不应为null");
        assertTrue(recommendations.size() <= n, "推荐数量不应超过N");

        // 验证降序排列
        for (int i = 1; i < recommendations.size(); i++) {
            assertTrue(recommendations.get(i - 1).getCfScore() >= recommendations.get(i).getCfScore(),
                    "推荐应按分数降序排列");
        }

        System.out.println("✅ 用户 " + userId + " 的推荐教员:");
        recommendations.forEach(rec -> System.out.println("   教员 " + rec.getTutorId() +
                ": 分数=" + String.format("%.4f", rec.getCfScore()) +
                ", 贡献者=" + rec.getContributorCount() +
                ", 来源=" + rec.getSource()));
    }

    // ============ 冷启动判断测试 ============

    @Test
    @Order(9)
    @DisplayName("9. 冷启动判断 - 有历史用户")
    void testHasEnoughHistoryWithData() {
        Long userId = 1L;

        boolean hasHistory = cfService.hasEnoughHistory(userId);

        System.out.println("✅ 用户 " + userId + " 是否有足够历史: " + hasHistory);
        System.out.println("   冷启动阈值: " + cfConfig.getColdStartThreshold());
    }

    @Test
    @Order(10)
    @DisplayName("10. 冷启动判断 - 新用户")
    void testHasEnoughHistoryColdStart() {
        Long userId = 999999L;

        boolean hasHistory = cfService.hasEnoughHistory(userId);

        assertFalse(hasHistory, "新用户应未达到冷启动阈值");
        System.out.println("✅ 新用户无足够历史，将降级到内容匹配");
    }

    // ============ 配置测试 ============

    @Test
    @Order(11)
    @DisplayName("11. CF配置参数验证")
    void testCFConfig() {
        assertNotNull(cfConfig, "CFConfig不应为null");

        System.out.println("✅ CF配置参数:");
        System.out.println("   最小共同交互: " + cfConfig.getMinCommonItems());
        System.out.println("   相似用户TopK: " + cfConfig.getTopKSimilarUsers());
        System.out.println("   CF权重: " + cfConfig.getCfWeight());
        System.out.println("   最小相似度: " + cfConfig.getMinSimilarity());
        System.out.println("   冷启动阈值: " + cfConfig.getColdStartThreshold());
        System.out.println("   缓存启用: " + cfConfig.isEnableCache());
        System.out.println("   缓存过期(秒): " + cfConfig.getCacheExpireSeconds());

        // 验证行为权重
        System.out.println("   行为权重:");
        System.out.println("     查看(1): " + cfConfig.getActionWeight(1));
        System.out.println("     搜索(2): " + cfConfig.getActionWeight(2));
        System.out.println("     收藏(3): " + cfConfig.getActionWeight(3));
        System.out.println("     聊天(4): " + cfConfig.getActionWeight(4));
        System.out.println("     下单(5): " + cfConfig.getActionWeight(5));
    }

    // ============ 缓存测试 ============

    @Test
    @Order(12)
    @DisplayName("12. 缓存刷新测试")
    void testRefreshCache() {
        Long userId = 1L;

        // 刷新单个用户缓存
        assertDoesNotThrow(() -> cfService.refreshSimilarityCache(userId),
                "刷新单用户缓存不应抛异常");
        System.out.println("✅ 用户 " + userId + " 缓存已刷新");

        // 刷新全部缓存
        assertDoesNotThrow(() -> cfService.refreshSimilarityCache(null),
                "刷新全部缓存不应抛异常");
        System.out.println("✅ 全部缓存已刷新");
    }
}
