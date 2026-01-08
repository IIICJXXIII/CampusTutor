/*
 * CampusTutor 模拟数据脚本
 * 密码统一为: test123456 (MD5: 47ec2dd791e31e2ef2076caf64ed9b3d)
 * 包含: 字典, 用户, 钱包, 教员档案, 家长学生, 需求, 订单, 评价等
 */

USE `campus_tutor_db`;

-- ----------------------------
-- 1. 数据字典 (sys_dict)
-- ----------------------------
INSERT INTO `sys_dict` (type_code, label, value, sort) VALUES 
('subject', '数学', 'math', 1),
('subject', '语文', 'chinese', 2),
('subject', '英语', 'english', 3),
('subject', '物理', 'physics', 4),
('subject', '化学', 'chemistry', 5),
('subject', '生物', 'biology', 6),
('subject', '历史', 'history', 7),
('subject', '地理', 'geography', 8),
('subject', '政治', 'politics', 9),
('grade', '小学一年级', 'primary_1', 1),
('grade', '小学二年级', 'primary_2', 2),
('grade', '小学三年级', 'primary_3', 3),
('grade', '小学四年级', 'primary_4', 4),
('grade', '小学五年级', 'primary_5', 5),
('grade', '小学六年级', 'primary_6', 6),
('grade', '初一', 'junior_1', 7),
('grade', '初二', 'junior_2', 8),
('grade', '初三', 'junior_3', 9),
('grade', '高一', 'senior_1', 10),
('grade', '高二', 'senior_2', 11),
('grade', '高三', 'senior_3', 12);

-- ----------------------------
-- 2. 系统用户 (sys_user)
-- 密码均为 test123456
-- ----------------------------
-- 管理员 id:1
INSERT INTO `sys_user` (id, username, password, nickname, role, status, gender) VALUES 
(1, 'admin', '47ec2dd791e31e2ef2076caf64ed9b3d', '系统管理员', 0, 1, 1);

-- 教员 id: 101-120 (20名)
INSERT INTO `sys_user` (id, username, password, nickname, role, status, gender, avatar) VALUES 
(101, '13800138101', '47ec2dd791e31e2ef2076caf64ed9b3d', '张学霸', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'),
(102, '13800138102', '47ec2dd791e31e2ef2076caf64ed9b3d', '李英语', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka'),
(103, '13800138103', '47ec2dd791e31e2ef2076caf64ed9b3d', '王物理', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Bob'),
(104, '13800138104', '47ec2dd791e31e2ef2076caf64ed9b3d', '赵化学', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Cathy'),
(105, '13800138105', '47ec2dd791e31e2ef2076caf64ed9b3d', '孙全科', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=David'),
(106, '13800138106', '47ec2dd791e31e2ef2076caf64ed9b3d', '周数学', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Eva'),
(107, '13800138107', '47ec2dd791e31e2ef2076caf64ed9b3d', '吴语文', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Frank'),
(108, '13800138108', '47ec2dd791e31e2ef2076caf64ed9b3d', '郑历史', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Grace'),
(109, '13800138109', '47ec2dd791e31e2ef2076caf64ed9b3d', '冯地理', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Harry'),
(110, '13800138110', '47ec2dd791e31e2ef2076caf64ed9b3d', '陈生物', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Ivy'),
(111, '13800138111', '47ec2dd791e31e2ef2076caf64ed9b3d', '林清华', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jack'),
(112, '13800138112', '47ec2dd791e31e2ef2076caf64ed9b3d', '黄北大', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Kate'),
(113, '13800138113', '47ec2dd791e31e2ef2076caf64ed9b3d', '刘复旦', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Leo'),
(114, '13800138114', '47ec2dd791e31e2ef2076caf64ed9b3d', '张交大', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mia'),
(115, '13800138115', '47ec2dd791e31e2ef2076caf64ed9b3d', '徐浙大', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Nick'),
(116, '13800138116', '47ec2dd791e31e2ef2076caf64ed9b3d', '朱南大', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Olivia'),
(117, '13800138117', '47ec2dd791e31e2ef2076caf64ed9b3d', '何中科', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Paul'),
(118, '13800138118', '47ec2dd791e31e2ef2076caf64ed9b3d', '罗同济', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Queen'),
(119, '13800138119', '47ec2dd791e31e2ef2076caf64ed9b3d', '高人大', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Rick'),
(120, '13800138120', '47ec2dd791e31e2ef2076caf64ed9b3d', '马北航', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Sara');

-- 家长 id: 201-220 (20名)
INSERT INTO `sys_user` (id, username, password, nickname, role, status, gender, avatar) VALUES 
(201, '13900139201', '47ec2dd791e31e2ef2076caf64ed9b3d', '子涵妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom1'),
(202, '13900139202', '47ec2dd791e31e2ef2076caf64ed9b3d', '浩宇爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad1'),
(203, '13900139203', '47ec2dd791e31e2ef2076caf64ed9b3d', '欣怡妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom2'),
(204, '13900139204', '47ec2dd791e31e2ef2076caf64ed9b3d', '俊杰爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad2'),
(205, '13900139205', '47ec2dd791e31e2ef2076caf64ed9b3d', '梓涵妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom3'),
(206, '13900139206', '47ec2dd791e31e2ef2076caf64ed9b3d', '宇轩爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad3'),
(207, '13900139207', '47ec2dd791e31e2ef2076caf64ed9b3d', '雨桐妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom4'),
(208, '13900139208', '47ec2dd791e31e2ef2076caf64ed9b3d', '子轩爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad4'),
(209, '13900139209', '47ec2dd791e31e2ef2076caf64ed9b3d', '晨曦妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom5'),
(210, '13900139210', '47ec2dd791e31e2ef2076caf64ed9b3d', '浩然爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad5'),
(211, '13900139211', '47ec2dd791e31e2ef2076caf64ed9b3d', '诗涵妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom6'),
(212, '13900139212', '47ec2dd791e31e2ef2076caf64ed9b3d', '天宇爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad6'),
(213, '13900139213', '47ec2dd791e31e2ef2076caf64ed9b3d', '思琪妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom7'),
(214, '13900139214', '47ec2dd791e31e2ef2076caf64ed9b3d', '博文爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad7'),
(215, '13900139215', '47ec2dd791e31e2ef2076caf64ed9b3d', '紫萱妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom8'),
(216, '13900139216', '47ec2dd791e31e2ef2076caf64ed9b3d', '瑞泽爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad8'),
(217, '13900139217', '47ec2dd791e31e2ef2076caf64ed9b3d', '佳怡妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom9'),
(218, '13900139218', '47ec2dd791e31e2ef2076caf64ed9b3d', '子墨爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad9'),
(219, '13900139219', '47ec2dd791e31e2ef2076caf64ed9b3d', '雪儿妈妈', 2, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mom10'),
(220, '13900139220', '47ec2dd791e31e2ef2076caf64ed9b3d', '星宇爸爸', 2, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Dad10');

-- ----------------------------
-- 3. 用户钱包 (sys_wallet)
-- ----------------------------
INSERT INTO `sys_wallet` (user_id, balance, frozen_amount)
SELECT id, 0.00, 0.00 FROM sys_user;

-- 给部分用户充值
UPDATE `sys_wallet` SET balance = 5000.00 WHERE user_id BETWEEN 201 AND 210;

-- ----------------------------
-- 4. 教员档案 (tutor_profile)
-- ----------------------------
INSERT INTO `tutor_profile` (user_id, real_name, university_name, major, education, teach_subjects, teach_grades, expect_price, longitude, latitude, address, cert_status, rating, introduction, teach_style, can_visit, can_online) VALUES
(101, '张伟', '北京大学', '数学系', 2, '["数学","物理"]', '["初一","初二","初三","高一"]', 150.00, 116.310003, 39.991957, '北京市海淀区颐和园路5号', 2, 4.9, '数学奥赛金牌得主，擅长逻辑思维培养', '严谨细致，深入浅出', 1, 1),
(102, '李娜', '北京外国语大学', '英语', 3, '["英语"]', '["小学一年级","小学二年级","小学三年级","小学四年级","小学五年级","小学六年级","初一","初二"]', 120.00, 116.315055, 39.957583, '北京市海淀区西三环北路2号', 2, 4.8, '专八水平，口语流利，有两年少儿英语教学经验', '活泼生动，寓教于乐', 1, 1),
(103, '王强', '清华大学', '物理', 5, '["物理","数学"]', '["高一","高二","高三"]', 200.00, 116.326759, 40.003304, '北京市海淀区双清路30号', 2, 5.0, '物理系博士在读，擅长高考冲刺辅导', '重点突出，解题技巧丰富', 0, 1),
(104, '赵敏', '北京师范大学', '化学', 2, '["化学","生物"]', '["初三","高一"]', 100.00, 116.372551, 39.965874, '北京市海淀区新街口外大街19号', 2, 4.7, '耐心细致，善于引导学生建立知识体系', '亲切随和，循循善诱', 1, 0),
(105, '孙浩', '中国人民大学', '经济学', 3, '["数学","英语"]', '["小学一年级","小学二年级","小学三年级","小学四年级","小学五年级","小学六年级"]', 90.00, 116.316833, 39.971556, '北京市海淀区中关村大街59号', 1, 4.5, '喜欢孩子，有耐心，善于沟通', '轻松愉快', 1, 1),
(106, '周婷', '北京理工大学', '应用数学', 3, '["数学"]', '["初一","初二","高一","高二"]', 130.00, 116.321111, 39.960000, '北京市海淀区中关村南大街5号', 2, 4.8, '逻辑清晰，善于总结考点', '注重基础，稳扎稳打', 1, 1),
(107, '吴刚', '北京复旦大学', '汉语言文学', 4, '["语文","历史"]', '["小学一年级","小学二年级","小学三年级","小学四年级","小学五年级","小学六年级","初一"]', 110.00, 116.300000, 39.980000, '北京市海淀区', 2, 4.6, '文学功底深厚，作文辅导经验丰富', '文采飞扬，启发式教学', 1, 1),
(108, '郑洁', '中国政法大学', '法学', 3, '["政治","历史"]', '["初三","高三"]', 140.00, 116.350000, 39.970000, '北京市昌平区', 2, 4.9, '善于梳理知识脉络，记忆技巧传授', '条理清晰', 0, 1),
(109, '冯涛', '北京航空航天大学', '地理信息', 4, '["地理"]', '["初一","初二","高一"]', 120.00, 116.340000, 39.985000, '北京市海淀区学院路', 2, 4.7, '善于结合实际案例教学', '图文并茂，生动有趣', 1, 0),
(110, '陈静', '中国农业大学', '生物科学', 3, '["生物","化学"]', '["高一","高二"]', 110.00, 116.360000, 40.010000, '北京市海淀区清华东路', 2, 4.8, '实验能力强，知识点讲解透彻', '细致入微', 1, 1);

-- 更多教员数据，模拟不同状态 (待审核, 拒绝等)
INSERT INTO `tutor_profile` (user_id, real_name, university_name, major, education, teach_subjects, teach_grades, expect_price, cert_status, rating) VALUES
(111, '林风', '清华大学', '计算机', 3, '["数学","编程"]', '["初一","初二","初三"]', 180.00, 0, 0.0), -- 待提交
(112, '黄芸', '北京大学', '新闻', 2, '["语文","英语"]', '["小学一年级","小学二年级","小学三年级","小学四年级","小学五年级","小学六年级"]', 100.00, 1, 0.0), -- 待审核
(113, '刘星', '复旦大学', '物理', 4, '["物理"]', '["高一","高二","高三"]', 200.00, 3, 0.0), -- 已拒绝
(114, '张月', '上海交大', '英语', 3, '["英语"]', '["初一","初二","初三"]', 120.00, 2, 5.0),
(115, '徐阳', '浙江大学', '数学', 4, '["数学"]', '["高一","高二","高三"]', 160.00, 2, 4.8);


-- ----------------------------
-- 5. 教员排课 (tutor_schedule_config)
-- 为部分教员添加排课信息
-- ----------------------------
INSERT INTO `tutor_schedule_config` (tutor_id, day_of_week, start_time, end_time, available) VALUES
(1, 1, '18:00', '21:00', 1), (1, 3, '18:00', '21:00', 1), (1, 6, '09:00', '12:00', 1), -- 张伟
(2, 2, '19:00', '21:00', 1), (2, 4, '19:00', '21:00', 1), (2, 7, '14:00', '17:00', 1), -- 李娜
(3, 6, '08:00', '20:00', 1), (3, 7, '08:00', '20:00', 1); -- 王强 (周末全天)

-- ----------------------------
-- 6. 家长学生 (parent_student)
-- ----------------------------
INSERT INTO `parent_student` (parent_id, student_name, gender, grade, school_name, weak_subjects, study_desc) VALUES
(201, '子涵', 1, '初二', '北大附中', '数学,物理', '理科基础较弱，需要加强概念理解'),
(202, '浩宇', 1, '高一', '清华附中', '英语', '词汇量不足，阅读理解有困难'),
(203, '欣怡', 0, '小学三年级', '中关村一小', '奥数', '想拓展数学思维，备战竞赛'),
(204, '俊杰', 1, '初三', '人大附中', '化学', '临近中考，化学实验题丢分严重'),
(205, '梓涵', 0, '高二', '101中学', '物理', '力学部分掌握不好'),
(206, '宇轩', 1, '小学五年级', '实验二小', '英语', '口语不敢开口'),
(207, '雨桐', 0, '初一', '八一学校', '地理,历史', '文科背诵困难'),
(208, '子轩', 1, '高三', '十一学校', '数学', '冲刺140分'),
(209, '晨曦', 0, '小学四年级', '史家小学', '语文', '作文流水账'),
(210, '浩然', 1, '初二', '四中', '生物', '对此学科不感兴趣');

-- ----------------------------
-- 7. 需求发布 (demand_post)
-- ----------------------------
INSERT INTO `demand_post` (publisher_id, student_id, title, subject, grade, expect_price, teach_mode, longitude, latitude, address, detail, status) VALUES
-- 待匹配
(201, 1, '初二数学急需辅导', '数学', '初二', 150.00, 1, 116.310000, 39.990000, '北京市海淀区中关村', '孩子数学基础薄弱，希望找有耐心的老师', 1),
(202, 2, '高一英语提分', '英语', '高一', 120.00, 2, 116.320000, 39.980000, '北京市海淀区清华园', '主要辅导阅读和写作', 1),
(203, 3, '小学奥数启蒙', '数学', '小学三年级', 100.00, 3, 116.330000, 39.970000, '北京市海淀区知春路', '寻找有奥数经验的老师', 1),
(204, 4, '中考化学冲刺', '化学', '初三', 180.00, 1, 116.340000, 39.960000, '北京市海淀区万柳', '针对模考错题讲解', 1),
(205, 5, '高二物理力学', '物理', '高二', 200.00, 2, 116.350000, 39.950000, '北京市海淀区五棵松', '物理成绩一直提不上来', 1),
-- 已匹配 (模拟 historic data)
(206, 6, '小学英语口语', '英语', '小学五年级', 80.00, 2, 116.360000, 39.940000, '北京市西城区', '练习口语对话', 2),
(207, 7, '初一地理辅导', '地理', '初一', 90.00, 1, 116.370000, 39.930000, '北京市东城区', '帮助理解地理图像', 2),
(208, 8, '高三数学拔高', '数学', '高三', 250.00, 1, 116.380000, 39.920000, '北京市朝阳区', '目标清北', 2);

-- 绑定匹配教员 (demand_post id 6, 7, 8)
UPDATE `demand_post` SET matched_tutor_id = 101 WHERE id = 8;
UPDATE `demand_post` SET matched_tutor_id = 102 WHERE id = 6;
UPDATE `demand_post` SET matched_tutor_id = 109 WHERE id = 7;

-- ----------------------------
-- 8. 课程订单 (course_order)
-- ----------------------------
-- 订单1：已完成 (张学霸 教 子轩 数学)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status, pay_time, pay_type) VALUES
('ORD20260101001', 208, 8, 101, 1, 8, '数学', '高三', 1, 250.00, 10, 2500.00, 250.00, 2250.00, 10, 3, DATE_SUB(NOW(), INTERVAL 10 DAY), 1);

-- 订单2：进行中 (李娜 教 宇轩 英语)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status, pay_time, pay_type) VALUES
('ORD20260101002', 206, 6, 102, 2, 6, '英语', '小学五年级', 2, 80.00, 20, 1600.00, 160.00, 1440.00, 5, 2, DATE_SUB(NOW(), INTERVAL 5 DAY), 2);

-- 订单3：待支付 (冯涛 教 雨桐 地理)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status) VALUES
('ORD20260101003', 207, 7, 109, 9, 7, '地理', '初一', 1, 90.00, 10, 900.00, 90.00, 810.00, 0, 0);

-- ----------------------------
-- 9. 评价 (sys_comment)
-- ----------------------------
INSERT INTO `sys_comment` (order_id, from_user_id, to_user_id, score, content, tags) VALUES
(1, 208, 101, 5, '张老师非常有水平，孩子数学成绩提高很快！', '知识渊博,教学严谨');

-- ----------------------------
-- 10. 给钱包充值和流水
-- ----------------------------
-- 为订单1的教员加钱
UPDATE `sys_wallet` SET balance = balance + 2250.00 WHERE user_id = 101;
INSERT INTO `sys_transaction_flow` (user_id, amount, balance_after, flow_type, order_id, remark) VALUES
(101, 2250.00, 2250.00, 3, 1, '订单ORD20260101001课时费结算');

-- 为订单1的家长减钱 (假设之前充值了足够钱)
INSERT INTO `sys_transaction_flow` (user_id, amount, balance_after, flow_type, order_id, remark) VALUES
(208, -2500.00, 2500.00, 2, 1, '支付订单ORD20260101001');

-- ----------------------------
-- 11. 教学记录 (teaching_record)
-- ----------------------------
INSERT INTO `teaching_record` (order_id, lesson_index, start_time, end_time, content_summary, status) VALUES
(1, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 10 DAY), INTERVAL 2 HOUR), '导数基础', 1),
(1, 2, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 9 DAY), INTERVAL 2 HOUR), '导数应用', 1),
(2, 1, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 4 DAY), INTERVAL 1 HOUR), '一般现在时讲解', 1);
