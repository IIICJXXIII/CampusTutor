# CampusTutor 项目待办清单

> 更新日期: 2026-01-07 
> 本清单基于全端代码详细评估生成，包含后端、Web前端、管理后台、小程序

---

## 一、当前完成度总览

| 业务节点 | 后端 | campus-web | campus-web-admin | 小程序 | 状态 |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **用户认证 (JWT)** | ✅ | ✅ 已对接 | ⚠️ 演示模式 | ✅ 已对接 | 🟢 基本完成 |
| **微信登录** | ❌ 缺接口 | N/A | N/A | ⚠️ 前端就绪 | 🔴 **阻塞** |
| **教师认证 (OCR)** | ✅ 含百度OCR | ✅ 已对接 | ⚠️ Mock数据 | ✅ 已对接 | 🟢 待真实测试 |
| **家长发布需求** | ✅ CRUD+LBS | ✅ 已对接 | ⚠️ Mock数据 | ✅ 已对接 | 🟢 就绪 |
| **智能匹配** | ✅ 多维度算法 | ✅ 已对接 | ⚠️ Mock数据 | ✅ 已对接 | 🟢 就绪 |
| **订单与签约** | ✅ 全状态流转 | ✅ 已对接 | ⚠️ Mock数据 | ✅ 已对接 | 🟢 就绪 |
| **课时打卡** | ✅ 打卡+确认 | ✅ 已对接 | ⚠️ Mock数据 | ✅ 已对接 | 🟢 就绪 |
| **钱包与结算** | ✅ 余额+提现 | ✅ 已对接 | ⚠️ Mock数据 | ✅ 已对接 | 🟢 就绪 |
| **地图服务** | ✅ 高德API | ✅ 已集成 | N/A | ⚠️ 待完整集成 | 🟡 进行中 |
| **LLM智能服务** | ✅ DeepSeek | ✅ API封装 | N/A | ❌ 未对接 | 🟡 进行中 |

**图例**: ✅ 已完成 | ⚠️ 部分完成 | ❌ 未实现 | 🟢 就绪 | 🟡 进行中 | 🔴 阻塞

---

## 二、后端模块评估 (campus-backend)

### ✅ 已完成模块 (13个)
| 模块 | 说明 | API数量 |
| :--- | :--- | :---: |
| `auth` | JWT 登录/注册/验证码(Mock) | 3 |
| `user` | 用户 CRUD，角色区分 | 4 |
| `tutor` | 教员档案、认证提交、排课配置 | 6 |
| `parent` | 家长学生管理 CRUD | 5 |
| `demand` | 需求发布、上下架、LBS 搜索 | 9 |
| `match` | 多维度搜索、匹配分数计算 | 2 |
| `order` | 订单全流程（创建→支付→托管→完成） | 8 |
| `teaching` | 课时打卡、家长确认/申诉 | 7 |
| `wallet` | 余额查询、交易流水、提现 | 4 |
| `file` | 本地文件上传 | 2 |
| `ocr` | 百度OCR集成 (学生证/身份证/通用) | 4 |
| `map` | 高德地图 (逆地址解析/路径规划/距离计算) | 4 |
| `llm` | DeepSeek LLM (需求解析/对话/问答) | 3 |

### ✅ 单元测试全部通过 (40/40)
- CampusApplicationTests ✅
- ExternalServiceConnectionTest (6) ✅
- AuthServiceTest (5) ✅
- SysUserServiceTest (5) ✅
- WalletServiceTest (5) ✅
- DemandServiceTest (4) ✅
- MatchServiceTest (6) ✅
- AmapServiceTest (5) ✅
- LlmClientServiceTest (3) ✅

### ⚠️ 待完善/修复
| 问题 | 描述 | 优先级 |
| :--- | :--- | :---: |
| **微信登录接口缺失** | 后端无 `/api/auth/wx-login` 接口，小程序无法使用微信一键登录 | 🔴 **高** |
| 短信服务 Mock | `AuthServiceImpl` 中验证码为固定值，需接入真实短信 API | 🟡 中 |
| 密码加密弱 | 当前使用 MD5，建议升级 BCrypt | 🟡 中 |
| 权限控制不足 | 缺少基于角色的接口访问控制 | 🟡 中 |
| 申诉通知缺失 | `TeachingRecordServiceImpl` 中申诉后未通知管理员 | 🟢 低 |
| **Admin API 缺失** | 后端无 `/admin/*` 管理接口，导致管理后台无法对接 | 🔴 高 |

### 🔴 后端需新增：微信登录接口
**问题**: 小程序调用 `/api/auth/wx-login` 返回 404，后端未实现该接口。

**需要的修改**:
1. **新增配置** (`application.properties`):
```properties
# 微信小程序配置
wechat.miniapp.appId=YOUR_APPID
wechat.miniapp.secret=YOUR_SECRET
```

2. **新增DTO** (`WxLoginRequest.java`):
```java
public class WxLoginRequest {
    private String code;  // wx.login 返回的 code
}
```

3. **新增接口** (`AuthController.java`):
```java
@PostMapping("/wx-login")
public Result<LoginResponse> wxLogin(@RequestBody WxLoginRequest request)
```

4. **实现逻辑** (`AuthServiceImpl.java`):
   - 根据 code 调用微信 `jscode2session` 获取 openid
   - 若 openid 已绑定用户 → 生成 JWT → 返回 `LoginResponse`
   - 若未绑定 → 返回 `{ needBind: true, openid: 'xxx' }`

5. **响应格式**:
   - 成功: `{ code:200, msg:'登录成功', data: LoginResponse }`
   - 需绑定: `{ code:200, msg:'需要绑定', data:{ needBind:true, openid:'xxx' } }`

### ⚠️ 数据库表已定义但后端未实现
| 表名 | 说明 | 优先级 |
| :--- | :--- | :---: |
| `sys_chat_msg` | IM聊天记录表 | 🟢 低 |
| `sys_comment` | 订单评价表 | 🟡 中 |
| `community_post/reply` | 社区帖子/评论表 | 🟢 低 |
| `mistake_notebook` | 在线错题本 | 🟡 中 |
| `student_report` | 学生阶段报告表 | 🟢 低 |
| `insurance_policy` | 保险单记录表 | 🟢 低 |

---

## 三、Web 用户端评估 (campus-web)

### ✅ 基础设施 (已完成)
- [x] Vite + Vue 3.4.21 配置
- [x] Element Plus 2.5.6 + SCSS 主题
- [x] Pinia 状态管理 (User, Tutor, Order, Demand Stores)
- [x] Axios 请求封装 + API 代理
- [x] 路由配置 + 权限守卫 (Token验证、白名单)

### ✅ API模块封装 (12个)
| 文件 | 封装接口 | 状态 |
| :--- | :--- | :---: |
| `auth.js` | login, register, sendCode, getUserInfo | ✅ |
| `tutor.js` | submitCertification, getProfile, updateProfile, saveSchedule, getSchedule, getPublicProfile | ✅ |
| `demand.js` | publish, update, online, offline, delete, getMyList, getDetail, getList | ✅ |
| `match.js` | searchTutors, getNearbyDemands | ✅ |
| `order.js` | create, pay, cancel, confirmStart, complete, getDetail, getParentOrders | ✅ |
| `teaching.js` | checkIn, checkOut, parentConfirm, parentDispute, getMyRecords, getOrderRecords, getDetail | ✅ |
| `wallet.js` | getWallet, getTransactions, withdraw, getWithdrawals | ✅ |
| `file.js` | uploadFile | ✅ |
| `ocr.js` | recognizeStudentCard, recognizeIdCardFront, recognizeIdCardBack, recognizeGeneral | ✅ |
| `map.js` | reverseGeocode, geocode, getDirection, getDistance | ✅ |
| `llm.js` | parseDemand, chat, quickAnswer | ✅ |

### ✅ 已完成页面 (18个)

#### 公共页面
| 页面 | 文件 | API对接 | 表单验证 | 状态 |
| :--- | :--- | :---: | :---: | :---: |
| 登录 | `Login.vue` | ✅ 真实API | ✅ 手机号/密码 | ✅ |
| 注册 | `Register.vue` | ✅ 真实API | ✅ 验证码校验 | ✅ |

#### 家长端页面
| 页面 | 文件 | API对接 | 表单验证 | 状态 |
| :--- | :--- | :---: | :---: | :---: |
| 发布需求 | `DemandForm.vue` | ✅ demand.publish | ✅ 三步验证 | ✅ |
| 教师列表 | `TutorList.vue` | ✅ match.searchTutors | ✅ 筛选项 | ✅ |
| 预约签约 | `Booking.vue` | ✅ order.create | ✅ 日期/协议 | ✅ |
| 支付页面 | `Payment.vue` | ✅ order.pay | ✅ 支付方式 | ✅ |
| 错题本 | `WrongBook.vue` | ⚠️ Mock数据 | N/A | ⚠️ |

#### 教师端页面
| 页面 | 文件 | API对接 | 表单验证 | 状态 |
| :--- | :--- | :---: | :---: | :---: |
| 资质认证 | `TeacherAuth.vue` | ✅ tutor+ocr+file | ✅ 步骤验证 | ✅ |
| 我的简历 | `MyResume.vue` | ✅ tutor.profile | ✅ 必填项 | ✅ |
| 找学生(地图) | `FindStudents.vue` | ✅ demand.nearby | ✅ 高德地图 | ✅ |
| 教师列表 | `TeacherList.vue` | ✅ match.searchTutors | ✅ 排序筛选 | ✅ |
| 教师详情 | `TeacherProfile.vue` | ✅ tutor.getPublicProfile | N/A | ✅ |
| 学生详情 | `StudentDetail.vue` | ✅ demand.getDetail | N/A | ✅ |
| 学生列表 | `StudentList.vue` | ✅ demand.getList | N/A | ✅ |

#### 公共模块
| 页面 | 文件 | API对接 | 状态 |
| :--- | :--- | :---: | :---: |
| 个人中心 | `Mine.vue` | ✅ Pinia Store | ✅ |
| 订单列表 | `OrderList.vue` | ✅ order API | ✅ |
| 钱包 | `Wallet.vue` | ✅ wallet API | ✅ |
| 提现 | `Withdraw.vue` | ✅ wallet.withdraw | ✅ |
| 课时记录 | `ClassRecord.vue` | ✅ teaching API | ✅ |

### ⚠️ 待修复问题
| 问题 | 位置 | 说明 | 优先级 |
| :--- | :--- | :--- | :---: |
| API路径重复 | `wallet.js` | `/api/wallet` 应改为 `/wallet` (request已有baseURL) | 🔴 高 |
| API路径重复 | `llm.js` | `/api/llm/*` 应改为 `/llm/*` | 🔴 高 |
| API未导出 | `api/index.js` | wallet, llm, map 未统一导出 | 🟡 中 |
| 错题本Mock | `WrongBook.vue` | 使用 Mock 数据，后端暂无错题本API | 🟡 中 |
| 设置页面404 | 路由 | `/settings` 路由未配置 | 🟢 低 |
| 地图Key硬编码 | `FindStudents.vue` | 高德地图Key应改为环境变量 | 🟢 低 |

---

## 四、Web 管理后台评估 (campus-web-admin)

### ✅ 基础设施 (已完成)
- [x] Vite + Vue 3.4.21 配置
- [x] Element Plus 侧边栏布局 (AdminLayout)
- [x] Axios 请求封装 + API 代理 (端口 3001)
- [x] 路由配置 + Token守卫

### ✅ API模块封装 (11个)
| 模块 | 封装方法 | 后端对接 |
| :--- | :--- | :---: |
| `authApi` | login, logout, getProfile | ❌ 缺后端Admin接口 |
| `userApi` | getList, getById, update, updateStatus, delete | ❌ 缺后端Admin接口 |
| `tutorApi` | getList, getById, update, getPendingList, approve, reject | ❌ 缺后端Admin接口 |
| `parentApi` | getList, getById, update | ❌ 缺后端Admin接口 |
| `demandApi` | getList, getById, update, updateStatus, delete | ❌ 缺后端Admin接口 |
| `orderApi` | getList, getById, updateStatus, releaseEscrow, refund | ❌ 缺后端Admin接口 |
| `lessonApi` | getList, getById, confirm, reject | ❌ 缺后端Admin接口 |
| `matchApi` | getList, getById | ❌ 缺后端Admin接口 |
| `walletApi` | getList, getById, getTransactions, adjust | ❌ 缺后端Admin接口 |
| `statsApi` | getDashboard, getUserStats, getOrderStats, getRevenueStats | ❌ 缺后端Admin接口 |
| `settingsApi` | getSettings, updateSettings | ❌ 缺后端Admin接口 |

### ✅ 已完成页面 (12个) - UI已完成，API未对接
| 页面 | 路由 | UI完成 | API对接 | 状态 |
| :--- | :--- | :---: | :---: | :---: |
| 登录 | `/login` | ✅ | ⚠️ 仅演示模式 | ⚠️ |
| 仪表盘 | `/dashboard` | ✅ | ❌ 硬编码数据 | ⚠️ |
| 用户管理 | `/users` | ✅ | ❌ Mock数据 | ⚠️ |
| 教员列表 | `/tutors` | ✅ | ❌ Mock数据 | ⚠️ |
| 教员审核 | `/tutor-audit` | ✅ | ❌ Mock数据 | ⚠️ |
| 家长列表 | `/parents` | ✅ | ❌ Mock数据 | ⚠️ |
| 需求管理 | `/demands` | ✅ | ❌ Mock数据 | ⚠️ |
| 订单管理 | `/orders` | ✅ | ❌ Mock数据 | ⚠️ |
| 课时管理 | `/lessons` | ✅ | ❌ Mock数据 | ⚠️ |
| 匹配记录 | `/matches` | ✅ | ❌ Mock数据 | ⚠️ |
| 钱包管理 | `/wallets` | ✅ | ❌ Mock数据 | ⚠️ |
| 系统设置 | `/settings` | ✅ | ❌ Mock数据 | ⚠️ |

### 🔴 阻塞问题：后端缺少 Admin API
**问题描述**: 当前后端没有 `/admin/*` 管理接口，导致管理后台所有页面无法对接真实API。

**需要新建的后端接口**:
```
POST   /api/admin/auth/login              # 管理员登录
GET    /api/admin/stats/dashboard         # 仪表盘统计

GET    /api/admin/users                   # 用户列表（分页）
PUT    /api/admin/users/:id               # 更新用户
PUT    /api/admin/users/:id/status        # 禁用/启用用户

GET    /api/admin/tutors                  # 教师列表
GET    /api/admin/tutors/pending          # 待审核列表
POST   /api/admin/tutors/:id/approve      # 通过审核
POST   /api/admin/tutors/:id/reject       # 拒绝审核

GET    /api/admin/parents                 # 家长列表
GET    /api/admin/demands                 # 需求列表
GET    /api/admin/orders                  # 订单列表
GET    /api/admin/lessons                 # 课时列表
GET    /api/admin/wallets                 # 钱包列表
POST   /api/admin/wallets/:id/adjust      # 余额调整
```

---

## 五、微信小程序评估 (campus-user-app)

### ✅ 基础设施 (已完成)
- [x] 原生小程序项目结构
- [x] API 配置完整 (`apiConfig.js`) - 包含全部后端接口
- [x] 请求封装 (`request.js`) - Token自动携带、401处理
- [x] 本地存储工具 (`storageUtil.js`)
- [x] 表单验证工具 (`validateUtil.js`)
- [x] 日期格式化工具 (`dateUtil.js`)

### ✅ API配置模块 (10个)
| 模块 | 配置接口 | 状态 |
| :--- | :--- | :---: |
| 认证模块 | login, register, sendCode | ✅ |
| 用户模块 | info, byId | ✅ |
| 文件上传 | upload | ✅ |
| 教员模块 | profile, certification, schedule, detail | ✅ |
| 家长模块 | student, myStudents | ✅ |
| 需求模块 | publish, list, my, nearby, detail | ✅ |
| 匹配模块 | search | ✅ |
| 订单模块 | create, listParent, listTutor, pay, detail | ✅ |
| 钱包模块 | info, transactions, withdraw, withdrawals | ✅ |
| 课时打卡 | checkIn, checkOut, confirm, dispute, myRecords, orderRecords, detail | ✅ |

### ✅ 已完成页面 (25个)

#### 📁 pages/common/ - 通用页面 (4个)
| 页面 | 功能 | API对接 | 状态 |
| :--- | :--- | :---: | :---: |
| `index/index` | 统一首页（根据角色显示） | ✅ wallet, tutor, demand, order | ✅ |
| `login/login` | 登录页（密码/验证码双模式） | ✅ auth.login, auth.sendCode | ✅ |
| `register/register` | 注册页（教员/家长角色选择） | ✅ auth.register, auth.sendCode | ✅ |
| `personalCenter/personalCenter` | 个人中心（角色菜单） | ✅ wallet.info | ✅ |

#### 📁 pages/teacher/ - 教师端页面 (9个)
| 页面 | 功能 | API对接 | 状态 |
| :--- | :--- | :---: | :---: |
| `certification/step1-base` | 认证第一步：基本信息 | ✅ file.upload | ✅ |
| `certification/step2-ability` | 认证第二步：资质证明 | ✅ file.upload, tutor.certification | ✅ |
| `certification/step3-result` | 认证结果页 | ✅ 静态页面 | ✅ |
| `mapFindStudent/mapFindStudent` | 地图找学生（LBS） | ✅ demand.nearby | ✅ |
| `incomeDetail/incomeDetail` | 收入明细与提现 | ✅ wallet全部接口 | ✅ |
| `lessonList/lessonList` | 课时列表 | ✅ teaching.myRecords | ✅ |
| `lessonCheckIn/lessonCheckIn` | 课时打卡（上/下课） | ✅ teaching.checkIn/Out, file.upload | ✅ |
| `orderList/orderList` | 教师订单列表 | ✅ order.listTutor | ✅ |
| `orderDetail/orderDetail` | 教师订单详情 | ✅ order.detail | ✅ |

#### 📁 pages/parent/ - 家长端页面 (12个)
| 页面 | 功能 | API对接 | 状态 |
| :--- | :--- | :---: | :---: |
| `publishDemand/step1-student` | 发布需求：选择学生 | ✅ parent.myStudents | ✅ |
| `publishDemand/step2-content` | 发布需求：填写内容 | ✅ demand.publish | ✅ |
| `publishDemand/step2-teaching` | 发布需求：教学配置 | ✅ UI完成 | ✅ |
| `publishDemand/step3-preference` | 发布需求：偏好设置 | ✅ UI完成 | ✅ |
| `matchResult/matchResult` | 匹配结果列表 | ✅ match.search | ✅ |
| `teacherDetail/teacherDetail` | 教师详情页 | ✅ tutor.detail | ✅ |
| `order/confirm/confirm` | 订单确认与支付 | ✅ order.create, order.pay | ✅ |
| `order/list/list` | 家长订单列表 | ✅ order.listParent, order.pay | ✅ |
| `order/detail/detail` | 家长订单详情 | ✅ order.detail, order.pay | ✅ |
| `demand/myList/myList` | 我的需求列表 | ✅ demand.my | ✅ |
| `lessonList/lessonList` | 家长课时列表 | ✅ teaching + confirm/dispute | ✅ |
| `lessonDetail/lessonDetail` | 家长课时详情 | ✅ teaching.detail | ✅ |

### ✅ 组件完成情况
| 组件 | 功能 | 状态 |
| :--- | :--- | :---: |
| `teacherCard` | 教师卡片（头像、学校、价格、评分、标签） | ✅ |

### ⚠️ 待修复问题
| 问题 | 位置 | 说明 | 优先级 |
| :--- | :--- | :--- | :---: |
| **微信登录404** | 登录页 | 后端未实现 `/api/auth/wx-login`，前端已就绪 | 🔴 **高** |
| **高德地图SDK** | `mapFindStudent` | 地图UI完成，需真实集成SDK、标记点交互 | 🟡 中 |
| 页面冗余 | `teacher/index`, `parent/index` | 未在app.json注册，与common/index重复 | 🟢 低 |
| 认证状态查询 | `step3-result` | 缺少调用认证状态API | 🟢 低 |
| 接单功能未实现 | `mapFindStudent` | "立即接单"按钮仅提示"开发中" | 🟡 中 |

### ❌ 待开发模块
| 模块 | 需要页面 | 优先级 | 说明 |
| :--- | :--- | :---: | :--- |
| 课程表 | 教师课表、家长课表 | 🟡 P1 | 可视化排课展示 |
| 错题本 | 错题列表、拍照识题、题目详情 | 🟢 P2 | OCR识别功能 |
| 消息通知 | 消息列表、消息详情 | 🟢 P2 | 订单状态推送 |

---

## 六、完成度统计

### 各端完成度
| 项目 | 完成度 | 说明 |
| :--- | :---: | :--- |
| **campus-backend** | **90%** | 13模块完成，缺Admin接口、微信登录接口 |
| **campus-web** | **90%** | 18页面完成，API基本对接，少量路径问题 |
| **campus-web-admin** | **40%** | 12页面UI完成，全部使用Mock数据，缺后端Admin接口 |
| **campus-user-app** | **88%** | 25页面完成，API已对接，微信登录待后端支持 |

### 功能模块完成度
| 功能模块 | 后端 | Web端 | 管理后台 | 小程序 |
| :--- | :---: | :---: | :---: | :---: |
| 账号密码登录 | 100% | 100% | 30% | 100% |
| 微信一键登录 | **0%** | N/A | N/A | **前端就绪** |
| 教师认证 | 100% | 100% | 30% | 100% |
| 需求发布 | 100% | 100% | 30% | 100% |
| 智能匹配 | 100% | 100% | 30% | 100% |
| 订单管理 | 100% | 100% | 30% | 100% |
| 课时打卡 | 100% | 100% | 30% | 100% |
| 钱包结算 | 100% | 100% | 30% | 100% |
| 地图服务 | 100% | 100% | N/A | 80% |
| LLM智能 | 100% | 100% | N/A | 0% |

---

## 七、优先级任务清单

### 🔴 P0 - 阻塞性任务 (本周内)
1. [x] ~~执行数据库同步~~ - 已完成
2. [x] ~~campus-web API 对接~~ - 已完成 (90%)
3. [x] ~~小程序订单流程~~ - 已完成 (订单列表+详情+签约+支付)
4. [x] ~~小程序课时打卡与确认~~ - 已完成
5. [ ] **🔴 后端新增微信登录接口** - `/api/auth/wx-login` (小程序依赖)
6. [ ] **后端新增 Admin API** - 管理后台依赖此接口
7. [ ] **campus-web-admin 对接真实API** - 当前全部Mock数据

### 🟡 P1 - 重要任务 (下周)
8. [ ] **高德地图完整集成** - 小程序地图标记点交互
9. [ ] **修复 campus-web API路径问题** - wallet.js, llm.js 路径重复
10. [ ] **小程序接单功能** - mapFindStudent "立即接单"
11. [ ] **课程表页面** - 教师/家长可视化排课
12. [ ] **后端错题本模块** - 实现 mistake_notebook 相关API
13. [ ] **后端评价模块** - 实现 sys_comment 相关API

### 🟢 P2 - 优化任务 (后续)
14. [ ] **密码加密升级** - MD5 → BCrypt
15. [ ] **权限拦截完善** - 基于角色的 API 访问控制
16. [ ] **短信服务接入** - 替换 Mock 验证码
17. [ ] **Dashboard 图表** - 对接真实统计数据
18. [ ] **小程序LLM集成** - AI智能需求解析
19. [ ] **消息通知推送** - 订单状态变更通知
20. [ ] **错题本功能** - 小程序拍照识题

---

## 八、后端 API 接口清单

### 认证模块 `/api/auth`
| 方法 | 路径 | 功能 | 状态 |
| :--- | :--- | :--- | :---: |
| POST | `/login` | 用户登录 (账号密码) | ✅ |
| POST | `/register` | 用户注册 | ✅ |
| POST | `/send-code` | 发送验证码 | ✅ |
| POST | `/wx-login` | **微信小程序登录** | ❌ **待开发** |

### 用户模块 `/api/user`
| 方法 | 路径 | 功能 |
| :--- | :--- | :--- |
| GET | `/{id}` | 根据ID获取用户 |
| GET | `/username/{username}` | 根据用户名获取 |
| PUT | `/` | 更新用户信息 |
| PUT | `/{id}/status` | 更新用户状态 |

### 教员模块 `/api/tutor`
| 方法 | 路径 | 功能 |
| :--- | :--- | :--- |
| POST | `/certification` | 提交认证 |
| GET | `/profile` | 获取档案 |
| PUT | `/profile` | 更新档案 |
| POST | `/schedule` | 保存排课 |
| GET | `/schedule` | 获取排课 |
| GET | `/{id}` | 公开档案 |

### 家长模块 `/api/parent`
| 方法 | 路径 | 功能 |
| :--- | :--- | :--- |
| POST | `/student` | 添加学生 |
| PUT | `/student/{id}` | 更新学生 |
| DELETE | `/student/{id}` | 删除学生 |
| GET | `/students` | 学生列表 |
| GET | `/student/{id}` | 学生详情 |

### 需求模块 `/api/demand`
| 方法 | 路径 | 功能 |
| :--- | :--- | :--- |
| POST | `/` | 发布需求 |
| PUT | `/{id}` | 更新需求 |
| POST | `/{id}/online` | 上架 |
| POST | `/{id}/offline` | 下架 |
| DELETE | `/{id}` | 删除 |
| GET | `/my` | 我的需求 |
| GET | `/{id}` | 需求详情 |
| GET | `/list` | 公开列表 |
| GET | `/nearby` | 附近需求 |

### 匹配模块 `/api/match`
| 方法 | 路径 | 功能 |
| :--- | :--- | :--- |
| POST | `/tutors` | 高级搜索 |
| GET | `/tutors` | 简化搜索 |

### 订单模块 `/api/order`
| 方法 | 路径 | 功能 |
| :--- | :--- | :--- |
| POST | `/` | 创建订单 |
| POST | `/{id}/pay` | 支付订单 |
| POST | `/{id}/cancel` | 取消订单 |
| POST | `/{id}/confirm-start` | 确认开课 |
| POST | `/{id}/complete` | 完成订单 |
| GET | `/{id}` | 订单详情 |
| GET | `/parent` | 家长订单 |
| GET | `/tutor` | 教员订单 |

### 课时模块 `/api/teaching`
| 方法 | 路径 | 功能 |
| :--- | :--- | :--- |
| POST | `/check-in` | 上课打卡 |
| POST | `/{id}/check-out` | 下课打卡 |
| POST | `/{id}/confirm` | 家长确认 |
| POST | `/{id}/dispute` | 家长申诉 |
| GET | `/my` | 我的记录 |
| GET | `/order/{orderId}` | 订单记录 |
| GET | `/{id}` | 记录详情 |

### 钱包模块 `/api/wallet`
| 方法 | 路径 | 功能 |
| :--- | :--- | :--- |
| GET | `/` | 钱包信息 |
| GET | `/transactions` | 交易流水 |
| POST | `/withdraw` | 申请提现 |
| GET | `/withdrawals` | 提现记录 |

### 地图模块 `/api/map`
| 方法 | 路径 | 功能 |
| :--- | :--- | :--- |
| GET | `/geocoder/reverse` | 逆地址解析 |
| GET | `/geocoder` | 地址解析 |
| POST | `/direction` | 路径规划 |
| GET | `/distance` | 距离计算 |

### LLM模块 `/api/llm`
| 方法 | 路径 | 功能 |
| :--- | :--- | :--- |
| POST | `/demand/parse` | 需求解析 |
| POST | `/chat` | AI对话 |
| GET | `/quick-answer` | 快速问答 |

### 文件模块 `/api/file`
| 方法 | 路径 | 功能 |
| :--- | :--- | :--- |
| POST | `/upload` | 上传文件 |
| DELETE | `/` | 删除文件 |

### OCR模块 `/api/ocr`
| 方法 | 路径 | 功能 |
| :--- | :--- | :--- |
| POST | `/student-card` | 识别学生证 |
| POST | `/id-card/front` | 身份证正面 |
| POST | `/id-card/back` | 身份证背面 |
| POST | `/general` | 通用识别 |

---

## 九、测试账号

| 角色 | 账号 | 密码 | 说明 |
| :--- | :--- | :--- | :--- |
| 管理员 | `admin` | `123456` | Web 管理后台 |
| 教员 | `13800000002` | `123456` | 已通过认证 |
| 家长 | `13800000001` | `123456` | 已添加学生 |

---

## 十、启动命令速查

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

---

## 十一、数据库信息

### 数据库表 (17张)
| 分类 | 表名 | 说明 |
| :--- | :--- | :--- |
| 用户权限 | `sys_user` | 系统用户表 |
| | `sys_wallet` | 用户钱包表 |
| | `sys_transaction_flow` | 资金流水记录表 |
| | `sys_withdrawal` | 提现申请表 |
| 教员中心 | `tutor_profile` | 教员档案认证表 |
| | `tutor_schedule_config` | 教员排课配置表 |
| 家长需求 | `parent_student` | 学生档案表 |
| | `demand_post` | 需求发布表 |
| | `user_action_log` | 用户行为轨迹表 |
| 交易订单 | `course_order` | 课程订单表 |
| | `insurance_policy` | 保险单记录表 |
| 教学过程 | `teaching_record` | 课时打卡记录表 |
| | `student_report` | 学生阶段报告表 |
| | `mistake_notebook` | 在线错题本 |
| 系统交互 | `sys_chat_msg` | IM聊天记录表 |
| | `sys_comment` | 订单评价表 |
| | `sys_dict` | 数据字典表 |
| 社区功能 | `community_post` | 社区帖子表 |
| | `community_reply` | 社区评论表 |

### 数据库同步命令
```bash
# 1. 导入表结构
mysql -u root -p < campus-backend/sql/schema.sql

# 2. 导入测试数据
mysql -u root -p campus_tutor_db < campus-backend/sql/data.sql
```
