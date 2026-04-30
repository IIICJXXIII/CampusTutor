/**
 * LLM (AI) 相关 API (共享模块)
 */
import request from './request';

/**
 * AI解析需求
 * @param {string} text - 需求文本
 */
export function parseDemand(text) {
  return request.post('/llm/demand/parse', { text });
}

/**
 * AI对话
 * @param {Object} data - { messages, scene }
 */
export function chat(data) {
  return request.post('/llm/chat', data);
}

/**
 * 快速问答
 * @param {string} question - 问题
 */
export function quickAsk(question) {
  return request.get('/llm/quick-answer', {
    params: { question }
  });
}

/**
 * AI生成课程规划
 * @param {Object} data - { subject, studentLevel, totalHours, studentInfo }
 */
export function generateLessonPlan(data) {
  return request.post('/llm/lesson/plan', data);
}

/**
 * AI评语润色
 * @param {Object} data - { originalComment, subject, studentName }
 */
export function polishComment(data) {
  return request.post('/llm/lesson/comment', data);
}
