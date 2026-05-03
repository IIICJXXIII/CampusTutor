package com.campus.module.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.llm.entity.PromptTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {

    @Select("SELECT * FROM prompt_template WHERE scene = #{scene} AND is_active = 1 LIMIT 1")
    PromptTemplate findActiveByScene(@Param("scene") String scene);

    @Select("SELECT * FROM prompt_template WHERE is_active = 1 ORDER BY created_time DESC")
    List<PromptTemplate> findAllActive();
}
