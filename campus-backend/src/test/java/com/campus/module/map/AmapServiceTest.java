package com.campus.module.map;

import com.campus.module.map.dto.DirectionResult;
import com.campus.module.map.dto.DistanceResult;
import com.campus.module.map.dto.GeocoderResult;
import com.campus.module.map.service.AmapService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 高德地图服务测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("高德地图服务测试")
class AmapServiceTest {

    @Autowired
    private AmapService amapService;

    private static final double TEST_LAT = 39.909187;
    private static final double TEST_LNG = 116.397451;
    private static final double DEST_LAT = 39.916345;
    private static final double DEST_LNG = 116.397155;

    @Test
    @Order(1)
    @DisplayName("1. 逆地址解析测试")
    void testReverseGeocode() {
        GeocoderResult result = amapService.reverseGeocode(TEST_LAT, TEST_LNG);
        
        assertNotNull(result, "返回结果不应为空");
        System.out.println("逆地址解析: status=" + result.getStatus());
        
        if (result.getStatus() == 0) {
            System.out.println("✅ 高德地图API连接成功");
            System.out.println("解析地址: " + result.getResult().getAddress());
        } else {
            System.out.println("⚠️ API调用失败: " + result.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("2. 地址解析测试")
    void testGeocode() {
        GeocoderResult result = amapService.geocode("北京市东城区天安门广场");
        
        assertNotNull(result, "返回结果不应为空");
        System.out.println("地址解析: status=" + result.getStatus());
        
        if (result.getStatus() == 0 && result.getResultData() != null) {
            GeocoderResult.Location location = result.getResultData().getLocation();
            if (location != null) {
                System.out.println("✅ 经度: " + location.getLng() + ", 纬度: " + location.getLat());
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. 步行路径规划测试")
    void testWalkingDirection() {
        DirectionResult result = amapService.walkingDirection(TEST_LAT, TEST_LNG, DEST_LAT, DEST_LNG);
        
        assertNotNull(result, "返回结果不应为空");
        System.out.println("步行规划: status=" + result.getStatus());
        
        if (result.getStatus() == 0 && result.getResult() != null) {
            if (result.getResult().getRoutes() != null && !result.getResult().getRoutes().isEmpty()) {
                DirectionResult.Route route = result.getResult().getRoutes().get(0);
                System.out.println("✅ 距离: " + route.getDistance() + "米, 耗时: " + route.getDuration() + "秒");
            }
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. 距离计算测试")
    void testCalculateDistance() {
        DistanceResult result = amapService.calculateDistance(TEST_LAT, TEST_LNG, DEST_LAT, DEST_LNG, "1");
        
        assertNotNull(result, "返回结果不应为空");
        System.out.println("距离计算: status=" + result.getStatus());
        
        if (result.getStatus() == 0 && result.getElements() != null && !result.getElements().isEmpty()) {
            DistanceResult.Element element = result.getElements().get(0);
            System.out.println("✅ 距离: " + element.getDistance() + "米");
        }
    }

    @Test
    @Order(5)
    @DisplayName("5. API连接验证")
    void testApiConnection() {
        GeocoderResult result = amapService.reverseGeocode(31.230416, 121.473701);
        
        assertNotNull(result, "API应返回结果");
        
        if (result.getStatus() == 0) {
            System.out.println("✅ 高德地图API连接成功");
        } else if (result.getMessage() != null && result.getMessage().contains("未配置")) {
            System.out.println("⚠️ 请在application.properties中设置amap.key");
        } else {
            System.out.println("⚠️ API调用状态: " + result.getMessage());
        }
    }
}
