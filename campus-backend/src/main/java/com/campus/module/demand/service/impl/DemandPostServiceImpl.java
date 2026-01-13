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
import com.campus.module.match.service.MatchScoreCalculator;
import com.campus.module.order.service.CourseOrderService;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 需求帖子Service实现
 */
@Service
@RequiredArgsConstructor
public class DemandPostServiceImpl extends ServiceImpl<DemandPostMapper, DemandPost>
        implements DemandPostService {

    private final GeoService geoService;
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
        return list(new LambdaQueryWrapper<DemandPost>()
                .eq(DemandPost::getPublisherId, publisherId)
                .orderByDesc(DemandPost::getCreateTime));
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
    public List<DemandPost> searchNearby(Double longitude, Double latitude, Double radiusKm) {
        if (longitude == null || latitude == null) {
            return new ArrayList<>();
        }
        double radius = radiusKm != null ? radiusKm : 10.0; // 默认10公里
        List<Long> demandIds = geoService.searchNearbyDemands(longitude, latitude, radius);
        if (demandIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 查询并按ID顺序返回(保持距离排序)
        List<DemandPost> demands = listByIds(demandIds);
        // 按demandIds顺序排序
        demands.sort((a, b) -> {
            int indexA = demandIds.indexOf(a.getId());
            int indexB = demandIds.indexOf(b.getId());
            return Integer.compare(indexA, indexB);
        });
        // 只返回上架状态的
        demands.removeIf(d -> d.getStatus() != 1);

        // 过滤掉自己发布的需求
        Long currentUserId = com.campus.common.context.UserContext.getUserId();
        if (currentUserId != null) {
            demands.removeIf(d -> d.getPublisherId().equals(currentUserId));
        }

        return demands;
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

        // 2. 构建查询条件
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

        // 3. 分页查询
        IPage<DemandPost> demandPage = page(pageParam, wrapper);
        List<DemandPost> demands = demandPage.getRecords();

        // 4. 计算每个需求的匹配度
        List<DemandPost> demandsWithMatchScore = new ArrayList<>();
        for (DemandPost demand : demands) {
            // 计算距离
            Double distance = null;
            if (longitude != null && latitude != null && demand.getLongitude() != null
                    && demand.getLatitude() != null) {
                distance = geoService.calculateDistance(
                        longitude, latitude,
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
                // 教师视角：价格越高越好，关注需求新鲜度和详细度
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

        // 4. 更新需求状态为已匹配
        demand.setStatus(2);
        demand.setMatchedTutorId(tutorId);
        updateById(demand);

        return orderId;
    }
}
