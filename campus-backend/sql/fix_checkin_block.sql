-- 清理阻塞打卡的旧数据
-- 运行此脚本后，教师可以正常打卡

-- 查看当前所有待确认且有打卡数据的记录（这些会阻塞新打卡）
SELECT id, order_id, lesson_index, status, clock_in_lat, clock_in_lng, clock_in_img, start_time
FROM teaching_record 
WHERE status = 0 
  AND (clock_in_lat IS NOT NULL OR clock_in_img IS NOT NULL);

-- 方案1: 将这些记录状态改为已确认（模拟家长确认）
UPDATE teaching_record 
SET status = 1 
WHERE status = 0 
  AND (clock_in_lat IS NOT NULL OR clock_in_img IS NOT NULL);

-- 方案2: 或者直接删除所有teaching_record，重新开始
-- DELETE FROM teaching_record;

-- 验证：查看修复后的状态
SELECT id, order_id, lesson_index, status, clock_in_lat, clock_in_img 
FROM teaching_record 
ORDER BY order_id, lesson_index
LIMIT 20;
