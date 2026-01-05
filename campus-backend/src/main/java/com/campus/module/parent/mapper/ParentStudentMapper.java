package com.campus.module.parent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.parent.entity.ParentStudent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 家长学生信息Mapper
 */
@Mapper
public interface ParentStudentMapper extends BaseMapper<ParentStudent> {
}
