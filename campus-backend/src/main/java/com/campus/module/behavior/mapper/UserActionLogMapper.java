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
}
