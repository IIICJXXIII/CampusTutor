package com.campus.module.behavior.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.behavior.entity.UserActionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户行为日志 Mapper
 */
@Mapper
public interface UserActionLogMapper extends BaseMapper<UserActionLog> {

        /**
         * 统计教员在指定时间范围内的各类行为数
         */
        @Select("SELECT action_type, COUNT(*) as count, AVG(duration) as avg_duration " +
                        "FROM user_action_log " +
                        "WHERE target_id = #{tutorId} AND create_time >= #{startTime} " +
                        "GROUP BY action_type")
        List<Map<String, Object>> countTutorActions(@Param("tutorId") Long tutorId,
                        @Param("startTime") LocalDateTime startTime);

        /**
         * 统计用户的搜索次数
         */
        @Select("SELECT COUNT(*) FROM user_action_log " +
                        "WHERE user_id = #{userId} AND action_type = 2")
        int countUserSearches(@Param("userId") Long userId);

        /**
         * 统计教员的总收藏数
         */
        @Select("SELECT COUNT(*) FROM user_action_log " +
                        "WHERE target_id = #{tutorId} AND action_type = 3")
        int countTutorFavorites(@Param("tutorId") Long tutorId);

        /**
         * 获取24小时内被浏览最多的教员ID列表
         */
        @Select("SELECT target_id, COUNT(*) as view_count " +
                        "FROM user_action_log " +
                        "WHERE action_type = 1 AND create_time >= #{startTime} " +
                        "GROUP BY target_id " +
                        "ORDER BY view_count DESC " +
                        "LIMIT #{limit}")
        List<Map<String, Object>> getHotTutorIds(@Param("startTime") LocalDateTime startTime,
                        @Param("limit") int limit);

        // ============ 协同过滤相关查询 ============

        /**
         * 查找与某教员有交互的用户ID列表
         * 交互类型：查看(1)、收藏(3)、聊天(4)
         */
        @Select("SELECT DISTINCT user_id FROM user_action_log " +
                        "WHERE target_id = #{tutorId} AND action_type IN (1, 3, 4)")
        List<Long> findUsersWhoInteractedWith(@Param("tutorId") Long tutorId);

        /**
         * 查找用户交互过的教员ID列表
         */
        @Select("SELECT DISTINCT target_id FROM user_action_log " +
                        "WHERE user_id = #{userId} AND action_type IN (1, 3, 4) AND target_id IS NOT NULL")
        List<Long> findTutorsInteractedByUser(@Param("userId") Long userId);

        /**
         * 统计两个教员的共同交互用户数（协同过滤核心）
         */
        @Select("SELECT COUNT(DISTINCT a.user_id) FROM user_action_log a " +
                        "INNER JOIN user_action_log b ON a.user_id = b.user_id " +
                        "WHERE a.target_id = #{tutorId1} AND b.target_id = #{tutorId2} " +
                        "AND a.action_type IN (1, 3, 4) AND b.action_type IN (1, 3, 4)")
        int countCoInteractions(@Param("tutorId1") Long tutorId1, @Param("tutorId2") Long tutorId2);

        /**
         * 获取与目标教员有共同用户的其他教员及其共同用户数
         * 用于批量计算相似度
         */
        @Select("SELECT b.target_id as tutor_id, COUNT(DISTINCT a.user_id) as co_count " +
                        "FROM user_action_log a " +
                        "INNER JOIN user_action_log b ON a.user_id = b.user_id " +
                        "WHERE a.target_id = #{tutorId} AND b.target_id != #{tutorId} " +
                        "AND a.action_type IN (1, 3, 4) AND b.action_type IN (1, 3, 4) " +
                        "GROUP BY b.target_id " +
                        "ORDER BY co_count DESC " +
                        "LIMIT #{limit}")
        List<Map<String, Object>> findCoInteractedTutors(@Param("tutorId") Long tutorId,
                        @Param("limit") int limit);

        /**
         * 统计与某教员交互的用户总数
         */
        @Select("SELECT COUNT(DISTINCT user_id) FROM user_action_log " +
                        "WHERE target_id = #{tutorId} AND action_type IN (1, 3, 4)")
        int countInteractionUsers(@Param("tutorId") Long tutorId);
}
