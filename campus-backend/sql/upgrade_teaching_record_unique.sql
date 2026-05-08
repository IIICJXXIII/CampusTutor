-- 修复：为 teaching_record 表添加唯一约束，防止重复课时记录
-- 问题：支付并发时 generateTeachingRecords() 可能被多次调用，产生重复的 (order_id, lesson_index) 记录

-- 1. 清理已存在的重复数据（保留每组中 id 最小的记录）
DELETE t1 FROM teaching_record t1
INNER JOIN teaching_record t2
WHERE t1.id > t2.id AND t1.order_id = t2.order_id AND t1.lesson_index = t2.lesson_index;

-- 2. 添加唯一约束
ALTER TABLE teaching_record ADD UNIQUE KEY uk_order_lesson (order_id, lesson_index);
