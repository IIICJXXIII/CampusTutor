package com.campus.module.match;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.demand.service.GeoService;
import com.campus.module.match.dto.MatchScoreResult;
import com.campus.module.match.dto.TutorSearchRequest;
import com.campus.module.match.dto.TutorSearchResult;
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
        request.setSubject("数学");
        request.setPage(1);
        request.setSize(10);

        IPage<TutorSearchResult> result = matchService.searchTutors(request);
        
        assertNotNull(result);
        System.out.println("✅ 科目筛选成功，数学教员: " + result.getTotal());
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
        request.setSubject("数学");
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
        profile.setTeachSubjects("[\"数学\", \"物理\"]");
        profile.setTeachGrades("[\"初一\", \"初二\", \"初三\"]");
        profile.setExpectPrice(new BigDecimal("120"));
        profile.setCertStatus(2);

        DemandPost demand = new DemandPost();
        demand.setSubject("数学");
        demand.setGrade("初二");
        demand.setExpectPrice(new BigDecimal("120"));

        MatchScoreResult score = scoreCalculator.calculateScore(profile, demand, 2.0);
        
        assertNotNull(score);
        System.out.println("✅ 匹配评分计算成功");
        System.out.println("综合分: " + score.getMatchScore());
        System.out.println("科目分: " + score.getSubjectScore());
        System.out.println("年级分: " + score.getGradeScore());
    }
}
