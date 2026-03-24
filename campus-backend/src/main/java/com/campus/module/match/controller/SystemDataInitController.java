package com.campus.module.match.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.result.Result;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "系统数据维护", description = "用于开发和运维阶段的数据同步")
@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
@Slf4j
public class SystemDataInitController {

    private final TutorProfileMapper tutorProfileMapper;
    private final StringRedisTemplate stringRedisTemplate;

    // 对应你架构中预设的 Redis GEO Key
    private static final String TUTOR_GEO_KEY = "tutor:geo:locations";

    @Operation(summary = "同步教员LBS数据到Redis", description = "从MySQL全量读取教员坐标并批量刷入Redis")
    @PostMapping("/sync-geo")
    public Result<String> syncTutorGeoData() {
        log.info("开始执行教员 LBS 数据全量同步...");
        long startTime = System.currentTimeMillis();

        // 1. 从 MySQL 中查询所有具备坐标的教员
        LambdaQueryWrapper<TutorProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(TutorProfile::getLongitude)
                .isNotNull(TutorProfile::getLatitude);
        // 如果数据量极大（百万级），这里应该用分页查。目前一万条可以直接内存处理
        List<TutorProfile> tutors = tutorProfileMapper.selectList(queryWrapper);

        if (tutors.isEmpty()) {
            return Result.success("数据库中没有带坐标的教员数据，无需同步。");
        }

        // 2. 构造 Redis GEO 批量写入对象
        List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(tutors.size());
        for (TutorProfile tutor : tutors) {
            // 注意：Redis GEO 的标准坐标格式是 (经度 Longitude, 纬度 Latitude)
            Point point = new Point(tutor.getLongitude().doubleValue(), tutor.getLatitude().doubleValue());
            // member 我们存入教员的 ID
            locations.add(new RedisGeoCommands.GeoLocation<>(tutor.getId().toString(), point));
        }

        // 3. 执行 Redis 写入
        try {
            // 强烈建议：先删除旧的 Key，防止有已经注销的教员变成了“幽灵坐标”残留
            stringRedisTemplate.delete(TUTOR_GEO_KEY);

            // 批量 Add（底层会转换为单条 GEOADD 命令携带多个参数，性能极高）
            stringRedisTemplate.opsForGeo().add(TUTOR_GEO_KEY, locations);

            long costTime = System.currentTimeMillis() - startTime;
            log.info("LBS 数据同步成功！共 {} 条，耗时 {} ms", locations.size(), costTime);
            return Result.success("成功同步 " + locations.size() + " 条教员LBS数据，耗时 " + costTime + "ms");
        } catch (Exception e) {
            log.error("Redis GEO 同步失败", e);
            return Result.fail("同步失败: " + e.getMessage());
        }
    }
}