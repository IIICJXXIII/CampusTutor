# 教师端与家长端网页分离开发 - TODO 文档

> **项目目标**：将现有 campus-web 混合端拆分为独立的 campus-web-teacher 教师端和 campus-web-parents 家长端项目，实现业务解耦和差异化体验，同时复用通用代码和后端 API。
>
> **创建日期**：2026年1月9日
> **最后更新**：2026年1月9日

---

## 📊 进度概览

| 阶段 | 描述 | 状态 | 完成度 |
|------|------|------|--------|
| 第一阶段 | 项目初始化与基础架构 | ✅ 已完成 | 100% |
| 第二阶段 | 教师端页面开发 | 🔄 进行中 | 70% |
| 第三阶段 | 家长端页面开发 | 🔄 进行中 | 90% |
| 第四阶段 | 差异化与优化 | ⏳ 待开始 | 0% |

---

## 第一阶段：项目初始化与基础架构 ✅

### 1.1 项目结构初始化

- [x] 创建 `campus-web-teacher` 目录并初始化 Vue 3 + Vite 项目
- [x] 创建 `campus-web-parents` 目录并初始化 Vue 3 + Vite 项目
- [x] 创建 `campus-web-shared` 共享代码目录
- [x] 配置 `vite.config.js`（复用 campus-web 配置）
- [x] 配置 `package.json` 依赖

### 1.2 共享模块复用

- [x] 迁移 `api/` 请求模块到 `campus-web-shared/api/`
  - [x] `request.js` - 请求封装
  - [x] `auth.js` - 认证接口
  - [x] `user.js` - 用户接口
  - [x] `tutor.js` - 教师接口
  - [x] `parent.js` - 家长接口
  - [x] `demand.js` - 需求接口
  - [x] `order.js` - 订单接口
  - [x] `teaching.js` - 课时接口
  - [x] `wallet.js` - 钱包接口
  - [x] `chat.js` - 聊天接口
  - [x] `file.js` - 文件接口
  - [x] `ocr.js` - OCR接口
  - [x] `map.js` - 地图接口
  - [x] `llm.js` - AI接口
  - [x] `match.js` - 匹配接口
  - [x] `review.js` - 评价接口
  - [x] `wrongbook.js` - 错题本接口
- [x] 迁移 `stores/` 状态管理到 `campus-web-shared/stores/`
- [x] 迁移 `styles/` 公共样式到 `campus-web-shared/styles/`
  - [x] `variables.scss` - 样式变量
  - [x] `mixins.scss` - 混合宏
  - [x] `index.scss` - 公共样式

### 1.3 路由和状态管理配置

- [x] 教师端 `router/index.js` 配置
- [x] 家长端 `router/index.js` 配置
- [ ] 教师端 Pinia stores 配置（按需拆分）
- [ ] 家长端 Pinia stores 配置（按需拆分）

### 1.4 布局组件

- [x] 教师端 `MainLayout.vue` 布局组件
- [x] 家长端 `MainLayout.vue` 布局组件

---

## 第二阶段：教师端页面开发 (campus-web-teacher) 🔄

### 2.1 认证模块 ✅

- [x] `Login.vue` - 登录页面
  - [x] 调用 `/api/auth/login` 登录接口
  - [x] 调用 `/api/auth/send-code` 发送验证码
- [x] `Register.vue` - 注册页面
  - [x] 调用 `/api/auth/register` 注册接口
  - [x] 调用 `/api/auth/send-code` 发送验证码

### 2.2 资质认证模块 ✅

- [x] `TeacherAuth.vue` - 教师认证页面（3步骤）
  - [x] 步骤1：学生证OCR识别 - `/api/ocr/student-card`
  - [x] 步骤2：身份证OCR识别 - `/api/ocr/idcard-front`、`/api/ocr/idcard-back`
  - [x] 步骤3：信息确认提交 - `/api/tutor/certification`

### 2.3 个人简历模块 ✅

- [x] `MyResume.vue` - 教师简历编辑页
  - [x] 获取简历 `GET /api/tutor/profile`
  - [x] 更新简历 `PUT /api/tutor/profile`
- [x] `Schedule.vue` - 可用时间段管理
  - [x] 获取时间段 `GET /api/tutor/schedule`
  - [x] 更新时间段 `POST /api/tutor/schedule`

### 2.4 找学生模块 🔄

- [x] `FindStudents.vue` - 需求地图/列表页
  - [x] 附近需求 `/api/demand/nearby`
  - [x] 需求列表 `/api/demand/list`
  - [ ] 地图集成 `/api/map/*`
- [x] `StudentList.vue` - 学生需求列表
- [x] `DemandDetail.vue` - 需求详情页
  - [x] 获取详情 `/api/demand/{id}`

### 2.5 接单模块 🔄

- [x] `DemandDetail.vue` - 接单功能（复用需求详情页）
  - [ ] 接受订单 `/api/order/{id}/accept`

### 2.6 订单管理模块 ✅

- [x] `OrderList.vue` - 教师订单列表
  - [x] 获取订单列表 `/api/order/tutor/list`
- [x] `OrderDetail.vue` - 订单详情页
  - [x] 获取订单详情 `/api/order/{id}`
  - [ ] 确认开始 `/api/order/{id}/confirm-start`

### 2.7 课时打卡模块 ✅

- [x] `LessonList.vue` - 课时记录列表
  - [x] 我的课时 `/api/teaching/my`
  - [x] 订单课时 `/api/teaching/order/{orderId}`
- [x] `LessonDetail.vue` - 课时详情
- [x] `CheckIn.vue` - 上下课打卡页面
  - [x] 上课打卡 `/api/teaching/check-in`
  - [x] 下课打卡 `/api/teaching/{id}/check-out`

### 2.8 钱包提现模块 ✅

- [x] `Wallet.vue` - 钱包余额页面
  - [x] 获取余额 `/api/wallet/`
  - [x] 交易记录 `/api/wallet/transactions`
- [x] `Withdraw.vue` - 提现页面
  - [x] 发起提现 `/api/wallet/withdraw`
  - [x] 提现记录 `/api/wallet/withdrawals`

### 2.9 通用模块 🔄

- [x] `ChatList.vue` - 聊天列表页
  - [x] 会话列表 `/api/chat/*`
- [x] `ChatRoom.vue` - 聊天室页面
  - [ ] WebSocket连接 `/ws/chat`
- [x] `AiChat.vue` - AI助手页面
  - [x] AI对话 `/api/llm/*`
- [x] `Mine.vue` - 个人中心页面
- [x] `Settings.vue` - 设置页面

---

## 第三阶段：家长端页面开发 (campus-web-parents) 🔄

### 3.1 认证模块 ✅

- [x] `Login.vue` - 登录页面
  - [x] 调用 `/api/auth/login` 登录接口
  - [x] 调用 `/api/auth/send-code` 发送验证码
- [x] `Register.vue` - 注册页面
  - [x] 调用 `/api/auth/register` 注册接口

### 3.2 学生管理模块 ✅

- [x] `StudentList.vue` - 学生列表页
  - [x] 获取学生列表 `/api/parent/students`
- [x] `AddStudent.vue` - 学生编辑页
  - [x] 添加学生 `POST /api/parent/student`
  - [x] 编辑学生 `PUT /api/parent/student`
  - [x] 删除学生 `DELETE /api/parent/student`
- [x] `StudentDetail.vue` - 学生详情页
  - [x] 学生详情 `/api/parent/student/{id}`

### 3.3 需求发布模块 ✅

- [x] `CreateDemand.vue` - 需求发布页（3步骤）
  - [x] 步骤1：基本信息
  - [x] 步骤2：详细要求
  - [x] 步骤3：确认发布
  - [x] 创建需求 `POST /api/demand`
  - [x] AI解析需求 `/api/llm/parse-demand`
- [x] `EditDemand.vue` - 需求编辑页
  - [x] 编辑需求 `PUT /api/demand`
  - [x] 发布需求 `/api/demand/{id}/publish`
  - [x] 撤回需求 `/api/demand/{id}/withdraw`

### 3.4 我的需求模块 ✅

- [x] `DemandList.vue` - 需求管理列表页
  - [x] 我的需求 `/api/demand/my`
- [x] `DemandDetail.vue` - 需求详情页
  - [x] 需求详情 `/api/demand/{id}`
- [x] `ApplicantList.vue` - 申请人列表页
  - [x] 获取申请列表 `/api/demand/{id}/applicants`

### 3.5 找老师模块 ✅

- [x] `FindTeachers.vue` - 教师筛选匹配页
  - [x] 匹配教师 `GET /api/match/tutors`
  - [x] 智能匹配 `POST /api/match/tutors`
- [x] `TeacherList.vue` - 教师列表页
- [x] `TeacherDetail.vue` - 教师公开档案页
  - [x] 教师档案 `/api/tutor/public/{userId}`
  - [x] 教师评价 `/api/review/tutor/{tutorId}`

### 3.6 预约签约模块 ✅

- [x] `TeacherDetail.vue` - 预约功能（复用教师详情页）
  - [x] 创建订单 `POST /api/order`

### 3.7 支付模块 ✅

- [x] `OrderPay.vue` - 订单支付页面
  - [x] 支付订单 `/api/order/{id}/pay`
  - [x] 余额支付 `/api/wallet/pay`

### 3.8 订单管理模块 ✅

- [x] `OrderList.vue` - 家长订单列表
  - [x] 获取订单列表 `/api/order/parent/list`
- [x] `OrderDetail.vue` - 订单详情页
  - [x] 获取订单详情 `/api/order/{id}`
  - [x] 取消订单 `/api/order/{id}/cancel`
  - [x] 完成订单 `/api/order/{id}/complete`
- [x] `OrderReview.vue` - 订单评价页
  - [x] 提交评价 `POST /api/review`

### 3.9 课时确认模块 ✅

- [x] `LessonList.vue` - 课时记录列表
  - [x] 我的课时 `/api/teaching/my`
  - [x] 订单课时 `/api/teaching/order/{orderId}`
- [x] `LessonDetail.vue` - 课时详情（确认/申诉）
  - [x] 确认课时 `/api/teaching/{id}/confirm`
  - [x] 申诉课时 `/api/teaching/{id}/dispute`

### 3.10 错题本模块 ⏳

- [ ] `WrongBook.vue` - 错题本列表页
- [ ] `AddQuestion.vue` - OCR错题识别页
  - [ ] 通用OCR `/api/ocr/general`
  - [ ] ⚠️ **需后端补充**：错题存储API
- [ ] `QuestionDetail.vue` - 错题详情页

### 3.11 通用模块 ✅

- [x] `ChatList.vue` - 聊天列表页
  - [x] 会话列表 `/api/chat/conversations`
- [x] `ChatRoom.vue` - 聊天室页面
  - [x] 聊天记录 `/api/chat/history`
  - [x] 发送消息 `/api/chat/send`
  - [x] WebSocket连接 `/ws/chat`
- [x] `AiChat.vue` - AI助手页面
  - [x] AI对话 `/api/llm/chat`
  - [x] 流式响应 `/api/llm/stream`
- [x] `Wallet.vue` - 钱包页面
  - [x] 获取余额 `/api/wallet/`
  - [x] 交易记录 `/api/wallet/transactions`
  - [x] 充值功能 `/api/wallet/recharge`
- [x] `Mine.vue` - 个人中心页面
  - [x] 获取统计 `/api/parent/stats`
- [x] `Settings.vue` - 设置页面
- [x] `EditProfile.vue` - 编辑资料页
  - [x] 获取用户信息 `/api/user/info`
  - [x] 更新用户信息 `/api/user/info`

---

## 第四阶段：差异化与优化 ⏳

### 4.1 UI/UX 差异化

- [ ] 教师端主题配置
  - [ ] 蓝色系专业风格
  - [ ] 定制 `variables.scss` 主题变量
  - [ ] 教师端专属图标和插图
- [ ] 家长端主题配置
  - [ ] 橙色系亲和风格
  - [ ] 定制 `variables.scss` 主题变量
  - [ ] 家长端专属图标和插图

### 4.2 权限与路由守卫

- [ ] 实现基于 `userType` 的路由守卫
- [ ] 教师端角色验证（userType === 'tutor'）
- [ ] 家长端角色验证（userType === 'parent'）
- [ ] 防止角色误入对方系统
- [ ] 未登录跳转登录页

### 4.3 响应式适配

- [ ] 确定适配策略（PC端 / 移动端 / 两者兼顾）
- [ ] 实现响应式布局
- [ ] 移动端底部导航适配
- [ ] 触摸手势支持

### 4.4 性能优化

- [ ] 路由懒加载优化
- [ ] 组件按需加载
- [ ] 图片懒加载
- [ ] 请求缓存策略

### 4.5 测试与部署

- [ ] 单元测试覆盖
- [ ] E2E 测试
- [ ] 生产环境构建配置
- [ ] 部署文档

---

## 📋 详细接口对照表

| 模块 | 教师端接口 | 家长端接口 |
|------|-----------|-----------|
| **认证** | `/api/auth/*` | `/api/auth/*` |
| **用户** | `/api/user/*` | `/api/user/*` |
| **教师档案** | `/api/tutor/*`（全部） | `/api/tutor/public/{id}`（只读） |
| **家长学生** | - | `/api/parent/*`（全部） |
| **需求** | `/api/demand/list`, `/nearby`, `/{id}`（只读） | `/api/demand/*`（全部） |
| **匹配** | - | `/api/match/tutors` |
| **订单** | `/tutor/list`, `/accept`, `/confirm-start` | `/parent/list`, `/create`, `/confirm`, `/pay`, `/cancel`, `/complete` |
| **课时** | `/check-in`, `/check-out`, `/my` | `/confirm`, `/dispute`, `/my` |
| **钱包** | `/`, `/transactions`, `/withdraw`, `/withdrawals` | `/`, `/transactions`（只读） |
| **聊天** | `/api/chat/*` + WebSocket | `/api/chat/*` + WebSocket |
| **文件** | `/api/file/*` | `/api/file/*` |
| **OCR** | `/student-card`, `/idcard-*` | `/general` |
| **地图** | `/api/map/*` | `/api/map/*` |
| **LLM** | `/api/llm/*` | `/api/llm/*` |

---

## ⚠️ 待解决问题

### 后端接口补充

- [ ] 错题本存储 API（`/api/wrongbook/*`）
- [ ] 评价模块 API（`/api/review/*`）
- [ ] 微信登录接口（如需支持）

### 共享代码策略

**当前方案**：`campus-web-shared` 目录存放共用代码

**备选方案**：
1. npm 私有包
2. Git submodule
3. Monorepo (pnpm workspace)

### 移动端适配决策

- [ ] 确认是否需要响应式设计支持移动浏览器
- [ ] 确认是否仅针对 PC 端开发

---

## 📝 开发规范

### 文件命名

- Vue 组件：`PascalCase.vue`（如 `OrderList.vue`）
- JS 文件：`camelCase.js`（如 `request.js`）
- 样式文件：`kebab-case.scss`（如 `teacher.scss`）

### 接口调用

- 统一使用 `campus-web-shared/api/` 中封装的接口
- 错误处理统一在 `request.js` 中配置
- Token 自动注入

### Git 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 样式调整
refactor: 重构
test: 测试
chore: 构建/工具
```

---

## 📅 里程碑

| 里程碑 | 目标日期 | 状态 |
|--------|----------|------|
| 第一阶段完成 | 2026-01-08 | ✅ 已完成 |
| 教师端核心功能 | 2026-01-15 | 🔄 进行中 |
| 家长端核心功能 | 2026-01-22 | ⏳ 待开始 |
| 差异化与优化 | 2026-01-29 | ⏳ 待开始 |
| 测试与上线 | 2026-02-05 | ⏳ 待开始 |

---

> **图例说明**
> - ✅ 已完成
> - 🔄 进行中
> - ⏳ 待开始
> - ⚠️ 需要关注/有问题
