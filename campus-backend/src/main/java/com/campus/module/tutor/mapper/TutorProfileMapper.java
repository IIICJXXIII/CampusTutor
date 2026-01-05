package com.campus.module.tutor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.tutor.entity.TutorProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教员档案Mapper
 */
@Mapper
public interface TutorProfileMapper extends BaseMapper<TutorProfile> {
}
