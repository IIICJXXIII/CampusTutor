package com.campus.module.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.teaching.entity.TeachingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课时打卡记录 Mapper
 */
@Mapper
public interface TeachingRecordMapper extends BaseMapper<TeachingRecord> {

    /**
     * 查询订单下所有课时记录
     */
    @Select("SELECT * FROM teaching_record WHERE order_id = #{orderId} ORDER BY lesson_index ASC")
    List<TeachingRecord> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 查询订单下最新一条课时记录
     */
    @Select("SELECT * FROM teaching_record WHERE order_id = #{orderId} ORDER BY lesson_index DESC LIMIT 1")
    TeachingRecord selectLatestByOrderId(@Param("orderId") Long orderId);

    /**
     * 统计订单已完成课时数（家长已确认）
     */
    @Select("SELECT COUNT(*) FROM teaching_record WHERE order_id = #{orderId} AND status = 3")
    int countConfirmedByOrderId(@Param("orderId") Long orderId);
}
