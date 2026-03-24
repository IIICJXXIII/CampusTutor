package com.campus.module.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.llm.entity.PromptTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Prompt模板Mapper
 */
@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {
    
    /**
     * 根据场景查询启用的模板
     */
    @Select("SELECT * FROM prompt_template WHERE scene = #{scene} AND is_active = 1 ORDER BY version DESC LIMIT 1")
    PromptTemplate findActiveByScene(@Param("scene") String scene);
    
    /**
     * 根据场景查询所有模板（包括历史版本）
     */
    @Select("SELECT * FROM prompt_template WHERE scene = #{scene} ORDER BY version DESC")
    List<PromptTemplate> findAllByScene(@Param("scene") String scene);
    
    /**
     * 查询所有启用的模板
     */
    @Select("SELECT * FROM prompt_template WHERE is_active = 1 ORDER BY scene, version DESC")
    List<PromptTemplate> findAllActive();
    
    /**
     * 增加使用次数
     */
    @Update("UPDATE prompt_template SET usage_count = usage_count + 1 WHERE id = #{id}")
    int incrementUsageCount(@Param("id") Long id);
    
    /**
     * 更新平均评分
     */
    @Update("UPDATE prompt_template SET average_rating = #{rating} WHERE id = #{id}")
    int updateAverageRating(@Param("id") Long id, @Param("rating") Double rating);
    
    /**
     * 禁用其他版本的模板（当启用新版本时）
     */
    @Update("UPDATE prompt_template SET is_active = 0 WHERE scene = #{scene} AND id != #{excludeId}")
    int disableOtherVersions(@Param("scene") String scene, @Param("excludeId") Long excludeId);
}