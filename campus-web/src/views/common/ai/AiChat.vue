<template>
  <div class="ai-chat-page-container">
    <div class="chat-wrapper">
      <div class="chat-header">
        <div class="header-left">
          <el-button :icon="ArrowLeft" circle class="nav-btn" @click="goBack" />
          <div class="header-title">
            <div class="ai-avatar-small">
              <el-icon><Service /></el-icon>
            </div>
            <div class="title-text">
              <h2>Campus AI 助教</h2>
              <span class="online-status">
                <span v-if="loading" class="thinking-status">
                  <span class="pulse-dot"></span>思考中...
                </span>
                <template v-else><span class="dot"></span>全天候为您解答</template>
              </span>
            </div>
          </div>
        </div>
        <div class="header-right">
          <el-tooltip content="清空聊天记录" placement="bottom">
            <el-button :icon="Delete" circle class="nav-btn delete-btn" @click="clearHistory" />
          </el-tooltip>
        </div>
      </div>
      
      <div ref="messageContainer" class="message-container">
        <transition name="el-fade-in-linear">
          <div v-if="messages.length === 0" class="welcome-wrapper">
            <div class="welcome-card">
              <div class="glow-ring">
                <div class="ai-avatar-large">
                  <el-icon><Service /></el-icon>
                </div>
              </div>
              <h3>您好，我是 AI 智能助手</h3>
              <p class="desc">我可以帮您解答关于平台规则、素质教育课程选择、教员对接等任何问题。</p>
              
              <div class="quick-questions">
                <div class="qq-title">✨ 您可以试着这样问我：</div>
                <div class="qq-list">
                  <div
                    v-for="(q, index) in quickQuestions"
                    :key="index"
                    class="quick-item"
                    @click="sendQuickQuestion(q)"
                  >
                    <el-icon class="q-icon"><ChatLineSquare /></el-icon>
                    <span>{{ q }}</span>
                    <el-icon class="arrow-icon"><ArrowRight /></el-icon>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </transition>
        
        <div class="message-list">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-item"
            :class="msg.role === 'user' ? 'is-user' : 'is-ai'"
          >
            <div class="msg-avatar">
              <el-avatar v-if="msg.role === 'user'" :size="38" class="user-avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div v-else class="ai-avatar-msg">
                <el-icon><Service /></el-icon>
              </div>
            </div>
            
            <div class="msg-content-wrapper">
              <div class="msg-name">{{ msg.role === 'user' ? '我' : 'AI 助手' }}</div>
              <div class="message-bubble" :class="{ 'typing': msg.isThinking }">
                <template v-if="msg.isThinking">
                  <div class="thinking-indicator">
                    <div class="thinking-dots">
                      <span class="thinking-dot"></span>
                      <span class="thinking-dot"></span>
                      <span class="thinking-dot"></span>
                    </div>
                    <span class="thinking-text">AI 正在思考中...</span>
                  </div>
                </template>
                <template v-else-if="msg.content">
                  <div class="md-content" v-html="msg.content"></div>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-footer">
        <div class="input-box">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="3"
            resize="none"
            placeholder="输入您的问题，与 AI 助手对话 (Enter 发送，Shift + Enter 换行)..."
            @keydown="handleKeydown"
          />
          <div class="action-bar">
            <span class="tips">AI 生成的内容仅供参考，不代表平台最终观点</span>
            <el-button 
              type="primary" 
              class="send-btn"
              :disabled="!inputText || !inputText.trim() || loading"
              @click="sendMessage"
            >
              发送 <el-icon class="el-icon--right"><Promotion /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@shared/stores'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Service, Delete, ChatLineSquare, ArrowRight, UserFilled, Promotion } from '@element-plus/icons-vue'
import { chat } from '@shared/api/llm'
import { marked } from 'marked'

const router = useRouter()
const userStore = useUserStore()

const messageContainer = ref(null)
const messages = ref([])
const inputText = ref('')
const loading = ref(false)

const chatStorageKey = computed(() => {
  const prefix = userStore.userRole ? `${userStore.userRole}_` : ''
  return `${prefix}ai_chat_history`
})

const userName = computed(() => userStore.user?.name)
const userAvatar = computed(() => userStore.user?.avatar)

const quickQuestions = [
  '平台有哪些素质教育课程可以选？',
  '我想给孩子找个钢琴陪练老师',
  '少儿编程适合几岁的孩子学？',
  '中考体育专项训练怎么收费？'
]

const goBack = () => {
  router.back()
}

const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空聊天记录吗？', '提示')
    messages.value = []
    localStorage.removeItem(chatStorageKey.value)
    ElMessage.success('已清空')
  } catch {}
}

const formatMessage = (content) => {
  try {
    return marked.parse(content)
  } catch {
    return content
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

const sendQuickQuestion = (question) => {
  inputText.value = question
  sendMessage()
}

const sendMessage = async () => {
  if (!inputText.value.trim() || loading.value) return
  
  const userMessage = {
    id: Date.now(),
    role: 'user',
    content: inputText.value.trim()
  }
  messages.value.push(userMessage)
  const question = inputText.value.trim()
  inputText.value = ''
  scrollToBottom()
  
  loading.value = true

  const thinkingId = Date.now() + 1
  messages.value.push({
    id: thinkingId,
    role: 'assistant',
    content: '',
    isThinking: true
  })
  scrollToBottom()
  
  try {
    const res = await chat({
      scene: 'demand',
      messages: messages.value
        .filter(m => !m.isThinking)
        .map(m => ({
          role: m.role,
          content: m.content
        }))
    })
    
    if (res.code === 200) {
      const thinkingMsg = messages.value.find(m => m.id === thinkingId)
      if (thinkingMsg) {
        thinkingMsg.content = res.data.content || res.data.reply
        thinkingMsg.isThinking = false
      }
      scrollToBottom()
    } else {
      throw new Error(res.msg || '获取回复失败')
    }
    
    localStorage.setItem(chatStorageKey.value, JSON.stringify(
      messages.value.map(m => ({ id: m.id, role: m.role, content: m.content }))
    ))
  } catch (error) {
    console.error('AI回复失败:', error)
    const errMsg = error?.message || '服务暂时不可用，请稍后再试'
    const thinkingMsg = messages.value.find(m => m.id === thinkingId)
    if (thinkingMsg) {
      thinkingMsg.content = `抱歉，请求失败：${errMsg}`
      thinkingMsg.isThinking = false
    }
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

onMounted(() => {
  // 恢复聊天记录
  const saved = localStorage.getItem(chatStorageKey.value)
  if (saved) {
    try {
      messages.value = JSON.parse(saved)
    } catch {}
  }
})
</script>

<style lang="scss" scoped>
.ai-chat-page-container {
  min-height: calc(100vh - 64px);
  background-color: #f4f6f8;
  padding: 24px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.chat-wrapper {
  width: 100%;
  max-width: 850px;
  height: calc(100vh - 112px); 
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* --- 头部样式 --- */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #f0f2f5;
  z-index: 10;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .nav-btn {
      border: none;
      font-size: 18px;
      color: #606266;
      background: #f4f6f8;
      &:hover { color: var(--el-color-primary); background: #ecf5ff; }
    }
    
    .header-title {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .ai-avatar-small {
        width: 36px;
        height: 36px;
        border-radius: 10px;
        background: linear-gradient(135deg, #7b2ff7, #256bfe);
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        box-shadow: 0 4px 10px rgba(37, 107, 254, 0.2);
      }
      
      .title-text {
        h2 { margin: 0; font-size: 16px; font-weight: 600; color: #1d2129; }
        .online-status {
          font-size: 12px;
          color: #67c23a;
          display: flex;
          align-items: center;
          gap: 6px;
          margin-top: 2px;
          .dot { width: 6px; height: 6px; background: #67c23a; border-radius: 50%; }
          .thinking-status {
            display: flex;
            align-items: center;
            gap: 6px;
            color: #7b2ff7;
            .pulse-dot {
              width: 8px;
              height: 8px;
              border-radius: 50%;
              background: #7b2ff7;
              animation: headerPulse 1.5s ease-in-out infinite;
            }
          }
        }
      }
    }
  }
  
  .header-right {
    .delete-btn {
      border: none; background: transparent; font-size: 18px;
      &:hover { color: #f53f3f; background: #fff1f0; }
    }
  }
}

/* --- 聊天主体区 --- */
.message-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background-color: #fafbfc;
  position: relative;
  
  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 4px; }
}

/* --- 欢迎引导页 --- */
.welcome-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  
  .welcome-card {
    text-align: center;
    max-width: 500px;
    animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
    
    .glow-ring {
      display: inline-block;
      padding: 8px;
      border-radius: 50%;
      background: rgba(37, 107, 254, 0.08);
      margin-bottom: 16px;
      
      .ai-avatar-large {
        width: 72px;
        height: 72px;
        border-radius: 50%;
        background: linear-gradient(135deg, #7b2ff7, #256bfe);
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 36px;
        box-shadow: 0 8px 20px rgba(37, 107, 254, 0.3);
      }
    }
    
    h3 { font-size: 22px; font-weight: 600; color: #1d2129; margin: 0 0 12px; }
    .desc { font-size: 14px; color: #86909c; line-height: 1.6; margin-bottom: 32px; }
    
    .quick-questions {
      text-align: left;
      background: #fff;
      padding: 20px;
      border-radius: 16px;
      box-shadow: 0 4px 16px rgba(0,0,0,0.03);
      border: 1px solid #f0f2f5;
      
      .qq-title { font-size: 14px; font-weight: 600; color: #4e5969; margin-bottom: 16px; }
      
      .qq-list {
        display: flex;
        flex-direction: column;
        gap: 12px;
        
        .quick-item {
          display: flex;
          align-items: center;
          padding: 12px 16px;
          background: #f4f6f8;
          border-radius: 10px;
          color: #4e5969;
          font-size: 14px;
          cursor: pointer;
          transition: all 0.25s ease;
          
          .q-icon { color: var(--el-color-primary); margin-right: 12px; font-size: 16px; }
          span { flex: 1; }
          .arrow-icon { color: #c0c4cc; opacity: 0; transform: translateX(-10px); transition: all 0.25s; }
          
          &:hover {
            background: #e8f3ff; color: var(--el-color-primary);
            transform: translateY(-2px);
            box-shadow: 0 4px 10px rgba(64,158,255,0.1);
            .arrow-icon { opacity: 1; transform: translateX(0); color: var(--el-color-primary); }
          }
        }
      }
    }
  }
}

/* --- 聊天气泡样式 --- */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  
  .message-item {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    animation: slideUp 0.3s ease-out;
    
    .msg-avatar {
      flex-shrink: 0;
      .user-avatar { background: #c0c4cc; color: white; font-size: 20px;}
      .ai-avatar-msg {
        width: 38px; height: 38px; border-radius: 50%;
        background: linear-gradient(135deg, #7b2ff7, #256bfe);
        color: white; display: flex; align-items: center; justify-content: center;
        font-size: 20px; box-shadow: 0 2px 8px rgba(37,107,254,0.25);
      }
    }
    
    .msg-content-wrapper {
      max-width: 75%;
      
      .msg-name { font-size: 12px; color: #86909c; margin-bottom: 6px; margin-left: 2px; }
      
      .message-bubble {
        padding: 14px 18px;
        border-radius: 2px 16px 16px 16px;
        font-size: 15px;
        line-height: 1.6;
        word-break: break-word;
        box-shadow: 0 2px 6px rgba(0,0,0,0.02);
        
        :deep(p) { margin: 0 0 8px; &:last-child { margin-bottom: 0; } }
        :deep(pre) { background: #282c34; color: #abb2bf; padding: 12px; border-radius: 8px; overflow-x: auto; margin: 10px 0; }
        :deep(code) { font-family: Consolas, monospace; background: rgba(0,0,0,0.05); padding: 2px 6px; border-radius: 4px; font-size: 13.5px; }
        
        &.typing {
          display: flex; align-items: center; padding: 14px 18px;
          
          .thinking-indicator {
            display: flex;
            align-items: center;
            gap: 10px;
            
            .thinking-dots {
              display: flex;
              gap: 4px;
              align-items: center;
              
              .thinking-dot {
                width: 8px;
                height: 8px;
                border-radius: 50%;
                background: linear-gradient(135deg, #7b2ff7, #256bfe);
                animation: thinkingPulse 1.4s ease-in-out infinite;
                
                &:nth-child(1) { animation-delay: 0s; }
                &:nth-child(2) { animation-delay: 0.2s; }
                &:nth-child(3) { animation-delay: 0.4s; }
              }
            }
            
            .thinking-text {
              font-size: 14px;
              color: #86909c;
              animation: thinkingFade 2s ease-in-out infinite;
            }
          }
        }
      }
    }
    
    &.is-user {
      flex-direction: row-reverse;
      
      .msg-content-wrapper {
        display: flex; flex-direction: column; align-items: flex-end;
        .msg-name { margin-left: 0; margin-right: 2px; }
        .message-bubble {
          background: linear-gradient(135deg, #409EFC, #256bfe);
          color: #fff;
          border-radius: 16px 2px 16px 16px;
          box-shadow: 0 4px 12px rgba(37,107,254,0.2);
          
          :deep(code) { background: rgba(255,255,255,0.2); color: #fff; }
        }
      }
    }
    
    &.is-ai {
      .message-bubble { background: #fff; color: #1d2129; border: 1px solid #f0f2f5; }
    }
  }
}

/* --- 底部输入区 --- */
.chat-footer {
  padding: 16px 24px;
  background: #fff;
  border-top: 1px solid #f0f2f5;
  
  .input-box {
    background: #f4f6f8;
    border-radius: 12px;
    padding: 12px 16px;
    border: 1px solid transparent;
    transition: all 0.3s;
    
    &:focus-within { 
      background: #fff; 
      border-color: var(--el-color-primary); 
      box-shadow: 0 0 0 2px rgba(64,158,255,0.1); 
    }
    
    :deep(.el-textarea__inner) {
      border: none !important; box-shadow: none !important; padding: 0;
      background: transparent; font-size: 15px; color: #1d2129;
      &::-webkit-scrollbar { width: 4px; }
      &::-webkit-scrollbar-thumb { background: #c0c4cc; border-radius: 2px; }
    }
    
    .action-bar {
      display: flex; justify-content: space-between; align-items: center; margin-top: 10px;
      .tips { font-size: 12px; color: #b0b4b8; }
      .send-btn { border-radius: 8px; padding: 8px 24px; }
    }
  }
}

/* 动效 */
@keyframes slideUp { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
@keyframes thinkingPulse {
  0%, 100% { transform: scale(0.6); opacity: 0.4; }
  50% { transform: scale(1); opacity: 1; }
}
@keyframes thinkingFade {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}
@keyframes headerPulse {
  0%, 100% { transform: scale(0.8); opacity: 0.5; box-shadow: 0 0 0 0 rgba(123, 47, 247, 0.4); }
  50% { transform: scale(1); opacity: 1; box-shadow: 0 0 0 6px rgba(123, 47, 247, 0); }
}
</style>