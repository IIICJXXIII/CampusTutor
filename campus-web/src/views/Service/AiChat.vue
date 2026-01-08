<template>
  <div class="ai-chat-page">
    <div class="chat-container">
      <div class="chat-header">
        <div class="header-left">
          <div class="avatar-wrapper">
            <img src="https://api.dicebear.com/7.x/bottts/svg?seed=DeepSeek" alt="AI" />
          </div>
          <div class="info">
            <div class="name">校园智教 AI 助手</div>
            <div class="status">
              <span class="dot"></span>
              <span>DeepSeek V3 模型在线</span>
            </div>
          </div>
        </div>
        <div class="header-right">
          <el-select 
            v-model="currentScene" 
            size="small" 
            style="width: 130px" 
            @change="resetChat"
            placeholder="选择场景"
          >
            <el-option label="👨‍💻 通用客服" value="general" />
            <el-option label="📝 发布助手" value="demand" />
            <el-option label="🎓 找人咨询" value="tutor" />
          </el-select>
        </div>
      </div>

      <div class="message-list" ref="msgListRef">
        <div class="message-item assistant">
          <div class="avatar">
            <img src="https://api.dicebear.com/7.x/bottts/svg?seed=DeepSeek" />
          </div>
          <div class="bubble">
            <p>你好！我是基于 <b>DeepSeek V3</b> 的校园智教智能助手。🤖</p>
            <p style="margin-top: 5px;">我可以协助你：</p>
            <ul style="padding-left: 20px; margin: 5px 0;">
              <li>解答平台使用问题</li>
              <li>辅助撰写家教需求</li>
              <li>推荐合适的老师</li>
            </ul>
            <p>请问有什么可以帮你的吗？</p>
          </div>
        </div>

        <div 
          v-for="(msg, index) in messageHistory" 
          :key="index" 
          class="message-item"
          :class="msg.role"
        >
          <div class="avatar">
            <img v-if="msg.role === 'assistant'" src="https://api.dicebear.com/7.x/bottts/svg?seed=DeepSeek" />
            <img v-else :src="userAvatar" />
          </div>
          
          <div class="bubble">
            <div class="text-content">{{ msg.content }}</div>
          </div>
        </div>

        <div v-if="loading" class="message-item assistant">
          <div class="avatar">
            <img src="https://api.dicebear.com/7.x/bottts/svg?seed=DeepSeek" />
          </div>
          <div class="bubble typing">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="quick-questions" v-if="messageHistory.length === 0">
          <el-tag 
            v-for="q in quickQuestions" 
            :key="q" 
            class="question-tag" 
            effect="plain" 
            round
            @click="handleQuickAsk(q)"
          >
            {{ q }}
          </el-tag>
        </div>

        <div class="input-box">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题 (Shift + Enter 换行，Enter 发送)"
            @keydown.enter.prevent="handleEnterKey"
            resize="none"
          />
          <div class="send-btn-wrapper">
            <el-button type="primary" :loading="loading" @click="sendMessage">
              发送 <el-icon class="el-icon--right"><Position /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from 'vue'
import { chat } from '@/api/llm'
import { useUserStore } from '@/stores' // 确保你有这个store，或者改用localStorage
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const msgListRef = ref(null)
const inputText = ref('')
const loading = ref(false)
const currentScene = ref('general')

// 获取用户头像，没有则使用默认
const userAvatar = computed(() => {
  return userStore.userInfo?.avatar || 'https://api.dicebear.com/7.x/adventurer/svg?seed=User'
})

// 聊天记录
const messageHistory = ref([])

// 预设问题
const quickQuestions = [
  '我想给孩子找个初二数学老师',
  '平台的收费标准是怎样的？',
  '如何认证成为教员？',
  '教员提现多久到账？'
]

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (msgListRef.value) {
    msgListRef.value.scrollTop = msgListRef.value.scrollHeight
  }
}

// 处理回车键
const handleEnterKey = (e) => {
  if (e.shiftKey) return // Shift+Enter 换行
  sendMessage()
}

// 发送消息核心逻辑
const sendMessage = async () => {
  const content = inputText.value.trim()
  if (!content) return
  if (loading.value) return

  // 1. 本地立即显示用户消息
  messageHistory.value.push({ role: 'user', content: content })
  inputText.value = ''
  scrollToBottom()

  loading.value = true

  try {
    // 2. 构造上下文 (发送给后端)
    // 策略：只带最近 10 条历史记录，防止 token 超出限制
    const contextMessages = messageHistory.value.slice(-10).map(m => ({
      role: m.role,
      content: m.content
    }))

    // 3. 调用 API
    const res = await chat({
      messages: contextMessages,
      scene: currentScene.value
    })

    if (res.code === 200 && res.data.success) {
      // 成功收到 AI 回复
      messageHistory.value.push({
        role: 'assistant',
        content: res.data.content
      })
    } else {
      // 业务失败
      messageHistory.value.push({
        role: 'assistant',
        content: '🤖 AI 遇到了一点小问题：' + (res.data?.error || res.msg || '未知错误')
      })
    }
  } catch (error) {
    console.error('Chat Error:', error)
    messageHistory.value.push({
      role: 'assistant',
      content: '❌ 网络连接失败，请检查后端服务是否启动。'
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

// 点击快捷问题
const handleQuickAsk = (q) => {
  inputText.value = q
  sendMessage()
}

// 切换场景重置对话
const resetChat = () => {
  messageHistory.value = []
  ElMessage.success({
    message: '已切换对话场景，历史记录已重置',
    type: 'success',
    plain: true,
  })
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped lang="scss">
.ai-chat-page {
  /* 减去顶部导航高度，确保全屏 */
  height: calc(100vh - 60px); 
  background-color: #f2f3f5;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.chat-container {
  width: 100%;
  max-width: 900px; /* 限制最大宽度，大屏更好看 */
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  z-index: 10;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .avatar-wrapper {
      width: 42px; height: 42px; border-radius: 50%;
      background: #e6f7ff; padding: 4px; overflow: hidden;
      img { width: 100%; height: 100%; }
    }

    .info {
      .name { font-size: 16px; font-weight: 700; color: #303133; }
      .status {
        font-size: 12px; color: #67c23a; display: flex; align-items: center; gap: 4px;
        .dot { width: 8px; height: 8px; background: #67c23a; border-radius: 50%; animation: pulse 2s infinite; }
      }
    }
  }
}

/* 消息列表 */
.message-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f9fafe;
  display: flex;
  flex-direction: column;
  gap: 20px;

  /* 美化滚动条 */
  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 4px; }
}

.message-item {
  display: flex; gap: 12px; max-width: 85%;
  animation: fadeIn 0.3s ease;

  &.assistant {
    align-self: flex-start;
    .bubble { background: #fff; color: #303133; border-top-left-radius: 2px; border: 1px solid #ebeef5; }
  }

  &.user {
    align-self: flex-end;
    flex-direction: row-reverse;
    .bubble { 
      background: linear-gradient(135deg, #409eff 0%, #2b85e4 100%); 
      color: #fff; 
      border-top-right-radius: 2px; 
    }
  }

  .avatar {
    width: 38px; height: 38px; border-radius: 50%;
    background: #fff; flex-shrink: 0; overflow: hidden;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    img { width: 100%; height: 100%; }
  }

  .bubble {
    padding: 12px 16px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
    font-size: 15px;
    line-height: 1.6;
    
    .text-content { 
      white-space: pre-wrap; /* 关键：保留换行符 */
      word-break: break-all; 
    }
  }
}

/* 输入区域 */
.input-area {
  padding: 16px 24px;
  background: #fff;
  border-top: 1px solid #ebeef5;

  .quick-questions {
    display: flex; gap: 10px; margin-bottom: 12px; flex-wrap: wrap;
    .question-tag { 
      cursor: pointer; 
      transition: all 0.2s; 
      border-color: #d9ecff;
      &:hover { transform: translateY(-2px); border-color: #409eff; color: #409eff; }
    }
  }

  .input-box {
    position: relative;
    :deep(.el-textarea__inner) { 
      padding-right: 110px; /* 给按钮留位 */
      border-radius: 8px; 
      font-family: inherit;
    }
    .send-btn-wrapper { position: absolute; bottom: 8px; right: 8px; }
  }
}

/* 动画 */
@keyframes pulse { 0% { opacity: 0.6; } 50% { opacity: 1; } 100% { opacity: 0.6; } }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

/* 打字机 loading */
.typing span {
  width: 6px; height: 6px; background: #909399; border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
  display: inline-block; margin: 0 2px;
}
.typing span:nth-child(1) { animation-delay: 0s; }
.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing { 0%, 100% { transform: scale(1); opacity: 0.5; } 50% { transform: scale(1.2); opacity: 1; } }
</style>