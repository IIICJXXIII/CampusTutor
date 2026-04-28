package com.campus.module.ocr.service;

import com.campus.module.ocr.dto.OcrResultDTO;

/**
 * OCR 服务接口
 */
public interface OcrService {

    /**
     * 识别学生证
     * 
     * @param imageUrl 图片URL
     * @return OCR识别结果
     */
    OcrResultDTO recognizeStudentCard(String imageUrl);

    /**
     * 识别身份证正面
     * 
     * @param imageUrl 图片URL
     * @return OCR识别结果
     */
    OcrResultDTO recognizeIdCardFront(String imageUrl);

    /**
     * 识别身份证背面
     * 
     * @param imageUrl 图片URL
     * @return OCR识别结果
     */
    OcrResultDTO recognizeIdCardBack(String imageUrl);

    /**
     * 通用文字识别
     * 
     * @param imageUrl 图片URL
     * @return 识别出的文字内容
     */
    String recognizeGeneral(String imageUrl);

    /**
     * 识别学生证（Base64模式）
     * 
     * @param imageBase64 图片Base64编码
     * @return OCR识别结果
     */
    OcrResultDTO recognizeStudentCardByBase64(String imageBase64);
}
