package com.campus.module.parent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 家长学生信息表
 */
@Data
@TableName("parent_student")
public class ParentStudent {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 家长用户ID
     */
    private Long parentId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学生性别：0女 1男
     */
    private Integer gender;

    /**
     * 年级
     */
    private String grade;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 薄弱科目(JSON数组)
     */
    private String weakSubjects;

    /**
     * 学习情况描述
     */
    private String studyDesc;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
