package com.campus.module.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.llm.entity.UserProfileAi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserProfileAiMapper extends BaseMapper<UserProfileAi> {

    @Select("SELECT * FROM user_profile_ai WHERE user_id = #{userId}")
    UserProfileAi findByUserId(@Param("userId") Long userId);
}
