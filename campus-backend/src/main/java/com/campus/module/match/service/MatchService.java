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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    private final TutorProfileMapper tutorProfileMapper;
    private final SysUserMapper sysUserMapper;
    private final GeoService geoService;
    private final MatchScoreCalculator scoreCalculator;
    private final BehaviorService behaviorService;
    private final DynamicWeightCalculator dynamicWeightCalculator;
    private final CollaborativeFilteringService cfService;
    private final com.campus.module.match.config.CFConfig cfConfig;
    private final RealtimeIntentService realtimeIntentService;
    private final TrafficPoolService trafficPoolService;
    private final DeepFMInferenceService deepFMInferenceService;
    private final ThreadPoolTaskExecutor matchScoringExecutor;

    public MatchService(TutorProfileMapper tutorProfileMapper, SysUserMapper sysUserMapper,
            GeoService geoService, MatchScoreCalculator scoreCalculator,
            BehaviorService behaviorService, DynamicWeightCalculator dynamicWeightCalculator,
            CollaborativeFilteringService cfService,
            com.campus.module.match.config.CFConfig cfConfig,
            RealtimeIntentService realtimeIntentService,
            TrafficPoolService trafficPoolService,
            DeepFMInferenceService deepFMInferenceService,
            @Qualifier("matchScoringExecutor") ThreadPoolTaskExecutor matchScoringExecutor) {
        this.tutorProfileMapper = tutorProfileMapper;
        this.sysUserMapper = sysUserMapper;
        this.geoService = geoService;
        this.scoreCalculator = scoreCalculator;
        this.behaviorService = behaviorService;
        this.dynamicWeightCalculator = dynamicWeightCalculator;
        this.cfService = cfService;
        this.cfConfig = cfConfig;
        this.realtimeIntentService = realtimeIntentService;
        this.trafficPoolService = trafficPoolService;
        this.deepFMInferenceService = deepFMInferenceService;
        this.matchScoringExecutor = matchScoringExecutor;
    }

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

        // 科目筛选(模糊匹配JSON数组)
        if (StringUtils.hasText(request.getSubject())) {
            log.info("科目筛选条件: {}", request.getSubject());
            wrapper.like(TutorProfile::getTeachSubjects, request.getSubject());
        }

        // 年级筛选 - 使用GradeUtils进行智能匹配，同时匹配具体年级和对应的"全科"选项
        if (StringUtils.hasText(request.getGrade())) {
            String normalizedGrade = GradeUtils.normalize(request.getGrade());
            List<String> keywords = GradeUtils.getSearchKeywords(normalizedGrade);
            log.info("年级筛选条件: {} -> 标准化: {} -> 关键词: {}", request.getGrade(), normalizedGrade, keywords);

            // 构建OR条件：匹配具体年级或对应的全科
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

        // 关键词筛选（匹配姓名、学校、自我介绍、教学科目）
        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword();
            wrapper.and(w -> w.like(TutorProfile::getRealName, kw)
                    .or().like(TutorProfile::getUniversityName, kw)
                    .or().like(TutorProfile::getIntroduction, kw)
                    .or().like(TutorProfile::getTeachSubjects, kw));
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

        log.info("匹配查询完成: 总数={}, 当前页记录数={}", profilePage.getTotal(), profilePage.getRecords().size());

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

        // ============ 协同过滤预测 (User-Based CF) ============
        Map<Long, Double> cfScores = new HashMap<>();
        boolean enableCF = false;
        double cfWeight = 0.0;

        boolean useDeepFM = ("score".equals(sortBy) || StringUtils.isEmpty(sortBy))
                && request.getUserId() != null
                && deepFMInferenceService.isModelReady();

        if (("score".equals(sortBy) || StringUtils.isEmpty(sortBy)) && request.getUserId() != null) {
            try {
                if (cfConfig.isEnableCache() && cfService.hasEnoughHistory(request.getUserId())) {
                    List<Long> candidateIds = filteredProfiles.stream()
                            .map(TutorProfile::getId)
                            .collect(Collectors.toList());

                    if (!candidateIds.isEmpty()) {
                        cfScores = cfService.batchPredictScores(request.getUserId(), candidateIds);
                        enableCF = !cfScores.isEmpty();
                        cfWeight = cfConfig.getCfWeight();
                    }
                }
            } catch (Exception e) {
                log.error("协同过滤预测异常，降级到纯加权算法: {}", e.getMessage());
            }
        }

        // 6. 转换结果与评分计算
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

        boolean finalEnableCF = enableCF;
        double finalCfWeight = cfWeight;
        Map<Long, Double> finalCfScores = cfScores;

        // ============ DeepFM 深度学习精排 ============
        Map<Long, Double> deepFmScores = new HashMap<>();
        if (useDeepFM && !filteredProfiles.isEmpty()) {
            try {
                float[][] features = filteredProfiles.stream()
                        .map(tp -> buildFeatureVector(request.getUserId(), tp))
                        .toArray(float[][]::new);
                float[] predictions = deepFMInferenceService.predictScores(features);
                if (predictions != null && predictions.length == filteredProfiles.size()) {
                    for (int i = 0; i < predictions.length; i++) {
                        deepFmScores.put(filteredProfiles.get(i).getId(), (double) predictions[i]);
                    }
                    log.info("[DeepFM] 推理完成，有效预测数={}", deepFmScores.size());
                }
            } catch (Exception e) {
                log.warn("[DeepFM] 推理异常，降级到规则排序: {}", e.getMessage());
            }
        }
        boolean finalUseDeepFM = !deepFmScores.isEmpty();

        // 6. 并行评分：每个教员独立计算，使用线程池并行处理
        WeightConfig weights = dynamicWeightCalculator.getWeightsForUser(request.getUserId());
        Map<Long, Double> finalCfScoresCopy = finalCfScores;
        double finalCfWeightCopy = finalCfWeight;
        Long currentUserId = request.getUserId();

        List<CompletableFuture<TutorSearchResult>> futures = filteredProfiles.stream()
                .map(profile -> CompletableFuture.supplyAsync(() ->
                        computeCandidateScore(profile, currentUserId, weights,
                                finalUserMap, finalDistanceMap,
                                finalEnableCF, finalCfWeightCopy, finalCfScoresCopy,
                                finalUseDeepFM, deepFmScores),
                        matchScoringExecutor))
                .collect(Collectors.toList());

        List<TutorSearchResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // 如果按距离排序
        if ("distance".equals(sortBy) && !results.isEmpty()) {
            results.sort((a, b) -> {
                Double da = a.getDistance() != null ? a.getDistance() : Double.MAX_VALUE;
                Double db = b.getDistance() != null ? b.getDistance() : Double.MAX_VALUE;
                return isAsc ? da.compareTo(db) : db.compareTo(da);
            });
        }

        // 如果按匹配分数排序（智能推荐）
        if (("score".equals(sortBy) || StringUtils.isEmpty(sortBy)) && !results.isEmpty()) {
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

        // 7. 构建返回分页
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

    // ========================================================================================
    // LBS 召回 + DeepFM 深度学习精排 —— 级联推荐算法
    // ========================================================================================

    /**
     * 对单个教员执行完整评分流水线（供 searchTutors 并行调用）。
     */
    private TutorSearchResult computeCandidateScore(TutorProfile profile,
            Long userId, WeightConfig weights,
            Map<Long, SysUser> userMap, Map<Long, Double> distanceMap,
            boolean enableCF, double cfWeight, Map<Long, Double> cfScores,
            boolean useDeepFM, Map<Long, Double> deepFmScores) {

        TutorBehaviorStats stats = behaviorService.getTutorStats(profile.getId());
        Double hotnessScore = stats != null ? stats.getHotnessScore() : 0.0;

        MatchScoreResult scoreResult = scoreCalculator.calculateScoreWithBehavior(
                profile, null, null,
                distanceMap.get(profile.getId()),
                null, hotnessScore,
                weights.getSubjectWeight(), weights.getGradeWeight(),
                weights.getDistanceWeight(), weights.getPriceWeight(),
                weights.getRatingWeight(), weights.getExperienceWeight(),
                weights.getEducationWeight(), weights.getSpecialtyWeight(),
                weights.getHotnessWeight());

        // CF hybrid
        if (enableCF && cfScores.containsKey(profile.getId())) {
            Double cfScore = cfScores.get(profile.getId());
            if (cfScore != null) {
                double contentScore = scoreResult.getMatchScore();
                double cfNormalized = cfScore * 100;
                scoreResult.setMatchScore(Math.min(100.0, (1 - cfWeight) * contentScore + cfWeight * cfNormalized));
                scoreResult.setCfScore(cfScore);
                if (cfScore >= 0.7) {
                    scoreResult.getMatchTags().add("相似家长推荐");
                } else if (cfScore >= 0.5) {
                    scoreResult.getMatchTags().add("猜你喜欢");
                }
            }
        }

        // DeepFM fusion
        if (useDeepFM && deepFmScores.containsKey(profile.getId())) {
            Double deepFmScore = deepFmScores.get(profile.getId());
            if (deepFmScore != null) {
                double current = scoreResult.getMatchScore();
                double fused = (1 - 0.3) * current + 0.3 * Math.min(100.0, deepFmScore * 100);
                scoreResult.setMatchScore(Math.min(100.0, fused));
            }
        }

        // intent boost
        try {
            double boost = realtimeIntentService.calculateIntentBoost(userId, profile);
            if (boost > 0) {
                scoreResult.setMatchScore(Math.min(100.0, scoreResult.getMatchScore() + boost));
                scoreResult.getMatchTags().add("系统推荐");
            }
        } catch (Exception e) {
            log.debug("意图加分失败(Redis不可用)，跳过: {}", e.getMessage());
        }

        // traffic pool boost
        try {
            com.campus.module.match.dto.TrafficPoolLevel poolLevel =
                    trafficPoolService.getPoolLevel(profile.getId());
            double poolBoost = trafficPoolService.getPoolBoostScore(poolLevel);
            if (poolBoost > 0) {
                scoreResult.setMatchScore(Math.min(100.0, scoreResult.getMatchScore() + poolBoost));
                String poolTag = trafficPoolService.getPoolTag(poolLevel);
                if (poolTag != null) {
                    scoreResult.getMatchTags().add(poolTag);
                }
            }
        } catch (Exception e) {
            log.debug("流量池加分失败(Redis不可用)，跳过: {}", e.getMessage());
        }

        scoreResult.setId(profile.getId());
        scoreResult.setUserId(profile.getUserId());
        scoreResult.setRealName(profile.getRealName());
        scoreResult.setCertStatus(profile.getCertStatus());

        SysUser user = userMap.get(profile.getUserId());
        if (user != null) {
            scoreResult.setAvatarUrl(user.getAvatarUrl());
            scoreResult.setGender(user.getGender());
        }
        scoreResult.setUniversityName(profile.getUniversityName());
        scoreResult.setMajor(profile.getMajor());
        scoreResult.setEducation(profile.getEducation());

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
        scoreResult.setDistance(distanceMap.get(profile.getId()));
        if (profile.getLongitude() != null) scoreResult.setLongitude(profile.getLongitude().doubleValue());
        if (profile.getLatitude() != null) scoreResult.setLatitude(profile.getLatitude().doubleValue());

        return scoreResult;
    }

    private static final float PRICE_NORM = 500f;
    private static final float RATING_NORM = 5.0f;
    private static final float ORDER_NORM = 1000f;

    /**
     * 基于 "LBS 空间召回 + DeepFM 深度学习精排" 的级联推荐
     *
     * @param userId   当前家长用户 ID
     * @param lng      家长所在经度
     * @param lat      家长所在纬度
     * @param radiusKm 搜索半径(公里)
     * @return 按 matchScore 降序排列的推荐教员列表
     */
    public List<TutorSearchResult> getRecommendedTutors(Long userId,
            double lng,
            double lat,
            double radiusKm,
            String subject) {
        // ==================== Step 1: 空间召回 (Recall Phase) ====================
        log.info("[DeepFM推荐] 开始 LBS 召回, userId={}, lng={}, lat={}, radius={}km, subject={}",
                userId, lng, lat, radiusKm, subject);

        // 1-1. 从 Redis GEO 中捞出半径内的所有教员 ID 及距离
        Map<Long, Double> nearbyWithDistance = geoService.searchNearbyTutorsWithDistance(lng, lat, radiusKm);

        if (nearbyWithDistance.isEmpty()) {
            log.warn("[DeepFM推荐] LBS 召回为空(Redis 无数据或无附近教员), 返回空列表");
            return Collections.emptyList();
        }

        Set<Long> nearbyTutorIds = nearbyWithDistance.keySet();
        log.info("[DeepFM推荐] LBS 召回教员数量: {}", nearbyTutorIds.size());

        // 1-2. 根据 ID 批量查询教员详细信息（仅已认证教员，并按学科初步过滤）
        LambdaQueryWrapper<TutorProfile> wrapper = new LambdaQueryWrapper<TutorProfile>()
                .in(TutorProfile::getId, nearbyTutorIds)
                .eq(TutorProfile::getCertStatus, 2);

        if (org.springframework.util.StringUtils.hasText(subject)) {
            wrapper.and(w -> w.like(TutorProfile::getTeachSubjects, subject)
                    .or().isNull(TutorProfile::getTeachSubjects)
                    .or().eq(TutorProfile::getTeachSubjects, "")
                    .or().eq(TutorProfile::getTeachSubjects, "[]"));
        }

        List<TutorProfile> tutorProfiles = tutorProfileMapper.selectList(wrapper);

        if (tutorProfiles.isEmpty()) {
            log.warn("[DeepFM推荐] 召回教员全部未认证或查询为空, 返回空列表");
            return Collections.emptyList();
        }

        log.info("[DeepFM推荐] 数据库查询到已认证教员: {} 位", tutorProfiles.size());

        // ==================== Step 2: 特征工程 (Feature Engineering) ====================
        int n = tutorProfiles.size();
        float[][] features = new float[n][8];

        for (int i = 0; i < n; i++) {
            TutorProfile tp = tutorProfiles.get(i);
            features[i] = buildFeatureVector(userId, tp);
        }

        log.debug("[DeepFM推荐] 特征矩阵构建完成, shape=[{}, 8]", n);

        // ==================== Step 3: 深度学习推理 (Ranking Phase) ====================
        float[] scores = null;
        boolean inferenceFailed = false;

        try {
            if (deepFMInferenceService.isModelReady()) {
                scores = deepFMInferenceService.predictScores(features);
            }
        } catch (Exception e) {
            log.error("[DeepFM推荐] 模型推理异常: {}", e.getMessage(), e);
        }

        if (scores == null) {
            inferenceFailed = true;
            log.warn("[DeepFM推荐] 模型推理失败，启用降级排序策略：协同过滤 + 意图分 + 流量池机制");
        }

        // ==================== Step 4: 降级路径——预计算 CF 协同过滤得分 ====================
        Map<Long, Double> cfScoresMap = new HashMap<>();
        boolean cfEnabled = false;
        double cfWeightVal = 0.0;

        if (inferenceFailed && userId != null && userId > 0) {
            try {
                List<Long> candidateIds = tutorProfiles.stream()
                        .map(TutorProfile::getId)
                        .collect(Collectors.toList());

                if (cfConfig.isEnableCache() && cfService.hasEnoughHistory(userId) && !candidateIds.isEmpty()) {
                    cfScoresMap = cfService.batchPredictScores(userId, candidateIds);
                    cfEnabled = !cfScoresMap.isEmpty();
                    cfWeightVal = cfConfig.getCfWeight();
                    log.info("[DeepFM降级] CF 协同过滤预测完成，有效得分教员数: {}", cfScoresMap.size());
                }
            } catch (Exception e) {
                log.error("[DeepFM降级] CF 协同过滤预测异常，继续使用基础分: {}", e.getMessage());
            }
        }

        // ==================== Step 5: 结果组装与排序 ====================
        // 查询用户信息（获取头像等）
        List<Long> userIds = tutorProfiles.stream()
                .map(TutorProfile::getUserId)
                .collect(Collectors.toList());
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            sysUserMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        List<MatchScoreResult> results;

        if (!inferenceFailed && scores != null) {
            // DeepFM 主路径 — 并行组装结果
            float[] finalScores = scores;
            List<CompletableFuture<MatchScoreResult>> futures = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                final int idx = i;
                TutorProfile profile = tutorProfiles.get(i);
                futures.add(CompletableFuture.supplyAsync(() ->
                        buildRecommendedResultDeepFM(profile, userId, userMap,
                                nearbyWithDistance, finalScores[idx]),
                        matchScoringExecutor));
            }
            results = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        } else {
            // 降级路径 — 并行计算
            boolean finalCfEnabled = cfEnabled;
            double finalCfWeight = cfWeightVal;
            Map<Long, Double> finalCfScores = cfScoresMap;
            List<CompletableFuture<MatchScoreResult>> futures = new ArrayList<>(n);
            for (TutorProfile profile : tutorProfiles) {
                futures.add(CompletableFuture.supplyAsync(() ->
                        buildRecommendedResultFallback(profile, userId, userMap,
                                nearbyWithDistance, finalCfEnabled, finalCfWeight, finalCfScores),
                        matchScoringExecutor));
            }
            results = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        }

        // 按 matchScore 从高到低排序
        results.sort((a, b) -> {
            Double sa = a.getMatchScore() != null ? a.getMatchScore() : 0.0;
            Double sb = b.getMatchScore() != null ? b.getMatchScore() : 0.0;
            return sb.compareTo(sa);
        });

        log.info("[DeepFM推荐] 推荐完成, 返回教员数: {}, 推理方式: {}",
                results.size(), inferenceFailed ? "降级(协同过滤+意图流+流量池)" : "DeepFM模型");

        return new ArrayList<>(results);
    }

    /**
     * DeepFM 路径：并行组装单个教员结果
     */
    private MatchScoreResult buildRecommendedResultDeepFM(TutorProfile profile,
            Long userId, Map<Long, SysUser> userMap,
            Map<Long, Double> nearbyWithDistance, float deepFmScore) {
        MatchScoreResult result = new MatchScoreResult();
        List<String> tags = new ArrayList<>();

        double matchScore = Math.min(100.0, Math.max(0.0, deepFmScore * 100.0));
        result.setDeepFmScore((double) deepFmScore);
        if (deepFmScore >= 0.8f) {
            tags.add("AI精选");
        }

        fillRecommendedResultFields(result, profile, userId, userMap, nearbyWithDistance, tags, matchScore);
        return result;
    }

    /**
     * 降级路径：并行计算单个教员结果
     */
    private MatchScoreResult buildRecommendedResultFallback(TutorProfile profile,
            Long userId, Map<Long, SysUser> userMap,
            Map<Long, Double> nearbyWithDistance,
            boolean cfEnabled, double cfWeight, Map<Long, Double> cfScores) {
        MatchScoreResult result = new MatchScoreResult();
        List<String> tags = new ArrayList<>();

        double ratingVal = profile.getRating() != null ? profile.getRating().doubleValue() : 0.0;
        int orderVal = profile.getOrderCount() != null ? profile.getOrderCount() : 0;
        double matchScore = ratingVal / 5.0 * 60.0 + Math.min(orderVal / 1000.0, 1.0) * 40.0;

        if (cfEnabled && cfScores.containsKey(profile.getId())) {
            Double cfScore = cfScores.get(profile.getId());
            if (cfScore != null) {
                double cfNormalized = cfScore * 100.0;
                matchScore = (1 - cfWeight) * matchScore + cfWeight * cfNormalized;
                result.setCfScore(cfScore);
                if (cfScore >= 0.7) tags.add("相似家长推荐");
                else if (cfScore >= 0.5) tags.add("猜你喜欢");
            }
        }

        try {
            double boost = realtimeIntentService.calculateIntentBoost(userId, profile);
            if (boost > 0) {
                matchScore = Math.min(100.0, matchScore + boost);
                tags.add("系统推荐");
            }
        } catch (Exception e) {
            log.debug("[DeepFM降级] 意图加分失败(Redis不可用)，跳过: {}", e.getMessage());
        }

        try {
            com.campus.module.match.dto.TrafficPoolLevel poolLevel =
                    trafficPoolService.getPoolLevel(profile.getId());
            double poolBoost = trafficPoolService.getPoolBoostScore(poolLevel);
            if (poolBoost > 0) {
                matchScore = Math.min(100.0, matchScore + poolBoost);
                String poolTag = trafficPoolService.getPoolTag(poolLevel);
                if (poolTag != null) tags.add(poolTag);
            }
        } catch (Exception e) {
            log.debug("[DeepFM降级] 流量池加分失败(Redis不可用)，跳过: {}", e.getMessage());
        }

        fillRecommendedResultFields(result, profile, userId, userMap, nearbyWithDistance, tags, matchScore);
        return result;
    }

    /**
     * 填充推荐结果的公共字段
     */
    private void fillRecommendedResultFields(MatchScoreResult result, TutorProfile profile,
            Long userId, Map<Long, SysUser> userMap,
            Map<Long, Double> nearbyWithDistance, List<String> tags, double matchScore) {
        result.setMatchScore(matchScore);
        result.setId(profile.getId());
        result.setUserId(profile.getUserId());
        result.setRealName(profile.getRealName());

        SysUser user = userMap.get(profile.getUserId());
        if (user != null) {
            result.setAvatarUrl(user.getAvatarUrl());
            result.setGender(user.getGender());
        }

        result.setUniversityName(profile.getUniversityName());
        result.setMajor(profile.getMajor());
        result.setEducation(profile.getEducation());

        if (org.springframework.util.StringUtils.hasText(profile.getTeachSubjects())) {
            result.setTeachSubjects(JSONUtil.toList(profile.getTeachSubjects(), String.class));
        }
        if (org.springframework.util.StringUtils.hasText(profile.getTeachGrades())) {
            result.setTeachGrades(JSONUtil.toList(profile.getTeachGrades(), String.class));
        }

        result.setTeachStyle(profile.getTeachStyle());
        result.setIntroduction(profile.getIntroduction());
        result.setExpectPrice(profile.getExpectPrice());
        result.setCanVisit(profile.getCanVisit());
        result.setCanOnline(profile.getCanOnline());
        result.setRating(profile.getRating());
        result.setOrderCount(profile.getOrderCount());
        result.setDistance(nearbyWithDistance.get(profile.getId()));

        Double dist = nearbyWithDistance.get(profile.getId());
        if (dist != null) {
            if (dist <= 1.0) tags.add("超近");
            else if (dist <= 3.0) tags.add("距离近");
            else if (dist <= 5.0) tags.add("同城");
        }
        if (profile.getRating() != null) {
            double r = profile.getRating().doubleValue();
            if (r >= 4.8) tags.add("口碑之星");
            else if (r >= 4.5) tags.add("高评分");
            else if (r >= 4.0) tags.add("好评");
        }
        int orderCnt = profile.getOrderCount() != null ? profile.getOrderCount() : 0;
        if (orderCnt >= 50) tags.add("资深名师");
        else if (orderCnt >= 20) tags.add("经验丰富");
        else if (orderCnt >= 10) tags.add("教学有方");

        result.setMatchTags(tags);
    }

    /**
     * 构建单个教员的 DeepFM 特征向量
     * 特征顺序: [user_id, tutor_id, university_name, teach_subjects,
     * can_online, expect_price, rating, order_count]
     *
     * @param userId 家长用户 ID
     * @param tp     教员档案
     * @return 长度为 8 的 float 数组
     */
    private float[] buildFeatureVector(Long userId, TutorProfile tp) {
        float[] vec = new float[8];

        vec[0] = (float) DeepFMInferenceService.hashFeature(userId, DeepFMInferenceService.VOCAB_USER_ID);

        vec[1] = (float) DeepFMInferenceService.hashFeature(tp.getId(), DeepFMInferenceService.VOCAB_TUTOR_ID);

        vec[2] = (float) DeepFMInferenceService.hashFeature(tp.getUniversityName(), DeepFMInferenceService.VOCAB_UNIVERSITY_NAME);

        vec[3] = (float) DeepFMInferenceService.hashFeature(tp.getTeachSubjects(), DeepFMInferenceService.VOCAB_TEACH_SUBJECTS);

        vec[4] = tp.getCanOnline() != null
                ? (float) DeepFMInferenceService.hashFeature(tp.getCanOnline(), DeepFMInferenceService.VOCAB_CAN_ONLINE)
                : 0f;

        vec[5] = tp.getExpectPrice() != null
                ? Math.min(tp.getExpectPrice().floatValue() / PRICE_NORM, 1.0f)
                : 0f;

        vec[6] = tp.getRating() != null
                ? tp.getRating().floatValue() / RATING_NORM
                : 0f;

        vec[7] = tp.getOrderCount() != null
                ? tp.getOrderCount().floatValue() / ORDER_NORM
                : 0f;

        return vec;
    }
}
