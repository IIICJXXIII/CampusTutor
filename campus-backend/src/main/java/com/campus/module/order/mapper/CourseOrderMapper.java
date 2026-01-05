package com.campus.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.order.entity.CourseOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程订单Mapper
 */
@Mapper
public interface CourseOrderMapper extends BaseMapper<CourseOrder> {
}
