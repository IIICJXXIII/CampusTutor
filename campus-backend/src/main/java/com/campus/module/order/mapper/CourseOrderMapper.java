package com.campus.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.order.entity.CourseOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 课程订单Mapper
 */
@Mapper
public interface CourseOrderMapper extends BaseMapper<CourseOrder> {

    // ============ 协同过滤相关查询 ============

    /**
     * 查找用户已完成订单的教员ID列表
     * 状态：3=已完成
     */
    @Select("SELECT DISTINCT tutor_id FROM course_order " +
            "WHERE parent_id = #{parentId} AND status = 3")
    List<Long> findOrderedTutorsByParent(@Param("parentId") Long parentId);

    /**
     * 查找有共同订单教员的其他家长
     * 用于发现相似用户
     */
    @Select("SELECT DISTINCT b.parent_id FROM course_order a " +
            "INNER JOIN course_order b ON a.tutor_id = b.tutor_id " +
            "WHERE a.parent_id = #{parentId} AND b.parent_id != #{parentId} " +
            "AND a.status = 3 AND b.status = 3")
    List<Long> findParentsWithCommonTutors(@Param("parentId") Long parentId);

    /**
     * 统计两个家长的共同订单教员数量
     */
    @Select("SELECT COUNT(DISTINCT a.tutor_id) FROM course_order a " +
            "INNER JOIN course_order b ON a.tutor_id = b.tutor_id " +
            "WHERE a.parent_id = #{parentA} AND b.parent_id = #{parentB} " +
            "AND a.status = 3 AND b.status = 3")
    int countCommonTutors(@Param("parentA") Long parentA, @Param("parentB") Long parentB);

    /**
     * 获取用户的所有订单历史（含教员和科目信息）
     * 用于构建用户评分向量
     */
    @Select("SELECT tutor_id, subject, grade, status, " +
            "CASE WHEN status = 3 THEN 1.0 " + // 完成
            "     WHEN status = 2 THEN 0.8 " + // 进行中
            "     WHEN status = 1 THEN 0.6 " + // 已支付
            "     ELSE 0.3 END as score " +
            "FROM course_order WHERE parent_id = #{parentId}")
    List<Map<String, Object>> getUserOrderHistory(@Param("parentId") Long parentId);
}
