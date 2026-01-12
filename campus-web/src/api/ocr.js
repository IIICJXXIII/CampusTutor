/**
 * OCR 识别相关 API
 */
import request from './request';

/**
 * 识别学生证
 * @param {string} imageUrl - 学生证图片URL
 */
export function recognizeStudentCard(imageUrl) {
  return request.post('/ocr/student-card', null, {
    params: { imageUrl }
  });
}

/**
 * 识别身份证正面
 * @param {string} imageUrl - 身份证正面图片URL
 */
export function recognizeIdCardFront(imageUrl) {
  return request.post('/ocr/id-card/front', null, {
    params: { imageUrl }
  });
}

/**
 * 识别身份证背面
 * @param {string} imageUrl - 身份证背面图片URL
 */
export function recognizeIdCardBack(imageUrl) {
  return request.post('/ocr/id-card/back', null, {
    params: { imageUrl }
  });
}

/**
 * 通用文字识别
 * @param {string} imageUrl - 图片URL
 */
export function recognizeGeneral(imageUrl) {
  return request.post('/ocr/general', null, {
    params: { imageUrl }
  });
}

/**
 * 【新增】识别学生证（Base64模式）
 * 解决本地开发图片URL无法被外网访问的问题
 * @param {string} imageBase64 - 图片的Base64编码字符串
 */
export function recognizeStudentCardByBase64(imageBase64) {
  // 注意：这里把 imageBase64 直接作为请求体(body)发送
  return request.post('/ocr/student-card-base64', imageBase64);
}