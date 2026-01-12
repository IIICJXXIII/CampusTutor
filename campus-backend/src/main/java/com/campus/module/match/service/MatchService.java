package com.campus.module.match.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.utils.GradeUtils;
import com.campus.module.behavior.dto.TutorBehaviorStats;
import com.campus.module.behavior.service.BehaviorService;
import com.campus.module.demand.service.GeoService;
import com.campus.module.match.dto.MatchScoreResult;
import com.campus.module.match.dto.TutorSearchRequest;
import com.campus.module.match.dto.TutorSearchResult;
import com.campus.module.match.dto.WeightConfig;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 匹配搜索服务
 * 升级版：支持用户行为信号和动态权重
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final TutorProfileMapper tutorProfileMapper;
    private final SysUserMapper sysUserMapper;
    private final GeoService geoService;
    private final MatchScoreCalculator scoreCalculator;
    private final BehaviorService behaviorService;
    private final DynamicWeightCalculator dynamicWeightCalculator;

    /**
     * 搜索教员
     * 
     * @param request 搜索条件
     * @return 分页结果
     */
    public IPage<TutorSearchResult> searchTutors(TutorSearchRequest request) {
        // 1. 如果有位置信息，先从GEO获取附近的教员ID和真实距离
        Set<Long> nearbyTutorIds = null;
        Map<Long, Double> distanceMap = new HashMap<>();

        if (request.getLongitude() != null && request.getLatitude() != null) {
            double radius = request.getRadius() != null ? request.getRadius() : 10.0;
            // 使用新方法获取带距离信息的结果
            Map<Long, Double> nearbyWithDistance = geoService.searchNearbyTutorsWithDistance(
                    request.getLongitude(), request.getLatitude(), radius);

            // 如果Redis返回空（Redis不可用或无数据），则不限制ID，后续从数据库计算距离
            if (nearbyWithDistance.isEmpty()) {
                // Redis无数据，查询所有已认证教员并在内存中计算距离
                nearbyTutorIds = null; // 不限制ID
            } else {
                nearbyTutorIds = nearbyWithDistance.keySet();
                distanceMap = nearbyWithDistance;
            }
        }

        // 2. 构建查询条件
        LambdaQueryWrapper<TutorProfile> wrapper = new LambdaQueryWrapper<TutorProfile>()
                .eq(TutorProfile::getCertStatus, 2); // 只查已认证的

        // 科目筛选(模糊匹配JSON数组) - 修复：空字符串和空数组也应该匹配所有
        if (StringUtils.hasText(request.getSubject())) {
            log.info("科目筛选条件: {}", request.getSubject());
            wrapper.and(w -> w.like(TutorProfile::getTeachSubjects, request.getSubject())
                    .or().isNull(TutorProfile::getTeachSubjects)
                    .or().eq(TutorProfile::getTeachSubjects, "")
                    .or().eq(TutorProfile::getTeachSubjects, "[]"));
        }

        // 年级筛选 - 使用GradeUtils进行智能匹配，同时匹配具体年级和对应的"全科"选项
        if (StringUtils.hasText(request.getGrade())) {
            String normalizedGrade = GradeUtils.normalize(request.getGrade());
            List<String> keywords = GradeUtils.getSearchKeywords(normalizedGrade);
            log.info("年级筛选条件: {} -> 标准化: {} -> 关键词: {}", request.getGrade(), normalizedGrade, keywords);

            // 构建OR条件：匹配具体年级或对应的全科或年级为NULL/空
            wrapper.and(w -> {
                boolean first = true;
                for (String keyword : keywords) {
                    if (first) {
                        w.like(TutorProfile::getTeachGrades, keyword);
                        first = false;
                    } else {
                        w.or().like(TutorProfile::getTeachGrades, keyword);
                    }
                }
                // 添加年级为NULL或空的情况（兜底：如果教师没设置年级，应该能配所有需求）
                w.or().isNull(TutorProfile::getTeachGrades);
                w.or().eq(TutorProfile::getTeachGrades, "");
                w.or().eq(TutorProfile::getTeachGrades, "[]");
            });
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

        // 性别筛选
        if (request.getGender() != null) {
            // 需要关联用户表查询性别
            List<Long> genderUserIds = sysUserMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getGender, request.getGender()))
                    .stream().map(SysUser::getId).collect(Collectors.toList());
            if (!genderUserIds.isEmpty()) {
                wrapper.in(TutorProfile::getUserId, genderUserIds);
            } else {
                // 没有符合性别条件的用户，返回空结果
                return new Page<>(request.getPage(), request.getSize());
            }
        }

        // 学历筛选
        if (request.getEducations() != null && !request.getEducations().isEmpty()) {
            wrapper.in(TutorProfile::getEducation, request.getEducations());
        }

        // LBS筛选 - 只有当Redis返回有效数据时才按ID过滤
        if (nearbyTutorIds != null && !nearbyTutorIds.isEmpty()) {
            wrapper.in(TutorProfile::getId, nearbyTutorIds);
        }
        // 注意：如果nearbyTutorIds为null，表示Redis不可用，此时不限制ID，查询所有教员

        // 排序
        String sortBy = request.getSortBy();
        boolean isAsc = "asc".equalsIgnoreCase(request.getSortOrder());
        if ("rating".equals(sortBy)) {
            wrapper.orderBy(true, isAsc, TutorProfile::getRating);
        } else if ("price".equals(sortBy)) {
            wrapper.orderBy(true, isAsc, TutorProfile::getExpectPrice);
        } else if ("orderCount".equals(sortBy)) {
            wrapper.orderBy(true, isAsc, TutorProfile::getOrderCount);
        } else if (!"distance".equals(sortBy) && !"score".equals(sortBy)) {
            // 默认按评分降序（distance和score排序在后面处理）
            wrapper.orderByDesc(TutorProfile::getRating);
        }

        // 3. 分页查询
        Page<TutorProfile> pageParam = new Page<>(request.getPage(), request.getSize());
        IPage<TutorProfile> profilePage = tutorProfileMapper.selectPage(pageParam, wrapper);

        // 调试日志：输出查询结果
        log.info("匹配查询完成: 总数={}, 当前页记录数={}", profilePage.getTotal(), profilePage.getRecords().size());
        for (TutorProfile p : profilePage.getRecords()) {
            log.debug("匹配教师: id={}, name={}, subjects={}, grades={}, price={}",
                    p.getId(), p.getRealName(), p.getTeachSubjects(), p.getTeachGrades(), p.getExpectPrice());
        }

        // 4. 如果Redis无数据但有位置请求，在内存中计算距离
        if (distanceMap.isEmpty() && request.getLongitude() != null && request.getLatitude() != null) {
            double radius = request.getRadius() != null ? request.getRadius() : 10.0;
            for (TutorProfile profile : profilePage.getRecords()) {
                if (profile.getLongitude() != null && profile.getLatitude() != null) {
                    double distance = geoService.calculateDistance(
                            request.getLongitude(), request.getLatitude(),
                            profile.getLongitude().doubleValue(), profile.getLatitude().doubleValue());
                    // 只保留在半径范围内的教员
                    if (distance <= radius) {
                        distanceMap.put(profile.getId(), distance);
                    }
                }
            }
        }

        // 5. 如果有距离过滤，只保留在distanceMap中的教师
        List<TutorProfile> filteredProfiles = profilePage.getRecords();
        Map<Long, Double> finalDistanceMapForFilter = distanceMap;
        if (!distanceMap.isEmpty()) {
            filteredProfiles = profilePage.getRecords().stream()
                    .filter(profile -> finalDistanceMapForFilter.containsKey(profile.getId()))
                    .collect(Collectors.toList());
        }

        // 5. 转换结果
        List<Long> userIds = filteredProfiles.stream()
                .map(TutorProfile::getUserId)
                .collect(Collectors.toList());

        Map<Long, SysUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        }

        Map<Long, SysUser> finalUserMap = userMap;
        Map<Long, Double> finalDistanceMap = distanceMap;

        List<TutorSearchResult> results = filteredProfiles.stream().map(profile -> {
            // 获取动态权重配置
            WeightConfig weights = dynamicWeightCalculator.getWeightsForUser(request.getUserId());

            // 获取教员行为统计（热度分）
            TutorBehaviorStats stats = behaviorService.getTutorStats(profile.getId());
            Double hotnessScore = stats != null ? stats.getHotnessScore() : 0.0;

            // 使用带行为信号的评分方法
            MatchScoreResult scoreResult = scoreCalculator.calculateScoreWithBehavior(
                    profile,
                    request.getSubject(),
                    request.getGrade(),
                    finalDistanceMap.get(profile.getId()),
                    request.getMaxPrice(),
                    hotnessScore,
                    weights.getSubjectWeight(),
                    weights.getGradeWeight(),
                    weights.getDistanceWeight(),
                    weights.getPriceWeight(),
                    weights.getRatingWeight(),
                    weights.getExperienceWeight(),
                    weights.getEducationWeight(),
                    weights.getSpecialtyWeight(),
                    weights.getHotnessWeight());

            // 复制评分数据到结果
            scoreResult.setId(profile.getId());
            scoreResult.setUserId(profile.getUserId());

            // 姓名脱敏
            String name = profile.getRealName();
            if (name != null && name.length() > 1) {
                scoreResult.setRealName(name.charAt(0) + "**");
            } else {
                scoreResult.setRealName(name);
            }

            // 获取头像
            SysUser user = finalUserMap.get(profile.getUserId());
            if (user != null) {
                scoreResult.setAvatarUrl(user.getAvatarUrl());
            }

            scoreResult.setUniversityName(profile.getUniversityName());
            scoreResult.setMajor(profile.getMajor());
            scoreResult.setEducation(profile.getEducation());

            // 解析JSON
            if (StringUtils.hasText(profile.getTeachSubjects())) {
                scoreResult.setTeachSubjects(JSONUtil.toList(profile.getTeachSubjects(), String.class));
            }
            if (StringUtils.hasText(profile.getTeachGrades())) {
                scoreResult.setTeachGrades(JSONUtil.toList(profile.getTeachGrades(), String.class));
            }

            scoreResult.setTeachStyle(profile.getTeachStyle());
            scoreResult.setIntroduction(profile.getIntroduction());
            scoreResult.setExpectPrice(profile.getExpectPrice());
            scoreResult.setCanVisit(profile.getCanVisit());
            scoreResult.setCanOnline(profile.getCanOnline());
            scoreResult.setRating(profile.getRating());
            scoreResult.setOrderCount(profile.getOrderCount());
            scoreResult.setDistance(finalDistanceMap.get(profile.getId()));

            return (TutorSearchResult) scoreResult;
        }).collect(Collectors.toList());

        // 如果按距离排序
        if ("distance".equals(sortBy) && !results.isEmpty()) {
            results.sort((a, b) -> {
                Double da = a.getDistance() != null ? a.getDistance() : Double.MAX_VALUE;
                Double db = b.getDistance() != null ? b.getDistance() : Double.MAX_VALUE;
                return isAsc ? da.compareTo(db) : db.compareTo(da);
            });
        }

        // 如果按匹配分数排序（智能推荐）
        if ("score".equals(sortBy) && !results.isEmpty()) {
            results.sort((a, b) -> {
                if (a instanceof MatchScoreResult && b instanceof MatchScoreResult) {
                    Double sa = ((MatchScoreResult) a).getMatchScore();
                    Double sb = ((MatchScoreResult) b).getMatchScore();
                    sa = sa != null ? sa : 0.0;
                    sb = sb != null ? sb : 0.0;
                    return isAsc ? sa.compareTo(sb) : sb.compareTo(sa);
                }
                return 0;
            });
        }

        // 5. 构建返回分页
        Page<TutorSearchResult> resultPage = new Page<>(request.getPage(), request.getSize());
        resultPage.setRecords(results);
        // 如果有距离过滤，需要调整总记录数
        if (!distanceMap.isEmpty()) {
            resultPage.setTotal(filteredProfiles.size());
        } else {
            resultPage.setTotal(profilePage.getTotal());
        }
        return resultPage;
    }
}
