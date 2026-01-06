# CampusTutor 项目待办清单

> 更新日期: 2026-01-06  
> 本清单基于 `mission.md` 需求规格与最新代码评估生成

---

## 一、当前完成度总览

| 业务节点 | 后端 | campus-web | campus-web-admin | 小程序 | 状态 |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **用户认证 (JWT)** | ✅ | ✅ 已对接 | ✅ 已对接 | ⚠️ UI就绪 | 🟢 基本完成 |
| **教师认证 (OCR)** | ✅ 含百度OCR | ✅ 认证表单 | ✅ 审核页面 | ⚠️ UI就绪 | 🟢 待真实测试 |
| **家长发布需求** | ✅ CRUD+LBS | ✅ 三步表单 | ✅ 需求列表 | ⚠️ UI就绪 | 🟢 待对接 |
| **智能匹配** | ✅ 多维度算法 | ✅ 教师列表 | ✅ 匹配管理 | ⚠️ 地图模式 | 🟢 待对接 |
| **订单与签约** | ✅ 全状态流转 | ✅ 预约+支付 | ✅ 订单管理 | ❌ | 🟡 待对接 |
| **课时打卡** | ✅ 打卡+确认 | ✅ 课堂记录 | ✅ 课时列表 | ❌ | 🟡 待对接 |
| **钱包与结算** | ✅ 余额+冻结 | ⚠️ 简单展示 | ✅ 钱包管理 | ❌ | 🟡 待完善 |

**图例**: ✅ 已完成 | ⚠️ 部分完成 | ❌ 未实现 | 🟢 就绪 | 🟡 进行中 | 🔴 阻塞

---

## 二、后端模块评估 (campus-backend)

### ✅ 已完成模块
| 模块 | 说明 |
| :--- | :--- |
| `auth` | JWT 登录/注册/验证码(Mock) |
| `user` | 用户 CRUD，角色区分 |
| `tutor` | 教员档案、认证提交、排课配置 |
| `parent` | 家长学生管理 CRUD |
| `demand` | 需求发布、上下架、LBS 搜索 |
| `match` | 多维度搜索、匹配分数计算 |
| `order` | 订单全流程（创建→支付→托管→完成） |
| `teaching` | 课时打卡、家长确认/申诉 |
| `wallet` | 余额查询、冻结/解冻 |
| `file` | 本地文件上传 |
| `ocr` | 百度OCR集成 (含Mock模式) |

### ⚠️ 待完善/修复
| 问题 | 描述 | 优先级 |
| :--- | :--- | :---: |
| ~~数据库字段不匹配~~ | `demand_post` 表已在 `sql/schema.sql` 中修复 | ✅ 已修复 |
| 短信服务 | `AuthServiceImpl` 中 TODO: 接入真实短信 API | 🟡 中 |
| 申诉通知 | `TeachingRecordServiceImpl` 中 TODO: 申诉通知管理员 | 🟢 低 |
| 密码加密 | 当前使用 MD5，建议升级 BCrypt | 🟡 中 |
| 权限细化 | 缺少基于角色的接口访问控制 | 🟡 中 |

---

## 三、Web 用户端评估 (campus-web)

### ✅ 基础设施 (已完成)
- [x] Vite + Vue 3.4.21 配置
- [x] Element Plus 2.5.6 + SCSS 主题
- [x] Pinia 状态管理 (User, Tutor, Order, Demand Stores)
- [x] Axios 请求封装 + API 代理
- [x] 路由配置 + 权限守卫

### ✅ 已完成页面 (16个)
- [x] `Login.vue` - 登录 (已对接API)
- [x] `Register.vue` - 注册
- [x] `TeacherList.vue` - 教师列表/搜索
- [x] `TeacherProfile.vue` - 教师详情
- [x] `TeacherAuth.vue` - 教师认证 (3步)
- [x] `DemandForm.vue` - 发布需求 (3步)
- [x] `Mine.vue` - 个人中心
- [x] `OrderList.vue` - 订单列表
- [x] `FindStudents.vue` - 找学生 (地图/列表)
- [x] `MyResume.vue` - 我的简历
- [x] `ClassRecord.vue` - 课堂记录
- [x] `Booking.vue` - 预约课程
- [x] `Payment.vue` - 支付
- [x] `StudentDetail.vue` - 学生需求详情
- [x] `StudentList.vue` - 学生列表
- [x] `WrongBook.vue` - 错题本

### 🔴 待完成任务
| 任务 | 描述 | 优先级 |
| :--- | :--- | :---: |
| API 对接完善 | 部分页面仍使用 Mock 数据，需替换为真实 API | 🔴 高 |
| 错误处理 | 统一处理网络异常和业务错误提示 | 🟡 中 |
| 加载状态 | 添加骨架屏和加载动画 | 🟢 低 |
| 表单校验 | 完善各表单的前端校验规则 | 🟡 中 |

---

## 四、Web 管理后台评估 (campus-web-admin)

### ✅ 基础设施 (已完成)
- [x] Vite + Vue 3.4.21 配置
- [x] Element Plus 侧边栏布局
- [x] Axios 请求封装 + API 代理 (端口 3001)
- [x] 路由配置

### ✅ 已完成页面 (11个)
- [x] `Login.vue` - 管理员登录
- [x] `Dashboard.vue` - 仪表盘
- [x] `UserList.vue` - 用户管理
- [x] `TutorList.vue` - 教员列表
- [x] `TutorAudit.vue` - 教员审核
- [x] `ParentList.vue` - 家长列表
- [x] `DemandList.vue` - 需求管理
- [x] `MatchList.vue` - 匹配记录
- [x] `OrderList.vue` - 订单管理
- [x] `LessonList.vue` - 课时管理
- [x] `WalletList.vue` - 钱包/提现管理
- [x] `Settings.vue` - 系统设置

### 🔴 待完成任务
| 任务 | 描述 | 优先级 |
| :--- | :--- | :---: |
| API 对接 | 所有页面需对接后端真实 API | 🔴 高 |
| 仪表盘数据 | Dashboard 需对接统计 API | 🟡 中 |
| 图表可视化 | 集成 ECharts 展示业务数据 | 🟢 低 |

---

## 五、微信小程序评估 (campus-user-app)

### ✅ 基础设施 (已完成)
- [x] 原生小程序项目结构
- [x] API 配置文件 (`apiConfig.js`)
- [x] 请求封装 (`request.js`)
- [x] 路由配置 (`routeConfig.js`)

### ✅ 已完成页面
| 页面 | 路径 | 状态 |
| :--- | :--- | :---: |
| 登录 | `pages/common/login` | ✅ UI完成 |
| 注册 | `pages/common/register` | ✅ UI完成 |
| 个人中心 | `pages/common/personalCenter` | ✅ UI完成 |
| 认证步骤1 | `pages/teacher/certification/step1-base` | ✅ UI完成 |
| 认证步骤2 | `pages/teacher/certification/step2-ability` | ✅ UI完成 |
| 认证步骤3 | `pages/teacher/certification/step3-result` | ✅ UI完成 |
| 地图找学生 | `pages/teacher/mapFindStudent` | ✅ UI完成 |
| 发布需求步骤1 | `pages/parent/publishDemand/step1-student` | ✅ UI完成 |
| 发布需求步骤2 | `pages/parent/publishDemand/step2-teaching` | ✅ UI完成 |
| 发布需求步骤3 | `pages/parent/publishDemand/step3-preference` | ✅ UI完成 |
| 匹配结果 | `pages/parent/matchResult` | ✅ UI完成 |

### 🔴 待完成任务
| 任务 | 描述 | 优先级 |
| :--- | :--- | :---: |
| **API 对接** | 所有页面需对接后端 API | 🔴 高 |
| 微信登录 | 调用 `wx.login` 获取 code 对接后端 | 🔴 高 |
| 高德地图集成 | `mapFindStudent` 需集成地图 SDK | 🔴 高 |
| 订单流程页面 | 缺少订单列表、订单详情、支付页面 | 🟡 中 |
| 课时管理页面 | 缺少课时打卡、课程表页面 | 🟡 中 |
| 错题本页面 | 缺少错题本功能页面 | 🟢 低 |

---

## 六、数据库同步

### ⚠️ 重要：执行数据库同步
新的数据库脚本已更新至 `campus-backend/sql/` 目录：

```bash
# 1. 导入表结构 (会删除旧表)
mysql -u root -p < campus-backend/sql/schema.sql

# 2. 导入测试数据
mysql -u root -p campus_tutor_db < campus-backend/sql/data.sql
```

**已修复的表结构问题:**
- `demand_post`: 使用 `publisher_id` 替代 `parent_id`，字段与实体类完全匹配
- `tutor_schedule_config`: 从 JSON 改为独立字段
- `course_order`: 新增完整订单字段
- `teaching_record`: 新增时间戳字段

---

## 七、优先级任务清单

### 🔴 P0 - 阻塞性任务 (本周内)
1. [ ] **执行数据库同步** - 导入 `schema.sql` 和 `data.sql`
2. [ ] **campus-web API 对接** - 完成核心页面的 API 调用替换
3. [ ] **campus-web-admin API 对接** - 完成管理页面的 API 调用
4. [ ] **小程序登录对接** - 实现微信登录 + JWT 认证

### 🟡 P1 - 重要任务 (下周)
5. [ ] **小程序高德地图集成** - 地图模式找学生
6. [ ] **小程序订单流程** - 新增订单相关页面
7. [ ] **密码加密升级** - MD5 → BCrypt
8. [ ] **权限拦截完善** - 基于角色的 API 访问控制

### 🟢 P2 - 优化任务 (后续)
9. [ ] **短信服务接入** - 替换 Mock 验证码
10. [ ] **Dashboard 数据可视化** - ECharts 图表
11. [ ] **错题本 OCR** - 小程序拍照识别错题
12. [ ] **消息通知** - 订单状态变更推送

---

## 八、测试账号

| 角色 | 账号 | 密码 | 说明 |
| :--- | :--- | :--- | :--- |
| 管理员 | `admin` | `123456` | Web 管理后台 |
| 教员 | `13800000002` | `123456` | 已通过认证 |
| 家长 | `13800000001` | `123456` | 已添加学生 |

---

## 九、启动命令速查

```bash
# 后端 (端口 8080)
cd campus-backend && mvn spring-boot:run

# Web 用户端 (端口 5173)
cd campus-web && npm run dev

# Web 管理后台 (端口 3001)
cd campus-web-admin && npm run dev

# 小程序
微信开发者工具 → 导入 campus-user-app 目录
```
