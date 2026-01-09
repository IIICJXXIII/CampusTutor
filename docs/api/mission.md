以下是根据提供的《详细设计报告(2).docx》和《概要设计文档(2).docx》整合而成的**家教平台 MVP 系统综合设计文档**。

这份文档经过结构化重组，旨在方便 AI Agent 进行逻辑分析、代码生成和架构理解。

---

# 家教平台 MVP 系统综合设计文档

## 1. 项目背景与设计目标

本项目旨在构建一个**“10天快速交付可演示原型”**的家教平台。系统设计聚焦于核心业务闭环，强调“简化可行、流程完整、演示友好”三大目标 。

### 1.1 核心业务主线

全链路流程包含六个关键节点：


**教师简化认证 → 家长发布需求 → AI 分层匹配 → 试课签约（三方协议） → 课时记录 → 模拟支付结算** 。

---

## 2. 系统架构设计

### 2.1 总体架构

系统采用分层架构，分为客户端层、网关层、业务服务层和数据层 。

* 
**客户端层**：微信小程序（家长端/教师端）、Web 管理后台（Vue3 + Element Plus） 。


* 
**网关层**：API 网关 (Spring Cloud Gateway) 。


* 
**业务服务层**：用户服务、匹配服务、教学服务、支付服务、订单服务 。


* 
**第三方服务**：百度 AI OCR、高德地图 API、腾讯会议 API、微信支付沙箱 。



### 2.2 技术栈选型

为了满足 10 天开发周期，采用了以下高效技术栈 ：

| 技术领域 | 选型方案 | 理由 |
| --- | --- | --- |
| **前端** | 微信小程序 + Vue3 管理后台 | 生态完善，开发效率高 |
| **后端** | Spring Boot 2.7 (单体应用) | 避免微服务复杂度，适合 MVP |
| **数据库** | MySQL 8.0 + Redis 7.0 | MySQL 存核心数据，Redis 处理地理位置与缓存 |
| **OCR 服务** | 百度 AI 开放平台 | 每日 500 次免费额度，API 稳定 |
| **地图服务** | 高德地图小程序 SDK | 免费额度高，集成简单 |
| **部署** | Docker + Ngrok (内网穿透) | 简化部署，便于演示 |

---

## 3. 核心功能模块与业务逻辑

### 3.1 教师简化认证体系

* 
**设计逻辑**：采用“两步引导式设计”（基础认证 + 能力补充）。


* **关键技术**：
* 
**OCR 识别**：调用百度 AI OCR 识别学生证，自动填充学校、专业、学号 。


* 
**规则验证**：校验 OCR 信息与手动填写信息的一致性，代替人工审核 。


* 
**敏感信息处理**：学号、身份证号采用 MD5 加密 。




* **API 定义 (CertificationService)**：
```java
// 提交学生证认证
String verifyStudentCard(String teacherId, String studentCardImage, StudentInfoDTO studentInfo);
// OCR识别
OCRResultDTO ocrStudentCard(String imageBase64);

```






### 3.2 智能匹配系统

* 
**流程**：家长发布需求 -> 触发 AI 分层匹配 -> 生成推荐列表 。


* **分层匹配算法**：
1. 
**第一层（硬性筛选）**：科目、年级完全匹配，距离 5km 内，时间一致 。


2. 
**第二层（加权排序）**：`总分 = 0.3×认证状态 + 0.3×距离 + 0.2×价格 + 0.2×信用分` 。


3. 
**第三层（前端筛选）**：用户在界面进行手动筛选 。




* **API 定义 (MatchingService)**：
```java
// 智能匹配教师
List<MatchResultDTO> matchTeachers(String demandId, double latitude, double longitude, int page, int size);
// 地图模式查找
List<MapDemandDTO> findNearbyDemands(String teacherId, LocationDTO location, double radius, String subject);

```






### 3.3 试课与签约

* 
**试课预约**：集成腾讯会议 API，自动生成链接 。


* 
**三方协议**：模板化文本，仅需勾选“我已阅读并同意”，代替复杂的电子签名 。协议包含课时费、服务保障、退款规则核心条款 。



### 3.4 教学管理与支付模拟

* **课时记录**：
* 
**打卡方式**：粗略 GPS 定位（误差≤500米） + 拍摄授课场景照片（自动加水印） 。


* 
**确认流程**：教师打卡 -> 家长端显示“确认课时”按钮 -> 点击即确认 。




* **模拟支付**：
* 
**状态机**：待支付 → 已支付 → 托管中 → 已结算 。


* 
**资金托管**：前端显示“资金托管中”，后端仅做数据库状态流转，不对接真实资金接口 。


* **API 定义 (PaymentService)**：
```java
// 创建模拟支付
String createMockPayment(String orderId, BigDecimal amount, String paymentType);
// 更新托管状态
boolean updateEscrowStatus(String orderId, String status);

```








---

## 4. 数据库详细设计

### 4.1 核心数据表 (Schema)

以下整合了字段定义与约束关系 ：

| 表名 | 关键字段 | 说明 |
| --- | --- | --- |
| **users** | `user_id` (PK, openid), `role`, `phone`, `real_name` | 用户基础表 |
| **teachers** | `teacher_id` (FK), `school`, `major`, `certification_status`, `credit_score`, `location_lat/lng` | 教师特有信息，含地理位置 |
| **parents** | `parent_id` (FK), `student_grade`, `contact_address` | 家长特有信息 |
| **demands** | `demand_id` (PK), `subject`, `budget_min/max`, `status` | 需求表，存储预算和偏好 |
| **matches** | `match_id` (PK), `demand_id`, `teacher_id`, `match_score`, `distance` | 匹配中间表，存算分结果 |
| **orders** | `order_id` (PK), `payment_status`, `escrow_status`, `total_amount` | 订单表，核心状态流转 |
| **lessons** | `lesson_id` (PK), `checkin_photo`, `checkin_lat`, `parent_confirm` | 课时记录，含打卡证据 |
| **wrong_questions** | `question_id`, `ocr_text`, `tags` | 错题本，含 OCR 识别文本 |

### 4.2 数据流转逻辑

1. 
**认证**：教师认证通过 -> 更新 `teachers` 表 `certification_status` 。


2. 
**匹配**：发布需求 -> 存入 `demands` -> 触发引擎 -> 写入 `matches` 表 。


3. 
**课时**：教师打卡 -> 写入 `lessons` (`parent_confirm=0`) -> 家长确认 -> 更新为 1 -> 同步 `orders` 表 `escrow_status` 为“托管中” 。


4. 
**结算**：模拟支付 -> 更新 `orders` 表 `payment_status` 为“已支付” 。



---

## 5. 界面设计 (UI/UX)

### 5.1 设计风格

* 
**主色调**：蓝色（信任）+ 暖橙（活力），辅助浅灰底色 。


* 
**原则**：减少复杂动画，优先保证核心流程顺畅 。



### 5.2 关键页面布局

* 
**教师认证页**：顶部进度条，中部卡片表单。OCR 识别时显示加载动画，识别后高亮填充 。


* 
**匹配结果页**：卡片式布局（2列）。匹配分数用颜色区分（>80分绿色，60-80黄色，<60灰色） 。


* 
**发布需求页**：分步表单（学生信息/教学需求/授课偏好），薄弱科目使用复选框组 。


* 
**模拟支付页**：金额明细分栏设计，支付方式仅保留“微信支付”，资金托管提示加注“演示环境”字样 。



---

## 6. 开发与部署策略

### 6.1 10天开发计划

* 
**Day 1-2**: 基础框架 + 数据库 + 用户系统 。


* 
**Day 3-4**: 教师认证模块（OCR 集成重点） 。


* 
**Day 5-6**: 匹配算法 + 需求发布 。


* 
**Day 7-8**: 教学管理 + 支付模拟 。


* 
**Day 9**: 小程序界面开发集成 。


* 
**Day 10**: 测试 + 演示录制 。



### 6.2 数据模拟 (Mock Data)

为确保演示效果，需预置以下数据：

* 10 个教师账号（含清华、北大等名校背景） 。


* 8 个家长需求 。


* 20 条历史订单数据 。



### 6.3 风险控制

* 
**OCR 不稳定**：后端需准备人工审核后台，并做本地缓存 。


* 
**演示网络问题**：采用 Docker+Ngrok 部署，并提前录制备用演示视频 。


* 
**功能裁剪**：明确去掉了行程共享、人脸识别、真实分账等复杂功能 。