ALTER TABLE `course_order`
  ADD COLUMN `payment_mode` varchar(20) DEFAULT 'full' COMMENT '支付模式: full-一次性支付, per_lesson-按课时支付',
  ADD COLUMN `paid_hours` int DEFAULT 0 COMMENT '已支付课时数';

ALTER TABLE `teaching_record`
  ADD COLUMN `pay_status` tinyint DEFAULT 0 COMMENT '支付状态: 0-未支付, 1-已支付',
  ADD COLUMN `pay_time` datetime DEFAULT NULL COMMENT '支付时间';
