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
            throw new BusinessException(ResultCode.PARAM_ERROR, "需求ID不能为空");
        }
        DemandPost demand = getById(request.getId());
        if (demand == null || !demand.getPublisherId().equals(publisherId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "需求不存在或无权限");
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
            throw new BusinessException(ResultCode.PARAM_ERROR, "需求不存在或无权限");
        }
        demand.setStatus(status);
        updateById(demand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDemand(Long publisherId, Long demandId) {
        DemandPost demand = getById(demandId);
        if (demand == null || !demand.getPublisherId().equals(publisherId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "需求不存在或无权限");
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
                .eq(DemandPost::getStatus, 1); // 只查上架的

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
        return demands;
    }
}
