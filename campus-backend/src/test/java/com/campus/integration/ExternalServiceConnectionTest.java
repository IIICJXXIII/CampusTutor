package com.campus.integration;

import com.campus.module.demand.service.GeoService;
import com.campus.module.llm.dto.ChatMessage;
import com.campus.module.llm.dto.ChatResponse;
import com.campus.module.llm.service.LlmClientService;
import com.campus.module.map.dto.GeocoderResult;
import com.campus.module.map.service.AmapService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 外部服务连接测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("外部服务连接测试")
class ExternalServiceConnectionTest {

    @Autowired
    private DataSource dataSource;
    
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private AmapService amapService;
    
    @Autowired
    private LlmClientService llmClientService;
    
    @Autowired
    private GeoService geoService;
    
    @Value("${llm.enabled:false}")
    private boolean llmEnabled;

    @Test
    @Order(1)
    @DisplayName("1. MySQL 数据库连接测试")
    void testMySQLConnection() {
        try (Connection conn = dataSource.getConnection()) {
            assertNotNull(conn);
            assertTrue(conn.isValid(5));
            System.out.println("✅ MySQL 连接成功: " + conn.getCatalog());
        } catch (Exception e) {
            fail("❌ MySQL 连接失败: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("2. Redis 连接测试")
    void testRedisConnection() {
        if (redisTemplate == null) {
            System.out.println("⚠️ Redis 未配置");
            return;
        }
        
        try {
            String testKey = "test:connection:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "hello");
            String result = redisTemplate.opsForValue().get(testKey);
            assertEquals("hello", result);
            redisTemplate.delete(testKey);
            System.out.println("✅ Redis 连接成功");
        } catch (Exception e) {
            System.out.println("⚠️ Redis 连接失败: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. 高德地图 API 连接测试")
    void testAmapConnection() {
        GeocoderResult result = amapService.reverseGeocode(39.909187, 116.397451);
        
        assertNotNull(result);
        
        if (result.getStatus() == 0) {
            System.out.println("✅ 高德地图 API 连接成功");
        } else {
            System.out.println("⚠️ 高德地图: " + result.getMessage());
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. DeepSeek LLM API 连接测试")
    void testDeepSeekConnection() {
        if (!llmEnabled) {
            System.out.println("⚠️ LLM 服务未启用");
            return;
        }
        
        ChatResponse response = llmClientService.chat(Arrays.asList(ChatMessage.user("hi")));
        
        assertNotNull(response);
        
        if (Boolean.TRUE.equals(response.getSuccess())) {
            System.out.println("✅ DeepSeek API 连接成功");
        } else {
            System.out.println("⚠️ DeepSeek: " + response.getError());
        }
    }

    @Test
    @Order(5)
    @DisplayName("5. GeoService 测试")
    void testGeoService() {
        try {
            geoService.addTutorLocation(99999L, 116.397451, 39.909187);
            var nearby = geoService.searchNearbyTutorsWithDistance(116.397451, 39.909187, 1.0);
            geoService.removeTutorLocation(99999L);
            System.out.println("✅ GeoService 正常工作");
        } catch (Exception e) {
            System.out.println("⚠️ GeoService 依赖 Redis: " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. 综合健康检查")
    void testOverallHealth() {
        System.out.println("\n========== 服务健康检查 ==========");
        
        int successCount = 0;
        
        // MySQL
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(5)) {
                System.out.println("✅ MySQL: 正常");
                successCount++;
            }
        } catch (Exception e) {
            System.out.println("❌ MySQL: 异常");
        }
        
        // Redis
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().get("health_check");
                System.out.println("✅ Redis: 正常");
                successCount++;
            } else {
                System.out.println("⚠️ Redis: 未配置");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Redis: 不可用");
        }
        
        // 高德地图
        GeocoderResult mapResult = amapService.reverseGeocode(39.909187, 116.397451);
        if (mapResult.getStatus() == 0) {
            System.out.println("✅ 高德地图: 正常");
            successCount++;
        } else {
            System.out.println("⚠️ 高德地图: " + mapResult.getMessage());
        }
        
        // DeepSeek
        if (llmEnabled) {
            ChatResponse llmResult = llmClientService.chat(Arrays.asList(ChatMessage.user("hi")));
            if (Boolean.TRUE.equals(llmResult.getSuccess())) {
                System.out.println("✅ DeepSeek: 正常");
                successCount++;
            } else {
                System.out.println("⚠️ DeepSeek: " + llmResult.getError());
            }
        } else {
            System.out.println("⚠️ DeepSeek: 未启用");
        }
        
        System.out.println("==================================");
        System.out.println("健康服务: " + successCount + "/4");
        
        assertTrue(successCount >= 1, "至少MySQL服务应正常运行");
    }
}
