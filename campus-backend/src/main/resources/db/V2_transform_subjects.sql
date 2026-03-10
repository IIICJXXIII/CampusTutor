-- =====================================================
-- V2: 平台素质教育转型 —— 科目字典重构 + 需求表字段扩展
-- 执行前请备份数据！
-- =====================================================

-- 1. demand_post 表新增字段
ALTER TABLE demand_post ADD COLUMN skill_level VARCHAR(32) DEFAULT NULL COMMENT '基础水平：零基础/有基础/考级冲刺';
ALTER TABLE demand_post ADD COLUMN venue_type INT DEFAULT NULL COMMENT '场地类型：1教员上门 2学员上门 3公共场馆';

-- 2. 清空旧学科字典
TRUNCATE TABLE sys_subject;

-- 3. 插入新三类核心数据
-- 艺术类 (ID: 1)
INSERT INTO sys_subject (id, name, parent_id, icon) VALUES (1, '艺术素养', 0, 'palette');
INSERT INTO sys_subject (name, parent_id) VALUES ('钢琴/乐器陪练', 1), ('美术/书法', 1), ('声乐/视唱练耳', 1);

-- 体育类 (ID: 2)
INSERT INTO sys_subject (id, name, parent_id, icon) VALUES (2, '体育健康', 0, 'basketball');
INSERT INTO sys_subject (name, parent_id) VALUES ('中考体育专项', 2), ('羽毛球/网球陪练', 2), ('篮球/足球指导', 2);

-- 科创类 (ID: 3)
INSERT INTO sys_subject (id, name, parent_id, icon) VALUES (3, '科创STEAM', 0, 'cpu');
INSERT INTO sys_subject (name, parent_id) VALUES ('少儿编程(Scratch/Python)', 3), ('机器人/3D打印', 3), ('科学实验/航模', 3);
