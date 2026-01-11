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
 * 流式AI对话
 * @param {Object} data - { message, context }
 */
export function streamChat(data) {
  return request.post('/llm/chat/stream', data, {
    responseType: 'stream'
  });
}
