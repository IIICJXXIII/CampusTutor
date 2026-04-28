# CampusTutor 推荐系统改进 Walkthrough

本文档用于说明当前仓库中推荐系统的核心增强点与验证方式。

## 一、改动概览

| 类别 | 文件 | 说明 |
|------|------|------|
| 新增 | `campus-backend/src/main/java/com/campus/module/match/service/RealtimeIntentService.java` | 实时意图追踪与加分计算 |
| 新增 | `campus-backend/src/main/java/com/campus/module/match/service/TrafficPoolService.java` | 流量池级别评估与加分 |
| 新增 | `campus-backend/src/main/java/com/campus/module/match/config/IntentConfig.java` | 意图参数配置 |
| 新增 | `campus-backend/src/main/java/com/campus/module/match/config/TrafficPoolConfig.java` | 流量池参数配置 |
| 新增 | `campus-backend/src/main/java/com/campus/module/match/service/DeepFMInferenceService.java` | ONNX DeepFM 推理服务 |
| 修改 | `campus-backend/src/main/java/com/campus/module/match/service/MatchService.java` | 混排主流程整合 CF + 意图 + 流量池 + DeepFM |
| 修改 | `campus-backend/src/main/resources/application.properties` | 推荐相关参数开关与阈值 |

## 二、核心链路

### 1) 行为采集与意图追踪

1. 家长浏览、搜索、聊天等行为写入 `user_action_log`。
2. 实时意图模块将行为映射为标签权重（Redis ZSET）。
3. 搜索候选排序时计算命中标签的 `intentBoost`。

### 2) 流量池赛马

教员按数据表现处于 BASIC/WARM/HOT 级别，并获得不同曝光加分。

### 3) DeepFM 主路径 + 降级

- DeepFM 模型可用：参与精排打分。
- 模型不可用：自动降级为规则分 + CF + 意图 + 流量池组合，保持可用性。

## 三、接口侧体现

- 教员搜索：`/api/match/search`
- 附近推荐：`/api/llm/chat` 场景下可触发 `recommend_nearby_tutors` 工具调用
- 协同过滤接口：`/api/recommend/*`

## 四、验证建议

```bash
cd campus-backend
mvn -q -DskipTests compile
mvn -Dtest=RecommendImprovementTest test
```

如 DeepFM 模型加载失败，日志中会提示降级路径启用，可继续验证搜索结果与排序稳定性。
