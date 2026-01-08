import request from './request'

/**
 * AI 智能对话 (核心接口)
 * 对应后端: POST /api/llm/chat
 * @param {Object} data - { messages:Array, scene:String }
 */
export function chat(data) {
  return request.post('/llm/chat', data)
}

/**
 * 智能需求解析 (发布需求时使用)
 * @param {string} text - 自然语言描述，例如"帮我找个高三数学老师"
 */
export function parseDemand(text) {
  return request.post('/llm/demand/parse', { text })
}

/**
 * 快速问答 (预设问题)
 * @param {string} question 
 */
export function quickAnswer(question) {
  return request.get('/llm/quick-answer', { params: { question } })
}