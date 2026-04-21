<template>
  <div class="ai-chat">
    <!-- 头部 -->
    <div class="chat-header">
      <el-button :icon="ArrowLeft" link @click="goBack" />
      <div class="header-info">
        <el-avatar :size="32" class="ai-avatar">AI</el-avatar>
        <h3>AI 助手</h3>
      </div>
      <el-button :icon="Delete" link @click="clearHistory" />
    </div>
    
    <!-- 消息列表 -->
    <div ref="messageListRef" class="message-list">
      <!-- 欢迎消息 -->
      <div v-if="!messages.length" class="welcome-section">
        <div class="welcome-card">
          <el-avatar :size="64" class="ai-avatar-large">AI</el-avatar>
          <h2>你好，我是 AI 助手</h2>
          <p>我可以帮你解答素质教育教学问题、提供教案建议、分析学生学情等</p>
        </div>
        
        <div class="quick-questions">
          <h4>你可以问我：</h4>
          <div class="questions">
            <el-button 
              v-for="q in quickQuestions" 
              :key="q" 
              round 
              @click="askQuestion(q)"
            >
              {{ q }}
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 消息列表 -->
      <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ 'is-user': msg.role === 'user' }">
        <el-avatar v-if="msg.role === 'assistant'" :size="36" class="ai-avatar">AI</el-avatar>
        
        <div class="message-content">
          <div class="bubble" v-html="formatContent(msg.content)"></div>
        </div>
        
        <el-avatar v-if="msg.role === 'user'" :size="36" :src="userStore.avatar">
          {{ userStore.nickname?.charAt(0) }}
        </el-avatar>
      </div>
      
      <!-- 加载中 -->
      <div v-if="loading" class="message-item">
        <el-avatar :size="36" class="ai-avatar">AI</el-avatar>
        <div class="message-content">
          <div class="bubble typing">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 输入框 -->
    <div class="chat-input">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="1"
        :autosize="{ minRows: 1, maxRows: 4 }"
        placeholder="输入你的问题..."
        :disabled="loading"
        @keydown.enter.exact.prevent="sendMessage"
      />
      <el-button 
        type="primary" 
        :icon="Promotion" 
        :loading="loading"
        :disabled="!inputText.trim()"
        @click="sendMessage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Delete, Promotion } from '@element-plus/icons-vue'
import { useUserStore } from '@shared/stores'
import { chat } from '@shared/api/llm'
import { marked } from 'marked'

const router = useRouter()
const userStore = useUserStore()

const messageListRef = ref(null)
const inputText = ref('')
const messages = ref([])
const loading = ref(false)

const quickQuestions = [
  '如何让学生对乐器练习保持兴趣？',
  '少儿编程课怎么设计更有趣？',
  '体育训练如何制定科学的教案？',
  '怎样与家长高效沟通孩子的进步？'
]

const formatContent = (content) => {
  try {
    return marked(content)
  } catch {
    return content
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const askQuestion = (question) => {
  inputText.value = question
  sendMessage()
}

const sendMessage = async () => {
  const content = inputText.value.trim()
  if (!content || loading.value) return
  
  // 添加用户消息
  messages.value.push({
    id: Date.now(),
    role: 'user',
    content
  })
  inputText.value = ''
  scrollToBottom()
  
  loading.value = true
  
  try {
    const res = await chat({
      scene: 'tutor',
      messages: messages.value.slice(-10).map(m => ({
        role: m.role,
        content: m.content
      }))
    })
    
    if (res.code === 200) {
      // 响应结构调整为适配 res.data.content 或者是原始的 res.data.reply
      const replyContent = res.data.content || res.data.reply
      messages.value.push({
        id: Date.now() + 1,
        role: 'assistant',
        content: replyContent
      })
      scrollToBottom()
      
      // 保存到本地
      saveHistory()
    }
  } catch (error) {
    ElMessage.error(error.message || 'AI 回复失败')
  } finally {
    loading.value = false
  }
}

const clearHistory = () => {
  messages.value = []
  localStorage.removeItem('ai_chat_history')
}

const saveHistory = () => {
  localStorage.setItem('ai_chat_history', JSON.stringify(messages.value.slice(-50)))
}

const loadHistory = () => {
  const history = localStorage.getItem('ai_chat_history')
  if (history) {
    try {
      messages.value = JSON.parse(history)
    } catch {
      messages.value = []
    }
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadHistory()
  scrollToBottom()
})
</script>

<style lang="scss" scoped>
.ai-chat {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
  
  .chat-header {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background: #fff;
    border-bottom: 1px solid #ebeef5;
    
    .header-info {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
      }
    }
  }
  
  .ai-avatar, .ai-avatar-large {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    font-weight: 600;
  }
  
  .message-list {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    
    .welcome-section {
      text-align: center;
      padding: 32px 16px;
      
      .welcome-card {
        margin-bottom: 32px;
        
        h2 {
          font-size: 20px;
          font-weight: 600;
          margin: 16px 0 8px;
        }
        
        p {
          font-size: 14px;
          color: #606266;
        }
      }
      
      .quick-questions {
        h4 {
          font-size: 14px;
          color: #909399;
          margin-bottom: 12px;
        }
        
        .questions {
          display: flex;
          flex-wrap: wrap;
          justify-content: center;
          gap: 8px;
        }
      }
    }
    
    .message-item {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      margin-bottom: 16px;
      
      &.is-user {
        flex-direction: row-reverse;
        
        .bubble {
          background: #409eff;
          color: #fff;
        }
      }
      
      .message-content {
        max-width: 80%;
        
        .bubble {
          padding: 12px 16px;
          background: #fff;
          border-radius: 12px;
          font-size: 14px;
          line-height: 1.6;
          
          :deep(p) {
            margin: 0 0 8px;
            
            &:last-child {
              margin-bottom: 0;
            }
          }
          
          :deep(pre) {
            background: #f5f7fa;
            padding: 12px;
            border-radius: 8px;
            overflow-x: auto;
          }
          
          :deep(code) {
            background: #f5f7fa;
            padding: 2px 6px;
            border-radius: 4px;
            font-family: monospace;
          }
          
          &.typing {
            display: flex;
            gap: 4px;
            padding: 16px;
            
            span {
              width: 8px;
              height: 8px;
              background: #909399;
              border-radius: 50%;
              animation: typing 1.4s infinite;
              
              &:nth-child(2) {
                animation-delay: 0.2s;
              }
              
              &:nth-child(3) {
                animation-delay: 0.4s;
              }
            }
          }
        }
      }
    }
  }
  
  .chat-input {
    display: flex;
    align-items: flex-end;
    gap: 8px;
    padding: 12px 16px;
    background: #fff;
    border-top: 1px solid #ebeef5;
    
    .el-input {
      flex: 1;
    }
    
    :deep(.el-textarea__inner) {
      border-radius: 20px;
      padding: 8px 16px;
    }
  }
}

@keyframes typing {
  0%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
