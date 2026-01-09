/**
 * OCR识别相关 API (共享模块)
 */
import request from './request';

/**
 * 识别学生证
 * @param {string} imageUrl - 图片URL
 */
export function recognizeStudentCard(imageUrl) {
  return request.post('/ocr/student-card', null, {
    params: { imageUrl }
  });
}

/**
 * 识别身份证正面
 * @param {string} imageUrl - 图片URL
 */
export function recognizeIdCardFront(imageUrl) {
  return request.post('/ocr/idcard-front', null, {
    params: { imageUrl }
  });
}

/**
 * 识别身份证背面
 * @param {string} imageUrl - 图片URL
 */
export function recognizeIdCardBack(imageUrl) {
  return request.post('/ocr/idcard-back', null, {
    params: { imageUrl }
  });
}

/**
 * 通用文字识别 (错题本)
 * @param {string} imageUrl - 图片URL
 */
export function recognizeGeneral(imageUrl) {
  return request.post('/ocr/general', null, {
    params: { imageUrl }
  });
}
