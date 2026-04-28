package com.campus.module.match;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.module.behavior.dto.TutorBehaviorStats;
import com.campus.module.behavior.service.BehaviorService;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.demand.service.GeoService;
import com.campus.module.match.dto.MatchScoreResult;
import com.campus.module.match.dto.TutorSearchRequest;
import com.campus.module.match.dto.TutorSearchResult;
import com.campus.module.match.dto.WeightConfig;
import com.campus.module.match.service.DynamicWeightCalculator;
import com.campus.module.match.service.MatchScoreCalculator;
import com.campus.module.match.service.MatchService;
import com.campus.module.tutor.entity.TutorProfile;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 匹配服务测试
 * 包含动态权重、热度评分和行为信号测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("匹配服务测试")
class MatchServiceTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchScoreCalculator scoreCalculator;

    @Autowired
    private GeoService geoService;

    @Autowired
    private BehaviorService behaviorService;

    @Autowired
    private DynamicWeightCalculator dynamicWeightCalculator;

    @Test
    @Order(1)
    @DisplayName("1. 基础搜索测试")
    void testBasicSearch() {
        TutorSearchRequest request = new TutorSearchRequest();
        request.setPage(1);
        request.setSize(10);

        IPage<TutorSearchResult> result = matchService.searchTutors(request);

        assertNotNull(result);
        System.out.println("✅ 基础搜索成功，总数: " + result.getTotal());
    }

    @Test
    @Order(2)
    @DisplayName("2. 科目筛选测试")
    void testSubjectFilter() {
        TutorSearchRequest request = new TutorSearchRequest();
        request.setSubject("少儿编程(Scratch/Python)");
        request.setPage(1);
        request.setSize(10);

        IPage<TutorSearchResult> result = matchService.searchTutors(request);

        assertNotNull(result);
        System.out.println("✅ 科目筛选成功，编程教员: " + result.getTotal());
    }

    @Test
    @Order(3)
    @DisplayName("3. 价格区间筛选")
    void testPriceRangeFilter() {
        TutorSearchRequest request = new TutorSearchRequest();
        request.setMinPrice(new BigDecimal("80"));
        request.setMaxPrice(new BigDecimal("150"));
        request.setPage(1);
        request.setSize(10);

        IPage<TutorSearchResult> result = matchService.searchTutors(request);

        assertNotNull(result);
        System.out.println("✅ 价格筛选成功，符合条件: " + result.getTotal());
    }

    @Test
    @Order(4)
    @DisplayName("4. 位置筛选测试")
    void testLocationFilter() {
        geoService.addTutorLocation(1L, 116.397451, 39.909187);

        TutorSearchRequest request = new TutorSearchRequest();
        request.setLongitude(116.400000);
        request.setLatitude(39.910000);
        request.setRadius(5.0);
        request.setPage(1);
        request.setSize(10);

        IPage<TutorSearchResult> result = matchService.searchTutors(request);

        assertNotNull(result);
        System.out.println("✅ 位置筛选成功，附近教员: " + result.getTotal());
    }

    @Test
    @Order(5)
    @DisplayName("5. 组合条件搜索")
    void testCombinedSearch() {
        TutorSearchRequest request = new TutorSearchRequest();
        request.setSubject("少儿编程(Scratch/Python)");
        request.setGrade("初二");
        request.setMinPrice(new BigDecimal("100"));
        request.setMaxPrice(new BigDecimal("200"));
        request.setPage(1);
        request.setSize(10);

        IPage<TutorSearchResult> result = matchService.searchTutors(request);

        assertNotNull(result);
        System.out.println("✅ 组合搜索成功，符合条件: " + result.getTotal());
    }

    @Test
    @Order(6)
    @DisplayName("6. 匹配评分计算器测试")
    void testScoreCalculator() {
        TutorProfile profile = new TutorProfile();
        profile.setId(1L);
        profile.setUserId(1L);
        profile.setTeachSubjects("[\"少儿编程(Scratch/Python)\", \"机器人/3D打印\"]");
        profile.setTeachGrades("[\"初一\", \"初二\", \"初三\"]");
        profile.setExpectPrice(new BigDecimal("120"));
        profile.setCertStatus(2);

        DemandPost demand = new DemandPost();
        demand.setSubject("少儿编程(Scratch/Python)");
        demand.setGrade("初二");
        demand.setExpectPrice(new BigDecimal("120"));

        MatchScoreResult score = scoreCalculator.calculateScore(profile, demand, 2.0);

        assertNotNull(score);
        System.out.println("✅ 匹配评分计算成功");
        System.out.println("综合分: " + score.getMatchScore());
        System.out.println("科目分: " + score.getSubjectScore());
        System.out.println("年级分: " + score.getGradeScore());
    }

    // ============ 新增测试：动态权重 ============

    @Test
    @Order(7)
    @DisplayName("7. 动态权重 - 新用户配置")
    void testDynamicWeightsForNewUser() {
        // 新用户（搜索次数 < 3）
        WeightConfig config = dynamicWeightCalculator.getWeightsBySearchCount(0);

        assertNotNull(config);
        // 新用户评分权重应该更高
        assertEquals(15.0, config.getRatingWeight(), 0.1, "新用户评分权重应为15%");
        // 新用户学历权重应该更高
        assertEquals(8.0, config.getEducationWeight(), 0.1, "新用户学历权重应为8%");
        // 新用户热度权重应该更低
        assertEquals(2.0, config.getHotnessWeight(), 0.1, "新用户热度权重应为2%");

        System.out.println("✅ 新用户动态权重配置正确");
        System.out.println("评分权重: " + config.getRatingWeight() + "%");
        System.out.println("学历权重: " + config.getEducationWeight() + "%");
        System.out.println("热度权重: " + config.getHotnessWeight() + "%");
    }

    @Test
    @Order(8)
    @DisplayName("8. 动态权重 - 活跃用户配置")
    void testDynamicWeightsForActiveUser() {
        // 活跃用户（搜索次数 >= 10）
        WeightConfig config = dynamicWeightCalculator.getWeightsBySearchCount(15);

        assertNotNull(config);
        // 活跃用户热度权重应该更高
        assertEquals(10.0, config.getHotnessWeight(), 0.1, "活跃用户热度权重应为10%");
        // 活跃用户评分权重应该更低
        assertEquals(7.0, config.getRatingWeight(), 0.1, "活跃用户评分权重应为7%");

        System.out.println("✅ 活跃用户动态权重配置正确");
        System.out.println("热度权重: " + config.getHotnessWeight() + "%");
        System.out.println("评分权重: " + config.getRatingWeight() + "%");
    }

    @Test
    @Order(9)
    @DisplayName("9. 动态权重 - 普通用户配置")
    void testDynamicWeightsForNormalUser() {
        // 普通用户（3 <= 搜索次数 < 10）
        WeightConfig config = dynamicWeightCalculator.getWeightsBySearchCount(5);

        assertNotNull(config);
        // 普通用户使用默认权重
        assertEquals(10.0, config.getRatingWeight(), 0.1, "普通用户评分权重应为10%");
        assertEquals(5.0, config.getHotnessWeight(), 0.1, "普通用户热度权重应为5%");

        System.out.println("✅ 普通用户动态权重配置正确");
    }

    // ============ 新增测试：热度评分 ============

    @Test
    @Order(10)
    @DisplayName("10. 热度评分计算测试")
    void testHotnessScoreCalculation() {
        TutorBehaviorStats stats = new TutorBehaviorStats();
        stats.setViewCount24h(25); // 24h浏览量
        stats.setChatCount24h(5); // 24h聊天数
        stats.setFavoriteCount(10); // 总收藏数

        double hotnessScore = behaviorService.calculateHotnessScore(stats);

        // 热度分计算：
        // viewScore = min(25/50, 1.0) * 30 = 15
        // chatScore = min(5/10, 1.0) * 40 = 20
        // favScore = min(10/20, 1.0) * 30 = 15
        // total = 50
        assertTrue(hotnessScore >= 0 && hotnessScore <= 100, "热度分应在0-100之间");
        assertEquals(50.0, hotnessScore, 0.1, "热度分计算应为50");

        System.out.println("✅ 热度评分计算正确: " + hotnessScore);
    }

    @Test
    @Order(11)
    @DisplayName("11. 零行为热度评分测试")
    void testZeroBehaviorHotnessScore() {
        TutorBehaviorStats stats = new TutorBehaviorStats();
        stats.setViewCount24h(0);
        stats.setChatCount24h(0);
        stats.setFavoriteCount(0);

        double hotnessScore = behaviorService.calculateHotnessScore(stats);

        assertEquals(0.0, hotnessScore, 0.1, "无行为时热度分应为0");
        System.out.println("✅ 零行为热度评分正确: " + hotnessScore);
    }

    @Test
    @Order(12)
    @DisplayName("12. 满热度评分测试")
    void testMaxHotnessScore() {
        TutorBehaviorStats stats = new TutorBehaviorStats();
        stats.setViewCount24h(100); // 超过阈值
        stats.setChatCount24h(20); // 超过阈值
        stats.setFavoriteCount(30); // 超过阈值

        double hotnessScore = behaviorService.calculateHotnessScore(stats);

        assertEquals(100.0, hotnessScore, 0.1, "超阈值时热度分应为100");
        System.out.println("✅ 满热度评分正确: " + hotnessScore);
    }

    // ============ 新增测试：带行为信号的评分 ============

    @Test
    @Order(13)
    @DisplayName("13. 带热度的匹配评分计算")
    void testScoreCalculatorWithBehavior() {
        TutorProfile profile = new TutorProfile();
        profile.setId(1L);
        profile.setUserId(1L);
        profile.setTeachSubjects("[\"少儿编程(Scratch/Python)\", \"机器人/3D打印\"]");
        profile.setTeachGrades("[\"初一\", \"初二\", \"初三\"]");
        profile.setExpectPrice(new BigDecimal("120"));
        profile.setRating(new BigDecimal("4.8"));
        profile.setOrderCount(30);
        profile.setEducation(3); // 本科
        profile.setCertStatus(2);

        WeightConfig weights = WeightConfig.defaultConfig();
        Double hotnessScore = 60.0; // 热门教员

        MatchScoreResult score = scoreCalculator.calculateScoreWithBehavior(
                profile,
                "少儿编程(Scratch/Python)",
                "初二",
                2.0,
                new BigDecimal("150"),
                hotnessScore,
                weights.getSubjectWeight(),
                weights.getGradeWeight(),
                weights.getDistanceWeight(),
                weights.getPriceWeight(),
                weights.getRatingWeight(),
                weights.getExperienceWeight(),
                weights.getEducationWeight(),
                weights.getSpecialtyWeight(),
                weights.getHotnessWeight());

        assertNotNull(score);
        assertNotNull(score.getHotnessScore(), "应有热度分");
        assertTrue(score.getHotnessScore() > 0, "热度分应大于0");
        assertTrue(score.getMatchTags().contains("热门教员"), "应有热门教员标签");

        System.out.println("✅ 带热度评分计算成功");
        System.out.println("综合分: " + score.getMatchScore());
        System.out.println("热度分: " + score.getHotnessScore());
        System.out.println("标签: " + score.getMatchTags());
    }

    @Test
    @Order(14)
    @DisplayName("14. 不同用户类型搜索结果差异")
    void testSearchResultsForDifferentUserTypes() {
        // 新用户搜索
        TutorSearchRequest newUserRequest = new TutorSearchRequest();
        newUserRequest.setUserId(99999L); // 假设这是新用户
        newUserRequest.setSubject("少儿编程(Scratch/Python)");
        newUserRequest.setPage(1);
        newUserRequest.setSize(10);

        IPage<TutorSearchResult> newUserResult = matchService.searchTutors(newUserRequest);
        assertNotNull(newUserResult);

        System.out.println("✅ 不同用户类型搜索测试完成");
        System.out.println("新用户搜索结果数: " + newUserResult.getTotal());
    }
}
