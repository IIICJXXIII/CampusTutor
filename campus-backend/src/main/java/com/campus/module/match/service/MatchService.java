package com.campus.module.match.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.demand.service.GeoService;
import com.campus.module.match.dto.TutorSearchRequest;
import com.campus.module.match.dto.TutorSearchResult;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 匹配搜索服务
 */
@Service
@RequiredArgsConstructor
public class MatchService {

    private final TutorProfileMapper tutorProfileMapper;
    private final SysUserMapper sysUserMapper;
    private final GeoService geoService;

    /**
     * 搜索教员
     * @param request 搜索条件
     * @return 分页结果
     */
    public IPage<TutorSearchResult> searchTutors(TutorSearchRequest request) {
        // 1. 如果有位置信息，先从GEO获取附近的教员ID
        Set<Long> nearbyTutorIds = null;
        Map<Long, Double> distanceMap = new HashMap<>();
        
        if (request.getLongitude() != null && request.getLatitude() != null) {
            double radius = request.getRadius() != null ? request.getRadius() : 10.0;
            List<Long> nearbyIds = geoService.searchNearbyTutors(
                    request.getLongitude(), request.getLatitude(), radius);
            nearbyTutorIds = new HashSet<>(nearbyIds);
            // 计算距离(简化处理，实际应从Redis结果中获取)
            for (int i = 0; i < nearbyIds.size(); i++) {
                distanceMap.put(nearbyIds.get(i), (double) i * 0.5); // 模拟距离
            }
        }

        // 2. 构建查询条件
        LambdaQueryWrapper<TutorProfile> wrapper = new LambdaQueryWrapper<TutorProfile>()
                .eq(TutorProfile::getCertStatus, 2); // 只查已认证的

        // 科目筛选(模糊匹配JSON数组)
        if (StringUtils.hasText(request.getSubject())) {
            wrapper.like(TutorProfile::getTeachSubjects, request.getSubject());
        }

        // 年级筛选
        if (StringUtils.hasText(request.getGrade())) {
            wrapper.like(TutorProfile::getTeachGrades, request.getGrade());
        }

        // 价格区间
        if (request.getMinPrice() != null) {
            wrapper.ge(TutorProfile::getExpectPrice, request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            wrapper.le(TutorProfile::getExpectPrice, request.getMaxPrice());
        }

        // 授课方式
        if (request.getTeachMode() != null) {
            if (request.getTeachMode() == 1) {
                wrapper.eq(TutorProfile::getCanVisit, 1);
            } else if (request.getTeachMode() == 2) {
                wrapper.eq(TutorProfile::getCanOnline, 1);
            }
        }

        // 学历筛选
        if (request.getEducations() != null && !request.getEducations().isEmpty()) {
            wrapper.in(TutorProfile::getEducation, request.getEducations());
        }

        // LBS筛选
        if (nearbyTutorIds != null) {
            if (nearbyTutorIds.isEmpty()) {
                // 附近没有教员，返回空结果
                return new Page<>(request.getPage(), request.getSize());
            }
            wrapper.in(TutorProfile::getId, nearbyTutorIds);
        }

        // 排序
        String sortBy = request.getSortBy();
        boolean isAsc = "asc".equalsIgnoreCase(request.getSortOrder());
        if ("rating".equals(sortBy)) {
            wrapper.orderBy(true, isAsc, TutorProfile::getRating);
        } else if ("price".equals(sortBy)) {
            wrapper.orderBy(true, isAsc, TutorProfile::getExpectPrice);
        } else {
            // 默认按评分降序
            wrapper.orderByDesc(TutorProfile::getRating);
        }

        // 3. 分页查询
        Page<TutorProfile> pageParam = new Page<>(request.getPage(), request.getSize());
        IPage<TutorProfile> profilePage = tutorProfileMapper.selectPage(pageParam, wrapper);

        // 4. 转换结果
        List<Long> userIds = profilePage.getRecords().stream()
                .map(TutorProfile::getUserId)
                .collect(Collectors.toList());
        
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        }

        Map<Long, SysUser> finalUserMap = userMap;
        Map<Long, Double> finalDistanceMap = distanceMap;
        
        List<TutorSearchResult> results = profilePage.getRecords().stream().map(profile -> {
            TutorSearchResult result = new TutorSearchResult();
            result.setId(profile.getId());
            result.setUserId(profile.getUserId());
            
            // 姓名脱敏
            String name = profile.getRealName();
            if (name != null && name.length() > 1) {
                result.setRealName(name.charAt(0) + "**");
            } else {
                result.setRealName(name);
            }

            // 获取头像
            SysUser user = finalUserMap.get(profile.getUserId());
            if (user != null) {
                result.setAvatarUrl(user.getAvatarUrl());
            }

            result.setUniversityName(profile.getUniversityName());
            result.setMajor(profile.getMajor());
            result.setEducation(profile.getEducation());
            
            // 解析JSON
            if (StringUtils.hasText(profile.getTeachSubjects())) {
                result.setTeachSubjects(JSONUtil.toList(profile.getTeachSubjects(), String.class));
            }
            if (StringUtils.hasText(profile.getTeachGrades())) {
                result.setTeachGrades(JSONUtil.toList(profile.getTeachGrades(), String.class));
            }

            result.setTeachStyle(profile.getTeachStyle());
            result.setIntroduction(profile.getIntroduction());
            result.setExpectPrice(profile.getExpectPrice());
            result.setCanVisit(profile.getCanVisit());
            result.setCanOnline(profile.getCanOnline());
            result.setRating(profile.getRating());
            result.setOrderCount(profile.getOrderCount());
            result.setDistance(finalDistanceMap.get(profile.getId()));

            return result;
        }).collect(Collectors.toList());

        // 如果按距离排序
        if ("distance".equals(sortBy) && !results.isEmpty()) {
            results.sort((a, b) -> {
                Double da = a.getDistance() != null ? a.getDistance() : Double.MAX_VALUE;
                Double db = b.getDistance() != null ? b.getDistance() : Double.MAX_VALUE;
                return isAsc ? da.compareTo(db) : db.compareTo(da);
            });
        }

        // 5. 构建返回分页
        Page<TutorSearchResult> resultPage = new Page<>(request.getPage(), request.getSize());
        resultPage.setRecords(results);
        resultPage.setTotal(profilePage.getTotal());
        return resultPage;
    }
}
