package com.campus.module.parent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class StudentRequest {
    private Long id;

    @NotBlank(message = "学生姓名不能为空")
    private String studentName;

    @NotNull(message = "学生性别不能为空")
    private Integer gender;

    @NotBlank(message = "年级不能为空")
    private String grade;

    private String schoolName;

    private List<String> weakSubjects;

    private String studyDesc;
}
