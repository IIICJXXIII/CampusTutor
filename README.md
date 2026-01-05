# 🎓 CampusTutor (校园智教)

<div align="center">

**一个基于 Spring Boot 3 + Vue 3 + Uni-app 的大学生家教智能服务平台**

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-42b883.svg)](https://vuejs.org/)
[![Uni-app](https://img.shields.io/badge/Uni--app-latest-2b9939.svg)](https://uniapp.dcloud.io/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## 📖 项目简介

CampusTutor 是一个专为大学生打造的家教智能服务平台，旨在连接优质大学生教员与有需求的家长，提供高效、便捷的家教匹配服务。

### ✨ 核心特性

- 🎯 **智能匹配** - 基于地理位置、科目需求、价格区间等多维度智能推荐教员
- 👤 **多角色系统** - 支持管理员、教员、家长三种角色，权限分明
- 📱 **跨平台支持** - Web 端（管理后台）+ 小程序端（用户端）
- 🔐 **JWT 认证** - 安全的用户身份验证与授权机制
- 📍 **LBS 服务** - 基于地理位置的教员搜索与距离计算
- 💰 **订单管理** - 完整的订单创建、支付、评价流程
- ⭐ **评价系统** - 家长可对教员进行评分和评价
- 📊 **数据统计** - 多维度的数据分析和展示

---

## 🏗️ 技术架构

### 后端技术栈 (Java 77. 1%)

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.x | 核心框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis Plus | 3.x | ORM 框架 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.x | 缓存中间件 |
| JWT | - | Token 认证 |
| Swagger/Knife4j | - | API 文档 |
| Lombok | - | 代码简化 |

### 前端技术栈

#### Web 管理端 (Vue 21.5%)
- Vue 3
- Element Plus
- Axios
- Vue Router
- Pinia

#### 小程序端 (Uni-app)
- Uni-app
- Vue 3 Composition API
- uView UI
- 微信小程序原生组件

---

## 📂 项目结构

```
CampusTutor/
├── campus-backend/          # 后端服务 (Spring Boot)
│   ├── src/
│   │   └── main/
│   │       ├── java/com/campus/
│   │       │   ├── common/          # 公共模块
│   │       │   │   ├── context/     # 用户上下文
│   │       │   │   ├── result/      # 统一响应
│   │       │   │   └── utils/       # 工具类
│   │       │   ├── config/          # 配置类
│   │       │   │   ├── CorsConfig.java
│   │       │   │   └── JwtAuthenticationFilter.java
│   │       │   ├── module/          # 业务模块
│   │       │   │   ├── auth/        # 认证模块
│   │       │   │   ├── match/       # 匹配模块
│   │       │   │   ├── parent/      # 家长模块
│   │       │   │   └── tutor/       # 教员模块
│   │       │   └── CampusApplication.java
│   │       └── resources/
│   ├── pom.xml
│   └── initdatabase.sql     # 数据库初始化脚本
│
├── campus-web/              # Web 管理端 (Vue 3)
│   ├── src/
│   │   ├── api/             # API 接口
│   │   ├── components/      # 公共组件
│   │   ├── views/           # 页面视图
│   │   ├── router/          # 路由配置
│   │   └── store/           # 状态管理
│   └── package.json
│
├── campus-user-app/         # 小程序端 (Uni-app)
│   ├── pages/               # 页面
│   ├── components/          # 组件
│   ├── api/                 # API 接口
│   ├── utils/               # 工具类
│   └── manifest.json
│
├── tips. md                  # 团队协作手册
└── README.md                # 项目说明文档
```

---

## 🚀 快速开始

### 环境要求

- **Java**:  JDK 17+
- **Node.js**: 16+
- **MySQL**: 8.0+
- **Redis**:  7.x
- **Maven**: 3.8+
- **微信开发者工具**: 最新版 (小程序开发)

### 后端启动

1. **克隆项目**
```bash
git clone https://github.com/IIICJXXIII/CampusTutor.git
cd CampusTutor/campus-backend
```

2. **初始化数据库**
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE campus_tutor DEFAULT CHARACTER SET utf8mb4;

# 导入初始化脚本
mysql -u root -p campus_tutor < initdatabase.sql
```

3. **修改配置文件**
```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_tutor
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379
```

4. **启动后端服务**
```bash
mvn clean install
mvn spring-boot:run
```

5. **访问 API 文档**
```
http://localhost:8080/doc.html
```

### Web 前端启动

```bash
cd campus-web
npm install
npm run dev
```

访问:  `http://localhost:5173`

### 小程序启动

1. 使用 HBuilderX 或微信开发者工具打开 `campus-user-app` 目录
2. 修改 `utils/config.js` 中的后端 API 地址
3. 编译运行到微信开发者工具

---

## 👥 系统角色

| 角色 | 代码值 | 功能权限 |
|------|--------|---------|
| 管理员 | 0 | 平台管理、用户审核、数据统计 |
| 教员 | 1 | 个人信息管理、接单、查看订单 |
| 家长 | 2 | 发布需求、搜索教员、下单、评价 |

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
