# 📖 CampusTutor 后端 API 接口文档

> **版本**: v1.0.0  
> **更新日期**: 2026-01-09  
> **Base URL**: `http://localhost:8080`  
> **在线文档**: `http://localhost:8080/doc.html` (Knife4j)

---

## 🔐 认证说明

除白名单接口外，所有接口需要在请求头中携带 JWT Token：

```http
Authorization: Bearer <your_jwt_token>
```

### 白名单路径 (无需认证)

- `POST /api/auth/**` - 登录注册相关
- `POST /api/admin/auth/**` - 管理员登录
- `GET /api/file/**` - 文件访问
- `GET /api/match/tutors` - 教员搜索 (公开)
- `GET /api/demand/list` - 需求列表 (公开)
- `GET /api/demand/nearby` - 附近需求 (公开)
- `GET /api/tutor/public/**` - 教员公开信息

---

## 📦 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {},
  "timestamp": 1736380800000
}
```

### 状态码说明

| code | 说明 |
|------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未授权/Token失效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 5001-5006 | 用户相关错误 |
| 5101-5103 | 教员认证错误 |
| 5201-5203 | 订单/钱包错误 |

---

## 1️⃣ 认证模块 (Auth)

### 1.1 用户登录

**POST** `/api/auth/login`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | string | ✅ | 手机号或用户名 |
| password | string | ❌ | 密码 (密码登录时必填) |
| code | string | ❌ | 验证码 (验证码登录时必填) |
| loginType | string | ❌ | 登录方式: `password`(默认) / `code` |

**请求示例**:
```json
{
  "account": "13800138000",
  "password": "123456",
  "loginType": "password"
}
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "13800138000",
    "nickname": "张三",
    "avatar": "http://localhost:8080/uploads/avatar/xxx.jpg",
    "role": 2
  }
}
```

---

### 1.2 用户注册

**POST** `/api/auth/register`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | ✅ | 手机号 (正则: `^1[3-9]\d{9}$`) |
| password | string | ✅ | 密码 |
| code | string | ✅ | 短信验证码 |
| nickname | string | ❌ | 昵称 |
| role | integer | ✅ | 角色: `1`-教员, `2`-家长 |

**请求示例**:
```json
{
  "phone": "13800138000",
  "password": "123456",
  "code": "123456",
  "nickname": "小明老师",
  "role": 1
}
```

---

### 1.3 发送验证码

**POST** `/api/auth/send-code`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | ✅ | 手机号 |

**响应**: 验证码将打印在服务器日志中 (Mock模式)

---

## 2️⃣ 用户模块 (User)

### 2.1 获取当前用户信息

**GET** `/api/user/current`

🔒 需要认证

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "13800138000",
    "nickname": "张三",
    "avatarUrl": "http://...",
    "role": 2,
    "gender": 1,
    "status": 1,
    "createTime": "2026-01-01 10:00:00"
  }
}
```

---

### 2.2 根据ID获取用户

**GET** `/api/user/{id}`

🔒 需要认证

---

### 2.3 更新用户信息

**PUT** `/api/user`

🔒 需要认证

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 用户ID |
| nickname | string | 昵称 |
| avatarUrl | string | 头像URL |
| gender | integer | 性别: 0未知 1男 2女 |

---

## 3️⃣ 教员模块 (Tutor)

### 3.1 提交教员认证

**POST** `/api/tutor/certification`

🔒 需要认证 (role=1)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| realName | string | ✅ | 真实姓名 |
| idCard | string | ✅ | 身份证号 |
| idCardFrontUrl | string | ✅ | 身份证正面照URL |
| idCardBackUrl | string | ✅ | 身份证背面照URL |
| universityName | string | ✅ | 学校名称 |
| major | string | ✅ | 专业 |
| education | integer | ✅ | 学历: 1本科在读 2本科毕业 3硕士在读 4硕士毕业 5博士 |
| enrollYear | integer | ✅ | 入学年份 |
| studentCardUrl | string | ✅ | 学生证照片URL |
| certificateUrls | array | ❌ | 资质证书URLs |
| teachSubjects | array | ✅ | 可授科目 |
| teachGrades | array | ✅ | 可授年级 |
| teachStyle | string | ❌ | 教学风格 |
| introduction | string | ❌ | 自我介绍 |
| expectPrice | decimal | ✅ | 期望时薪 |

---

### 3.2 获取当前教员档案

**GET** `/api/tutor/profile`

🔒 需要认证 (role=1)

---

### 3.3 更新教员档案

**PUT** `/api/tutor/profile`

🔒 需要认证 (role=1)

---

### 3.4 保存时间配置

**POST** `/api/tutor/schedule`

🔒 需要认证 (role=1)

---

### 3.5 获取时间配置

**GET** `/api/tutor/schedule`

🔒 需要认证 (role=1)

---

### 3.6 获取教员公开档案

**GET** `/api/tutor/public/{id}`

🔓 无需认证

---

## 4️⃣ 家长模块 (Parent)

### 4.1 添加学生

**POST** `/api/parent/student`

🔒 需要认证 (role=2)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | ✅ | 学生姓名 |
| gender | integer | ❌ | 性别 |
| grade | string | ✅ | 年级 |
| school | string | ❌ | 学校 |
| weakSubjects | array | ❌ | 薄弱科目 |
| description | string | ❌ | 学习情况描述 |

**响应**: 返回学生ID

---

### 4.2 更新学生信息

**PUT** `/api/parent/student`

🔒 需要认证 (role=2)

---

### 4.3 删除学生

**DELETE** `/api/parent/student/{id}`

🔒 需要认证 (role=2)

---

### 4.4 获取我的学生列表

**GET** `/api/parent/students`

🔒 需要认证 (role=2)

---

### 4.5 获取学生详情

**GET** `/api/parent/student/{id}`

🔒 需要认证 (role=2)

---

## 5️⃣ 需求模块 (Demand)

### 5.1 发布需求

**POST** `/api/demand/publish`

🔒 需要认证 (role=2)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | ✅ | 需求标题 |
| studentId | long | ❌ | 关联学生ID |
| subject | string | ✅ | 需求科目 |
| grade | string | ✅ | 需求年级 |
| expectPrice | decimal | ✅ | 期望价格(元/小时) |
| scheduleRequire | string | ❌ | 课时要求(JSON) |
| teachMode | integer | ✅ | 授课方式: 1上门 2网课 3均可 |
| longitude | decimal | ❌ | 经度 |
| latitude | decimal | ❌ | 纬度 |
| address | string | ❌ | 详细地址 |
| detail | string | ❌ | 需求详情 |

**响应**: 返回需求ID

---

### 5.2 更新需求

**PUT** `/api/demand/update`

🔒 需要认证 (role=2)

---

### 5.3 上架需求

**POST** `/api/demand/{id}/online`

🔒 需要认证 (role=2)

---

### 5.4 下架需求

**POST** `/api/demand/{id}/offline`

🔒 需要认证 (role=2)

---

### 5.5 删除需求

**DELETE** `/api/demand/{id}`

🔒 需要认证 (role=2)

---

### 5.6 我发布的需求列表

**GET** `/api/demand/my`

🔒 需要认证 (role=2)

---

### 5.7 需求详情

**GET** `/api/demand/{id}`

🔒 需要认证

---

### 5.8 分页查询需求列表 (公开)

**GET** `/api/demand/list`

🔓 无需认证

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| subject | string | ❌ | - | 科目筛选 |
| grade | string | ❌ | - | 年级筛选 |
| page | integer | ❌ | 1 | 页码 |
| size | integer | ❌ | 10 | 每页大小 |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

---

### 5.9 附近需求搜索 (LBS)

**GET** `/api/demand/nearby`

🔓 无需认证

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| longitude | double | ✅ | - | 经度 |
| latitude | double | ✅ | - | 纬度 |
| radius | double | ❌ | 10 | 搜索半径(km) |

---

## 6️⃣ 匹配模块 (Match)

### 6.1 搜索教员 (POST)

**POST** `/api/match/tutors`

🔓 无需认证

| 参数 | 类型 | 说明 |
|------|------|------|
| subject | string | 科目 |
| grade | string | 年级 |
| longitude | double | 经度 |
| latitude | double | 纬度 |
| radius | double | 搜索半径(km) |
| minPrice | decimal | 最低价格 |
| maxPrice | decimal | 最高价格 |
| education | integer | 学历要求 |
| teachMode | integer | 授课方式 |
| page | integer | 页码 |
| size | integer | 每页大小 |

---

### 6.2 搜索教员 (GET)

**GET** `/api/match/tutors`

🔓 无需认证

| 参数 | 类型 | 必填 | 默认值 |
|------|------|------|--------|
| subject | string | ❌ | - |
| grade | string | ❌ | - |
| longitude | double | ❌ | - |
| latitude | double | ❌ | - |
| radius | double | ❌ | - |
| page | integer | ❌ | 1 |
| size | integer | ❌ | 10 |

---

## 7️⃣ 订单模块 (Order)

### 7.1 创建订单

**POST** `/api/order/create`

🔒 需要认证 (role=2 家长)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| tutorProfileId | long | ✅ | 教员档案ID |
| studentId | long | ❌ | 学生ID |
| demandId | long | ❌ | 需求ID |
| subject | string | ✅ | 科目 |
| grade | string | ✅ | 年级 |
| teachMode | integer | ✅ | 授课方式 |
| unitPrice | decimal | ✅ | 课时单价 |
| totalHours | integer | ✅ | 总课时数 |
| remark | string | ❌ | 备注 |

**响应**: 返回订单ID

---

### 7.2 教师接单

**POST** `/api/order/accept`

🔒 需要认证 (role=1 教员)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| demandId | long | ✅ | 需求帖ID |
| totalHours | integer | ❌ | 总课时数 (默认10) |
| remark | string | ❌ | 备注 |

**响应**: 返回订单ID (状态为待确认 status=-1)

---

### 7.3 家长确认订单

**POST** `/api/order/{id}/confirm`

🔒 需要认证 (role=2 家长)

**说明**: 确认后订单状态变为待支付 (status=0)

---

### 7.4 支付订单

**POST** `/api/order/pay`

🔒 需要认证 (role=2 家长)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | long | ✅ | 订单ID |
| payType | integer | ✅ | 支付方式: 1钱包 2微信 3支付宝 |

---

### 7.5 取消订单

**POST** `/api/order/{id}/cancel`

🔒 需要认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | string | ❌ | 取消原因 |

---

### 7.6 教员确认开课

**POST** `/api/order/{id}/start`

🔒 需要认证 (role=1 教员)

---

### 7.7 完成订单

**POST** `/api/order/{id}/complete`

🔒 需要认证

---

### 7.8 订单详情

**GET** `/api/order/{id}`

🔒 需要认证

---

### 7.9 家长订单列表

**GET** `/api/order/parent/list`

🔒 需要认证 (role=2 家长)

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| status | integer | - | 订单状态筛选 |
| page | integer | 1 | 页码 |
| size | integer | 10 | 每页大小 |

---

### 7.10 教员订单列表

**GET** `/api/order/tutor/list`

🔒 需要认证 (role=1 教员)

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| status | integer | - | 订单状态筛选 |
| page | integer | 1 | 页码 |
| size | integer | 10 | 每页大小 |

### 订单状态说明

| status | 说明 |
|--------|------|
| -1 | 待确认 (教员接单后待家长确认) |
| 0 | 待支付 |
| 1 | 已支付待上课 |
| 2 | 进行中 |
| 3 | 已完成 |
| 4 | 已取消 |
| 5 | 退款中 |
| 6 | 已退款 |

---

## 8️⃣ 钱包模块 (Wallet)

### 8.1 获取钱包信息

**GET** `/api/wallet`

🔒 需要认证

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "balance": "1000.00",
    "frozenAmount": "200.00"
  }
}
```

---

### 8.2 获取交易流水

**GET** `/api/wallet/transactions`

🔒 需要认证

| 参数 | 类型 | 默认值 |
|------|------|--------|
| page | integer | 1 |
| size | integer | 10 |

---

### 8.3 发起提现申请

**POST** `/api/wallet/withdraw`

🔒 需要认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| amount | decimal | ✅ | 提现金额 |
| channel | string | ✅ | 提现渠道 |
| accountNo | string | ✅ | 收款账号 |
| accountName | string | ✅ | 收款人姓名 |

---

### 8.4 获取提现记录

**GET** `/api/wallet/withdrawals`

🔒 需要认证

| 参数 | 类型 | 默认值 |
|------|------|--------|
| page | integer | 1 |
| size | integer | 10 |

---

## 9️⃣ 课时打卡模块 (Teaching)

### 9.1 教师打卡上课

**POST** `/api/teaching/check-in`

🔒 需要认证 (role=1 教员)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | long | ✅ | 订单ID |
| longitude | double | ✅ | 打卡经度 |
| latitude | double | ✅ | 打卡纬度 |
| address | string | ❌ | 打卡地址 |
| photoUrl | string | ❌ | 打卡照片 |

**响应**: 返回课时记录ID

---

### 9.2 教师打卡下课

**POST** `/api/teaching/check-out/{recordId}`

🔒 需要认证 (role=1 教员)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| contentSummary | string | ❌ | 教学内容摘要 |
| homeworkAssigned | string | ❌ | 布置作业 |

---

### 9.3 家长确认课时

**POST** `/api/teaching/confirm/{recordId}`

🔒 需要认证 (role=2 家长)

---

### 9.4 家长申诉课时

**POST** `/api/teaching/dispute/{recordId}`

🔒 需要认证 (role=2 家长)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | string | ✅ | 申诉原因 |

---

### 9.5 获取我的课时记录

**GET** `/api/teaching/my-records`

🔒 需要认证

---

### 9.6 获取订单课时记录

**GET** `/api/teaching/records/{orderId}`

🔒 需要认证

---

### 9.7 获取课时记录详情

**GET** `/api/teaching/record/{recordId}`

🔒 需要认证

---

## 🔟 文件模块 (File)

### 10.1 上传文件

**POST** `/api/file/upload`

🔓 无需认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | ✅ | 文件 (multipart/form-data) |
| folder | string | ❌ | 目录: avatar/cert/common 等 (默认common) |

**限制**:
- 最大文件大小: 10MB
- 支持类型: image/jpeg, image/png, image/gif, image/webp, application/pdf

**响应示例**:
```json
{
  "code": 200,
  "msg": "上传成功",
  "data": "http://localhost:8080/uploads/common/20260109/xxx.jpg"
}
```

---

### 10.2 删除文件

**DELETE** `/api/file`

🔓 无需认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileUrl | string | ✅ | 文件URL |

---

## 1️⃣1️⃣ 聊天模块 (Chat)

### 11.1 获取会话列表

**GET** `/api/chat/sessions`

🔒 需要认证

---

### 11.2 获取聊天历史

**GET** `/api/chat/history/{targetUserId}`

🔒 需要认证

| 参数 | 类型 | 默认值 |
|------|------|--------|
| page | integer | 1 |
| size | integer | 50 |

---

### 11.3 标记消息已读

**POST** `/api/chat/read/{targetUserId}`

🔒 需要认证

---

### 11.4 获取未读消息数

**GET** `/api/chat/unread-count`

🔒 需要认证

---

### 11.5 获取聊天用户信息

**GET** `/api/chat/user-info/{userId}`

🔒 需要认证

---

## 1️⃣2️⃣ 智能服务模块 (LLM)

### 12.1 智能解析需求

**POST** `/api/llm/demand/parse`

🔒 需要认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| text | string | ✅ | 自然语言需求描述 |

**请求示例**:
```json
{
  "text": "我家孩子今年初二，数学成绩不太好，想找个耐心的老师一对一辅导"
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "grade": "初二",
    "subject": "数学",
    "teachStyle": "耐心",
    "teachMode": 1
  }
}
```

---

### 12.2 智能对话

**POST** `/api/llm/chat`

🔒 需要认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| messages | array | ✅ | 对话消息列表 |
| scene | string | ❌ | 场景标识 |

---

### 12.3 快速问答

**GET** `/api/llm/quick-answer`

🔒 需要认证

| 参数 | 类型 | 必填 |
|------|------|------|
| question | string | ✅ |

---

## 1️⃣3️⃣ OCR识别模块 (OCR)

### 13.1 识别学生证

**POST** `/api/ocr/student-card`

🔒 需要认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| imageUrl | string | ✅ | 学生证图片URL |

---

### 13.2 识别身份证正面

**POST** `/api/ocr/id-card/front`

🔒 需要认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| imageUrl | string | ✅ | 身份证正面图片URL |

---

### 13.3 识别身份证背面

**POST** `/api/ocr/id-card/back`

🔒 需要认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| imageUrl | string | ✅ | 身份证背面图片URL |

---

### 13.4 通用文字识别

**POST** `/api/ocr/general`

🔒 需要认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| imageUrl | string | ✅ | 图片URL |

---

## 1️⃣4️⃣ 地图服务模块 (Map)

### 14.1 逆地址解析

**GET** `/api/map/geocoder/reverse`

🔒 需要认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| latitude | double | ✅ | 纬度 |
| longitude | double | ✅ | 经度 |

---

### 14.2 地址解析

**GET** `/api/map/geocoder`

🔒 需要认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| address | string | ✅ | 地址字符串 |

---

### 14.3 路径规划

**POST** `/api/map/direction`

🔒 需要认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fromLatitude | double | ✅ | 起点纬度 |
| fromLongitude | double | ✅ | 起点经度 |
| toLatitude | double | ✅ | 终点纬度 |
| toLongitude | double | ✅ | 终点经度 |
| mode | string | ❌ | 出行方式: walking/driving/transit |

---

### 14.4 距离计算

**GET** `/api/map/distance`

🔒 需要认证

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| fromLatitude | double | ✅ | - | 起点纬度 |
| fromLongitude | double | ✅ | - | 起点经度 |
| toLatitude | double | ✅ | - | 终点纬度 |
| toLongitude | double | ✅ | - | 终点经度 |
| mode | string | ❌ | walking | 出行方式 |

---

## 1️⃣5️⃣ 管理后台模块 (Admin)

### 管理员认证

#### 15.0.1 管理员登录

**POST** `/api/admin/auth/login`

🔓 无需认证

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | string | ✅ | 用户名 |
| password | string | ✅ | 密码 |

---

#### 15.0.2 退出登录

**POST** `/api/admin/auth/logout`

🔒 需要认证 (role=0)

---

#### 15.0.3 获取管理员信息

**GET** `/api/admin/auth/profile`

🔒 需要认证 (role=0)

---

### 仪表盘

#### 15.1 获取仪表盘统计

**GET** `/api/admin/stats/dashboard`

🔒 需要认证 (role=0)

---

### 用户管理

#### 15.2 用户列表

**GET** `/api/admin/users`

🔒 需要认证 (role=0)

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | integer | 1 | 页码 |
| size | integer | 10 | 每页大小 |
| keyword | string | - | 搜索关键词 |
| role | integer | - | 角色筛选 |
| status | integer | - | 状态筛选 |

---

#### 15.3 用户详情

**GET** `/api/admin/users/{id}`

🔒 需要认证 (role=0)

---

#### 15.4 更新用户

**PUT** `/api/admin/users/{id}`

🔒 需要认证 (role=0)

---

#### 15.5 更新用户状态

**PUT** `/api/admin/users/{id}/status`

🔒 需要认证 (role=0)

| 参数 | 类型 | 说明 |
|------|------|------|
| status | integer | 状态: 1正常 0禁用 |

---

#### 15.6 删除用户

**DELETE** `/api/admin/users/{id}`

🔒 需要认证 (role=0)

---

### 教师管理

#### 15.7 教师列表

**GET** `/api/admin/tutors`

🔒 需要认证 (role=0)

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | integer | 1 | 页码 |
| size | integer | 10 | 每页大小 |
| keyword | string | - | 搜索关键词 |
| certStatus | integer | - | 认证状态 |
| education | integer | - | 学历 |

---

#### 15.8 教师详情

**GET** `/api/admin/tutors/{id}`

🔒 需要认证 (role=0)

---

#### 15.9 待审核列表

**GET** `/api/admin/tutors/pending`

🔒 需要认证 (role=0)

---

#### 15.10 通过认证

**POST** `/api/admin/tutors/{id}/approve`

🔒 需要认证 (role=0)

---

#### 15.11 拒绝认证

**POST** `/api/admin/tutors/{id}/reject`

🔒 需要认证 (role=0)

| 参数 | 类型 | 说明 |
|------|------|------|
| reason | string | 拒绝原因 |

---

### 家长管理

#### 15.12 家长列表

**GET** `/api/admin/parents`

🔒 需要认证 (role=0)

---

#### 15.13 家长详情

**GET** `/api/admin/parents/{id}`

🔒 需要认证 (role=0)

---

### 需求管理

#### 15.14 需求列表

**GET** `/api/admin/demands`

🔒 需要认证 (role=0)

---

#### 15.15 更新需求状态

**PUT** `/api/admin/demands/{id}/status`

🔒 需要认证 (role=0)

---

#### 15.16 删除需求

**DELETE** `/api/admin/demands/{id}`

🔒 需要认证 (role=0)

---

### 订单管理

#### 15.17 订单列表

**GET** `/api/admin/orders`

🔒 需要认证 (role=0)

---

#### 15.18 订单详情

**GET** `/api/admin/orders/{id}`

🔒 需要认证 (role=0)

---

#### 15.19 更新订单状态

**PUT** `/api/admin/orders/{id}/status`

🔒 需要认证 (role=0)

---

#### 15.20 释放托管资金

**POST** `/api/admin/orders/{id}/release`

🔒 需要认证 (role=0)

---

#### 15.21 订单退款

**POST** `/api/admin/orders/{id}/refund`

🔒 需要认证 (role=0)

| 参数 | 类型 | 说明 |
|------|------|------|
| reason | string | 退款原因 |

---

### 课时管理

#### 15.22 课时列表

**GET** `/api/admin/lessons`

🔒 需要认证 (role=0)

---

#### 15.23 确认课时

**POST** `/api/admin/lessons/{id}/confirm`

🔒 需要认证 (role=0)

---

#### 15.24 拒绝课时

**POST** `/api/admin/lessons/{id}/reject`

🔒 需要认证 (role=0)

| 参数 | 类型 | 说明 |
|------|------|------|
| reason | string | 拒绝原因 |

---

### 钱包管理

#### 15.25 钱包列表

**GET** `/api/admin/wallets`

🔒 需要认证 (role=0)

---

#### 15.26 钱包详情

**GET** `/api/admin/wallets/{id}`

🔒 需要认证 (role=0)

---

#### 15.27 调整余额

**POST** `/api/admin/wallets/{id}/adjust`

🔒 需要认证 (role=0)

| 参数 | 类型 | 说明 |
|------|------|------|
| amount | decimal | 调整金额 (正数增加，负数减少) |
| reason | string | 调整原因 |

---

### 系统设置

#### 15.28 获取系统设置

**GET** `/api/admin/settings`

🔒 需要认证 (role=0)

---

#### 15.29 更新系统设置

**PUT** `/api/admin/settings`

🔒 需要认证 (role=0)

---

## 📚 附录

### 角色枚举

| 值 | 说明 |
|---|------|
| 0 | 管理员 |
| 1 | 教员 |
| 2 | 家长 |

### 学历枚举

| 值 | 说明 |
|---|------|
| 1 | 本科在读 |
| 2 | 本科毕业 |
| 3 | 硕士在读 |
| 4 | 硕士毕业 |
| 5 | 博士 |

### 授课方式枚举

| 值 | 说明 |
|---|------|
| 1 | 上门 |
| 2 | 网课 |
| 3 | 均可 |

### 认证状态枚举

| 值 | 说明 |
|---|------|
| 0 | 未认证 |
| 1 | 审核中 |
| 2 | 已认证 |
| 3 | 已驳回 |

---

*文档更新日期: 2026-01-09*
