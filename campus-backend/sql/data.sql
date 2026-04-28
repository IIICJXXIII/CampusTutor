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
('subject', '钢琴/乐器陪练', 'piano_instrument', 1),
('subject', '美术/书法', 'art_calligraphy', 2),
('subject', '声乐/视唱练耳', 'vocal_solfege', 3),
('subject', '中考体育专项', 'pe_exam', 4),
('subject', '羽毛球/网球陪练', 'badminton_tennis', 5),
('subject', '篮球/足球指导', 'basketball_football', 6),
('subject', '少儿编程(Scratch/Python)', 'kids_coding', 7),
('subject', '机器人/3D打印', 'robot_3dprint', 8),
('subject', '科学实验/航模', 'science_aeromodel', 9),
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
(102, '13800138102', '47ec2dd791e31e2ef2076caf64ed9b3d', '李音音', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka'),
(103, '13800138103', '47ec2dd791e31e2ef2076caf64ed9b3d', '王科创', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Bob'),
(104, '13800138104', '47ec2dd791e31e2ef2076caf64ed9b3d', '赵画画', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Cathy'),
(105, '13800138105', '47ec2dd791e31e2ef2076caf64ed9b3d', '孙全科', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=David'),
(106, '13800138106', '47ec2dd791e31e2ef2076caf64ed9b3d', '周编程', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Eva'),
(107, '13800138107', '47ec2dd791e31e2ef2076caf64ed9b3d', '吴书法', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Frank'),
(108, '13800138108', '47ec2dd791e31e2ef2076caf64ed9b3d', '郑声乐', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Grace'),
(109, '13800138109', '47ec2dd791e31e2ef2076caf64ed9b3d', '冯体育', 1, 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Harry'),
(110, '13800138110', '47ec2dd791e31e2ef2076caf64ed9b3d', '陈实验', 1, 1, 2, 'https://api.dicebear.com/7.x/avataaars/svg?seed=Ivy'),
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
(101, '张伟', '北京大学', '计算机科学', 2, '["少儿编程(Scratch/Python)","机器人/3D打印"]', '["初一","初二","初三","高一"]', 150.00, 116.310003, 39.991957, '北京市海淀区颐和园路5号', 2, 4.9, '编程竞赛金牌得主，擅长逻辑思维培养', '严谨细致，深入浅出', 1, 1),
(102, '李娜', '中央音乐学院', '钢琴表演', 3, '["钢琴/乐器陪练"]', '["小学一年级","小学二年级","小学三年级","小学四年级","小学五年级","小学六年级","初一","初二"]', 120.00, 116.315055, 39.957583, '北京市海淀区西三环北路2号', 2, 4.8, '钢琴十级，有两年少儿钢琴教学经验', '活泼生动，寓教于乐', 1, 1),
(103, '王强', '清华大学', '机械工程', 5, '["科学实验/航模","少儿编程(Scratch/Python)"]', '["高一","高二","高三"]', 200.00, 116.326759, 40.003304, '北京市海淀区双清路30号', 2, 5.0, '科创竞赛指导老师，擅长项目式教学', '重点突出，动手能力培养', 0, 1),
(104, '赵敏', '中央美术学院', '国画', 2, '["美术/书法","声乐/视唱练耳"]', '["初三","高一"]', 100.00, 116.372551, 39.965874, '北京市海淀区新街口外大街19号', 2, 4.7, '美院硕士，擅长国画书法启蒙和考级辅导', '亲切随和，循循善诱', 1, 0),
(105, '孙浩', '北京体育大学', '体育教育', 3, '["篮球/足球指导","羽毛球/网球陪练"]', '["小学一年级","小学二年级","小学三年级","小学四年级","小学五年级","小学六年级"]', 90.00, 116.316833, 39.971556, '北京市海淀区中关村大街59号', 1, 4.5, '喜欢孩子，有耐心，善于沟通', '轻松愉快', 1, 1),
(106, '周婷', '北京理工大学', '软件工程', 3, '["少儿编程(Scratch/Python)"]', '["初一","初二","高一","高二"]', 130.00, 116.321111, 39.960000, '北京市海淀区中关村南大街5号', 2, 4.8, '信息学奥赛辅导经验丰富，Scratch/Python教学', '注重基础，稳扎稳打', 1, 1),
(107, '吴刚', '中央美术学院', '书法', 4, '["美术/书法","钢琴/乐器陪练"]', '["小学一年级","小学二年级","小学三年级","小学四年级","小学五年级","小学六年级","初一"]', 110.00, 116.300000, 39.980000, '北京市海淀区', 2, 4.6, '书法功底深厚，硬笔软笔均可教学', '耐心引导，兴趣激发', 1, 1),
(108, '郑洁', '中国音乐学院', '声乐', 3, '["声乐/视唱练耳","美术/书法"]', '["初三","高三"]', 140.00, 116.350000, 39.970000, '北京市昌平区', 2, 4.9, '声乐专业硕士，视唱练耳考级辅导专家', '条理清晰', 0, 1),
(109, '冯涛', '北京体育大学', '运动训练', 4, '["中考体育专项"]', '["初一","初二","高一"]', 120.00, 116.340000, 39.985000, '北京市海淀区学院路', 2, 4.7, '中考体育满分指导，专项训练经验丰富', '因材施教，科学训练', 1, 0),
(110, '陈静', '北京航空航天大学', '航空宇航', 3, '["科学实验/航模","机器人/3D打印"]', '["高一","高二"]', 110.00, 116.360000, 40.010000, '北京市海淀区清华东路', 2, 4.8, '航模竞赛获奖者，科学实验教学能力强', '细致入微', 1, 1);

-- 更多教员数据，模拟不同状态 (待审核, 拒绝等)
INSERT INTO `tutor_profile` (user_id, real_name, university_name, major, education, teach_subjects, teach_grades, expect_price, longitude, latitude, address, cert_status, rating) VALUES
(111, '林风', '清华大学', '计算机', 3, '["少儿编程(Scratch/Python)","机器人/3D打印"]', '["初一","初二","初三"]', 180.00, 116.335000, 39.992000, '北京市海淀区五道口', 0, 0.0), -- 待提交
(112, '黄芸', '中央音乐学院', '音乐教育', 2, '["钢琴/乐器陪练","声乐/视唱练耳"]', '["小学一年级","小学二年级","小学三年级","小学四年级","小学五年级","小学六年级"]', 100.00, 116.348000, 39.978000, '北京市西城区西直门', 1, 0.0), -- 待审核
(113, '刘星', '同济大学', '机械工程', 4, '["科学实验/航模"]', '["高一","高二","高三"]', 200.00, 116.355000, 39.968000, '北京市朝阳区望京', 3, 0.0), -- 已拒绝
(114, '张月', '中央音乐学院', '钢琴', 3, '["钢琴/乐器陪练"]', '["初一","初二","初三"]', 120.00, 116.380000, 39.940000, '北京市朝阳区国贸', 2, 5.0),
(115, '徐阳', '浙江大学', '计算机', 4, '["少儿编程(Scratch/Python)"]', '["高一","高二","高三"]', 160.00, 116.290000, 39.960000, '北京市海淀区苏州街', 2, 4.8);


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
(201, '子涵', 1, '初二', '北大附中', '少儿编程(Scratch/Python),机器人/3D打印', '对编程很感兴趣，想系统学习'),
(202, '浩宇', 1, '高一', '清华附中', '钢琴/乐器陪练', '学钢琴两年，准备考级'),
(203, '欣怡', 0, '小学三年级', '中关村一小', '美术/书法', '喜欢画画，想学国画和书法'),
(204, '俊杰', 1, '初三', '人大附中', '中考体育专项', '临近中考，体育项目需要专项训练'),
(205, '梓涵', 0, '高二', '101中学', '科学实验/航模', '对航模和科学实验很感兴趣'),
(206, '宇轩', 1, '小学五年级', '实验二小', '篮球/足球指导', '想提高篮球技术'),
(207, '雨桐', 0, '初一', '八一学校', '声乐/视唱练耳,美术/书法', '想发展艺术特长'),
(208, '子轩', 1, '高三', '十一学校', '少儿编程(Scratch/Python)', '准备信息学竞赛'),
(209, '晨曦', 0, '小学四年级', '史家小学', '钢琴/乐器陪练', '刚开始学钢琴，需要陪练'),
(210, '浩然', 1, '初二', '四中', '羽毛球/网球陪练', '想培养运动爱好');

-- ----------------------------
-- 7. 需求发布 (demand_post)
-- ----------------------------
INSERT INTO `demand_post` (publisher_id, student_id, title, subject, grade, expect_price, teach_mode, longitude, latitude, address, detail, status) VALUES
-- 待匹配
(201, 1, '初二编程入门辅导', '少儿编程(Scratch/Python)', '初二', 150.00, 1, 116.310000, 39.990000, '北京市海淀区中关村', '孩子对编程感兴趣，希望系统学习Python', 1),
(202, 2, '高一钢琴考级陪练', '钢琴/乐器陪练', '高一', 120.00, 2, 116.320000, 39.980000, '北京市海淀区清华园', '准备钢琴八级考试，需要陪练指导', 1),
(203, 3, '小学美术书法启蒙', '美术/书法', '小学三年级', 100.00, 3, 116.330000, 39.970000, '北京市海淀区知春路', '寻找有国画书法教学经验的老师', 1),
(204, 4, '中考体育专项冲刺', '中考体育专项', '初三', 180.00, 1, 116.340000, 39.960000, '北京市海淀区万柳', '针对中考体育各项目专项训练', 1),
(205, 5, '高二科学实验航模', '科学实验/航模', '高二', 200.00, 2, 116.350000, 39.950000, '北京市海淀区五棵松', '想参加航模比赛，需要指导', 1),
-- 已匹配 (模拟 historic data)
(206, 6, '小学篮球启蒙', '篮球/足球指导', '小学五年级', 80.00, 2, 116.360000, 39.940000, '北京市西城区', '培养篮球兴趣和基本功', 2),
(207, 7, '初一声乐入门', '声乐/视唱练耳', '初一', 90.00, 1, 116.370000, 39.930000, '北京市东城区', '零基础学习声乐和视唱练耳', 2),
(208, 8, '高三编程竞赛冲刺', '少儿编程(Scratch/Python)', '高三', 250.00, 1, 116.380000, 39.920000, '北京市朝阳区', '目标信息学竞赛省一', 2);

-- 绑定匹配教员 (demand_post id 6, 7, 8)
UPDATE `demand_post` SET matched_tutor_id = 101 WHERE id = 8;
UPDATE `demand_post` SET matched_tutor_id = 105 WHERE id = 6;
UPDATE `demand_post` SET matched_tutor_id = 108 WHERE id = 7;

-- ----------------------------
-- 8. 课程订单 (course_order)
-- ----------------------------
-- 订单1：已完成 (张学霸 教 子轩 数学)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status, pay_time, pay_type) VALUES
('ORD20260101001', 208, 8, 101, 1, 8, '少儿编程(Scratch/Python)', '高三', 1, 250.00, 10, 2500.00, 250.00, 2250.00, 10, 3, DATE_SUB(NOW(), INTERVAL 10 DAY), 1);

-- 订单2：进行中 (李娜 教 宇轩 英语)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status, pay_time, pay_type) VALUES
('ORD20260101002', 206, 6, 105, 5, 6, '篮球/足球指导', '小学五年级', 2, 80.00, 20, 1600.00, 160.00, 1440.00, 5, 2, DATE_SUB(NOW(), INTERVAL 5 DAY), 2);

-- 订单3：待支付 (冯涛 教 雨桐 地理)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status) VALUES
('ORD20260101003', 207, 7, 108, 8, 7, '声乐/视唱练耳', '初一', 1, 90.00, 10, 900.00, 90.00, 810.00, 0, 0);

-- ----------------------------
-- 9. 评价 (sys_comment)
-- ----------------------------
INSERT INTO `sys_comment` (order_id, from_user_id, to_user_id, score, content, tags) VALUES
(1, 208, 101, 5, '张老师非常有水平，孩子编程能力提高很快！', '知识渊博,教学严谨');

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
(1, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 10 DAY), INTERVAL 2 HOUR), 'Python基础语法', 1),
(1, 2, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 9 DAY), INTERVAL 2 HOUR), 'Python函数与模块', 1),
(1, 3, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 8 DAY), INTERVAL 2 HOUR), 'Python列表和字典', 1),
(1, 4, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 7 DAY), INTERVAL 2 HOUR), 'Python面向对象', 1),
(1, 5, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 6 DAY), INTERVAL 2 HOUR), 'Python文件操作', 1),
(2, 1, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 4 DAY), INTERVAL 1 HOUR), '篮球运球基本功', 1),
(2, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 3 DAY), INTERVAL 1 HOUR), '篮球投篮技巧', 1),
(2, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 2 DAY), INTERVAL 1 HOUR), '篮球传球配合', 1);

-- ----------------------------
-- 12. 更多课程订单 (丰富数据展示DeepFM推荐效果)
-- ----------------------------
-- 订单4：已完成 (李娜 教 晨曦 钢琴陪练)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status, pay_time, pay_type) VALUES
('ORD20260115001', 209, 9, 102, 2, NULL, '钢琴/乐器陪练', '小学四年级', 1, 120.00, 8, 960.00, 96.00, 864.00, 8, 3, DATE_SUB(NOW(), INTERVAL 20 DAY), 1);

-- 订单5：已完成 (赵敏 教 欣怡 美术书法)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status, pay_time, pay_type) VALUES
('ORD20260120001', 203, 3, 104, 4, 3, '美术/书法', '小学三年级', 1, 100.00, 12, 1200.00, 120.00, 1080.00, 12, 3, DATE_SUB(NOW(), INTERVAL 15 DAY), 2);

-- 订单6：进行中 (冯涛 教 俊杰 中考体育)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status, pay_time, pay_type) VALUES
('ORD20260201001', 204, 4, 109, 9, 4, '中考体育专项', '初三', 1, 120.00, 20, 2400.00, 240.00, 2160.00, 8, 2, DATE_SUB(NOW(), INTERVAL 7 DAY), 1);

-- 订单7：已完成 (王科创 教 梓涵 科学实验)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status, pay_time, pay_type) VALUES
('ORD20260205001', 205, 5, 103, 3, 5, '科学实验/航模', '高二', 2, 200.00, 6, 1200.00, 120.00, 1080.00, 6, 3, DATE_SUB(NOW(), INTERVAL 12 DAY), 1);

-- 订单8：进行中 (周编程 教 子涵 少儿编程)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status, pay_time, pay_type) VALUES
('ORD20260210001', 201, 1, 106, 6, 1, '少儿编程(Scratch/Python)', '初二', 1, 130.00, 15, 1950.00, 195.00, 1755.00, 4, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), 2);

-- 订单9：已完成 (吴书法 教 203欣怡 书法)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status, pay_time, pay_type) VALUES
('ORD20260212001', 203, 3, 107, 7, NULL, '美术/书法', '小学三年级', 1, 110.00, 10, 1100.00, 110.00, 990.00, 10, 3, DATE_SUB(NOW(), INTERVAL 25 DAY), 1);

-- 订单10：已完成 (陈实验 教 205梓涵 机器人3D打印)
INSERT INTO `course_order` (order_no, parent_id, student_id, tutor_id, tutor_profile_id, demand_id, subject, grade, teach_mode, unit_price, total_hours, total_amount, service_fee, tutor_amount, used_hours, status, pay_time, pay_type) VALUES
('ORD20260215001', 205, 5, 110, 10, NULL, '机器人/3D打印', '高二', 2, 110.00, 8, 880.00, 88.00, 792.00, 8, 3, DATE_SUB(NOW(), INTERVAL 18 DAY), 1);

-- ----------------------------
-- 13. 更多评价 (丰富推荐系统数据源)
-- ----------------------------
INSERT INTO `sys_comment` (order_id, from_user_id, to_user_id, score, content, tags) VALUES
(4, 209, 102, 5, '李老师非常专业，孩子钢琴进步很快！', '耐心,专业,有经验'),
(5, 203, 104, 5, '赵老师教国画特别好，孩子很喜欢！', '亲切,专业,寓教于乐'),
(7, 205, 103, 5, '王老师科学实验课太棒了，孩子爱上了科创！', '知识渊博,动手能力强,课程有趣'),
(9, 203, 107, 4, '吴老师书法功底深厚，孩子进步明显', '功底扎实,教学认真'),
(10, 205, 110, 5, '陈老师3D打印课程很有创意', '课程新颖,有耐心');

-- 补充教员订单计数和钱包
UPDATE `tutor_profile` SET order_count = 2 WHERE user_id = 101;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 102;
UPDATE `tutor_profile` SET order_count = 2 WHERE user_id = 103;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 104;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 105;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 106;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 107;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 108;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 109;
UPDATE `tutor_profile` SET order_count = 1 WHERE user_id = 110;

-- 补充更多教学记录
INSERT INTO `teaching_record` (order_id, lesson_index, start_time, end_time, content_summary, status) VALUES
(4, 1, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 20 DAY), INTERVAL 1 HOUR), '钢琴基础指法练习', 1),
(4, 2, DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 19 DAY), INTERVAL 1 HOUR), '简易乐曲演奏', 1),
(5, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 15 DAY), INTERVAL 2 HOUR), '国画基础: 笔墨纸砚', 1),
(5, 2, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 14 DAY), INTERVAL 2 HOUR), '山水画入门', 1),
(6, 1, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 7 DAY), INTERVAL 1 HOUR), '中考体育: 跑步热身与技巧', 1),
(6, 2, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 6 DAY), INTERVAL 1 HOUR), '中考体育: 引体向上训练', 1),
(7, 1, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 12 DAY), INTERVAL 2 HOUR), '航模原理与结构', 1),
(7, 2, DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 11 DAY), INTERVAL 2 HOUR), '航模组装实操', 1),
(8, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 3 DAY), INTERVAL 2 HOUR), 'Scratch图形化编程入门', 1),
(8, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 2 DAY), INTERVAL 2 HOUR), 'Scratch动画与游戏制作', 1);

-- ----------------------------
-- 14. 更多需求 (让教师端"找学生"页面有丰富数据)
-- ----------------------------
INSERT INTO `demand_post` (publisher_id, student_id, title, subject, grade, expect_price, teach_mode, longitude, latitude, address, detail, status) VALUES
(209, 9, '小学钢琴启蒙', '钢琴/乐器陪练', '小学四年级', 100.00, 1, 116.325000, 39.975000, '北京市东城区东直门', '孩子刚开始学钢琴，需要有耐心的老师', 1),
(210, 10, '初二羽毛球训练', '羽毛球/网球陪练', '初二', 80.00, 1, 116.345000, 39.980000, '北京市海淀区中关村', '培养运动特长，每周2-3次', 1),
(211, NULL, '声乐考级辅导', '声乐/视唱练耳', '初三', 130.00, 3, 116.362000, 39.955000, '北京市西城区金融街', '准备声乐考级，需要有经验的老师', 1),
(212, NULL, '中考体育训练', '中考体育专项', '初三', 100.00, 1, 116.318000, 39.998000, '北京市海淀区北大', '体育分不理想，急需提升', 1),
(213, NULL, '机器人编程入门', '机器人/3D打印', '初一', 150.00, 2, 116.370000, 39.945000, '北京市朝阳区三里屯', '对机器人充满兴趣', 1),
(214, NULL, '篮球基本功训练', '篮球/足球指导', '小学六年级', 70.00, 1, 116.310000, 39.965000, '北京市海淀区万泉河路', '培养孩子运动习惯', 1),
(215, NULL, '书法硬笔入门', '美术/书法', '小学二年级', 80.00, 1, 116.395000, 39.920000, '北京市朝阳区望京', '写字不好看，想练硬笔', 1);

-- ----------------------------
-- 15. 补充排课数据 (让更多教师有排课展示)
-- ----------------------------
INSERT INTO `tutor_schedule_config` (tutor_id, day_of_week, start_time, end_time, available) VALUES
(4, 1, '14:00', '17:00', 1), (4, 3, '14:00', '17:00', 1), (4, 6, '09:00', '17:00', 1), -- 赵敏
(5, 2, '16:00', '18:00', 1), (5, 4, '16:00', '18:00', 1), (5, 6, '08:00', '12:00', 1), (5, 7, '08:00', '12:00', 1), -- 孙浩
(6, 1, '19:00', '21:00', 1), (6, 3, '19:00', '21:00', 1), (6, 5, '19:00', '21:00', 1), -- 周婷
(7, 2, '15:00', '18:00', 1), (7, 4, '15:00', '18:00', 1), (7, 7, '09:00', '12:00', 1), -- 吴刚
(8, 6, '10:00', '12:00', 1), (8, 6, '14:00', '17:00', 1), (8, 7, '10:00', '17:00', 1), -- 郑洁
(9, 1, '06:00', '08:00', 1), (9, 3, '06:00', '08:00', 1), (9, 5, '06:00', '08:00', 1), (9, 6, '06:00', '10:00', 1), -- 冯涛
(10, 2, '18:00', '21:00', 1), (10, 4, '18:00', '21:00', 1), (10, 7, '14:00', '18:00', 1); -- 陈静
