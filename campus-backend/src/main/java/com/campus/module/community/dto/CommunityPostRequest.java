package com.campus.module.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 社区帖子发布请求DTO
 */
@Data
@Schema(description = "社区帖子发布请求")
public class CommunityPostRequest {

    @Schema(description = "帖子标题", example = "如何高效备考期末？")
    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 128, message = "标题不能超过128个字符")
    private String title;

    @Schema(description = "帖子内容", example = "分享一下我的备考经验...")
    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 5000, message = "内容不能超过5000个字符")
    private String content;

    @Schema(description = "话题类型: 1-经验分享, 2-难题求助", example = "1")
    @NotNull(message = "话题类型不能为空")
    private Integer topicType;

    @Schema(description = "标签，逗号分隔，如: 学习经验,考试技巧",
            example = "学习经验,校园生活")
    @Size(max = 256, message = "标签不能超过256个字符")
    private String tags;

    @Schema(description = "图片列表，JSON数组格式",
            example = "[\"uploads/img1.jpg\", \"uploads/img2.jpg\"]")
    @Size(max = 1024, message = "图片信息过长")
    private String images;
}
