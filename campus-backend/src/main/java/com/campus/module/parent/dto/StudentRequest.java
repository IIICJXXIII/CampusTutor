package com.campus.module.parent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 添加/编辑学生请求
 */
@Data
public class StudentRequest {

    /**
     * 学生ID(编辑时必填)
     */
    private Long id;

    @NotBlank(message = "学生姓名不能为空")
    private String studentName;

    @NotNull(message = "学生性别不能为空")
    private Integer gender;

    @NotBlank(message = "年级不能为空")
    private String grade;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 薄弱科目
     */
    private List<String> weakSubjects;

    /**
     * 学习情况描述
     */
    private String studyDesc;
}
