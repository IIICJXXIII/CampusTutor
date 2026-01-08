# CampusTutor

# 🎓 CampusTutor (校园智教)

<div align="center">

**一个基于 Spring Boot 3 + Vue 3 + 微信小程序的大学生家教智能服务平台**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.21-42b883.svg)](https://vuejs.org/)
[![WeChat MiniProgram](https://img.shields.io/badge/WeChat-Native-07c160.svg)](https://developers.weixin.qq.com/miniprogram/dev/framework/)
[![Element Plus](https://img.shields.io/badge/Element%20Plus-2.5.6-409eff.svg)](https://element-plus.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## 📖 项目简介

**CampusTutor (校园智教)** 是一个专为解决家庭教育需求与大学生兼职需求不匹配问题而打造的**O2O 家教服务平台**，旨在连接优质大学生教员与有需求的家长，提供高效、透明、安全的家教匹配服务。

通过数字化手段，我们解决了传统家教中介**信息不透明**、**中介费高昂**、**信任建立难**三大痛点，构建了一个**“认证严、匹配准、服务全”**的家教生态闭环。

### 🧩 核心业务流程
系统围绕六大关键节点构建全链路业务闭环：
> **教师认证** (OCR验证) → **需求发布** (LBS定位) → **智能匹配** (AI算法) → **在线签约** (电子协议) → **教学管理** (打卡/错题) → **资金结算** (担保交易)

---

## ✨ 系统功能详解

系统包含 **Web 管理端**、**Web 用户端** 和 **微信小程序端**，服务于 **家长**、**教员** 和 **管理员** 三类角色。

### 👨‍👩‍👧 家长端 (需求方)
- **智能推荐**：根据学科、年级、距离、价格期望，智能匹配最合适的大学生家教。
- **透明选师**：查看教员的详细档案，包括实名认证、学历认证 (OCR识别)、历史评价、教学风格等。
- **课程及订单**：在线发布家教需求，管理课程订单，支持微信支付及退款申请。
- **教学监控**：查看教员上课打卡记录，确认课时，保障服务质量。
- **错题本**：支持拍照上传错题 (OCR)，构建专属的错题知识库。

### 🧑‍🏫 教员端 (服务方)
- **极速认证**：通过 OCR 技术自动识别身份证和学生证，快速完成实名与学历双重认证。
- **LBS 接单**：基于地理位置查看附近的家教需求，地图/列表双模式展示，支持一键抢单。
- **课时管理**：自动生成的课程表，上课签到/签退功能，支持拍照打卡。
- **收入管理**：透明的资金流水记录，支持课时费提现（模拟）。
- **个人名片**：自定义教学优势、试讲视频、成功案例，打造个人 IP。

### 👨‍💻 管理端 (总控中心)
- **全流程审核**：对教员入驻、需求发布、资金提现进行人工/系统双重审核。
- **数据仪表盘**：实时监控用户增长、订单成交、资金流水等核心业务指标。
- **订单仲裁**：处理家长与教员的交易纠纷，管理退款流程。
- **系统配置**：管理基础数据字典（科目、年级）、广告轮播图等。

---

## 🏗️ 技术架构

### 后端技术栈 (campus-backend)
- **核心框架**: Spring Boot 3.2.1
- **持久层**: MyBatis Plus 3.5.5 + MySQL 8.0
- **缓存与会话**: Redis 7.x + Spring Data Redis
- **安全认证**: JWT (JJWT 0.12.5) + 自定义拦截器
- **工具支持**: Hutool 5.8.25, Lombok, Knife4j 4.4.0 (API 文档)

### 前端技术栈

#### 🌐 Web 用户端 (campus-web)
与管理后台采用一致的技术底座，面向 PC 端家长和教员用户。
- **框架**: Vue 3.4.21 + Vite 5.1.4
- **UI 组件**: Element Plus 2.5.6 (SCSS 定制主题)
- **状态管理**: Pinia 2.1.7 (User, Tutor, Order, Demand Stores)
- **可视化**: ECharts 5.5.0 (数据分析图表)

#### 🖥️ Web 管理后台 (campus-web-admin)
- **架构**: 标准的后台管理系统 (Admin Dashboard)
- **特性**: 动态路由, 权限控制, 响应式布局

#### 📱 移动端 (campus-user-app)
- **平台**: 微信小程序原生开发 (Native)
- **能力**: 
  - 调用微信登录、支付能力
  - 集成高德地图 SDK 实现 LBS 定位
  - 使用相机 API 实现 OCR 拍照上传

---

## 📂 项目结构

```
CampusTutor/
├── campus-backend/          # 后端 API 服务
│   ├── src/main/java/com/campus/
│   │   ├── module/          # 垂直业务模块
│   │   │   ├── auth/        # 认证 (JWT, 微信登录)
│   │   │   ├── demand/      # 需求 (CRUD, 状态流转)
│   │   │   ├── match/       # 匹配 (推荐算法)
│   │   │   ├── order/       # 订单 (支付, 退款)
│   │   │   ├── user/        # 用户 (档案, 接单及钱包)
│   │   │   └── ...
│   │   └── ...
│   └── initdatabase.sql     # 数据库初始化脚本 (包含 测试数据)
│
├── campus-web/              # Web 用户端 (Vue3 + Element Plus)
│   ├── src/
│   │   ├── views/           # 业务视图 (Home, TeacherList, Profile...)
│   │   ├── stores/          # 全局状态 (Pinia)
│   │   └── api/             # 后端接口定义
│   └── ...
│
├── campus-web-admin/        # Web 管理后台 (Vue3 + Element Plus)
│   ├── src/views/           # 后台视图 (Audit, Dashboard, UserMgr...)
│   └── ...
│
└── campus-user-app/         # 微信小程序端
    ├── miniprogram/
    │   ├── pages/           # 页面 (home, mine, teacher-detail...)
    │   ├── components/      # 组件 (teacher-card, rate...)
    │   └── utils/           # 工具 (request, format...)
    └── ...
```

---

## 🚀 快速开始 (开发环境)

### 1. 环境准备
- **JDK**: 17+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **IDE**: IntelliJ IDEA / VS Code / 微信开发者工具

### 2. 后端部署
1. 创建数据库 `campus_tutor_db` 并导入 `initdatabase.sql`。
2. 修改 `application.properties` 配置数据库连接和 Redis 地址。
3. 运行 `CampusApplication.java`。
4. 访问 `http://localhost:8080/doc.html` 查看 API 文档。

### 3. Web 端启动
```bash
# 进入目录
cd campus-web  # 或 campus-web-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 4. 小程序端调试
1. 打开微信开发者工具，导入 `campus-user-app` 目录。
2. 修改 `miniprogram/config/apiConfig.js` 中的 `BASE_URL` 指向你的后端地址。
3. 编译运行 (需配置 AppID 或使用测试号)。

---

## 📅 版本记录

- **v1.0.0 (MVP)**
  - 完成核心业务闭环：认证、发布、匹配、支付、结算
  - 实现 Web 端与小程序端的互通
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
mvn test

# 前端测试
cd campus-web
npm run test
```

---

## 📦 部署

### Docker 部署 (推荐)

```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d
```

### 传统部署

1. 后端打包
```bash
cd campus-backend
mvn clean package -DskipTests
java -jar target/campus-backend-1.0.0.jar
```

2. 前端打包
```bash
cd campus-web
npm run build
# 将 dist 目录部署到 Nginx
```

---

## 🤝 贡献指南

我们欢迎所有形式的贡献！

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的改动 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议

---


<div align="center">

**如果这个项目对你有帮助，请给我们一个 ⭐️ Star！**

Made with ❤️ by CampusTutor Team

</div>

