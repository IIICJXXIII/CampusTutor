# CampusTutor 数据库表结构文档

本文档自动生成，包含项目整体数据库表结构。

## 1. 用户与权限模块 (User & Auth)

### 1.1 系统用户表 (`sys_user`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT, 主键ID |
| `username` | varchar(64) | NO | | 用户名/手机号 |
| `password` | varchar(128) | NO | | 加密密码 |
| `nickname` | varchar(64) | YES | | 昵称 |
| `avatar` | varchar(255) | YES | | 头像URL |
| `gender` | tinyint | YES | | 性别: 1-男, 2-女 |
| `role` | tinyint | NO | | 角色: 0-管理员, 1-教员, 2-家长 |
| `openid` | varchar(64) | YES | | 微信OpenID (小程序用) |
| `status` | tinyint | YES | `1` | 状态: 1-正常, 0-禁用 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |

### 1.2 钱包表 (`sys_wallet`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `user_id` | bigint | NO | | 关联用户ID |
| `balance` | decimal(10,2) | YES | `0.00` | 可用余额 |
| `frozen_amount` | decimal(10,2) | YES | `0.00` | 冻结金额(担保交易中) |
| `pay_password` | varchar(128) | YES | | 支付密码(加密) |
| `version` | int | YES | `0` | 乐观锁版本号 |
| `update_time` | datetime | YES | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 1.3 资金流水表 (`sys_transaction_flow`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT |
| `user_id` | bigint | NO | | 用户ID |
| `amount` | decimal(10,2) | NO | | 变动金额 (正数收入, 负数支出) |
| `balance_after` | decimal(10,2) | NO | | 变动后余额 (快照) |
| `flow_type` | tinyint | NO | | 类型: 1-充值, 2-支付订单, 3-课时费解冻收入, 4-提现, 5-退款 |
| `order_id` | bigint | YES | | 关联订单ID (可为空) |
| `remark` | varchar(255) | YES | | 备注 (如: 订单1001课时费结算) |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |

### 1.4 提现申请表 (`sys_withdrawal`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT |
| `user_id` | bigint | NO | | 用户ID |
| `amount` | decimal(10,2) | NO | | 提现金额 |
| `channel` | tinyint | YES | `1` | 渠道: 1-微信, 2-支付宝, 3-银行卡 |
| `account_no` | varchar(64) | NO | | 收款账号 |
| `status` | tinyint | YES | `0` | 状态: 0-审核中, 1-已打款, 2-驳回 |
| `audit_remark` | varchar(255) | YES | | 审核备注 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |

## 2. 教员中心模块 (Tutor Center)

### 2.1 教员档案认证表 (`tutor_profile`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT, 主键ID |
| `user_id` | bigint | NO | | 关联用户ID |
| `real_name` | varchar(32) | YES | | 真实姓名 |
| `id_card` | varchar(32) | YES | | 身份证号(加密存储) |
| `id_card_front_url` | varchar(255) | YES | | 身份证正面照URL |
| `id_card_back_url` | varchar(255) | YES | | 身份证背面照URL |
| `university_name` | varchar(64) | YES | | 学校名称 |
| `major` | varchar(64) | YES | | 专业 |
| `education` | tinyint | YES | | 学历：1本科在读 2本科毕业 3硕士在读 4硕士毕业 5博士 |
| `enroll_year` | int | YES | | 入学年份 |
| `student_card_url` | varchar(255) | YES | | 学生证照片URL |
| `certificate_urls` | text | YES | | 资质证书URLs(JSON数组) |
| `teach_subjects` | text | YES | | 可授科目(JSON数组) |
| `teach_grades` | text | YES | | 可授年级(JSON数组) |
| `teach_style` | varchar(255) | YES | | 教学风格 |
| `introduction` | text | YES | | 自我介绍 |
| `expect_price` | decimal(10,2) | YES | | 期望时薪(元) |
| `can_visit` | tinyint | YES | `1` | 可上门：0否 1是 |
| `can_online` | tinyint | YES | `1` | 可网课：0否 1是 |
| `longitude` | decimal(10,6) | YES | | 经度 |
| `latitude` | decimal(10,6) | YES | | 纬度 |
| `address` | varchar(255) | YES | | 详细地址 |
| `cert_status` | tinyint | YES | `0` | 认证状态：0待提交 1待审核 2已通过 3已拒绝 |
| `reject_reason` | varchar(255) | YES | | 审核拒绝原因 |
| `rating` | decimal(2,1) | YES | `5.0` | 综合评分(1-5星) |
| `order_count` | int | YES | `0` | 完成订单数 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | datetime | YES | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 2.2 教员排课配置表 (`tutor_schedule_config`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT, 主键ID |
| `tutor_id` | bigint | NO | | 教员档案ID |
| `day_of_week` | tinyint | NO | | 星期几：1-7 |
| `start_time` | varchar(10) | YES | | 开始时间(HH:mm格式) |
| `end_time` | varchar(10) | YES | | 结束时间(HH:mm格式) |
| `available` | tinyint | YES | `1` | 是否可用：0否 1是 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | datetime | YES | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

## 3. 家长需求与匹配模块 (Demand & Matching)

### 3.1 学生档案表 (`parent_student`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT, 主键ID |
| `parent_id` | bigint | NO | | 家长用户ID |
| `student_name` | varchar(32) | YES | | 学生姓名 |
| `gender` | tinyint | YES | `1` | 学生性别：0女 1男 |
| `grade` | varchar(32) | YES | | 年级 (如: 小学三年级) |
| `school_name` | varchar(64) | YES | | 学校名称 |
| `weak_subjects` | varchar(255) | YES | | 薄弱科目(JSON或逗号分隔) |
| `study_desc` | text | YES | | 学习情况描述 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | datetime | YES | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.2 需求发布表 (`demand_post`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT, 主键ID |
| `publisher_id` | bigint | NO | | 发布者ID（家长用户ID） |
| `student_id` | bigint | YES | | 关联学生ID |
| `title` | varchar(128) | YES | | 需求标题 |
| `subject` | varchar(32) | NO | | 需求科目 |
| `grade` | varchar(32) | NO | | 需求年级 |
| `skill_level` | varchar(32) | YES | | 基础水平：零基础/有基础/考级冲刺 |
| `venue_type` | tinyint(4) | YES | | 场地类型：1教员上门 2学员上门 3公共场馆 |
| `expect_price` | decimal(10,2) | YES | | 期望价格(元/小时) |
| `schedule_require` | text | YES | | 课时要求(JSON数组) |
| `teach_mode` | tinyint | YES | `3` | 授课方式：1上门 2网课 3均可 |
| `longitude` | decimal(10,6) | YES | | 经度 |
| `latitude` | decimal(10,6) | YES | | 纬度 |
| `address` | varchar(255) | YES | | 详细地址 |
| `detail` | text | YES | | 需求详情 |
| `status` | tinyint | YES | `1` | 状态：0下架 1上架 2已匹配 |
| `matched_tutor_id` | bigint | YES | | 匹配的教员ID |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | datetime | YES | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.3 用户行为轨迹表 (`user_action_log`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT |
| `user_id` | bigint | NO | | 家长ID |
| `target_id` | bigint | NO | | 被查看的教员ID 或 搜索的关键词ID |
| `action_type` | tinyint | YES | `1` | 1-查看教员详情, 2-搜索科目, 3-收藏教员 |
| `duration` | int | YES | `0` | 停留时长(秒) |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |

## 4. 交易与订单模块 (Transaction)

### 4.1 课程订单表 (`course_order`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT, 订单号/主键ID |
| `order_no` | varchar(64) | YES | | 订单编号 |
| `parent_id` | bigint | NO | | 家长用户ID |
| `student_id` | bigint | YES | | 学生ID |
| `tutor_id` | bigint | NO | | 教员用户ID |
| `tutor_profile_id` | bigint | YES | | 教员档案ID |
| `demand_id` | bigint | YES | | 来源需求ID |
| `subject` | varchar(32) | YES | | 课程科目 |
| `grade` | varchar(32) | YES | | 课程年级 |
| `teach_mode` | tinyint | YES | `1` | 授课方式：1上门 2网课 |
| `unit_price` | decimal(10,2) | NO | | 课时单价(元/小时) |
| `total_hours` | int | YES | `1` | 总课时数 |
| `total_amount` | decimal(10,2) | NO | | 订单总金额(托管金额) |
| `service_fee` | decimal(10,2) | YES | `0.00` | 平台服务费 |
| `tutor_amount` | decimal(10,2) | YES | `0.00` | 教员实收金额 |
| `used_hours` | int | YES | `0` | 已上课时 |
| `status` | tinyint | YES | `0` | 状态: 0-待支付, 1-已支付待上课, 2-进行中, 3-已完成, 4-已取消, 5-退款中, 6-已退款 |
| `pay_time` | datetime | YES | | 支付时间 |
| `pay_type` | tinyint | YES | | 支付方式：1钱包 2微信 3支付宝 |
| `pay_trade_no` | varchar(64) | YES | | 第三方支付流水号 |
| `cancel_reason` | varchar(255) | YES | | 取消原因 |
| `remark` | varchar(255) | YES | | 备注 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | datetime | YES | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 4.2 保险单记录表 (`insurance_policy`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT |
| `order_id` | bigint | NO | | 关联订单ID |
| `policy_no` | varchar(64) | YES | | 保险单号 |
| `provider` | varchar(64) | YES | `'PingAn'` | 保险公司 |
| `status` | tinyint | YES | `1` | 1-生效中, 2-已过期 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |

## 5. 教学过程管控模块 (Process Control)

### 5.1 课时打卡记录表 (`teaching_record`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT, 主键ID |
| `order_id` | bigint | NO | | 订单ID |
| `lesson_index` | int | NO | | 第几节课 |
| `start_time` | datetime | YES | | 实际上课时间 |
| `end_time` | datetime | YES | | 实际下课时间 |
| `clock_in_lat` | decimal(10,6) | YES | | 打卡纬度 |
| `clock_in_lng` | decimal(10,6) | YES | | 打卡经度 |
| `clock_in_img` | varchar(255) | YES | | 现场拍照(水印) |
| `content_summary` | text | YES | | 教学内容摘要 |
| `homework_assigned` | text | YES | | 布置作业 |
| `status` | tinyint | YES | `0` | 状态：0-待确认, 1-家长已确认, 2-异常/申诉 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | datetime | YES | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 5.2 学生阶段报告表 (`student_report`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT |
| `order_id` | bigint | NO | | 关联订单ID |
| `student_id` | bigint | NO | | 学生ID |
| `report_type` | tinyint | YES | `1` | 1-月度报告, 2-阶段总结 |
| `score_chart_data` | json | YES | | 成绩变化数据(ECharts JSON) |
| `tutor_comment` | text | YES | | 老师评语 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |

### 5.3 在线错题本 (`mistake_notebook`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT |
| `student_id` | bigint | NO | | 学生ID |
| `subject` | varchar(32) | YES | | 科目 |
| `question_img` | varchar(255) | NO | | 题目图片 |
| `tags` | varchar(255) | YES | | 知识点标签 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |

## 6. 系统、社区与交互模块 (System & Interaction)

### 6.1 IM聊天记录表 (`sys_chat_msg`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT |
| `sender_id` | bigint | NO | | 发送者ID |
| `receiver_id` | bigint | NO | | 接收者ID |
| `content` | text | YES | | 消息内容 |
| `msg_type` | tinyint | YES | `1` | 1-文本, 2-图片, 3-简历卡片, 4-订单邀约 |
| `is_read` | tinyint | YES | `0` | 0-未读, 1-已读 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |

### 6.2 订单评价表 (`sys_comment`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT |
| `order_id` | bigint | NO | | 关联订单ID |
| `from_user_id` | bigint | NO | | 评论人 |
| `to_user_id` | bigint | NO | | 被评人 |
| `score` | tinyint | YES | `5` | 星级 1-5 |
| `content` | varchar(512) | YES | | 评价内容 |
| `tags` | varchar(255) | YES | | 评价标签: 准时, 讲课好 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |

### 6.3 数据字典表 (`sys_dict`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT |
| `type_code` | varchar(64) | NO | | 类型编码: subject, grade, tag |
| `label` | varchar(64) | NO | | 展示名: 数学, 高三 |
| `value` | varchar(64) | NO | | 存储值: math, grade_3 |
| `sort` | int | YES | `0` | 排序 |

### 6.4 社区帖子表 (`community_post`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT |
| `user_id` | bigint | NO | | 发帖用户ID |
| `topic_type` | tinyint | YES | `1` | 1-经验分享, 2-难题求助 |
| `title` | varchar(128) | NO | | 标题 |
| `content` | text | YES | | 内容 |
| `images` | json | YES | | 图片列表 |
| `view_count` | int | YES | `0` | 浏览量 |
| `like_count` | int | YES | `0` | 点赞量 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |

### 6.5 社区评论表 (`community_reply`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT |
| `post_id` | bigint | NO | | 帖子ID |
| `user_id` | bigint | NO | | 回复用户ID |
| `content` | varchar(512) | YES | | 回复内容 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |

## 7. 其他模块

### 7.1 预约请求表 (`booking_request`)
| 字段名 | 数据类型 | 是否为空 | 默认值 | 约束 / 备注 |
| --- | --- | --- | --- | --- |
| `id` | bigint | NO | | AUTO_INCREMENT, 主键ID |
| `parent_id` | bigint | NO | | 家长用户ID |
| `tutor_id` | bigint | NO | | 教师用户ID |
| `student_id` | bigint | NO | | 学生ID |
| `subject` | varchar(50) | NO | | 科目 |
| `grade` | varchar(50) | NO | | 年级 |
| `booking_date` | datetime | NO | | 预约日期 |
| `start_time` | varchar(10) | NO | | 开始时间 |
| `end_time` | varchar(10) | NO | | 结束时间 |
| `status` | tinyint | YES | `0` | 状态：0-待教师确认, 1-教师已确认, 2-教师已拒绝, 3-家长已取消 |
| `remark` | text | YES | | 备注 |
| `create_time` | datetime | YES | CURRENT_TIMESTAMP | 创建时间 |
| `update_time` | datetime | YES | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
