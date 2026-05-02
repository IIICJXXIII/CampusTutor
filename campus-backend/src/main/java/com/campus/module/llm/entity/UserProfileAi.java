package com.campus.module.llm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_profile_ai")
public class UserProfileAi implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String role;

    private String teachingStyle;

    private String expertSubjects;

    private String teachingExperience;

    private String studentGrade;

    private String learningStyle;

    private String weakSubjects;

    private String learningNeeds;

    private String preferences;

    private String personalizationSettings;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
