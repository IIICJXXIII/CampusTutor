package com.campus.module.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 教案生成请求
 */
@Data
@Schema(description = "教案生成请求")
public class LessonPlanRequest {

    @Schema(description = "教学科目", required = true, example = "数学")
    private String subject;

    @Schema(description = "学生水平", required = true, example = "初中一年级")
    private String studentLevel;

    @Schema(description = "课时时长", required = true, example = "60分钟")
    private String lessonDuration;

    @Schema(description = "学生情况", example = "基础较差")
    private String studentInfo;
}