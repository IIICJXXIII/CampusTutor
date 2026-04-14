# 素质教育匹配平台 架构总览

## 1. 项目定位

素质教育匹配平台是一个 Web 多端素质教育服务平台，围绕家长找服务提供者、服务提供者接单授课、管理员运营审核三类核心角色运行。

当前技术主线：

- 前端多应用拆分：家长端、服务提供者端、管理端 + shared 共享层
- 后端单体服务：Spring Boot 模块化分层
- 推荐链路：规则打分 + CF + 实时意图 + 流量池 + DeepFM
- AI 能力：需求解析、对话助手、教案生成、评语润色
- 地图能力：地理编码、逆地理编码、距离计算、路径规划

## 2. 代码结构

```text
素质教育匹配平台/
├── campus-backend/
│   ├── src/main/java/com/campus/
│   │   ├── common/                  # 公共能力（上下文、异常、统一返回）
│   │   ├── config/                  # 安全、拦截器、Swagger、跨域等配置
│   │   └── module/
│   │       ├── auth/                # 登录注册、验证码、JWT
│   │       ├── demand/              # 需求发布、状态流转、附近需求
│   │       ├── order/               # 接单、确认、支付、退款、订单状态
│   │       ├── tutor/ parent/       # 服务提供者档案与家长侧数据
│   │       ├── teaching/            # 上下课打卡与课时记录
│   │       ├── match/               # 搜索匹配、综合评分、DeepFM 推理
│   │       ├── recommend/           # 协同过滤推荐接口
│   │       ├── behavior/            # 用户行为采集
│   │       ├── llm/                 # AI 对话、需求解析、教案与评语
│   │       ├── map/                 # 地图能力封装
│   │       └── admin/               # 运营管理接口
│   └── src/main/resources/          # application.properties、mapper XML、模型文件
│
├── campus-web-parents/              # 家长端 Web（Vite:5175）
├── campus-web-teacher/              # 服务提供者端 Web（Vite:5174）
├── campus-web-admin/                # 管理端 Web（Vite:3001）
├── campus-web-shared/               # 共享 API/样式/工具
├── campus-web/                      # 已废弃历史单体前端（仅参考）
└── docs/                            # 项目文档
```

## 3. 关键业务流

### 3.1 需求发布与接单

1. 家长发布需求（`/api/demand/publish`），可携带经纬度。
2. 服务提供者通过附近需求或匹配接口发现需求。
3. 服务提供者调用 `/api/order/accept` 接单，系统创建待确认订单。
4. 家长确认订单（`/api/order/{id}/confirm`）后进入支付和服务阶段。

### 3.2 推荐链路（主备双通道）

1. 候选召回：优先 Redis GEO，失败则数据库距离降级计算。
2. 规则分：科目、年级、距离、价格、评分、经验等因子。
3. CF 混排：用户行为达到阈值时注入协同过滤分值。
4. 意图加分：依据近期行为标签实时加权。
5. 流量池加分：按 BASIC/WARM/HOT 提供曝光倾斜。
6. DeepFM 精排：模型可用则输出深度学习分；不可用则继续规则/CF链路。

### 3.3 AI 服务链路

- `POST /api/llm/chat`：多轮对话，支持函数调用检索服务提供者。
- `POST /api/llm/demand/parse`：自然语言需求转结构化字段。
- `POST /api/llm/lesson/plan`：生成教学计划。
- `POST /api/llm/lesson/comment`：润色评语。

## 4. 地图服务能力

`/api/map` 提供统一抽象接口，包含：

- `/geocoder` 地址转坐标
- `/geocoder/reverse` 坐标转地址
- `/direction` 路径规划（步行/驾车/公交）
- `/distance` 两点距离与耗时估算

同时，需求与服务提供者的坐标会进入 Redis GEO 以支持附近检索。

## 5. 运行与调试

### 后端

```bash
cd campus-backend
mvn spring-boot:run
```

### 前端

```bash
cd campus-web-parents && npm run dev
cd campus-web-teacher && npm run dev
cd campus-web-admin && npm run dev
```

## 6. 当前文档状态说明

- 本文档描述的是当前仓库可见结构与能力，不再包含已移除的小程序目录说明。
- `campus-web` 已标记弃用，请勿作为新功能开发入口。
- 涉及推荐细节请配合 `docs/deepfm_architecture.md` 一并阅读。
