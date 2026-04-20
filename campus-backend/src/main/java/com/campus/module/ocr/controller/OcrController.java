package com.campus.module.ocr.controller;

import com.campus.common.result.Result;
import com.campus.module.ocr.dto.OcrRequestDTO;
import com.campus.module.ocr.dto.OcrResultDTO;
import com.campus.module.ocr.service.OcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * OCR 识别控制器
 */
@Tag(name = "OCR识别", description = "百度OCR识别相关接口")
@Slf4j
@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    @Operation(summary = "识别学生证", description = "识别学生证图片，返回姓名、学校、专业等信息")
    @PostMapping("/student-card")
    public Result<OcrResultDTO> recognizeStudentCard(
            @Parameter(description = "OCR识别请求") @RequestBody OcrRequestDTO request) {
        log.info("接收OCR识别请求对象: {}", request);
        log.info("接收OCR识别请求imageUrl: {}", request.getImageUrl());
        if (request.getImageUrl() == null) {
            log.error("OCR请求图片URL为空，完整请求对象: {}", request);
            // 尝试记录原始请求体
            return Result.fail("图片URL不能为空");
        }
        OcrResultDTO result = ocrService.recognizeStudentCard(request.getImageUrl());
        return result.getSuccess() ? Result.success(result) : Result.fail(result.getErrorMsg());
    }

    @Operation(summary = "识别身份证正面", description = "识别身份证正面，返回姓名、身份证号等信息")
    @PostMapping("/id-card/front")
    public Result<OcrResultDTO> recognizeIdCardFront(
            @Parameter(description = "OCR识别请求") @RequestBody OcrRequestDTO request) {
        log.info("接收身份证OCR识别请求对象: {}", request);
        log.info("接收身份证OCR识别请求imageUrl: {}", request.getImageUrl());
        if (request.getImageUrl() == null) {
            log.error("身份证OCR请求图片URL为空，完整请求对象: {}", request);
            return Result.fail("图片URL不能为空");
        }
        OcrResultDTO result = ocrService.recognizeIdCardFront(request.getImageUrl());
        return result.getSuccess() ? Result.success(result) : Result.fail(result.getErrorMsg());
    }

    @Operation(summary = "识别身份证背面", description = "识别身份证背面，返回签发机关等信息")
    @PostMapping("/id-card/back")
    public Result<OcrResultDTO> recognizeIdCardBack(
            @Parameter(description = "OCR识别请求") @RequestBody OcrRequestDTO request) {
        OcrResultDTO result = ocrService.recognizeIdCardBack(request.getImageUrl());
        return result.getSuccess() ? Result.success(result) : Result.fail(result.getErrorMsg());
    }

    @Operation(summary = "通用文字识别", description = "识别图片中的文字内容")
    @PostMapping("/general")
    public Result<String> recognizeGeneral(
            @Parameter(description = "OCR识别请求") @RequestBody OcrRequestDTO request) {
        String text = ocrService.recognizeGeneral(request.getImageUrl());
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
