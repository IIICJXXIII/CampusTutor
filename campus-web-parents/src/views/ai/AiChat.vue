<template>
  <div class="ai-chat-page">
    <div class="chat-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <div class="chat-title">
        <el-icon><Service /></el-icon>
        AI智能助手
      </div>
      <el-button link @click="clearHistory">
        <el-icon><Delete /></el-icon>
      </el-button>
    </div>
    
    <div ref="messageContainer" class="message-container">
      <!-- 欢迎消息 -->
      <div v-if="messages.length === 0" class="welcome-card">
        <div class="ai-avatar">
          <el-icon><Service /></el-icon>
        </div>
        <h3>您好，我是AI助手</h3>
        <p>我可以帮您解答关于家教、学习等方面的问题</p>
        <div class="quick-questions">
          <div
            v-for="(q, index) in quickQuestions"
            :key="index"
            class="quick-item"
            @click="sendQuickQuestion(q)"
          >
            {{ q }}
          </div>
        </div>
      </div>
      
      <!-- 消息列表 -->
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="message-item"
        :class="{ 'is-user': msg.role === 'user' }"
      >
        <div v-if="msg.role === 'assistant'" class="ai-avatar">
          <el-icon><Service /></el-icon>
        </div>
        
        <div class="message-content">
          <div class="message-bubble" v-html="formatMessage(msg.content)" />
        </div>
        
        <el-avatar v-if="msg.role === 'user'" :size="40" :src="userAvatar">
          {{ userName?.charAt(0) }}
        </el-avatar>
      </div>
      
      <!-- 加载中 -->
      <div v-if="loading" class="message-item">
        <div class="ai-avatar">
          <el-icon><Service /></el-icon>
        </div>
        <div class="message-content">
          <div class="message-bubble typing">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="input-bar">
      <el-input
        v-model="inputText"
        placeholder="输入您的问题..."
        :disabled="loading"
        @keyup.enter="sendMessage"
      >
        <template #suffix>
          <el-button
            type="primary"
            :icon="Position"
            circle
            size="small"
            :disabled="!inputText.trim() || loading"
            @click="sendMessage"
          />
        </template>
      </el-input>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@shared/stores'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Service, Delete, Position } from '@element-plus/icons-vue'
import { chat, streamChat } from '@shared/api/llm'
import { marked } from 'marked'

const router = useRouter()
const userStore = useUserStore()

const messageContainer = ref(null)
const messages = ref([])
const inputText = ref('')
const loading = ref(false)

const userName = computed(() => userStore.user?.name)
const userAvatar = computed(() => userStore.user?.avatar)

const quickQuestions = [
  '如何选择合适的家教老师？',
  '孩子学习成绩不好怎么办？',
  '家教费用一般是多少？',
  '如何提高孩子的学习兴趣？'
]

const goBack = () => {
  router.back()
}

const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空聊天记录吗？', '提示')
    messages.value = []
    localStorage.removeItem('ai_chat_history')
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
  
  try {
    const res = await chat({
      scene: 'demand',
      messages: messages.value.map(m => ({
        role: m.role,
        content: m.content
      }))
    })
    
    if (res.code === 200) {
      messages.value.push({
        id: Date.now() + 1,
        role: 'assistant',
        content: res.data.content || res.data.reply
      })
      scrollToBottom()
    } else {
      throw new Error(res.msg || '获取回复失败')
    }
    
    // 保存聊天记录
    localStorage.setItem('ai_chat_history', JSON.stringify(messages.value))
  } catch (error) {
    console.error('AI回复失败:', error)
    const errMsg = error?.message || '服务暂时不可用，请稍后再试'
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: `抱歉，请求失败：${errMsg}`
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

onMounted(() => {
  // 恢复聊天记录
  const saved = localStorage.getItem('ai_chat_history')
  if (saved) {
    try {
      messages.value = JSON.parse(saved)
    } catch {}
  }
})
</script>

<style lang="scss" scoped>
.ai-chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(180deg, #e8f4fd 0%, #f5f7fa 100%);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  .chat-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 17px;
    font-weight: 600;
    
    .el-icon {
      color: #409eff;
    }
  }
}

.message-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.welcome-card {
  text-align: center;
  padding: 40px 20px;
  
  .ai-avatar {
    width: 80px;
    height: 80px;
    margin: 0 auto 20px;
    background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .el-icon {
      font-size: 40px;
      color: #fff;
    }
  }
  
  h3 {
    font-size: 20px;
    margin: 0 0 8px;
  }
  
  p {
    color: #666;
    margin: 0 0 24px;
  }
  
  .quick-questions {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    justify-content: center;
    
    .quick-item {
      padding: 10px 16px;
      background: #fff;
      border-radius: 20px;
      cursor: pointer;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
      transition: all 0.2s;
      
      &:hover {
        background: #409eff;
        color: #fff;
      }
    }
  }
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 16px;
  
  &.is-user {
    flex-direction: row-reverse;
    
    .message-bubble {
      background: #409eff;
      color: #fff;
    }
  }
  
  .ai-avatar {
    width: 40px;
    height: 40px;
    min-width: 40px;
    background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .el-icon {
      font-size: 20px;
      color: #fff;
    }
  }
  
  .message-content {
    max-width: 70%;
  }
  
  .message-bubble {
    padding: 12px 16px;
    background: #fff;
    border-radius: 12px;
    line-height: 1.6;
    word-break: break-word;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
    
    :deep(p) {
      margin: 0 0 8px;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
    
    :deep(pre) {
      background: #f5f5f5;
      padding: 12px;
      border-radius: 8px;
      overflow-x: auto;
    }
    
    :deep(code) {
      background: #f5f5f5;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 13px;
    }
    
    &.typing {
      display: flex;
      gap: 4px;
      padding: 16px 20px;
      
      .dot {
        width: 8px;
        height: 8px;
        background: #999;
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

@keyframes typing {
  0%, 60%, 100% {
    opacity: 0.3;
    transform: translateY(0);
  }
  30% {
    opacity: 1;
    transform: translateY(-4px);
  }
}

.input-bar {
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  
  .el-input {
    :deep(.el-input__inner) {
      border-radius: 20px;
      padding-right: 50px;
    }
    
    :deep(.el-input__suffix) {
      right: 4px;
    }
  }
}
</style>
