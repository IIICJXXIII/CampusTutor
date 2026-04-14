# CampusTutor

## 项目简介

CampusTutor 是一个面向家长、教员与管理员的校园家教服务平台，采用前后端分离架构。

**一个基于 Spring Boot 3 + Vue 3 的大学生家教智能服务平台**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.21-42b883.svg)](https://vuejs.org/)
[![Element Plus](https://img.shields.io/badge/Element%20Plus-2.5.6-409eff.svg)](https://element-plus.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

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

系统包含 **家长端**、**教师端** 和 **管理端** 三个 Web 应用，服务于 **家长**、**教员** 和 **管理员** 三类角色。

### 👨‍👩‍👧 家长端 (需求方)
- **智能推荐**：根据学科、年级、距离、价格期望，智能匹配最合适的大学生家教。
- **透明选师**：查看教员的详细档案，包括实名认证、学历认证 (OCR识别)、历史评价、教学风格等。
- **课程及订单**：在线发布家教需求，管理课程订单，支持钱包支付及退款申请。
- **教学监控**：查看教员上课打卡记录，确认课时，保障服务质量。
- **错题本**：支持拍照上传错题 (OCR)，构建专属的错题知识库。

- 逆地理编码：`GET /api/map/geocoder/reverse`
- 地理编码：`GET /api/map/geocoder`
- 路径规划：`POST /api/map/direction`
- 距离计算：`GET /api/map/distance`

## 项目目录

---

## 🏗️ 技术架构

### 后端技术栈 (campus-backend)
- **核心框架**: Spring Boot 3.2.1
- **持久层**: MyBatis Plus 3.5.5 + MySQL 8.0
- **缓存与会话**: Redis 7.x + Spring Data Redis
- **安全认证**: JWT (JJWT 0.12.5) + 自定义拦截器
- **工具支持**: Hutool 5.8.25, Lombok, Knife4j 4.4.0 (API 文档)

### 前端技术栈

#### 🏠 家长端 (campus-web-parents)
- **框架**: Vue 3.4.21 + Vite 5.1.4
- **UI 组件**: Element Plus 2.5.6 (SCSS 定制主题)
- **状态管理**: Pinia 2.1.7
- **地图**: 高德地图 JS API

#### 🧑‍🏫 教师端 (campus-web-teacher)
- **框架**: Vue 3.4.21 + Vite 5.1.4
- **UI 组件**: Element Plus 2.5.6 (SCSS 定制主题)
- **可视化**: ECharts 5.5.0
- **AI 工具**: 教案生成、评语润色

#### 🖥️ 管理后台 (campus-web-admin)
- **架构**: 标准的后台管理系统 (Admin Dashboard)
- **特性**: 动态路由, 权限控制, 响应式布局

#### 📦 共享模块 (campus-web-shared)
- **定位**: 跨端复用的 API 封装、工具函数和公共样式

---

## 📂 项目结构

```
CampusTutor/
├── campus-backend/          # 后端 API 服务 (Spring Boot 3)
│   ├── src/main/java/com/campus/
│   │   ├── module/          # 垂直业务模块
│   │   │   ├── auth/        # 认证 (JWT + BCrypt)
│   │   │   ├── demand/      # 需求 (CRUD, 状态流转)
│   │   │   ├── match/       # 匹配 (DeepFM + 协同过滤)
│   │   │   ├── order/       # 订单 (钱包支付, 担保交易)
│   │   │   ├── user/        # 用户 (档案, 钱包)
│   │   │   ├── llm/         # AI 大模型 (教案/评语/对话)
│   │   │   └── ...
│   │   └── ...
│   └── sql/                 # 数据库初始化脚本
│
├── campus-web-parents/      # 家长端 (Vue3 + Element Plus)
│   └── src/views/           # 需求、订单、课时、聊天等
│
├── campus-web-teacher/      # 教师端 (Vue3 + Element Plus)
│   └── src/views/           # 接单、课时、AI工具、钱包等
│
├── campus-web-admin/        # 管理后台 (Vue3 + Element Plus)
│   └── src/views/           # 审核、仪表盘、用户管理等
│
├── campus-web-shared/       # 前端共享模块
│   ├── api/                 # 统一 API 封装
│   ├── utils/               # 工具函数 (format, status, parse)
│   └── styles/              # 公共样式
│
└── campus-web/              # [已废弃] 旧版单体前端
```

## 本地启动

### 1. 环境准备
- **JDK**: 17+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **IDE**: IntelliJ IDEA / VS Code

### 2. 后端部署
1. 创建数据库 `campus_tutor_db` 并导入 `sql/schema.sql`。
2. 复制 `.env.example` 为 `.env` 并填写数据库、Redis 等连接信息。
3. 运行 `CampusApplication.java` (使用 `--spring.profiles.active=dev` 开启开发模式)。
4. 访问 `http://localhost:8080/doc.html` 查看 API 文档。

### 3. Web 端启动
```bash
# 家长端
cd campus-web-parents && npm install && npm run dev

# 教师端
cd campus-web-teacher && npm install && npm run dev

# 管理端
cd campus-web-admin && npm install && npm run dev
```

---

## 📅 版本记录

- **v2.0.0 (Web 聚焦版)**
  - 移除微信小程序，聚焦 Web 端
  - 拆分为家长端、教师端、管理端三个独立应用
  - 密码安全升级为 BCrypt
  - 事务安全加固（乐观锁、分布式锁）
  - 新增 AI 工具（教案生成、评语润色、AI 对话）

- **v1.0.0 (MVP)**
  - 完成核心业务闭环：认证、发布、匹配、支付、结算
  - 集成 Knife4j 接口文档

---

## 🔑 核心功能模块

### 1️⃣ 认证模块 (Auth)
- 用户注册/登录
- 手机验证码发送与校验
- JWT Token 生成与验证
- 用户信息上下文管理

### 2️⃣ 教员模块 (Tutor)
- 教员资质认证 (学生证、成绩单上传)
- 个人信息完善
- 授课科目与价格设置
- 接单记录查询

### 3️⃣ 家长模块 (Parent)
- 学生信息管理 (支持多个孩子)
- 薄弱科目标注
- 学习情况描述

### 4️⃣ 智能匹配模块 (Match)
- **多维度搜索**: 
  - 科目筛选
  - 年级匹配
  - 价格区间
  - 性别偏好
  - 学历要求
  - 授课方式 (上门/网课)
- **LBS 地理位置搜索**: 
  - 基于经纬度的距离计算
  - 半径范围筛选
  - 距离排序
- **智能排序**:
  - 按距离排序
  - 按评分排序
  - 按价格排序

### 5️⃣ 订单模块 (Order)
- 订单创建
- 在线支付
- 订单状态管理
- 订单评价

---

## 📊 数据库设计

主要数据表: 

- `sys_user` - 用户表 (统一管理所有角色)
- `tutor_info` - 教员信息表
- `tutor_subject` - 教员授课科目表
- `parent_info` - 家长信息表
- `student_info` - 学生信息表
- `tutor_order` - 订单表
- `order_evaluation` - 订单评价表
- `admin_info` - 管理员信息表

详细的表结构请查看 `campus-backend/initdatabase.sql`

---

## 🔐 API 认证

### 请求头格式

```http
Authorization: Bearer <JWT_TOKEN>
```

### 白名单路径 (无需认证)

- `/api/auth/**` - 登录注册接口
- `/doc.html` - API 文档
- `/swagger-ui/**` - Swagger UI
- `/v3/api-docs/**` - OpenAPI 规范

---

## 🛠️ 团队协作规范

我们使用 Git Flow 工作流，详细的协作指南请参考 [tips.md](tips.md)

### 核心原则

1. ❌ 永远不要直接在 `main` 分支上写代码
2. ❌ 永远不要强制推送 (`git push -f`)
3. ✅ 每天开工前，必须先同步 `main` 分支的最新代码

### 标准工作流

```bash
# 1. 同步主分支
git checkout main
git pull origin main

# 2. 切换到功能分支并合并最新代码
git checkout feature/your-feature
git merge main

# 3. 开发完成后提交
git add .
git commit -m "feat: 完成xx功能"
git push origin feature/your-feature

# 4. 在 GitHub 上创建 Pull Request
```

---

## 📝 提交规范

我们采用 [约定式提交](https://www.conventionalcommits.org/zh-hans/) 规范: 

- `feat`: 新功能
- `fix`: 修复 Bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构代码
- `test`: 测试相关
- `chore`: 构建/工具链更新

示例: 
```bash
git commit -m "feat: 添加教员地理位置搜索功能"
git commit -m "fix: 修复订单状态更新异常的问题"
```

---

## 🧪 测试

```bash
# 后端单元测试
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
