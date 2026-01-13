-- ==========================================
-- 协同过滤算法测试数据 (完整修正版)
-- 场景：验证基于用户的协同过滤推荐效果
-- 修复问题：
-- 1. teach_grades 必须包含具体的年级(如"高二")或"高中全科"，不能只写"高中"
-- 2. 否则 GradeUtils 生成的搜索关键词无法匹配
-- ==========================================

USE campus_tutor_db;

-- 1. 创建测试用户（家长）
INSERT INTO sys_user (id, username, password, nickname, role, status, avatar, create_time) VALUES
(1001, 'cf_test_p1', '47ec2dd791e31e2ef2076caf64ed9b3d', '数学家长A', 2, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=P1', NOW()),
(1002, 'cf_test_p2', '47ec2dd791e31e2ef2076caf64ed9b3d', '数学家长B', 2, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=P2', NOW()),
(1003, 'cf_test_p3', '47ec2dd791e31e2ef2076caf64ed9b3d', '英语家长C', 2, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=P3', NOW())
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), status = VALUES(status), avatar = VALUES(avatar);

-- 2. 创建测试用户（教员关联账号）
INSERT INTO sys_user (id, username, password, nickname, role, status, avatar, create_time) VALUES
(2001, 'cf_tutor_a', '47ec2dd791e31e2ef2076caf64ed9b3d', '数学教员A', 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=T1', NOW()),
(2002, 'cf_tutor_b', '47ec2dd791e31e2ef2076caf64ed9b3d', '数学教员B', 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=T2', NOW()),
(2003, 'cf_tutor_c', '47ec2dd791e31e2ef2076caf64ed9b3d', '数学教员C', 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=T3', NOW()),
(2004, 'cf_tutor_d', '47ec2dd791e31e2ef2076caf64ed9b3d', '英语教员D', 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=T4', NOW()),
(2005, 'cf_tutor_e', '47ec2dd791e31e2ef2076caf64ed9b3d', '英语教员E', 1, 1, 'https://api.dicebear.com/7.x/avataaars/svg?seed=T5', NOW())
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), status = VALUES(status), avatar = VALUES(avatar);

-- 3. 创建教员档案
-- 修复：teach_grades 从 ["高中"] 改为 ["高一","高二","高三"] 或 ["高中全科"]
INSERT INTO tutor_profile (
    id, user_id, real_name, university_name, major, education, 
    teach_subjects, teach_grades, expect_price, rating, order_count,
    cert_status, create_time, update_time,
    longitude, latitude, address,
    introduction, teach_style, can_visit, can_online
) VALUES
(2001, 2001, '张数学', '清华大学', '数学系', 2,
 '["数学"]', '["高一","高二","高三"]', 200.00, 5.0, 10,
 2, NOW(), NOW(),
 116.310003, 39.991957, '北京市海淀区清华大学',
 '清华数学系学霸，奥赛金牌', '逻辑严密，解题快', 1, 1
),
(2002, 2002, '李代数', '北京大学', '数学系', 3,
 '["数学"]', '["高一","高二","高三"]', 180.00, 4.8, 8,
 2, NOW(), NOW(),
 116.326759, 40.003304, '北京市海淀区北京大学',
 '北大数院研究生，擅长代数方向', '深入浅出，耐心', 1, 1
),
(2003, 2003, '王几何', '复旦大学', '数学系', 4,
 '["数学"]', '["高一","高二","高三"]', 190.00, 4.9, 5,
 2, NOW(), NOW(),
 116.350000, 39.970000, '北京市海淀区五道口',
 '复旦高材生，几何直观教学法', '幽默风趣', 1, 1
),
(2004, 2004, '赵英语', '北外', '英语系', 2,
 '["英语"]', '["高一","高二","高三"]', 150.00, 4.7, 12,
 2, NOW(), NOW(),
 116.300000, 39.980000, '北京市海淀区中关村',
 '北外英语专业，专八通过', '全英教学，互动多', 1, 1
),
(2005, 2005, '钱口语', '上外', '英语系', 3,
 '["英语"]', '["高一","高二","高三"]', 160.00, 4.8, 6,
 2, NOW(), NOW(),
 116.340000, 39.960000, '北京市海淀区知春路',
 '口语流利，有海外交换经验', '注重发音', 1, 1
)
ON DUPLICATE KEY UPDATE 
    expect_price = VALUES(expect_price), 
    teach_subjects = VALUES(teach_subjects),
    teach_grades = VALUES(teach_grades),  -- 更新年级字段
    introduction = VALUES(introduction),
    update_time = NOW();

-- 4. 创建订单数据
INSERT INTO course_order (id, order_no, parent_id, tutor_id, subject, grade, unit_price, total_amount, status, create_time, update_time) VALUES
(3001, 'ORDER_CF_01', 1001, 2001, '数学', '高一', 200.00, 2000.00, 3, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
(3002, 'ORDER_CF_02', 1001, 2002, '数学', '高一', 180.00, 1800.00, 3, DATE_SUB(NOW(), INTERVAL 9 DAY), NOW()),
(3003, 'ORDER_CF_03', 1002, 2001, '数学', '高二', 200.00, 2000.00, 3, DATE_SUB(NOW(), INTERVAL 8 DAY), NOW()),
(3004, 'ORDER_CF_04', 1002, 2002, '数学', '高二', 180.00, 1800.00, 3, DATE_SUB(NOW(), INTERVAL 7 DAY), NOW()),
(3005, 'ORDER_CF_05', 1002, 2003, '数学', '高二', 190.00, 1900.00, 3, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()),
(3006, 'ORDER_CF_06', 1003, 2004, '英语', '高一', 150.00, 1500.00, 3, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
(3007, 'ORDER_CF_07', 1003, 2005, '英语', '高一', 160.00, 1600.00, 3, DATE_SUB(NOW(), INTERVAL 4 DAY), NOW())
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 5. 创建行为日志
INSERT INTO user_action_log (user_id, target_id, action_type, duration, create_time) VALUES
(1001, 2003, 1, 60, NOW()),
(1002, 2003, 3, 0, NOW()),
(1001, 2001, 1, 30, NOW()),
(1001, 2002, 4, 0, NOW()), 
(1001, 2001, 1, 120, NOW()),
(1002, 2001, 4, 0, NOW()),
(1002, 2002, 1, 45, NOW());
