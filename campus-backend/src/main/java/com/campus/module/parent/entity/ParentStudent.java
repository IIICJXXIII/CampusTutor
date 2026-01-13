package com.campus.module.parent.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 家长学生信息表
 */
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
   /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 显式的getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getWeakSubjects() {
        return weakSubjects;
    }

    public void setWeakSubjects(String weakSubjects) {
        this.weakSubjects = weakSubjects;
    }

    public String getStudyDesc() {
        return studyDesc;
    }

    public void setStudyDesc(String studyDesc) {
        this.studyDesc = studyDesc;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
