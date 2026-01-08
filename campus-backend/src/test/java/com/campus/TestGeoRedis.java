package com.campus;

import com.campus.module.demand.service.GeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;
import java.util.Map;

/**
 * 测试Redis GEO索引功能
 */
@SpringBootApplication
@ComponentScan("com.campus")
public class TestGeoRedis implements CommandLineRunner {

    @Autowired
    private GeoService geoService;

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(TestGeoRedis.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // 添加测试数据到Redis GEO索引
            geoService.addDemandLocation(1L, 121.4737, 31.2304); // 上海
            geoService.addDemandLocation(2L, 121.4896, 31.2396); // 上海
            geoService.addDemandLocation(3L, 121.4338, 31.1925); // 上海
            geoService.addDemandLocation(4L, 116.3975, 39.9088); // 北京
            geoService.addDemandLocation(5L, 116.4107, 39.9151); // 北京

            System.out.println("=== Redis GEO测试 ===");
            System.out.println("1. 添加测试数据完成");

            // 测试搜索附近的需求
            List<Long> demandIds = geoService.searchNearbyDemands(121.4737, 31.2304, 50);
            System.out.println("2. 搜索附近需求结果: " + demandIds);

            // 测试搜索附近的需求(带距离)
            Map<Long, Double> demandIdsWithDistance = geoService.searchNearbyDemandsWithDistance(121.4737, 31.2304, 50);
            System.out.println("3. 搜索附近需求结果(带距离): " + demandIdsWithDistance);

            // 测试计算两点间距离
            Double distance = geoService.calculateDistance(121.4737, 31.2304, 121.4896, 31.2396);
            System.out.println("4. 计算两点间距离: " + distance + " km");

            System.out.println("=== 测试完成 ===");
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭应用
            SpringApplication.exit(applicationContext, () -> 0);
        }
    }
}