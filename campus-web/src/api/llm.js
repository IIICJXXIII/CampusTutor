import request from './request'

// 智能需求解析
export function parseDemand(text) {
  return request.post('/api/llm/demand/parse', { text })
}

// AI对话
export function chat(data) {
  return request.post('/api/llm/chat', data)
}

// 快速问答
export function quickAnswer(question) {
  return request.get('/api/llm/quick-answer', { params: { question } })
}
