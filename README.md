# CampusTutor

## 项目简介

CampusTutor 是一个面向家长、教员与管理员的校园家教服务平台，采用前后端分离架构。

- 后端：Spring Boot 3 + MyBatis-Plus + MySQL + Redis
- 前端：Vue 3 + Vite，多端拆分（家长端 / 教员端 / 管理端）
- 智能能力：DeepFM 精排、协同过滤、实时意图、流量池、LLM 对话与教案能力
- 地图能力：地理编码、逆地理编码、距离计算、路径规划

## 当前架构（2026-03）

项目已从单体前端演进为多 Web 应用形态：

- `campus-web-parents`：家长端
- `campus-web-teacher`：教员端
- `campus-web-admin`：管理后台
- `campus-web-shared`：共享 API、样式、工具、状态
- `campus-web`：历史单体前端，已废弃，仅保留参考

## 主要能力

### 1. 需求与订单闭环

- 家长发布需求、管理需求状态
- 教员接单、家长确认、支付、开课、完课、退款
- 管理端执行审核、仲裁、资金处理

### 2. 推荐与匹配

- 基础多维打分（科目、年级、距离、价格、评分等）
- 协同过滤（CF）混合评分
- 实时意图加分（Redis ZSET / Stream）
- 教员流量池赛马加分
- DeepFM ONNX 推理精排（不可用时自动降级）

### 3. AI 能力

- 智能需求解析：`POST /api/llm/demand/parse`
- 多轮对话助手：`POST /api/llm/chat`
- 快速问答：`GET /api/llm/quick-answer`
- AI 教案生成：`POST /api/llm/lesson/plan`
- AI 评语润色：`POST /api/llm/lesson/comment`

### 4. 地图能力

- 逆地理编码：`GET /api/map/geocoder/reverse`
- 地理编码：`GET /api/map/geocoder`
- 路径规划：`POST /api/map/direction`
- 距离计算：`GET /api/map/distance`

## 项目目录

```text
CampusTutor/
├── campus-backend/          # 后端服务
├── campus-web-parents/      # 家长端 Web
├── campus-web-teacher/      # 教员端 Web
├── campus-web-admin/        # 管理端 Web
├── campus-web-shared/       # 前端共享层
├── campus-web/              # 已废弃（历史参考）
└── docs/                    # 设计、数据库、接口与说明文档
```

## 本地启动

### 1) 后端

```bash
cd campus-backend
mvn spring-boot:run
```

默认端口 `8080`，接口文档：`http://localhost:8080/doc.html`

### 2) 前端

```bash
# 家长端
cd campus-web-parents
npm install
npm run dev

# 教员端
cd ../campus-web-teacher
npm install
npm run dev

# 管理端
cd ../campus-web-admin
npm install
npm run dev
```

默认端口：

- 家长端：`5175`
- 教员端：`5174`
- 管理端：`3001`

## 环境建议

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7+

## 文档说明

- 架构总览见 `ARCHITECTURE.md`
- 数据库结构见 `docs/DATABASE_SCHEMA.md`
- 推荐算法细节见 `docs/deepfm_architecture.md`
- RAG/提示词说明见 `docs/RAG_Prompt_实现文档.md`

## 备注

`campus-backend/src/main/resources/application.properties` 当前包含本地开发配置与示例密钥。建议在团队协作中逐步迁移为环境变量注入，避免敏感信息进入仓库。
