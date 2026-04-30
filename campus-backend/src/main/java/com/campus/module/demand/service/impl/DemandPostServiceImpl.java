package com.campus.module.demand.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.ResultCode;
import com.campus.module.demand.dto.DemandPostRequest;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.demand.mapper.DemandPostMapper;
import com.campus.module.demand.service.DemandPostService;
import com.campus.module.demand.service.GeoService;
import com.campus.module.map.dto.GeocoderResult;
import com.campus.module.map.service.MapService;
import com.campus.module.match.service.MatchScoreCalculator;
import com.campus.module.order.service.CourseOrderService;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 需求帖子Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemandPostServiceImpl extends ServiceImpl<DemandPostMapper, DemandPost>
        implements DemandPostService {

    private final GeoService geoService;
    private final MapService mapService;
    private final TutorProfileMapper tutorProfileMapper;
    private final MatchScoreCalculator matchScoreCalculator;
    private final CourseOrderService courseOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishDemand(Long publisherId, DemandPostRequest request) {
        DemandPost demand = new DemandPost();
        demand.setPublisherId(publisherId);
        demand.setStudentId(request.getStudentId());
        demand.setTitle(request.getTitle());
        demand.setSubject(request.getSubject());
        demand.setGrade(request.getGrade());
        demand.setExpectPrice(request.getExpectPrice());
        if (request.getScheduleRequire() != null) {
            demand.setScheduleRequire(JSONUtil.toJsonStr(request.getScheduleRequire()));
        }
        demand.setTeachMode(request.getTeachMode());
        demand.setLongitude(request.getLongitude());
        demand.setLatitude(request.getLatitude());
        demand.setAddress(request.getAddress());
        demand.setDetail(request.getDetail());
        demand.setStatus(1); // 默认上架
        save(demand);

        // 如果有位置信息，加入GEO索引
        if (request.getLongitude() != null && request.getLatitude() != null) {
            geoService.addDemandLocation(demand.getId(),
                    request.getLongitude().doubleValue(),
                    request.getLatitude().doubleValue());
        }

        return demand.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDemand(Long publisherId, DemandPostRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "需求ID不能为空");
        }
        DemandPost demand = getById(request.getId());
        if (demand == null || !demand.getPublisherId().equals(publisherId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "需求不存在或无权限");
        }

        demand.setStudentId(request.getStudentId());
        demand.setTitle(request.getTitle());
        demand.setSubject(request.getSubject());
        demand.setGrade(request.getGrade());
        demand.setExpectPrice(request.getExpectPrice());
        if (request.getScheduleRequire() != null) {
            demand.setScheduleRequire(JSONUtil.toJsonStr(request.getScheduleRequire()));
        }
        demand.setTeachMode(request.getTeachMode());
        demand.setLongitude(request.getLongitude());
        demand.setLatitude(request.getLatitude());
        demand.setAddress(request.getAddress());
        demand.setDetail(request.getDetail());
        updateById(demand);

        // 更新GEO索引
        geoService.removeDemandLocation(demand.getId());
        if (request.getLongitude() != null && request.getLatitude() != null) {
            geoService.addDemandLocation(demand.getId(),
                    request.getLongitude().doubleValue(),
                    request.getLatitude().doubleValue());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long publisherId, Long demandId, Integer status) {
        DemandPost demand = getById(demandId);
        if (demand == null || !demand.getPublisherId().equals(publisherId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "需求不存在或无权限");
        }
        demand.setStatus(status);
        updateById(demand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDemand(Long publisherId, Long demandId) {
        DemandPost demand = getById(demandId);
        if (demand == null || !demand.getPublisherId().equals(publisherId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "需求不存在或无权限");
        }
        removeById(demandId);
        // 移除GEO索引
        geoService.removeDemandLocation(demandId);
    }

    @Override
    public List<DemandPost> listMyDemands(Long publisherId) {
        List<DemandPost> demands = list(new LambdaQueryWrapper<DemandPost>()
                .eq(DemandPost::getPublisherId, publisherId)
                .orderByDesc(DemandPost::getCreateTime));
        // 填充申请数量
        for (DemandPost demand : demands) {
            long count = com.baomidou.mybatisplus.extension.toolkit.Db.lambdaQuery(
                    com.campus.module.demand.entity.TutorApplication.class)
                    .eq(com.campus.module.demand.entity.TutorApplication::getDemandId, demand.getId())
                    .eq(com.campus.module.demand.entity.TutorApplication::getStatus, 0)
                    .count();
            demand.setApplyCount((int) count);
        }
        return demands;
    }

    @Override
    public IPage<DemandPost> pageList(String subject, String grade, Integer page, Integer size) {
        Page<DemandPost> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DemandPost> wrapper = new LambdaQueryWrapper<DemandPost>()
                .eq(DemandPost::getStatus, 1) // 只查上架的
                .isNull(DemandPost::getMatchedTutorId); // 过滤掉已被接单的需求

        if (StringUtils.hasText(subject)) {
            wrapper.eq(DemandPost::getSubject, subject);
        }
        if (StringUtils.hasText(grade)) {
            wrapper.eq(DemandPost::getGrade, grade);
        }
        wrapper.orderByDesc(DemandPost::getCreateTime);
        return page(pageParam, wrapper);
    }

    @Override
    public List<DemandPost> searchNearby(Double longitude, Double latitude, Double radiusKm, String subject, String grade) {
        if (longitude == null || latitude == null) {
            return new ArrayList<>();
        }
        double radius = radiusKm != null ? radiusKm : 10.0;
        log.info("搜索附近需求: 经度={}, 纬度={}, 半径={}km, 科目={}, 年级={}", longitude, latitude, radius, subject, grade);

        List<DemandPost> demands = searchNearbyByDatabase(longitude, latitude, radius, subject, grade);
        log.info("数据库 Haversine 返回 {} 条附近需求", demands.size());

        // 按距离升序排序
        final Double searchLon = longitude;
        final Double searchLat = latitude;
        demands.sort(Comparator.comparingDouble(d -> geoService.calculateDistance(searchLon, searchLat,
                d.getLongitude().doubleValue(), d.getLatitude().doubleValue())));

        // 只返回上架状态的
        demands.removeIf(d -> d.getStatus() != 1);

        // 过滤掉已被接单的需求
        demands.removeIf(d -> d.getMatchedTutorId() != null);

        // 过滤掉自己发布的需求
        Long currentUserId = com.campus.common.context.UserContext.getUserId();
        if (currentUserId != null) {
            demands.removeIf(d -> d.getPublisherId().equals(currentUserId));
        }

        // 为缺少 address 的需求通过反向地理编码补全地址
        fillMissingAddresses(demands);

        log.info("最终返回 {} 条附近需求", demands.size());
        return demands;
    }

    /**
     * 数据库降级搜索：使用 Haversine 公式在 SQL 层面按距离筛选
     */
    private List<DemandPost> searchNearbyByDatabase(Double longitude, Double latitude, double radiusKm, String subject, String grade) {
        LambdaQueryWrapper<DemandPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DemandPost::getStatus, 1)
                .isNotNull(DemandPost::getLongitude)
                .isNotNull(DemandPost::getLatitude);
        if (subject != null && !subject.isEmpty()) {
            wrapper.eq(DemandPost::getSubject, subject);
        }
        if (grade != null && !grade.isEmpty()) {
            wrapper.eq(DemandPost::getGrade, grade);
        }
        List<DemandPost> allDemands = list(wrapper);

        // 使用 Haversine 公式过滤并排序
        List<DemandPost> nearbyDemands = new ArrayList<>();
        for (DemandPost demand : allDemands) {
            double distance = geoService.calculateDistance(
                    longitude, latitude,
                    demand.getLongitude().doubleValue(), demand.getLatitude().doubleValue());
            if (distance <= radiusKm) {
                nearbyDemands.add(demand);
            }
        }

        // 按距离升序排序
        nearbyDemands.sort(Comparator.comparingDouble(d -> geoService.calculateDistance(longitude, latitude,
                d.getLongitude().doubleValue(), d.getLatitude().doubleValue())));

        return nearbyDemands;
    }

    /**
     * 为缺少 address 的需求通过逆地理编码补全地址
     */
    private void fillMissingAddresses(List<DemandPost> demands) {
        for (DemandPost demand : demands) {
            if (!StringUtils.hasText(demand.getAddress())
                    && demand.getLatitude() != null && demand.getLongitude() != null) {
                try {
                    GeocoderResult result = mapService.reverseGeocode(
                            demand.getLatitude().doubleValue(),
                            demand.getLongitude().doubleValue());
                    if (result != null && result.getResult() != null
                            && StringUtils.hasText(result.getResult().getAddress())) {
                        demand.setAddress(result.getResult().getAddress());
                        // 异步回写数据库（可选，避免每次都请求地图API）
                        try {
                            updateById(demand);
                        } catch (Exception e) {
                            log.debug("回写地址到数据库失败，不影响主流程: {}", e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    log.debug("逆地理编码失败(demandId={}): {}", demand.getId(), e.getMessage());
                }
            }
        }
    }

    @Override
    public IPage<DemandPost> pageListWithMatchScore(Long tutorId, String subject, String grade, Double longitude,
            Double latitude, Integer page, Integer size, String sortBy, String sortOrder) {
        
        // 1. 获取教师档案
        TutorProfile tutorProfile = null;
        if (tutorId != null) {
            tutorProfile = tutorProfileMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TutorProfile>()
                            .eq(TutorProfile::getUserId, tutorId));
        }

        // ================= 🚨 终极修复区：坐标兜底与严格空间隔离 =================
        // 确定检索基准坐标（如果前端没授权/没传GPS，兜底使用该教员档案中的注册地）
        Double searchLng = longitude;
        Double searchLat = latitude;
        if (searchLng == null && tutorProfile != null && tutorProfile.getLongitude() != null) {
            searchLng = tutorProfile.getLongitude().doubleValue();
        }
        if (searchLat == null && tutorProfile != null && tutorProfile.getLatitude() != null) {
            searchLat = tutorProfile.getLatitude().doubleValue();
        }

        // 2. 构建基础查询条件
        Page<DemandPost> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DemandPost> wrapper = new LambdaQueryWrapper<DemandPost>()
                .eq(DemandPost::getStatus, 1) // 只查上架的
                .isNull(DemandPost::getMatchedTutorId); // 过滤掉已被接单的需求

        if (StringUtils.hasText(subject)) {
            wrapper.eq(DemandPost::getSubject, subject);
        }
        if (StringUtils.hasText(grade)) {
            wrapper.eq(DemandPost::getGrade, grade);
        }

        // 核心拦截：废弃不可靠的 Redis GEO，使用绝对准确的 Haversine 内存公式进行 50 公里同城圈定
        if (searchLng != null && searchLat != null) {
            double maxRadius = 50.0; // 同城最大展示距离 50km
            
            // 直接调用类内现成的内存运算兜底方法，严格保证查出来的只有同城需求！
            List<DemandPost> strictNearbyDemands = searchNearbyByDatabase(searchLng, searchLat, maxRadius, subject, grade);
            
            if (strictNearbyDemands.isEmpty()) {
                // 如果附近 50km 确实一个需求都没有，直接返回空分页，绝不查库！
                Page<DemandPost> emptyPage = new Page<>(page, size);
                emptyPage.setTotal(0);
                return emptyPage;
            }
            
            // 提取严格圈定后的安全 ID，塞入查询条件
            List<Long> strictIds = strictNearbyDemands.stream().map(DemandPost::getId).toList();
            wrapper.in(DemandPost::getId, strictIds);
        }
        // =====================================================================

        // 3. 分页查询
        IPage<DemandPost> demandPage = page(pageParam, wrapper);
        List<DemandPost> demands = demandPage.getRecords();

        // 4. 计算每个需求的匹配度
        List<DemandPost> demandsWithMatchScore = new ArrayList<>();
        for (DemandPost demand : demands) {
            // 🚨 计算距离 (此处统一改为使用前面确定好的安全基准坐标 searchLng 和 searchLat)
            Double distance = null;
            if (searchLng != null && searchLat != null && demand.getLongitude() != null
                    && demand.getLatitude() != null) {
                distance = geoService.calculateDistance(
                        searchLng, searchLat,
                        demand.getLongitude().doubleValue(), demand.getLatitude().doubleValue());
            }

            // 创建带有匹配度的需求对象
            com.campus.module.demand.dto.DemandWithMatchScore demandWithScore = new com.campus.module.demand.dto.DemandWithMatchScore();
            // 复制基本信息
            demandWithScore.setId(demand.getId());
            demandWithScore.setPublisherId(demand.getPublisherId());
            demandWithScore.setStudentId(demand.getStudentId());
            demandWithScore.setTitle(demand.getTitle());
            demandWithScore.setSubject(demand.getSubject());
            demandWithScore.setGrade(demand.getGrade());
            demandWithScore.setExpectPrice(demand.getExpectPrice());
            demandWithScore.setScheduleRequire(demand.getScheduleRequire());
            demandWithScore.setTeachMode(demand.getTeachMode());
            demandWithScore.setLongitude(demand.getLongitude());
            demandWithScore.setLatitude(demand.getLatitude());
            demandWithScore.setAddress(demand.getAddress());
            demandWithScore.setDetail(demand.getDetail());
            demandWithScore.setStatus(demand.getStatus());
            demandWithScore.setCreateTime(demand.getCreateTime());
            demandWithScore.setUpdateTime(demand.getUpdateTime());

            // 计算匹配分数（如果教师档案存在）- 使用教师视角算法
            if (tutorProfile != null) {
                com.campus.module.match.dto.MatchScoreResult scoreResult = matchScoreCalculator
                        .calculateScoreForTeacher(
                                tutorProfile,
                                demand,
                                distance);

                // 设置匹配度信息
                demandWithScore.setMatchScore(scoreResult.getMatchScore());
                demandWithScore.setSubjectScore(scoreResult.getSubjectScore());
                demandWithScore.setGradeScore(scoreResult.getGradeScore());
                demandWithScore.setDistanceScore(scoreResult.getDistanceScore());
                demandWithScore.setPriceScore(scoreResult.getPriceScore());
                demandWithScore.setMatchTags(scoreResult.getMatchTags());

                // 计算匹配等级
                if (scoreResult.getMatchScore() != null) {
                    if (scoreResult.getMatchScore() >= 90) {
                        demandWithScore.setMatchLevel("excellent");
                    } else if (scoreResult.getMatchScore() >= 75) {
                        demandWithScore.setMatchLevel("good");
                    } else if (scoreResult.getMatchScore() >= 60) {
                        demandWithScore.setMatchLevel("fair");
                    } else {
                        demandWithScore.setMatchLevel("poor");
                    }
                }
            }

            demandsWithMatchScore.add(demandWithScore);
        }

        // 5. 排序处理
        if ("score".equals(sortBy) && !demandsWithMatchScore.isEmpty()) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            demandsWithMatchScore.sort((a, b) -> {
                if (a instanceof com.campus.module.demand.dto.DemandWithMatchScore
                        && b instanceof com.campus.module.demand.dto.DemandWithMatchScore) {
                    Double sa = ((com.campus.module.demand.dto.DemandWithMatchScore) a).getMatchScore();
                    Double sb = ((com.campus.module.demand.dto.DemandWithMatchScore) b).getMatchScore();
                    sa = sa != null ? sa : 0.0;
                    sb = sb != null ? sb : 0.0;
                    return isAsc ? sa.compareTo(sb) : sb.compareTo(sa);
                }
                return 0;
            });
        }

        // 6. 构建返回结果
        Page<DemandPost> resultPage = new Page<>(page, size);
        resultPage.setRecords(demandsWithMatchScore);
        resultPage.setTotal(demandPage.getTotal());
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long matchDemand(Long tutorId, Long demandId) {
        // 1. 验证需求是否存在且状态为上架
        DemandPost demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "需求不存在");
        }
        if (demand.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "需求已下架或已匹配");
        }
        if (demand.getMatchedTutorId() != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "需求已被其他教师匹配");
        }

        // 2. 验证教师是否存在且已认证
        TutorProfile tutorProfile = tutorProfileMapper.selectOne(
                new LambdaQueryWrapper<TutorProfile>()
                        .eq(TutorProfile::getUserId, tutorId));
        if (tutorProfile == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "教师档案不存在");
        }
        if (tutorProfile.getCertStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "教师未认证或认证未通过");
        }

        // 3. 创建订单
        com.campus.module.order.dto.AcceptDemandRequest acceptRequest = new com.campus.module.order.dto.AcceptDemandRequest();
        acceptRequest.setDemandId(demandId);
        acceptRequest.setTotalHours(10); // 默认10课时
        acceptRequest.setRemark("系统自动创建的订单");

        Long orderId = courseOrderService.acceptDemand(tutorId, acceptRequest);

        // acceptDemand() 已经更新了需求状态(status=2, matchedTutorId=tutorId)并移除了GEO索引
        // 无需再次更新，避免使用 stale 对象覆盖数据

        return orderId;
    }
}
