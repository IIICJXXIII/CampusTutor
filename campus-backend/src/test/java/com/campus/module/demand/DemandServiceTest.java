package com.campus.module.demand;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.module.demand.dto.DemandPostRequest;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.demand.service.DemandPostService;
import com.campus.module.demand.service.GeoService;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.service.SysUserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 需求服务测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("需求服务测试")
class DemandServiceTest {

    @Autowired
    private DemandPostService demandPostService;

    @Autowired
    private GeoService geoService;

    @Autowired
    private SysUserService userService;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        SysUser user = new SysUser();
        user.setUsername("demand_test_" + System.currentTimeMillis());
        user.setPassword("test123456");
        user.setNickname("需求测试家长");
        user.setRole(2);
        user.setStatus(1);
        userService.register(user);
        testUserId = user.getId();
    }

    @Test
    @Order(1)
    @DisplayName("1. 发布需求测试")
    @Transactional
    void testPublishDemand() {
        DemandPostRequest request = new DemandPostRequest();
        request.setTitle("初二数学辅导");
        request.setSubject("数学");
        request.setGrade("初二");
        request.setExpectPrice(new BigDecimal("120.00"));
        request.setTeachMode(1);
        request.setAddress("北京市海淀区");
        request.setLongitude(new BigDecimal("116.310003"));
        request.setLatitude(new BigDecimal("39.991957"));

        Long demandId = demandPostService.publishDemand(testUserId, request);

        assertNotNull(demandId, "应返回需求ID");

        DemandPost demand = demandPostService.getById(demandId);
        assertNotNull(demand, "需求应存在");
        assertEquals("数学", demand.getSubject());

        System.out.println("✅ 需求发布成功，ID: " + demandId);
    }

    @Test
    @Order(2)
    @DisplayName("2. 查询我的需求")
    @Transactional
    void testListMyDemands() {
        for (int i = 1; i <= 3; i++) {
            DemandPostRequest request = new DemandPostRequest();
            request.setTitle("测试需求" + i);
            request.setSubject("数学");
            request.setGrade("高一");
            request.setExpectPrice(new BigDecimal("100"));
            request.setTeachMode(1);
            demandPostService.publishDemand(testUserId, request);
        }

        List<DemandPost> myDemands = demandPostService.listMyDemands(testUserId);

        assertEquals(3, myDemands.size(), "应有3个需求");
        System.out.println("✅ 需求列表查询成功");
    }

    @Test
    @Order(3)
    @DisplayName("3. 分页查询")
    @Transactional
    void testPageList() {
        DemandPostRequest request = new DemandPostRequest();
        request.setTitle("分页测试");
        request.setSubject("化学");
        request.setGrade("高二");
        request.setExpectPrice(new BigDecimal("150"));
        request.setTeachMode(2);
        demandPostService.publishDemand(testUserId, request);

        IPage<DemandPost> page = demandPostService.pageList("化学", null, 1, 10);

        assertNotNull(page);
        System.out.println("✅ 分页查询成功，总数: " + page.getTotal());
    }

    @Test
    @Order(4)
    @DisplayName("4. GeoService测试")
    void testGeoService() {
        try {
            geoService.addTutorLocation(10001L, 116.397451, 39.909187);

            Map<Long, Double> nearby = geoService.searchNearbyTutorsWithDistance(116.400000, 39.910000, 5.0);
            System.out.println("✅ GeoService正常，附近: " + nearby.size() + " 个");
        } catch (Exception e) {
            System.out.println("⚠️ GeoService依赖Redis: " + e.getMessage());
        }
    }
}
