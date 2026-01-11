<template>
  <div class="chat-room">
    <!-- 头部 -->
    <div class="chat-header">
      <el-button :icon="ArrowLeft" link @click="goBack" />
      <div class="header-info">
        <h3>{{ targetUser.name || '聊天' }}</h3>
        <span v-if="targetUser.online" class="online">在线</span>
      </div>
      <el-dropdown>
        <el-button :icon="More" link />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="viewProfile">查看资料</el-dropdown-item>
            <el-dropdown-item @click="clearMessages">清空记录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    
    <!-- 消息列表 -->
    <div ref="messageListRef" class="message-list" @scroll="handleScroll">
      <div v-if="loadingMore" class="loading-more">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
      
      <div v-for="(msg, index) in messages" :key="msg.id" class="message-wrapper">
        <!-- 时间分割线 -->
        <div v-if="showTimeLabel(msg, index)" class="time-label">
          {{ formatTimeLabel(msg.createTime) }}
        </div>
        
        <!-- 消息气泡 -->
        <div class="message-item" :class="{ 'is-self': msg.isSelf }">
          <el-avatar v-if="!msg.isSelf" :size="36" :src="targetUser.avatar">
            {{ targetUser.name?.charAt(0) }}
          </el-avatar>
          
          <div class="message-content">
            <!-- 文本消息 -->
            <div v-if="msg.type === 'text'" class="bubble text">
              {{ msg.content }}
            </div>
            <!-- 图片消息 -->
            <div v-else-if="msg.type === 'image'" class="bubble image">
              <el-image :src="msg.content" fit="cover" :preview-src-list="[msg.content]" />
            </div>
            
            <span class="message-time">{{ formatTime(msg.createTime) }}</span>
          </div>
          
          <el-avatar v-if="msg.isSelf" :size="36" :src="userStore.avatar">
            {{ userStore.nickname?.charAt(0) }}
          </el-avatar>
        </div>
      </div>
    </div>
    
    <!-- 输入框 -->
    <div class="chat-input">
      <div class="input-tools">
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleImageUpload"
          accept="image/*"
        >
          <el-button :icon="Picture" link />
        </el-upload>
      </div>
      
      <div class="input-area">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="1"
          :autosize="{ minRows: 1, maxRows: 4 }"
          placeholder="输入消息..."
          @keydown.enter.exact.prevent="sendMessage"
        />
      </div>
      
      <el-button 
        type="primary" 
        :icon="Promotion" 
        :disabled="!inputText.trim()"
        @click="sendMessage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, More, Picture, Promotion, Loading } from '@element-plus/icons-vue'
import { useUserStore, useChatStore } from '@shared/stores'
import { getMessages, sendMessage as sendMessageApi } from '@shared/api/chat'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const messageListRef = ref(null)
const inputText = ref('')
const messages = ref([])
const targetUser = ref({})
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(1)
const hasMore = ref(true)

const uploadUrl = '/api/file/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

let ws = null
let reconnectTimer = null

const formatTime = (time) => dayjs(time).format('HH:mm')

const formatTimeLabel = (time) => {
  const date = dayjs(time)
  const now = dayjs()
  
  if (date.isSame(now, 'day')) {
    return date.format('HH:mm')
  } else if (date.isSame(now.subtract(1, 'day'), 'day')) {
    return '昨天 ' + date.format('HH:mm')
  } else if (date.isSame(now, 'year')) {
    return date.format('M月D日 HH:mm')
  } else {
    return date.format('YYYY年M月D日 HH:mm')
  }
}

const showTimeLabel = (msg, index) => {
  if (index === 0) return true
  const prevMsg = messages.value[index - 1]
  const diff = dayjs(msg.createTime).diff(dayjs(prevMsg.createTime), 'minute')
  return diff > 5
}

const loadMessages = async (reset = false) => {
  if (reset) {
    page.value = 1
    messages.value = []
    hasMore.value = true
  }
  
  const targetId = route.params.id
  loading.value = true
  
  try {
    const res = await getMessages(targetId, { page: page.value, pageSize: 20 })
    if (res.code === 200) {
      const list = (res.data?.list || []).reverse()
      if (reset) {
        messages.value = list
        scrollToBottom()
      } else {
        messages.value = [...list, ...messages.value]
      }
      hasMore.value = list.length === 20
      targetUser.value = res.data?.targetUser || {}
    }
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const handleScroll = () => {
  if (messageListRef.value?.scrollTop < 50 && hasMore.value && !loadingMore.value) {
    loadingMore.value = true
    page.value++
    loadMessages()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  const content = inputText.value.trim()
  if (!content) return
  
  const targetId = route.params.id
  
  // 乐观更新
  const tempMsg = {
    id: Date.now(),
    type: 'text',
    content,
    isSelf: true,
    createTime: new Date().toISOString()
  }
  messages.value.push(tempMsg)
  scrollToBottom()
  inputText.value = ''
  
  try {
    await sendMessageApi(targetId, { type: 'text', content })
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  }
}

const handleImageUpload = async (response) => {
  if (response.code === 200) {
    const targetId = route.params.id
    const imageUrl = response.data.url
    
    const tempMsg = {
      id: Date.now(),
      type: 'image',
      content: imageUrl,
      isSelf: true,
      createTime: new Date().toISOString()
    }
    messages.value.push(tempMsg)
    scrollToBottom()
    
    try {
      await sendMessageApi(targetId, { type: 'image', content: imageUrl })
    } catch (error) {
      ElMessage.error(error.message || '发送失败')
    }
  }
}

const connectWebSocket = () => {
  const token = userStore.token
  const wsUrl = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080'}/ws/chat?token=${token}`
  
  ws = new WebSocket(wsUrl)
  
  ws.onopen = () => {
    console.log('WebSocket connected')
  }
  
  ws.onmessage = (event) => {
    const data = JSON.parse(event.data)
    if (data.targetId === route.params.id) {
      messages.value.push({
        ...data,
        isSelf: false
      })
      scrollToBottom()
    }
  }
  
  ws.onclose = () => {
    console.log('WebSocket closed')
    reconnectTimer = setTimeout(connectWebSocket, 3000)
  }
}

const viewProfile = () => {
  ElMessage.info('查看资料功能开发中')
}

const clearMessages = () => {
  messages.value = []
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadMessages(true)
  connectWebSocket()
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
  }
})
</script>

<style lang="scss" scoped>
.chat-room {
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
      text-align: center;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
      }
      
      .online {
        font-size: 12px;
        color: #67c23a;
      }
    }
  }
  
  .message-list {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    
    .loading-more {
      text-align: center;
      padding: 12px;
      color: #909399;
    }
    
    .time-label {
      text-align: center;
      font-size: 12px;
      color: #909399;
      margin: 16px 0;
    }
    
    .message-item {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      margin-bottom: 16px;
      
      &.is-self {
        flex-direction: row-reverse;
        
        .bubble {
          background: #409eff;
          color: #fff;
          
          &::after {
            left: auto;
            right: -6px;
            border-left-color: #409eff;
            border-right-color: transparent;
          }
        }
        
        .message-time {
          text-align: right;
        }
      }
      
      .message-content {
        max-width: 70%;
        
        .bubble {
          position: relative;
          padding: 10px 14px;
          background: #fff;
          border-radius: 12px;
          font-size: 14px;
          line-height: 1.5;
          word-break: break-word;
          
          &.image {
            padding: 4px;
            
            .el-image {
              max-width: 200px;
              max-height: 200px;
              border-radius: 8px;
            }
          }
        }
        
        .message-time {
          font-size: 11px;
          color: #909399;
          margin-top: 4px;
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
    
    .input-tools {
      display: flex;
      gap: 4px;
    }
    
    .input-area {
      flex: 1;
      
      :deep(.el-textarea__inner) {
        border-radius: 20px;
        padding: 8px 16px;
      }
    }
  }
}
</style>
