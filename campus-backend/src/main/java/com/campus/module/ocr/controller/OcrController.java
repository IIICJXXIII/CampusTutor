package com.campus.module.ocr.controller;

import com.campus.common.result.Result;
import com.campus.module.ocr.dto.OcrResultDTO;
import com.campus.module.ocr.service.OcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * OCR 识别控制器
 */
@Tag(name = "OCR识别", description = "百度OCR识别相关接口")
@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    @Operation(summary = "识别学生证", description = "识别学生证图片，返回姓名、学校、专业等信息")
    @PostMapping("/student-card")
    public Result<OcrResultDTO> recognizeStudentCard(
            @Parameter(description = "学生证图片URL") @RequestParam String imageUrl) {
        OcrResultDTO result = ocrService.recognizeStudentCard(imageUrl);
        return result.getSuccess() ? Result.success(result) : Result.fail(result.getErrorMsg());
    }

    @Operation(summary = "识别身份证正面", description = "识别身份证正面，返回姓名、身份证号等信息")
    @PostMapping("/id-card/front")
    public Result<OcrResultDTO> recognizeIdCardFront(
            @Parameter(description = "身份证正面图片URL") @RequestParam String imageUrl) {
        OcrResultDTO result = ocrService.recognizeIdCardFront(imageUrl);
        return result.getSuccess() ? Result.success(result) : Result.fail(result.getErrorMsg());
    }

    @Operation(summary = "识别身份证背面", description = "识别身份证背面，返回签发机关等信息")
    @PostMapping("/id-card/back")
    public Result<OcrResultDTO> recognizeIdCardBack(
            @Parameter(description = "身份证背面图片URL") @RequestParam String imageUrl) {
        OcrResultDTO result = ocrService.recognizeIdCardBack(imageUrl);
        return result.getSuccess() ? Result.success(result) : Result.fail(result.getErrorMsg());
    }

    @Operation(summary = "通用文字识别", description = "识别图片中的文字内容")
    @PostMapping("/general")
    public Result<String> recognizeGeneral(
            @Parameter(description = "图片URL") @RequestParam String imageUrl) {
        String text = ocrService.recognizeGeneral(imageUrl);
        return text != null ? Result.success(text) : Result.fail("识别失败");
    }

    @Operation(summary = "识别学生证(Base64)", description = "使用Base64编码图片识别学生证，支持本地开发环境")
    @PostMapping("/student-card-base64")
    public Result<OcrResultDTO> recognizeStudentCardByBase64(
            @Parameter(description = "学生证图片Base64编码") @RequestBody String imageBase64) {
        OcrResultDTO result = ocrService.recognizeStudentCardByBase64(imageBase64);
        return result.getSuccess() ? Result.success(result) : Result.fail(result.getErrorMsg());
    }
}
