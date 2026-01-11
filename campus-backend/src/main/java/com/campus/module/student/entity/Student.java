package com.campus.module.student.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生实体类
 */
@Data
@TableName("t_student")
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联家长ID
     */
    private Long parentId;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 性别: 1-男, 2-女
     */
    private Integer gender;

    /**
     * 年级
     */
    private String grade;

    /**
     * 就读学校(可选)
     */
    private String universityName;

    /**
     * 补习科目(JSON存储)
     */
    private String subjects;

    // --- 新增的核心地理位置字段 ---

    /**
     * 家庭详细地址
     */
    private String address;

    /**
     * 地址纬度
     */
    private Double latitude;

    /**
     * 地址经度
     */
    private Double longitude;

    // -------------------------

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}