# 素质教育匹配平台

<p align="center">
  <strong>基于人工智能的素质教育服务匹配平台</strong>
</p>

<p align="center">
  <a href="#项目简介">项目简介</a> •
  <a href="#核心特性">核心特性</a> •
  <a href="#技术架构">技术架构</a> •
  <a href="#功能模块">功能模块</a> •
  <a href="#快速开始">快速开始</a> •
  <a href="#部署指南">部署指南</a> •
  <a href="#开发指南">开发指南</a>
</p>

## 📖 项目简介

**素质教育匹配平台**是一个面向家长、素质教育服务提供者与管理员的现代化服务平台，采用微服务架构设计，集成了人工智能、推荐算法和地理位置服务等先进技术。

### 🎯 项目定位

本平台致力于解决素质教育领域供需匹配的难题，通过智能算法精准连接优质教育资源与需求方，构建一个高效、透明、可靠的素质教育服务生态系统。

### 🌟 设计理念

- **智能匹配**：基于深度学习与协同过滤的精准推荐
- **全流程管理**：从需求发布到服务评价的完整闭环
- **多端协同**：家长端、服务提供者端、管理端分离设计
- **技术驱动**：人工智能赋能素质教育服务全流程

## 🚀 核心特性

### 1. 🧠 智能推荐与匹配
- **DeepFM深度学习模型**：基于深度学习的精准匹配算法
- **协同过滤**：基于用户行为的个性化推荐
- **实时意图识别**：通过Redis Stream实时捕捉用户兴趣
- **流量池赛马机制**：公平的曝光与晋升体系
- **地理位置服务**：智能距离计算与路径规划

### 2. 🤖 AI智能助手
- **智能需求解析**：自然语言转结构化需求信息
- **多轮对话系统**：上下文感知的智能对话助手
- **教案智能生成**：基于学生情况的个性化教学方案
- **评语专业润色**：AI辅助评语优化与专业表达

### 3. 📱 多端应用架构
- **家长端**：需求发布、服务搜索、订单管理
- **服务提供者端**：服务展示、订单接单、教学管理
- **管理后台**：平台运营、审核管理、数据监控
- **共享组件层**：统一API、样式规范、工具函数

### 4. 🛠️ 全流程管理
- **需求闭环**：发布、匹配、接单、支付、评价完整流程
- **课时管理**：上下课打卡、课时记录、进度跟踪
- **资金管理**：钱包体系、充值提现、资金安全保障
- **信用体系**：评价系统、信用评分、服务质量保障

## 🏗️ 技术架构

### 后端架构
```text
campus-backend/
├── Spring Boot 3.2.1          # 后端主框架
├── MyBatis-Plus 3.5.5         # ORM框架
├── MySQL 8.0                  # 主数据库
├── Redis 7.0                  # 缓存与实时数据处理
├── JWT                        # 安全认证
└── Knife4j                    # API文档
```

### 前端架构
```text
多端分离架构：
- 家长端 (Vue 3 + Vite + Element Plus)
- 服务提供者端 (Vue 3 + Vite + Element Plus)  
- 管理后台 (Vue 3 + Vite + Element Plus)
- 共享层 (统一API、状态管理、工具函数)
```

### 智能算法栈
- **DeepFM**：深度学习匹配模型（ONNX推理）
- **协同过滤**：基于物品的推荐算法
- **实时意图**：Redis Stream + ZSET实现
- **流量池**：BASIC/WARM/HOT三级曝光体系

## 📦 功能模块

### 1. 认证与权限模块
- 多角色登录注册（家长、服务提供者、管理员）
- JWT令牌认证与权限控制
- 验证码安全机制
- 个人信息管理

### 2. 需求与订单模块
- 智能需求发布与解析
- 多维度服务搜索与筛选
- 智能匹配推荐排序
- 订单全生命周期管理

### 3. 教学服务模块
- 课时计划与排期管理
- 上下课打卡与考勤
- 教学记录与评价
- 学习进度跟踪

### 4. 智能算法模块
- 深度学习匹配模型服务
- 实时用户行为分析
- 个性化推荐引擎
- 服务质量评估体系

### 5. 运营管理模块
- 用户与内容审核
- 订单与资金监管
- 数据统计与分析
- 系统配置管理

## 🚀 快速开始

### 环境要求

- **Java**: JDK 17+
- **Node.js**: 18.0.0+
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **Maven**: 3.8+

### 1. 后端服务启动

```bash
# 进入后端目录
cd campus-backend

# 安装依赖并启动服务
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动，API文档地址：`http://localhost:8080/doc.html`

### 2. 数据库初始化

确保MySQL服务已启动，创建数据库：
```sql
CREATE DATABASE IF NOT EXISTS campus_tutor_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

数据库脚本位于 `campus-backend/sql/` 目录下。

### 3. 前端服务启动

#### 家长端启动
```bash
cd campus-web-parents
npm install
npm run dev
```
访问地址：`http://localhost:5175`

#### 服务提供者端启动
```bash
cd campus-web-teacher
npm install  
npm run dev
```
访问地址：`http://localhost:5174`

#### 管理后台启动
```bash
cd campus-web-admin
npm install
npm run dev
```
访问地址：`http://localhost:3001`

## 🐳 Docker部署

项目支持Docker容器化部署，具体配置请参考 `campus-backend/docker-compose.yml` 文件。

### 使用Docker Compose启动
```bash
cd campus-backend
docker-compose up -d
```

## ⚙️ 配置说明

### 后端配置
主要配置文件：`campus-backend/src/main/resources/application.properties`

关键配置项：
```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/campus_tutor_db
spring.datasource.username=root
spring.datasource.password=your_password

# Redis配置
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT配置
jwt.secret=your_jwt_secret_key
jwt.expiration=604800000

# AI服务配置（DeepSeek API）
llm.enabled=true
llm.provider=deepseek
llm.api-key=your_deepseek_api_key
llm.base-url=https://api.deepseek.com
```

### 前端配置
各端配置文件位于对应目录的 `vite.config.js` 和环境变量文件中。

## 🧪 测试与验证

### 后端测试
```bash
cd campus-backend
mvn test
```

测试报告将显示在控制台，详细测试结果可查看 `campus-backend/TEST_REPORT.md`。

### API接口测试
平台提供完整的Swagger API文档，启动后端服务后访问：
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Knife4j文档: `http://localhost:8080/doc.html`

## 📁 项目目录结构

```text
素质教育匹配平台/
├── campus-backend/                    # 后端服务
│   ├── src/main/java/com/campus/      # 后端源代码
│   ├── src/main/resources/            # 配置文件与资源
│   ├── sql/                          # 数据库脚本
│   └── pom.xml                       # Maven依赖配置
│
├── campus-web-parents/                # 家长端前端
├── campus-web-teacher/                # 服务提供者端前端
├── campus-web-admin/                  # 管理后台前端
├── campus-web-shared/                 # 前端共享层
│   ├── api/                          # 统一API接口
│   ├── stores/                       # 状态管理
│   ├── styles/                       # 公共样式
│   └── utils/                        # 工具函数
│
├── docs/                             # 项目文档
│   ├── DATABASE_SCHEMA.md            # 数据库设计
│   ├── deepfm_architecture.md        # 深度学习架构
│   ├── RAG_Prompt_实现文档.md         # AI提示词设计
│   └── Web端功能总览.md               # 功能总览
│
├── ARCHITECTURE.md                   # 系统架构总览
├── README.md                         # 项目说明文档
└── docker-compose.yml                # Docker编排配置
```

## 🔧 开发指南

### 代码规范

- **后端代码**：遵循阿里巴巴Java开发规范
- **前端代码**：遵循Vue.js官方风格指南
- **Git提交**：使用Conventional Commits规范
- **API设计**：遵循RESTful API设计原则

### 分支管理

- `main`: 生产环境稳定分支
- `develop`: 开发主分支
- `feature/*`: 功能开发分支
- `release/*`: 版本发布分支
- `hotfix/*`: 紧急修复分支

### 提交规范
```bash
git commit -m "feat: 新增智能匹配算法"
git commit -m "fix: 修复订单状态同步问题"
git commit -m "docs: 更新API接口文档"
git commit -m "refactor: 重构用户认证模块"
```

## 📈 技术亮点

### 1. 深度学习与推荐系统融合
- DeepFM模型实现精准匹配
- 协同过滤补充冷启动问题
- 实时意图捕捉提升推荐时效性

### 2. 多级缓存架构
- Redis缓存热点数据
- 本地缓存高频访问数据
- 数据库持久化存储

### 3. 微服务化设计
- 模块化业务拆分
- 清晰的接口边界
- 可独立部署的服务单元

### 4. 全链路监控
- 应用性能监控
- 业务指标追踪
- 异常报警机制

## 🤝 贡献指南

我们欢迎任何形式的贡献，包括但不限于：

1. **问题反馈**：提交Issue报告bug或建议新功能
2. **代码贡献**：提交Pull Request改进代码
3. **文档完善**：改进或翻译文档
4. **测试用例**：补充单元测试或集成测试

### 贡献流程
1. Fork本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系与支持

如有问题或需要支持，请通过以下方式联系我们：

- **项目Issues**: [提交Issue](https://github.com/your-repo/issues)
- **电子邮件**: support@example.com
- **文档中心**: 查看项目文档获取详细使用指南

## 🙏 致谢

感谢所有为项目做出贡献的开发者，以及使用本平台的用户。素质教育匹配平台的发展离不开社区的共同努力。

---

<p align="center">
  <sub>构建未来教育的智能连接平台 🌟</sub>
</p>

<p align="center">
  <sub>最后更新: 2026年4月</sub>
</p>