-- ============================================
-- 协同过滤相似度缓存表
-- 用于存储用户相似度计算结果，提升查询性能
-- ============================================
use campus_tutor_db;
CREATE TABLE IF NOT EXISTS user_similarity_cache (
    -- 用户A的ID
    user_a_id BIGINT NOT NULL,
    
    -- 用户B的ID（用户A和B可互换）
    user_b_id BIGINT NOT NULL,
    
    -- 相似度分数 (0.0000 ~ 1.0000)
    similarity DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
    
    -- 共同交互的教员数量
    common_items INT NOT NULL DEFAULT 0,
    
    -- 更新时间
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 联合主键（确保每对用户只有一条记录）
    PRIMARY KEY (user_a_id, user_b_id),
    
    -- 索引优化
    INDEX idx_user_a (user_a_id),
    INDEX idx_user_b (user_b_id),
    INDEX idx_similarity (similarity DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户相似度缓存表';

-- ============================================
-- 使用说明：
-- 1. 该表可选，Redis缓存优先
-- 2. 适用于无Redis环境或需要持久化相似度的场景
-- 3. 建议定期清理过期数据：
--    DELETE FROM user_similarity_cache WHERE update_time < DATE_SUB(NOW(), INTERVAL 24 HOUR);
-- ============================================
