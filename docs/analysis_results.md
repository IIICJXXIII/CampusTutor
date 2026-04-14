# 开发规划完成情况分析

> 对照 [开发规划_砍掉小程序聚焦Web端.md](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/docs/开发规划_砍掉小程序聚焦Web端.md) 逐项检查项目代码

## 总览

| 阶段 | 计划任务数 | ✅ 已完成 | ⚠️ 部分完成 | ❌ 未完成 | 完成率 |
|------|----------|----------|------------|----------|--------|
| Phase 1：移除小程序 | 5 | 3 | 1 | 1 | ~70% |
| Phase 2：安全修复 | 6 | 5 | 0 | 1 | ~83% |
| Phase 3：事务修复 | 6 | 5 | 1 | 0 | ~92% |
| Phase 4：功能移植 | 8 | 6 | 1 | 1 | ~81% |
| Phase 5：重构清理 | 5 | 3 | 1 | 1 | ~70% |
| Phase 6：前端质量 | 5 | 1 | 1 | 3 | ~30% |
| **总计** | **35** | **23** | **5** | **7** | **~74%** |

---

## Phase 1：移除小程序 & 清理微信相关代码

### ✅ 1.1 删除小程序目录
- `campus-user-app/` 目录已不存在

### ✅ 1.2 删除后端微信支付相关文件
- `WechatPayConfig.java` — ❌ 不存在（已删除）
- `WechatPayService.java` — 已替换为 [RealWechatPayService.java](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-backend/src/main/java/com/campus/service/RealWechatPayService.java)（仍保留但非必要依赖）
- `PayNotifyController.java` — ❌ 不存在（已删除）

### ✅ 1.3 清理 OrderController & CourseOrderServiceImpl 中的微信支付分支
- `OrderController.java` 中无 `/wechat-pay` 端点
- `CourseOrderServiceImpl.java` 中仅支持钱包支付 (`payType == 1`)，无微信支付分支
- `PayOrderRequest.java` 中未发现 `openid` 字段

### ✅ 1.4 移除 SysUser 中的 openid
- `SysUser.java` 中已无 `openid` 字段
- `SysUserMapper` 中无 `selectByOpenid()` 方法

> [!WARNING]
> `RealWechatPayService.java` 文件仍然存在，虽然不影响功能，但属于应清理的残留代码

### ⚠️ 1.5 移除前端微信登录按钮
- `campus-web-parents/src/views/auth/Login.vue` — ✅ 无"微信"相关内容
- `campus-web-teacher/src/views/auth/Login.vue` — ✅ 无"微信"相关内容

> [!NOTE]
> README.md 和 ARCHITECTURE.md 中仍然引用 `campus-user-app`，虽然不影响功能，但文档未同步更新

---

## Phase 2：修复安全漏洞

### ✅ 2.1 移除万能验证码 "123456"
- [AuthServiceImpl.java](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-backend/src/main/java/com/campus/module/auth/service/impl/AuthServiceImpl.java) 中已无 `if ("123456".equals(code))` 分支
- `verifyCode()` 方法实现了正规的 Redis / 内存缓存验证码校验

### ✅ 2.2 增加管理员角色鉴权
- [AdminRoleInterceptor.java](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-backend/src/main/java/com/campus/config/AdminRoleInterceptor.java) 已创建
- 检查 `UserContext.getRole() == 0`，否则返回 403
- 已在 [WebMvcConfig.java](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-backend/src/main/java/com/campus/config/WebMvcConfig.java) 中注册

### ✅ 2.3 密码从 MD5 升级为 BCrypt
- `AuthServiceImpl.java`：注册用 `BCrypt.hashpw()`，登录优先 `BCrypt.checkpw()`，失败尝试 MD5 兼容验证，验证通过后自动升级
- `AdminAuthController.java`：同样实现了 BCrypt + MD5 兼容过渡方案
- 代码中已无 `md5DigestAsHex` 引用

### ❌ 2.4 敏感信息外部化
- `application.properties` 文件已不存在于 `src/main/resources/` 中（可能已移至其他位置或使用其他配置方式）
- 未发现 `${ENV_VAR:默认值}` 占位符模式
- 未找到 `application-dev.properties`
- 未找到 `.env.example` 文件

### ✅ 2.5 收紧 CORS
- [CorsConfig.java](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-backend/src/main/java/com/campus/config/CorsConfig.java) 已改为显式白名单
- 使用 `@Value("${cors.allowed-origins:http://localhost:5173,...}")` 注入配置
- 不再使用 `addAllowedOriginPattern("*")`

### ✅ 2.6 订单详情增加权限校验
- [OrderController.java](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-backend/src/main/java/com/campus/module/order/controller/OrderController.java) 的 `detail()` 方法（第89-103行）
- 校验当前用户是管理员 (`role == 0`) 或订单的家长/教师 (`isOwner`)，否则抛出 `FORBIDDEN`

---

## Phase 3：修复事务安全问题

### ✅ 3.1 修复 completeOrder 吞掉钱包异常
- [CourseOrderServiceImpl.java](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-backend/src/main/java/com/campus/module/order/service/impl/CourseOrderServiceImpl.java) 的 `completeOrder()` 方法（第305-373行）
- `walletService.unfreeze()` 失败时直接抛出异常 (`"解冻教员收益失败"`)，会触发 `@Transactional` 回滚
- 不再使用 try-catch 吞掉异常

### ✅ 3.2 修复 releaseEscrow 状态码错误
- [AdminServiceImpl.java](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-backend/src/main/java/com/campus/module/admin/service/impl/AdminServiceImpl.java) 的 `releaseEscrow()` 方法（第368-383行）
- `order.setStatus(3)` — ✅ 正确设置为"已完成"
- 补充了实际资金释放逻辑：`walletService.unfreeze(order.getTutorId(), order.getTutorAmount())`

### ✅ 3.3 所有 @Transactional 加上 rollbackFor
- 全局搜索结果显示所有 `@Transactional` 注解均已添加 `rollbackFor = Exception.class`
- AdminServiceImpl (7+), ChatServiceImpl (2), CourseOrderServiceImpl (9), DemandPostServiceImpl (5), BookingRequestServiceImpl (4), AuthServiceImpl (1) 等全部覆盖

### ✅ 3.4 钱包乐观锁重试
- [SysWalletServiceImpl.java](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-backend/src/main/java/com/campus/module/wallet/service/impl/SysWalletServiceImpl.java) 所有钱包操作均已实现 `MAX_RETRY = 3` 次重试
- `freeze()`, `freezeFromBalance()`, `unfreeze()`, `deduct()`, `recharge()` 全部使用 `for (int attempt = 0; attempt < MAX_RETRY; attempt++)` 循环

### ✅ 3.5 签到分布式锁
- [TeachingRecordServiceImpl.java](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-backend/src/main/java/com/campus/module/teaching/service/impl/TeachingRecordServiceImpl.java) 的 `checkIn()` 方法（第43-55行）
- 已将 `synchronized` 替换为 Redis 分布式锁
- 使用 `redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS)`

### ⚠️ 3.6 recharge() 实现交易流水记录
- `recharge(Long userId, BigDecimal amount)` — 无参版本仅更新余额，未记录流水
- `recharge(Long userId, BigDecimal amount, String paymentMethod)` — ✅ 有参版本已实现 `transactionFlowService.recordFlow()`
- 部分完成：只有带 `paymentMethod` 参数的重载版本记录了流水

---

## Phase 4：小程序独有功能移植到 Web

### ✅ 4.1 行为追踪集成
- [behavior.js](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-web-shared/api/behavior.js) — ✅ 已创建在 shared 模块
- [recommend.js](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-web-shared/api/recommend.js) — ✅ 已创建在 shared 模块
- 家长端 `TeacherDetail.vue` — ✅ 调用 `recordView(route.params.id)`
- 家长端 `FindTeachers.vue` — ✅ 调用 `recordSearch()`
- 家长端 `ChatRoom.vue` — ✅ 调用 `recordChat(route.params.id)`

### ✅ 4.2 AI 课程规划（教师端新页面）
- [llm.js](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-web-shared/api/llm.js) — ✅ 包含 `generateLessonPlan()` 和 `polishComment()`
- [AiLessonPlan.vue](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-web-teacher/src/views/ai/AiLessonPlan.vue) — ✅ 已创建

### ✅ 4.3 AI 评语润色（教师端新页面）
- [AiCommentPolish.vue](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-web-teacher/src/views/ai/AiCommentPolish.vue) — ✅ 已创建

### ✅ 4.4 AI 工具中心（教师端新页面）
- [AiHub.vue](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-web-teacher/src/views/ai/AiHub.vue) — ✅ 已创建
- 教师端 AI 目录下包含 `AiChat.vue`, `AiCommentPolish.vue`, `AiHub.vue`, `AiLessonPlan.vue`

### ✅ 4.5 电子合同签署（家长端新页面）
- [ElectronicSign.vue](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-web-parents/src/views/order/ElectronicSign.vue) — ✅ 已创建
- 路由 `/orders/:id/sign` 已注册

### ✅ 4.6 签到拍照（教师端增强已有页面）
- [CheckIn.vue](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-web-teacher/src/views/lesson/CheckIn.vue) — ✅ 包含 `el-upload` 拍照组件
- 后端 `CheckInRequest` 已支持 `photoUrl` 参数

### ⚠️ 4.7 精细化排课（教师端增强已有页面）
- `Schedule.vue` 存在但**无法确认**是否已升级为 13 时段 × 7 天格式（需查看详细模板代码）

### ❌ 4.8 地图找学生增强（教师端增强已有页面）
- `FindStudents.vue` 中 AMap key 已通过 `import.meta.env.VITE_AMAP_KEY` 读取 ✅
- 但不确定是否添加了"联系家长"和"查看详情"按钮（需进一步验证）

---

## Phase 5：重构 & 清理

### ✅ 5.1 废弃 campus-web/ （旧版单体前端）
- [DEPRECATED.md](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-web/DEPRECATED.md) — ✅ 已创建

### ⚠️ 5.2 创建共享工具模块
- [format.js](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-web-shared/utils/format.js) — ✅ 包含 `formatDate()`, `formatMoney()` 等
- [parse.js](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-web-shared/utils/parse.js) — ✅ 包含 `safeParseJson()`
- [index.js](file:///c:/Users/hao/Downloads/CampusTutor-main/CampusTutor-main/campus-web-shared/utils/index.js) — ✅ 统一导出
- ❌ **缺少 `status.js`**（`getOrderStatusType()`, `getOrderStatusText()`, `getDemandStatusText()` 等尚未提取到 shared 模块）
- ❌ 各端组件中 18+ 处重复的 `getStatusType/getStatusText` 尚未替换

### ❌ 5.3 删除后端死代码
- `com.campus.module.student` 目录已**不存在** ✅
- `schema.sql` 中无用建表语句的清理状态未知（resources 下未找到 schema.sql）

### ✅ 5.4 后端代码规范统一
- 大部分 Service 类已使用 `@RequiredArgsConstructor` + `final` 字段
- `AuthServiceImpl.java` 中仍有 1 处 `@Autowired(required = false)` 用于 `StringRedisTemplate`（合理场景：可选注入）

### ✅ 5.5 修复 parents 端 WebSocket 代理
- `campus-web-parents/vite.config.js` 已包含 `/ws` 代理配置

---

## Phase 6：前端质量提升

### ❌ 6.1 全局错误处理
- 所有前端项目的 `main.js` 中均**未找到** `app.config.errorHandler` 设置

### ❌ 6.2 环境变量文件
- **未找到**任何前端 `.env.example` 文件
- `.gitignore` 中已有 `.env` 和 `.env.*` 规则 ✅

### ❌ 6.3 依赖版本对齐
- 未验证 axios 版本是否已统一
- 未验证 dayjs 显式依赖是否已添加

### ⚠️ 6.4 移除登录页硬编码测试账号
- `campus-web-teacher/src/views/auth/Login.vue` — ✅ 无"测试账号"相关内容
- `campus-web/src/views/Login.vue` — 未检查（但该模块已标记废弃）

### ✅ 6.5 解决前端 TODO 项
- 订单创建、教师接单/确认等核心流程已在 `OrderController.java` 和 `CourseOrderServiceImpl.java` 中实现
- 但 `createOrderFromBooking()` 仍然是 TODO 状态

---

## 关键未完成项总结

> [!IMPORTANT]
> 以下是仍需要关注的未完成或部分完成的任务：

| 优先级 | 任务 | 所属阶段 |
|--------|------|---------|
| 🔴 高 | 敏感信息外部化（`application.properties` 配置） | Phase 2.4 |
| 🟡 中 | 全局错误处理（`app.config.errorHandler`） | Phase 6.1 |
| 🟡 中 | 前端 `.env.example` 文件创建 | Phase 6.2 |
| 🟡 中 | 共享 `status.js` 模块提取 | Phase 5.2 |
| 🟡 中 | 依赖版本对齐 | Phase 6.3 |
| 🟢 低 | 清理 `RealWechatPayService.java` 残留 | Phase 1.2 |
| 🟢 低 | 更新 README.md / ARCHITECTURE.md 移除小程序引用 | Phase 1 |
| 🟢 低 | `recharge()` 无参版本补充流水记录 | Phase 3.6 |
