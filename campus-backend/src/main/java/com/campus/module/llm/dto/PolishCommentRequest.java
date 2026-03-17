package com.campus.module.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评语润色请求
 */
@Data
@Schema(description = "评语润色请求")
public class PolishCommentRequest {

    @Schema(description = "原始评语", required = true, example = "学生上课认真，作业完成及时")
    private String rawComment;

    @Schema(description = "教学科目", required = true, example = "数学")
    private String subject;

    @Schema(description = "学生情况", example = "初中一年级，基础一般")
    private String studentInfo;
}