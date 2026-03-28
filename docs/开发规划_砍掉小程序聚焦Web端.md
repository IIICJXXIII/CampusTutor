# CampusTutor 开发规划：砍掉小程序，聚焦 Web 端

## Context

微信小程序（`campus-user-app`）因管控问题无法上线，需要彻底移除。同时项目经过多人快速开发，存在安全漏洞、事务隐患、代码冗余等问题。本规划覆盖 6 个阶段，其中 Phase 2/3 与 Phase 4 可以并行推进。

### 决策记录
- **campus-web（旧版单体前端）**：标记废弃，保留作参考，后续确认无遗漏功能后再删除
- **安全修复**：与功能移植并行推进，不阻塞业务开发
- **小程序独有功能**：全部移植到 Web 端
- **本文档定位**：团队执行参考，由各成员领取任务手动实施

### 阶段依赖与并行关系

```
Phase 1 (移除小程序) ← 所有后续工作的前提
   ↓
Phase 2 (安全修复) ←→ Phase 3 (事务修复) ←→ Phase 4 (功能移植)  [三者可并行]
   ↓
Phase 5 (重构) ←→ Phase 6 (质量)  [可并行]
```

### 任务分配建议（6 人团队）

| 成员 | 建议任务 | 阶段 |
|------|---------|------|
| 成员 A | Phase 1 全部（移除小程序，~1-2 天） | 最先启动 |
| 成员 B | Phase 2.1-2.3 安全修复（验证码、鉴权、BCrypt） | Phase 1 完成后 |
| 成员 C | Phase 2.4-2.6 + Phase 3 全部（配置外部化、事务修复） | Phase 1 完成后 |
| 成员 D | Phase 4.1-4.4（行为追踪、AI 工具三件套） | Phase 1 完成后 |
| 成员 E | Phase 4.5-4.8（电子合同、签到拍照、排课、地图） | Phase 1 完成后 |
| 成员 F | Phase 5 + Phase 6（重构清理 + 质量提升） | Phase 2-4 基本完成后 |

### 预估总工时
- 单人串行：**16-23 个工作日**
- 团队并行（按上述分工）：**8-12 个工作日**

---

## Phase 1：移除小程序 & 清理微信相关代码（~1-2 天）

### 1.1 删除小程序目录
- 删除整个 `campus-user-app/` 目录

### 1.2 删除后端微信支付相关文件（3 个文件整体删除）
- `campus-backend/src/main/java/com/campus/config/WechatPayConfig.java`
- `campus-backend/src/main/java/com/campus/service/WechatPayService.java`
- `campus-backend/src/main/java/com/campus/module/order/controller/PayNotifyController.java`

### 1.3 清理 OrderController & CourseOrderServiceImpl 中的微信支付分支
- `CourseOrderServiceImpl.java`：移除 `WechatPayService` 注入、`payOrder()` 中的微信支付分支、`createWechatPayParams()`、`handlePaySuccess()` 方法、`applyRefund()` 中的微信退款逻辑
- `OrderController.java`：移除 `/wechat-pay` 端点
- `PayOrderRequest.java`：移除 `openid` 字段

### 1.4 移除 SysUser 中的 openid
- `SysUser.java`：删除 `openid` 字段
- `SysUserMapper.xml`：删除 `selectByOpenid` 查询及 resultMap/columnList 中的 openid
- `SysUserMapper.java`：删除 `selectByOpenid()` 方法
- `schema.sql`：删除 `openid` 列和 `uk_openid` 唯一索引
- 编写 SQL 迁移：`ALTER TABLE sys_user DROP INDEX uk_openid; ALTER TABLE sys_user DROP COLUMN openid;`

### 1.5 移除前端微信登录按钮
- `campus-web-parents/src/views/auth/Login.vue`：删除微信登录按钮及相关方法
- `campus-web-teacher/src/views/auth/Login.vue`：同上

### 验证
- `mvn clean compile` 编译通过
- 各前端 `npm run build` 无报错
- 钱包支付流程端到端正常

---

## Phase 2：修复安全漏洞（~3-4 天）

### 2.1 移除万能验证码 "123456"（**最紧急**）
- `AuthServiceImpl.java`：删除 `if ("123456".equals(code))` 分支
- 如需开发环境 bypass，改为 `@Profile("dev")` 条件注入或配置项 `sms.bypass.enabled=false`

### 2.2 增加管理员角色鉴权（**最紧急**）
- 新建 `campus-backend/src/main/java/com/campus/config/AdminRoleInterceptor.java`
  - `preHandle()` 中检查 `UserContext.getRole() == 0`，否则返回 403
- `WebMvcConfig.java`：注册拦截器到 `/api/admin/**`，排除 `/api/admin/auth/**`

### 2.3 密码从 MD5 升级为 BCrypt
- `AuthServiceImpl.java`：注册时用 `BCrypt.hashpw()`，登录时用 `BCrypt.checkpw()`
- 兼容过渡方案：登录时先尝试 BCrypt 验证，失败则用 MD5 验证，验证通过后自动将密码升级为 BCrypt 存储
- `AdminAuthController.java`：同步修改（当前用 `DigestUtils.md5DigestAsHex`）

### 2.4 敏感信息外部化
- `application.properties`：所有密钥改为 `${ENV_VAR:默认值}` 占位符
  - `spring.datasource.password=${DB_PASSWORD:}`
  - `jwt.secret=${JWT_SECRET:}`
  - `baidu.ocr.api-key=${BAIDU_OCR_API_KEY:}`
  - `baidu.ocr.secret-key=${BAIDU_OCR_SECRET_KEY:}`
  - `amap.key=${AMAP_KEY:}`
  - `llm.api-key=${LLM_API_KEY:}`
- 新建 `application-dev.properties` 放开发环境默认值
- 新建 `.env.example` 文档说明所有变量
- `.gitignore` 添加 `application-dev.properties`

### 2.5 收紧 CORS
- `CorsConfig.java`：将 `addAllowedOriginPattern("*")` 改为显式白名单
  - `http://localhost:5173`、`5174`、`5175`、`3001`
  - 生产环境通过配置项 `cors.allowed-origins` 注入

### 2.6 订单详情增加权限校验
- `OrderController.java` 的 `detail()` 方法：校验当前用户是订单的家长或教师（或管理员），否则 403

### 验证
- 验证码 "123456" 不再生效
- 非管理员 JWT 访问 `/api/admin/*` 返回 403
- BCrypt 密码注册→登录→旧 MD5 用户登录自动升级
- `application.properties` 中无明文密钥
- 跨域请求非白名单域名被拒绝

---

## Phase 3：修复事务安全问题（~2-3 天）

### 3.1 修复 completeOrder 吞掉钱包异常
- `CourseOrderServiceImpl.java` 的 `completeOrder()`：移除包裹 `walletService.unfreeze()` 的 try-catch，让异常传播触发 `@Transactional` 回滚

### 3.2 修复 releaseEscrow 状态码错误
- `AdminServiceImpl.java` 的 `releaseEscrow()`：`order.setStatus(4)` → `order.setStatus(3)`
- 补充实际资金释放逻辑：调用 `walletService.unfreeze()` 和扣除服务费

### 3.3 所有 @Transactional 加上 rollbackFor
- `AdminServiceImpl.java`：15 个方法
- `ChatServiceImpl.java`：2 个方法
- 全局搜索 `@Transactional` 确保无遗漏

### 3.4 钱包乐观锁重试
- `SysWalletServiceImpl.java`：所有 `updateById(wallet)` 失败时重试 2-3 次（重新读取后重算）

### 3.5 签到分布式锁
- `TeachingRecordServiceImpl.java`：将 `synchronized` 替换为 Redis 分布式锁
  - `String lockKey = "checkin:order:" + orderId`
  - 使用 `RedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, SECONDS)`

### 3.6 recharge() 实现交易流水记录
- `SysWalletServiceImpl.java` 的 `recharge()`：当前返回硬编码 `0L`，补充 `transactionFlowService.record()` 调用

### 验证
- 模拟 unfreeze 失败 → 订单不会被标记为完成
- releaseEscrow 后订单状态为 3，资金正确释放
- 并发钱包操作不会丢失更新
- 并发签到只产生一条记录

---

## Phase 4：小程序独有功能移植到 Web（~5-7 天）

### 4.1 行为追踪集成
- 新建 `campus-web-shared/api/behavior.js`（从 `campus-web/src/api/behavior.js` 移植）
- 新建 `campus-web-shared/api/recommend.js`（从 `campus-web/src/api/recommend.js` 移植）
- 家长端 `TeacherDetail.vue`：加载时调用 `recordView(tutorId)`
- 家长端 `TeacherList.vue`/`FindTeachers.vue`：搜索时调用 `recordSearch()`
- 家长端 `ChatRoom.vue`：发起聊天时调用 `recordChat(tutorId)`

### 4.2 AI 课程规划（教师端新页面）
- `campus-web-shared/api/llm.js`：新增 `generateLessonPlan(data)` 调用 `POST /llm/lesson/plan`
- 新建 `campus-web-teacher/src/views/ai/AiLessonPlan.vue`：表单（科目、学生水平、课时、学生信息）+ Markdown 渲染结果
- 教师端路由新增 `/ai/lesson-plan`

### 4.3 AI 评语润色（教师端新页面）
- `campus-web-shared/api/llm.js`：新增 `polishComment(data)` 调用 `POST /llm/lesson/comment`
- 新建 `campus-web-teacher/src/views/ai/AiCommentPolish.vue`：原始评语输入 + 润色结果展示
- 教师端路由新增 `/ai/comment-polish`

### 4.4 AI 工具中心（教师端新页面）
- 新建 `campus-web-teacher/src/views/ai/AiHub.vue`：卡片导航到 AI 聊天、课程规划、评语润色
- 修改教师端导航菜单：原 "AI助手" 链接改为指向 AiHub
- 教师端路由新增 `/ai/hub`

### 4.5 电子合同签署（家长端新页面）
- 新建 `campus-web-parents/src/views/order/ElectronicSign.vue`：展示合同条款（服务内容、价格、双方权责、违约条款）、确认勾选、签署按钮
- 接入订单确认流程：家长确认教师后，跳转合同签署页 → 签署后进入支付
- 家长端路由新增 `/orders/:id/sign`

### 4.6 签到拍照（教师端增强已有页面）
- 修改 `campus-web-teacher/src/views/lesson/CheckIn.vue`：
  - 添加 `el-upload` 拍照/选图组件（accept="image/*" capture="environment"）
  - 调用 `uploadFile()` 上传照片
  - 签到请求中携带 `photoUrl` 参数
  - 设为必填项

### 4.7 精细化排课（教师端增强已有页面）
- 修改 `campus-web-teacher/src/views/resume/Schedule.vue`：
  - 从 3 时段 × 7 天 升级为 13 时段 × 7 天（与小程序一致：08:00-08:40, 08:50-09:30 等）
  - 改为可点击网格 UI

### 4.8 地图找学生增强（教师端增强已有页面）
- 修改 `campus-web-teacher/src/views/home/FindStudents.vue`：
  - AMap key 通过 `import.meta.env.VITE_AMAP_KEY` 读取
  - 标记点显示价格/科目/年级信息
  - 添加"联系家长"和"查看详情"操作按钮

### 验证
- 浏览教师详情后 `user_action_log` 表有记录
- AI 课程规划返回 Markdown 格式教案
- 电子签署流程完整（确认→签署→支付）
- 拍照签到照片上传成功且 URL 存入 `teaching_record`

---

## Phase 5：重构 & 清理（~3-4 天）

### 5.1 废弃 `campus-web/`（旧版单体前端）
- 在 `campus-web/` 下添加 `DEPRECATED.md` 说明已废弃
- 确认无其他模块依赖后，后续可整目录删除
- 理由：有自己独立的 API 层和 Store，与 shared 严重分叉，存在双重前缀 bug（`/api/api/wallet`）

### 5.2 创建共享工具模块
- 新建 `campus-web-shared/utils/status.js`：导出 `getOrderStatusType()`、`getOrderStatusText()`、`getDemandStatusText()` 等
- 新建 `campus-web-shared/utils/format.js`：导出 `formatDate()`、`formatMoney()`、`formatPhone()`
- 新建 `campus-web-shared/utils/parse.js`：导出 `safeParseJson()`
- 新建 `campus-web-shared/utils/index.js`：统一导出
- 逐步替换家长端、教师端中 18+ 处重复的本地 `getStatusType/getStatusText` 实现

### 5.3 删除后端死代码
- 删除整个 `campus-backend/src/main/java/com/campus/module/student/` 目录（`Student` 实体映射的 `t_student` 表不存在，无任何引用）
- `schema.sql`：注释掉 7 个无后端代码的建表语句（`insurance_policy`、`student_report`、`mistake_notebook`、`sys_comment`、`sys_dict`、`community_post`、`community_reply`），标注 "Reserved for future"

### 5.4 后端代码规范统一
- Entity 类统一使用 `@Data`（移除 `ParentStudent`、`SysWallet` 中手写的 getter/setter）
- 依赖注入统一为 `@RequiredArgsConstructor` + `final` 字段（移除 `AuthController` 中的 `@Autowired`）
- 生产环境关闭 SQL 日志：`application.properties` 中注释掉 `mybatis-plus.configuration.log-impl`，改到 `application-dev.properties` 中

### 5.5 修复 parents 端 WebSocket 代理
- `campus-web-parents/vite.config.js`：添加 `/ws` 代理配置（当前缺失，教师端已有）

### 验证
- 所有前端 build 通过
- 后端编译无报错
- 家长端 WebSocket 聊天正常工作

---

## Phase 6：前端质量提升（~2-3 天）

### 6.1 全局错误处理
- `campus-web-parents/src/main.js`、`campus-web-teacher/src/main.js`、`campus-web-admin/src/main.js`：
  ```js
  app.config.errorHandler = (err, vm, info) => {
    console.error('Unhandled error:', err, info)
    ElMessage.error('系统异常，请稍后重试')
  }
  ```

### 6.2 环境变量文件
- 各前端项目新建 `.env.example`，文档化 `VITE_AMAP_KEY`、`VITE_WS_URL`、`VITE_API_BASE_URL`
- `.gitignore` 添加 `.env`、`.env.local`

### 6.3 依赖版本对齐
- 统一 axios 版本（对齐到 `^1.13.x`）
- `campus-web-teacher/package.json` 补充 `dayjs` 显式依赖
- 考虑引入 pnpm workspace 统一管理

### 6.4 移除登录页硬编码测试账号
- `campus-web-teacher/src/views/auth/Login.vue`：移除模板中的测试账号提示文字
- `campus-web/src/views/Login.vue`：移除测试账号快捷填充按钮

### 6.5 解决前端 TODO 项
- `ApplicantList.vue` (line 143)：实现创建订单 API 调用
- `DemandDetail.vue` (line 220)：实现选择老师接口调用
- `TeacherDetail.vue` (line 217)：实现预约流程

### 验证
- JS 运行时错误被全局捕获，用户看到友好提示
- 所有 `.env.example` 文件齐全
- 无依赖版本冲突告警

---

---

## 附录：发现的问题清单（供团队参考）

### 后端安全漏洞
| 问题 | 严重性 | 涉及文件 |
|------|--------|---------|
| 万能验证码 "123456" 可绕过任何短信验证 | **严重** | `AuthServiceImpl.java` |
| 任何登录用户可调用管理员接口（无角色校验） | **严重** | `WebMvcConfig.java` |
| MD5 无盐密码存储 | **高** | `AuthServiceImpl.java`, `AdminAuthController.java` |
| application.properties 明文存储数据库密码、JWT 密钥、API Key | **高** | `application.properties` |
| CORS 允许所有来源 + 携带凭证 | **高** | `CorsConfig.java` |
| 订单详情无权限校验，任何用户可查任何订单 | **中** | `OrderController.java` |

### 事务安全缺陷
| 问题 | 涉及文件 |
|------|---------|
| completeOrder 吞掉钱包异常，订单标记完成但教师未收款 | `CourseOrderServiceImpl.java` |
| releaseEscrow 设置 status=4（取消）而非 3（完成） | `AdminServiceImpl.java` |
| 15 个 @Transactional 缺少 rollbackFor | `AdminServiceImpl.java` |
| 钱包无乐观锁重试，并发操作可能丢失更新 | `SysWalletServiceImpl.java` |
| synchronized 签到在集群部署下失效 | `TeachingRecordServiceImpl.java` |
| recharge() 返回硬编码 0，无交易流水 | `SysWalletServiceImpl.java` |

### 前端代码问题
| 问题 | 涉及范围 |
|------|---------|
| campus-web 与 shared 完全分叉，API 有双重前缀 bug | `campus-web/src/api/` |
| 18+ 组件重复定义 getStatusType/getStatusText | 家长端、教师端多个 Vue 文件 |
| 无全局错误处理（app.config.errorHandler） | 所有前端 main.js |
| 无 .env 文件，API Key 硬编码在源码中 | `FindStudents.vue`, Login 页面 |
| parents 端 vite.config 缺少 WebSocket 代理 | `campus-web-parents/vite.config.js` |
| axios 版本不一致（1.6 vs 1.13） | 各 package.json |
| 3 个前端 TODO 未实现（创建订单、选择老师、预约流程） | `ApplicantList.vue`, `DemandDetail.vue`, `TeacherDetail.vue` |

### 后端死代码
| 项目 | 说明 |
|------|------|
| `com.campus.module.student` | 整个模块无用，映射不存在的 t_student 表 |
| schema.sql 7 张空表 | insurance_policy, student_report, mistake_notebook, sys_comment, sys_dict, community_post, community_reply |
| Entity 代码风格不统一 | 部分用 @Data，部分手写 getter/setter |
| SQL 日志输出到 stdout | 生产环境性能和安全隐患 |
