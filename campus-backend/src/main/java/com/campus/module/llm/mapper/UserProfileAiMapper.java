package com.campus.module.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.llm.entity.UserProfileAi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * AI用户画像Mapper
 */
@Mapper
public interface UserProfileAiMapper extends BaseMapper<UserProfileAi> {
    
    /**
     * 根据用户ID查询用户画像
     */
    @Select("SELECT * FROM user_profile_ai WHERE user_id = #{userId}")
    UserProfileAi findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色查询用户画像
     */
    @Select("SELECT * FROM user_profile_ai WHERE role = #{role} ORDER BY updated_time DESC LIMIT #{limit}")
    List<UserProfileAi> findByRole(@Param("role") String role, @Param("limit") Integer limit);
    
    /**
     * 根据教学风格查询教员画像
     */
    @Select("SELECT * FROM user_profile_ai WHERE role = 'TEACHER' AND teaching_style = #{teachingStyle} ORDER BY updated_time DESC")
    List<UserProfileAi> findTeachersByTeachingStyle(@Param("teachingStyle") String teachingStyle);
    
    /**
     * 根据擅长科目查询教员画像
     */
    @Select("""
        SELECT * FROM user_profile_ai 
        WHERE role = 'TEACHER' 
        AND expert_subjects LIKE CONCAT('%', #{subject}, '%')
        ORDER BY updated_time DESC
        LIMIT #{limit}
        """)
    List<UserProfileAi> findTeachersBySubject(@Param("subject") String subject, @Param("limit") Integer limit);
    
    /**
     * 根据学生年级查询家长/学生画像
     */
    @Select("SELECT * FROM user_profile_ai WHERE role = 'PARENT' AND student_grade = #{grade} ORDER BY updated_time DESC")
    List<UserProfileAi> findParentsByStudentGrade(@Param("grade") String grade);
    
    /**
     * 更新交互历史摘要
     */
    @Update("UPDATE user_profile_ai SET interaction_summary = #{summary}, updated_time = NOW() WHERE user_id = #{userId}")
    int updateInteractionSummary(@Param("userId") Long userId, @Param("summary") String summary);
    
    /**
     * 更新个性化设置
     */
    @Update("UPDATE user_profile_ai SET personalization_settings = #{settings}, updated_time = NOW() WHERE user_id = #{userId}")
    int updatePersonalizationSettings(@Param("userId") Long userId, @Param("settings") String settings);
}