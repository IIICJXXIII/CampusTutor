package com.campus.module.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 需求解析请求
 */
@Data
@Schema(description = "需求解析请求")
public class DemandParseRequest {

    @Schema(description = "用户输入的自然语言描述", required = true,
            example = "孩子初二数学不太好，想找个有经验的女老师，最好是985本科以上，能上门辅导，预算150左右每小时，周末上午有时间")
    @NotBlank(message = "需求描述不能为空")
    private String text;

    @Schema(description = "是否返回原始LLM响应(调试用)", example = "false")
    private Boolean debug = false;
}
