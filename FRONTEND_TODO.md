# 🎯 前端 & 小程序 TODO 事项

> 本文档总结后端已完成的API接口，前端(campus-web)和小程序(campus-user-app)需要对接实现的功能。

---

## 📌 目录

1. [钱包模块](#一钱包模块-wallet)
2. [智能匹配增强](#二智能匹配增强-match)
3. [地图功能（高德地图）](#三地图功能高德地图)
4. [LLM智能服务（DeepSeek V3）](#四llm智能服务deepseek-v3)
5. [新增API接口清单](#五新增api接口清单)
6. [配置说明](#六配置说明)

---

## 一、钱包模块 (Wallet)

### campus-web（Vue.js 前端）

| 优先级 | 文件/位置 | TODO | 说明 |
|:---:|-----------|------|------|
| 🔴 P0 | `src/api/wallet.js` | **创建钱包API模块** | 封装钱包相关接口 |
| 🔴 P0 | `src/views/Mine/Wallet.vue` | **创建钱包页面** | 显示余额、冻结金额、交易记录列表 |
| 🔴 P0 | `src/views/Mine/Withdraw.vue` | **创建提现页面** | 表单：金额、渠道(微信/支付宝/银行卡)、账号、支付密码 |
| 🟡 P1 | `src/router/index.js` | **添加钱包路由** | `/wallet`、`/wallet/withdraw` |
| 🟡 P1 | `src/views/Mine/TransactionDetail.vue` | **交易详情页** | 显示单笔交易详细信息 |

**API调用示例：**
```javascript
// src/api/wallet.js
import request from './request'

// 获取钱包信息
export function getWallet() {
  return request.get('/api/wallet')
}

// 获取交易流水
export function getTransactions(params) {
  return request.get('/api/wallet/transactions', { params })
}

// 申请提现
export function withdraw(data) {
  return request.post('/api/wallet/withdraw', data)
}

// 获取提现记录
export function getWithdrawals(params) {
  return request.get('/api/wallet/withdrawals', { params })
}
```

### campus-user-app（微信小程序）

| 优先级 | 文件/位置 | TODO | 说明 |
|:---:|-----------|------|------|
| 🔴 P0 | `miniprogram/config/apiConfig.js` | **添加钱包API配置** | 配置4个钱包接口地址 |
| 🔴 P0 | `miniprogram/pages/common/wallet/` | **创建钱包页面** | 余额展示、交易流水列表 |
| 🔴 P0 | `miniprogram/pages/common/withdraw/` | **创建提现页面** | 提现申请表单 |
| 🟡 P1 | `miniprogram/app.json` | **注册新页面** | 添加 wallet、withdraw 页面路径 |

---

## 二、智能匹配增强 (Match)

后端已优化匹配算法，新增多维度加权评分系统：

- **科目匹配**: 30%
- **年级匹配**: 20%
- **距离评分**: 25%（真实距离计算）
- **价格匹配**: 15%
- **评分权重**: 10%

### campus-web

| 优先级 | 文件/位置 | TODO | 说明 |
|:---:|-----------|------|------|
| 🔴 P0 | `src/views/Parent/TutorList.vue` | **展示匹配分数** | 显示 `matchScore`、`matchTags` 标签 |
| 🔴 P0 | `src/api/match.js` | **更新请求参数** | 支持 `sortBy: 'score'` 智能推荐排序 |
| 🟡 P1 | `src/components/TutorCard.vue` | **匹配标签展示** | 展示"科目匹配"、"距离近"、"高评分"等标签 |
| 🟡 P1 | `src/views/Parent/TutorList.vue` | **排序选项** | 添加"智能推荐"排序选项 |

**响应数据结构变化：**
```json
{
  "tutorId": 1,
  "realName": "张老师",
  "matchScore": 85.5,
  "subjectScore": 100,
  "gradeScore": 80,
  "distanceScore": 90,
  "priceScore": 70,
  "ratingScore": 85,
  "matchTags": ["科目匹配", "距离近", "高评分"],
  "distance": 2.5
}
```

### campus-user-app

| 优先级 | 文件/位置 | TODO | 说明 |
|:---:|-----------|------|------|
| 🔴 P0 | `miniprogram/components/teacherCard/` | **增强卡片展示** | 添加匹配分数、匹配标签展示 |
| 🔴 P0 | `miniprogram/pages/parent/matchResult/` | **匹配结果页** | 按匹配分数排序、显示各维度评分 |
| 🟡 P1 | `miniprogram/pages/teacher/mapFindStudent/` | **地图找学生** | 显示真实距离（来自后端） |

---

## 三、地图功能（高德地图）

> ⚠️ 已从腾讯地图更换为**高德地图**

### campus-web

| 优先级 | 文件/位置 | TODO | 说明 |
|:---:|-----------|------|------|
| 🔴 P0 | `src/api/map.js` | **创建地图API模块** | 封装逆地址解析、正向解码、路线规划、距离计算 |
| 🟡 P1 | `src/views/Parent/Booking.vue` | **地址选择优化** | 使用 `/api/map/geocoder` 进行地址搜索 |
| 🟡 P1 | `src/views/Teacher/RouteView.vue` | **路线规划页面** | 调用 `/api/map/direction` 展示路线 |
| 🟢 P2 | `src/components/AddressPicker.vue` | **地址选择组件** | 结合高德地图SDK实现地址搜索、定位 |

**API调用示例：**
```javascript
// src/api/map.js
import request from './request'

// 逆地址解析
export function reverseGeocode(latitude, longitude) {
  return request.get('/api/map/geocoder/reverse', {
    params: { latitude, longitude }
  })
}

// 正向地址解析
export function geocode(address) {
  return request.get('/api/map/geocoder', { params: { address } })
}

// 路线规划
export function getDirection(data) {
  return request.post('/api/map/direction', data)
}

// 距离计算
export function getDistance(params) {
  return request.get('/api/map/distance', { params })
}
```

### campus-user-app

| 优先级 | 文件/位置 | TODO | 说明 |
|:---:|-----------|------|------|
| 🔴 P0 | `miniprogram/utils/mapUtil.js` | **地图工具类** | 封装高德地图相关API调用 |
| 🟡 P1 | `miniprogram/pages/teacher/mapFindStudent/` | **完善地图功能** | 使用后端 `/api/map/distance` 计算真实距离 |
| 🟢 P2 | 小程序地图组件 | **路线规划** | 展示步行/驾车路线 |

> 💡 **提示**: 小程序端可使用微信内置地图组件配合后端API，无需额外引入高德地图SDK

---

## 四、LLM智能服务（DeepSeek V3）

> ⚠️ 已更换为 **DeepSeek V3** 模型

### campus-web

| 优先级 | 文件/位置 | TODO | 说明 |
|:---:|-----------|------|------|
| 🔴 P0 | `src/api/llm.js` | **创建LLM API模块** | 封装需求解析、对话、快速问答接口 |
| 🔴 P0 | `src/views/Parent/PublishDemand.vue` | **智能需求解析** | 添加"AI智能填写"按钮，自动解析填充表单 |
| 🟡 P1 | `src/components/AiChatWidget.vue` | **AI助手组件** | 悬浮AI助手，支持对话交互 |
| 🟢 P2 | `src/views/common/AiChat.vue` | **AI对话页面** | 完整的AI对话界面 |

**API调用示例：**
```javascript
// src/api/llm.js
import request from './request'

// 智能需求解析
export function parseDemand(text) {
  return request.post('/api/llm/demand/parse', { text })
}

// AI对话
export function chat(data) {
  return request.post('/api/llm/chat', data)
}

// 快速问答
export function quickAnswer(question) {
  return request.get('/api/llm/quick-answer', { params: { question } })
}
```

**需求解析使用示例：**
```javascript
// 用户输入自然语言描述
const userInput = "我家孩子上初二，数学不太好，想找个女老师，周末上门辅导，预算150一小时左右"

// 调用AI解析
const result = await parseDemand(userInput)
// 返回结构化数据
{
  "subject": "数学",
  "grade": "初二",
  "expectPrice": 150,
  "teachMode": 1,  // 上门
  "preferGender": 2, // 女
  "scheduleRequire": "周末",
  "confidence": 0.92
}
```

### campus-user-app

| 优先级 | 文件/位置 | TODO | 说明 |
|:---:|-----------|------|------|
| 🔴 P0 | `miniprogram/pages/parent/publishDemand/` | **智能需求解析** | 语音/文本输入 → AI解析 → 自动填充 |
| 🟡 P1 | `miniprogram/pages/common/aiChat/` | **AI助手页面** | 对话式AI助手 |
| 🟢 P2 | `miniprogram/components/aiAssistant/` | **AI助手组件** | 悬浮按钮入口 |

---

## 五、新增API接口清单

### 钱包模块
| 方法 | 接口 | 说明 | 认证 |
|------|------|------|:---:|
| GET | `/api/wallet` | 获取钱包信息 | ✅ |
| GET | `/api/wallet/transactions` | 交易流水分页 | ✅ |
| POST | `/api/wallet/withdraw` | 申请提现 | ✅ |
| GET | `/api/wallet/withdrawals` | 提现记录分页 | ✅ |

### 地图模块（高德地图）
| 方法 | 接口 | 说明 | 认证 |
|------|------|------|:---:|
| GET | `/api/map/geocoder/reverse` | 逆地址解析 | ❌ |
| GET | `/api/map/geocoder` | 正向解码 | ❌ |
| POST | `/api/map/direction` | 路线规划 | ❌ |
| GET | `/api/map/distance` | 距离计算 | ❌ |

### LLM模块（DeepSeek V3）
| 方法 | 接口 | 说明 | 认证 |
|------|------|------|:---:|
| POST | `/api/llm/demand/parse` | 智能需求解析 | ✅ |
| POST | `/api/llm/chat` | AI对话 | ✅ |
| GET | `/api/llm/quick-answer` | 快速问答 | ✅ |

---

## 六、配置说明

后端 `application.properties` 已配置，部署时需替换实际密钥：

```properties
# ==================== 高德地图配置 ====================
amap.key=your_amap_key_here
amap.secret-key=
amap.base-url=https://restapi.amap.com

# ==================== DeepSeek LLM配置 ====================
llm.enabled=true
llm.provider=deepseek
llm.api-key=your_deepseek_api_key_here
llm.base-url=https://api.deepseek.com
llm.model=deepseek-chat
llm.max-tokens=2000
llm.temperature=0.3
llm.timeout=60
```

### 获取API Key

| 服务 | 申请地址 | 说明 |
|------|----------|------|
| 高德地图 | https://lbs.amap.com | 注册后创建应用，获取Web服务API Key |
| DeepSeek | https://platform.deepseek.com | 注册后在控制台创建API Key |

---

## 七、开发优先级建议

### 🔴 P0 - 高优先级（核心功能）
1. 钱包模块 - 余额查询、提现功能
2. 智能匹配展示 - 匹配分数、标签展示
3. LLM需求解析 - AI智能填充表单

### 🟡 P1 - 中优先级（体验增强）
1. 地图路线规划 - 展示到家教地点的路线
2. AI对话助手 - 智能客服问答
3. 交易明细页面

### 🟢 P2 - 低优先级（锦上添花）
1. 完整AI对话界面
2. 地址选择器组件优化
3. 高级地图功能

---

## 八、技术栈提示

### campus-web (Vue.js)
- 使用 `axios` 或项目内 `request.js` 进行API调用
- 推荐使用 `pinia` 管理钱包状态
- 地图可选择集成 `@amap/amap-jsapi-loader`

### campus-user-app (微信小程序)
- 使用 `wx.request` 或项目内 `request.js` 封装
- 地图使用微信内置 `map` 组件
- AI功能可结合语音识别 `wx.startRecord`

---

> 📅 文档生成时间: 2026年1月7日  
> 🔧 后端版本: campus-backend v1.0  
> 📱 适用项目: CampusTutor 校园智教平台
