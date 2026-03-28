package com.campus.module.match;

import com.campus.module.match.config.IntentConfig;
import com.campus.module.match.config.TrafficPoolConfig;
import com.campus.module.match.dto.TrafficPoolLevel;
import com.campus.module.match.service.RealtimeIntentService;
import com.campus.module.match.service.TrafficPoolService;
import com.campus.module.tutor.entity.TutorProfile;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实时意图追踪与流量池赛马测试
 * 验证 MVP 推荐架构方案中的核心改进
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MVP推荐系统改进测试")
class RecommendImprovementTest {

    @Autowired
    private RealtimeIntentService realtimeIntentService;

    @Autowired
    private TrafficPoolService trafficPoolService;

    @Autowired
    private IntentConfig intentConfig;

    @Autowired
    private TrafficPoolConfig trafficPoolConfig;

    // ============ Phase 1: 实时意图追踪测试 ============

    @Test
    @Order(1)
    @DisplayName("1. IntentConfig 配置加载测试")
    void testIntentConfigLoaded() {
        assertNotNull(intentConfig);
        assertTrue(intentConfig.isEnabled(), "意图追踪应默认启用");
        assertEquals(1.0, intentConfig.getWeightView(), 0.01, "查看权重应为1.0");
        assertEquals(3.0, intentConfig.getWeightFavorite(), 0.01, "收藏权重应为3.0");
        assertEquals(5.0, intentConfig.getWeightChat(), 0.01, "聊天权重应为5.0");
        assertEquals(5.0, intentConfig.getHalfLifeMinutes(), 0.01, "半衰期应为5分钟");
        assertTrue(intentConfig.getDecayLambda() > 0, "衰减常数应大于0");

        System.out.println("IntentConfig 配置加载成功");
        System.out.println("衰减常数 lambda = " + intentConfig.getDecayLambda());
    }

    @Test
    @Order(2)
    @DisplayName("2. 教员标签构建测试")
    void testBuildTutorTags() {
        TutorProfile tutor = new TutorProfile();
        tutor.setTeachSubjects("[\"少儿编程(Scratch/Python)\", \"机器人/3D打印\"]");
        tutor.setTeachGrades("[\"初一\", \"初二\"]");
        tutor.setUniversityName("北京大学");
        tutor.setExpectPrice(new BigDecimal("120"));
        tutor.setEducation(3); // 本科
        tutor.setCanVisit(1);
        tutor.setCanOnline(1);

        List<String> tags = realtimeIntentService.buildTutorTags(tutor);

        assertNotNull(tags);
        assertFalse(tags.isEmpty(), "标签列表不应为空");

        // 验证各维度标签存在
        assertTrue(tags.contains("科目:少儿编程(Scratch/Python)"), "应包含科目标签");
        assertTrue(tags.contains("科目:机器人/3D打印"), "应包含科目标签");
        assertTrue(tags.contains("大学:北京大学"), "应包含大学标签");
        assertTrue(tags.contains("价格带:100-150"), "应包含价格带标签");
        assertTrue(tags.contains("学历:本科"), "应包含学历标签");
        assertTrue(tags.contains("授课方式:上门"), "应包含上门标签");
        assertTrue(tags.contains("授课方式:网课"), "应包含网课标签");
        assertTrue(tags.contains("年级:初一"), "应包含年级标签");

        System.out.println("教员标签构建成功: " + tags);
    }

    @Test
    @Order(3)
    @DisplayName("3. 价格带离散化测试")
    void testPriceBandDiscretization() {
        // 通过构建不同价格的教员来测试离散化
        TutorProfile tutor50 = new TutorProfile();
        tutor50.setExpectPrice(new BigDecimal("50"));
        List<String> tags50 = realtimeIntentService.buildTutorTags(tutor50);
        assertTrue(tags50.contains("价格带:40-70"), "50元应在40-70价格带");

        TutorProfile tutor80 = new TutorProfile();
        tutor80.setExpectPrice(new BigDecimal("80"));
        List<String> tags80 = realtimeIntentService.buildTutorTags(tutor80);
        assertTrue(tags80.contains("价格带:70-100"), "80元应在70-100价格带");

        TutorProfile tutor130 = new TutorProfile();
        tutor130.setExpectPrice(new BigDecimal("130"));
        List<String> tags130 = realtimeIntentService.buildTutorTags(tutor130);
        assertTrue(tags130.contains("价格带:100-150"), "130元应在100-150价格带");

        TutorProfile tutor250 = new TutorProfile();
        tutor250.setExpectPrice(new BigDecimal("250"));
        List<String> tags250 = realtimeIntentService.buildTutorTags(tutor250);
        assertTrue(tags250.contains("价格带:200+"), "250元应在200+价格带");

        System.out.println("价格带离散化正确");
    }

    @Test
    @Order(4)
    @DisplayName("4. 意图加分权重配置测试")
    void testActionWeights() {
        assertEquals(1.0, intentConfig.getActionWeight(1), "查看=1.0");
        assertEquals(0.0, intentConfig.getActionWeight(2), "搜索=0（不计入意图）");
        assertEquals(3.0, intentConfig.getActionWeight(3), "收藏=3.0");
        assertEquals(5.0, intentConfig.getActionWeight(4), "聊天=5.0");
        assertEquals(8.0, intentConfig.getActionWeight(5), "下单=8.0");

        System.out.println("行为权重配置正确");
    }

    @Test
    @Order(5)
    @DisplayName("5. 空用户意图加分应为0")
    void testIntentBoostForNullUser() {
        TutorProfile tutor = new TutorProfile();
        tutor.setId(1L);

        double boost = realtimeIntentService.calculateIntentBoost(null, tutor);
        assertEquals(0.0, boost, 0.01, "空用户意图加分应为0");

        System.out.println("空用户意图加分验证通过");
    }

    // ============ Phase 2: 流量池赛马测试 ============

    @Test
    @Order(6)
    @DisplayName("6. TrafficPoolConfig 配置加载测试")
    void testTrafficPoolConfigLoaded() {
        assertNotNull(trafficPoolConfig);
        assertTrue(trafficPoolConfig.isEnabled(), "流量池应默认启用");
        assertEquals(5.0, trafficPoolConfig.getBasicBoost(), 0.01, "BASIC加分应为5.0");
        assertEquals(3.0, trafficPoolConfig.getWarmBoost(), 0.01, "WARM加分应为3.0");
        assertEquals(8.0, trafficPoolConfig.getHotBoost(), 0.01, "HOT加分应为8.0");

        System.out.println("TrafficPoolConfig 配置加载成功");
    }

    @Test
    @Order(7)
    @DisplayName("7. 流量池加分值测试")
    void testPoolBoostScores() {
        double basicBoost = trafficPoolService.getPoolBoostScore(TrafficPoolLevel.BASIC);
        double warmBoost = trafficPoolService.getPoolBoostScore(TrafficPoolLevel.WARM);
        double hotBoost = trafficPoolService.getPoolBoostScore(TrafficPoolLevel.HOT);

        assertEquals(5.0, basicBoost, 0.01, "BASIC池加分应为5.0");
        assertEquals(3.0, warmBoost, 0.01, "WARM池加分应为3.0");
        assertEquals(8.0, hotBoost, 0.01, "HOT池加分应为8.0");

        // HOT > BASIC > WARM
        assertTrue(hotBoost > basicBoost, "HOT加分应大于BASIC");
        assertTrue(basicBoost > warmBoost, "BASIC加分应大于WARM（新教员保护）");

        System.out.println("流量池加分值验证通过");
    }

    @Test
    @Order(8)
    @DisplayName("8. 流量池标签测试")
    void testPoolTags() {
        String hotTag = trafficPoolService.getPoolTag(TrafficPoolLevel.HOT);
        String basicTag = trafficPoolService.getPoolTag(TrafficPoolLevel.BASIC);
        String warmTag = trafficPoolService.getPoolTag(TrafficPoolLevel.WARM);

        assertNotNull(hotTag, "HOT池应有标签");
        assertNotNull(basicTag, "BASIC池应有标签");
        assertNull(warmTag, "WARM池无特殊标签");

        System.out.println("流量池标签验证通过");
        System.out.println("HOT标签: " + hotTag);
        System.out.println("BASIC标签: " + basicTag);
    }

    @Test
    @Order(9)
    @DisplayName("9. 流量池级别枚举测试")
    void testTrafficPoolLevelEnum() {
        assertEquals("基础池", TrafficPoolLevel.BASIC.getDisplayName());
        assertEquals("验证池", TrafficPoolLevel.WARM.getDisplayName());
        assertEquals("热门池", TrafficPoolLevel.HOT.getDisplayName());

        assertEquals("新晋教员", TrafficPoolLevel.BASIC.getTag());
        assertEquals("潜力教员", TrafficPoolLevel.WARM.getTag());

        System.out.println("流量池枚举验证通过");
    }

    @Test
    @Order(10)
    @DisplayName("10. 指数衰减常数计算验证")
    void testDecayLambdaCalculation() {
        // 半衰期5分钟 -> lambda = ln(2)/5 ≈ 0.1386
        double lambda = intentConfig.getDecayLambda();
        double expected = Math.log(2) / 5.0;
        assertEquals(expected, lambda, 0.0001, "衰减常数应等于 ln(2)/半衰期");

        // 验证5分钟后权重衰减到一半
        double decayAfter5Min = Math.exp(-lambda * 5.0);
        assertEquals(0.5, decayAfter5Min, 0.0001, "5分钟后衰减因子应为0.5");

        // 验证10分钟后权重衰减到四分之一
        double decayAfter10Min = Math.exp(-lambda * 10.0);
        assertEquals(0.25, decayAfter10Min, 0.0001, "10分钟后衰减因子应为0.25");

        System.out.println("指数衰减常数验证通过: lambda=" + lambda);
        System.out.println("5分钟后衰减: " + decayAfter5Min);
        System.out.println("10分钟后衰减: " + decayAfter10Min);
    }
}
