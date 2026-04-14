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

整个工作区分为后端 + 三个前端应用 + 共享模块。以下为过滤了编译产物和临时文件后的核心架构树及职责说明：

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

---

## 5. 开发规范总结

通过扫描后端源码（如 `DemandController.java`, `GlobalExceptionHandler.java` 等），总结出本项目已实施的以下开发规约：

1. **命名规约**
   - **类名及分层**：严格遵循 `Controller` -> `Service` (接口与实现 `ServiceImpl`) -> `Mapper` -> `Entity` 的层级命名。
   - **数据传输对象 (DTO)**：前后端交互的对象必须封装，请求入参一般命名为 `XxxRequest` (如 `DemandPostRequest`)，绝不直接对外暴露数据库 Entity 进行接收。
2. **RESTful API 风格**
   - 路径全小写并支持复数/资源概念定语（如 `/api/demand/publish`，`/api/demand/{id}/match`）。
   - 严格区分 HTTP Method：`@GetMapping` (查询)，`@PostMapping` (新建/执行动作)，`@PutMapping` (更新)，`@DeleteMapping` (删除)。
3. **统一异常处理与响应拆包机制**
   - 包含基于 `@RestControllerAdvice` 注入的 `GlobalExceptionHandler`，它能拦截系统级异常或自定义 `BusinessException`。
   - 所有的 API 响应强制约束为 `Result<T>` 泛型包装结构，包含 `code`, `msg`, `data`, `timestamp`四大字段。业务异常被捕获后优雅转化为带自定义 Code 和明文错误提示（不抛出 HTTP 500）。
   - 参数校验强依赖 JSR-380 (`@Valid`)，违反校验规则同样能在全局被捕获为格式化文本返回。
4. **部署与环境配置**
   - 遵循 `application.properties` 统一管理，所有敏感值均通过 `${ENV_VAR:默认值}` 环境变量占位符注入，抽离了 DB、Redis、LLM AI-Key、地图 API Key 以及推荐系统的核心算法权重阈值。
   - 项目根路径提供 `Dockerfile` 和 `docker-compose.yml`，符合容器化一键编排要求。
