package com.campus.module.demand.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.demand.mapper.DemandPostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 需求地理位置同步服务
 * 应用启动时将数据库中所有上架且有坐标的需求同步到 Redis GEO 索引
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemandGeoSyncService {

    private final DemandPostMapper demandPostMapper;
    private final GeoService geoService;

    /**
     * 应用启动完成后，将所有上架需求同步到 Redis GEO 索引。
     * 确保 Redis GEO 索引与数据库保持一致，解决直接插入数据库的需求
     * 不在 Redis GEO 中的问题。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void syncDemandGeoOnStartup() {
        log.info("[GEO同步] 开始同步需求地理位置到 Redis GEO 索引...");

        try {
            // 查询所有上架、未匹配、有坐标的需求
            LambdaQueryWrapper<DemandPost> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DemandPost::getStatus, 1)
                    .isNull(DemandPost::getMatchedTutorId)
                    .isNotNull(DemandPost::getLongitude)
                    .isNotNull(DemandPost::getLatitude);
            List<DemandPost> demands = demandPostMapper.selectList(wrapper);

            int successCount = 0;
            int failCount = 0;

            for (DemandPost demand : demands) {
                try {
                    geoService.addDemandLocation(
                            demand.getId(),
                            demand.getLongitude().doubleValue(),
                            demand.getLatitude().doubleValue());
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.warn("[GEO同步] 需求 {} 同步失败: {}", demand.getId(), e.getMessage());
                }
            }

            log.info("[GEO同步] 需求地理位置同步完成: 总数={}, 成功={}, 失败={}",
                    demands.size(), successCount, failCount);
        } catch (Exception e) {
            log.error("[GEO同步] 需求地理位置同步异常，不影响主业务", e);
        }
    }
}
