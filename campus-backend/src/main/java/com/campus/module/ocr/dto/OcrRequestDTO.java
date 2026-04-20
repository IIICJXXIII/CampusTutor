package com.campus.module.ocr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * OCR 识别请求 DTO
 */
@Data
@Schema(description = "OCR识别请求")
public class OcrRequestDTO {

    @Schema(description = "图片URL", required = true)
    private String imageUrl;
}