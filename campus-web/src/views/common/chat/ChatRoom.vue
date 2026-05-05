<template>
  <div class="chat-page-container">
    <div class="chat-window">
      <div class="chat-header">
        <div class="header-left">
          <el-button :icon="ArrowLeft" circle @click="goBack" class="back-btn" />
          <div class="user-info">
            <h3 class="user-name">{{ targetUser.nickname || targetUser.realName || targetUser.name || '聊天' }}</h3>
            <span v-if="targetUser.online" class="online-status">
              <span class="dot"></span>在线
            </span>
          </div>
        </div>
        <div class="header-right">
          <el-dropdown>
            <el-button :icon="MoreFilled" circle class="more-btn" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="viewProfile">查看资料</el-dropdown-item>
                <el-dropdown-item @click="clearMessages">清空记录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <div ref="messageListRef" class="message-list" @scroll="handleScroll">
        <div v-if="loadingMore" class="loading-more">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载历史消息...</span>
        </div>
        
        <div v-for="(msg, index) in messages" :key="msg.id" class="message-wrapper">
          <div v-if="showTimeLabel(msg, index)" class="time-label">
            <span>{{ formatTimeLabel(msg.createTime) }}</span>
          </div>
          
          <div class="message-item" :class="{ 'is-self': msg.isSelf }">
            <el-avatar 
              class="avatar" 
              :size="40" 
              :src="msg.isSelf ? userStore.avatar : (targetUser.avatar || targetUser.avatarUrl)"
            >
              {{ (msg.isSelf ? userStore.nickname : (targetUser.nickname || targetUser.realName || targetUser.name))?.charAt(0) || 'U' }}
            </el-avatar>
            
            <div class="message-content">
              <div class="message-sender" v-if="!msg.isSelf">
                {{ targetUser.nickname || targetUser.realName || targetUser.name }}
              </div>
              
              <div v-if="msg.type === 'text'" class="bubble text">
                {{ msg.content }}
              </div>
              <div v-else-if="msg.type === 'image'" class="bubble image">
                <el-image 
                  :src="msg.content" 
                  fit="cover" 
                  :preview-src-list="[msg.content]" 
                  hide-on-click-modal
                />
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="chat-footer">
        <div class="toolbar">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImageUpload"
            accept="image/*"
            class="upload-btn"
          >
            <el-tooltip content="发送图片" placement="top">
              <el-icon size="22"><Picture /></el-icon>
            </el-tooltip>
          </el-upload>
        </div>
        
        <div class="input-area">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="3"
            resize="none"
            placeholder="按 Enter 发送消息，Shift + Enter 换行..."
            @keydown="handleKeydown"
          />
        </div>
        
        <div class="action-bar">
          <span class="tips">Enter 发送 / Shift+Enter 换行</span>
          <el-button 
            type="primary" 
            :disabled="!inputText.trim()"
            @click="sendMessage"
            class="send-btn"
          >
            发送 <el-icon class="el-icon--right"><Promotion /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, MoreFilled, Picture, Promotion, Loading } from '@element-plus/icons-vue'
import { useUserStore, useChatStore } from '@shared/stores'
import { getChatHistory, sendMessage as sendMessageApi, markAsRead } from '@shared/api/chat'
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

const formatTimeLabel = (time) => {
  const date = dayjs(time)
  const now = dayjs()
  if (date.isSame(now, 'day')) {
    return date.format('HH:mm')
  } else if (date.isSame(now.subtract(1, 'day'), 'day')) {
    return '昨天 ' + date.format('HH:mm')
  } else if (date.isSame(now, 'year')) {
    return date.format('MM-DD HH:mm')
  } else {
    return date.format('YYYY-MM-DD HH:mm')
  }
}

const showTimeLabel = (msg, index) => {
  if (index === 0) return true
  const prevMsg = messages.value[index - 1]
  const diff = dayjs(msg.createTime).diff(dayjs(prevMsg.createTime), 'minute')
  return diff > 5 
}

// 🚨 新增：独立获取对方用户信息的兜底机制
const loadTargetUserInfo = async () => {
  const targetId = route.params.id
  try {
    // 结合现有的 token 发起原生 fetch 获取对方资料，避免外部依赖路径错误
    const response = await fetch(`/api/chat/user-info/${targetId}`, {
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    const res = await response.json()
    if (res.code === 200 && res.data) {
      targetUser.value = res.data
    }
  } catch (error) {
  }
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
    if (reset) {
      markAsRead(targetId).then(() => {
        chatStore.setUnreadCount(Math.max(0, chatStore.unreadCount - 1))
      }).catch(() => {})
    }
    const res = await getChatHistory(targetId, { page: page.value, pageSize: 20 })
    if (res.code === 200) {
      const rawList = Array.isArray(res.data) ? res.data : (res.data?.list || [])
      
      // 🚨 核心修复：遍历后端返回的数据，补充 isSelf 和 type 字段
      const list = rawList.map(msg => {
        // 修复 1: 只要发送者 ID 不是当前聊天对象的 ID，那就是“我”发的
        const isSelf = String(msg.senderId) !== String(targetId)
        
        // 修复 2: 将后端的 msgType(1/2) 映射为前端的 type('text'/'image')
        let msgTypeStr = msg.type || 'text' 
        if (msg.msgType === 1) msgTypeStr = 'text'
        else if (msg.msgType === 2) msgTypeStr = 'image'
        else if (msg.msgType === 3) msgTypeStr = 'resume' // 如果后续有简历卡片
        else if (msg.msgType === 4) msgTypeStr = 'order'  // 如果后续有订单邀约

        return {
          ...msg,
          isSelf,
          type: msgTypeStr
        }
      }).reverse()

      if (reset) {
        messages.value = list
        scrollToBottom()
      } else {
        messages.value = [...list, ...messages.value]
      }
      hasMore.value = list.length === 20
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
    const oldScrollHeight = messageListRef.value.scrollHeight
    loadMessages().then(() => {
      nextTick(() => {
        const newScrollHeight = messageListRef.value.scrollHeight
        messageListRef.value.scrollTop = newScrollHeight - oldScrollHeight
      })
    })
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault() 
    sendMessage()
  }
}

const sendMessage = async () => {
  const content = inputText.value.trim()
  if (!content) return
  
  const targetId = route.params.id
  
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
    // 🚨 修复：严格对齐后端期望的 targetUserId 字段
    await sendMessageApi({
      targetUserId: targetId, 
      type: 'text',
      content
    })
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  }
}

const handleImageUpload = async (response) => {
  if (response.code === 200) {
    const targetId = route.params.id
    const imageUrl = response.data 
    
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
      await sendMessageApi({
        targetUserId: targetId,
        type: 'image',
        content: imageUrl
      })
    } catch (error) {
      ElMessage.error(error.message || '发送失败')
    }
  }
}

const connectWebSocket = () => {
  const token = userStore.token
  const wsUrl = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080'}/ws/chat?token=${token}`

  ws = new WebSocket(wsUrl)

  ws.onopen = () => {}

  ws.onmessage = (event) => {
    const data = JSON.parse(event.data)
    if (String(data.targetId) === String(route.params.id) || String(data.senderId) === String(route.params.id)) {

      let msgTypeStr = data.type || 'text'
      if (data.msgType === 1) msgTypeStr = 'text'
      else if (data.msgType === 2) msgTypeStr = 'image'

      messages.value.push({
        ...data,
        type: msgTypeStr,
        isSelf: String(data.senderId) !== String(route.params.id)
      })
      scrollToBottom()

      if (String(data.senderId) !== String(userStore.userId)) {
        markAsRead(String(data.senderId)).catch(() => {})
      }
    } else if (String(data.targetId) === String(userStore.userId)) {
      chatStore.setUnreadCount(chatStore.unreadCount + 1)
    }
  }

  ws.onclose = () => {
    reconnectTimer = setTimeout(connectWebSocket, 3000)
  }
}

const viewProfile = () => {
  const targetId = Number(route.params.id)
  if (targetId) router.push(`/user/${targetId}`)
}
const clearMessages = () => { messages.value = [] }
const goBack = () => router.back()

onMounted(() => {
  loadTargetUserInfo() // 🚨 加载对方信息兜底
  loadMessages(true)
  connectWebSocket()
})

onUnmounted(() => {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  
  if (ws) {
    ws.onclose = null 
    ws.close()
    ws = null
  }

  chatStore.refreshUnreadCount()
})
</script>

<style lang="scss" scoped>
.chat-page-container {
  height: calc(100vh - 64px); 
  padding: 20px;
  box-sizing: border-box;
  background-color: #f0f2f5;
  display: flex;
  justify-content: center;
}

.chat-window {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 850px; 
  height: 100%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .back-btn {
      border: none;
      font-size: 18px;
    }
    
    .user-info {
      .user-name {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
      
      .online-status {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        color: #67c23a;
        margin-top: 4px;
        
        .dot {
          width: 6px;
          height: 6px;
          background-color: #67c23a;
          border-radius: 50%;
        }
      }
    }
  }
  
  .more-btn {
    border: none;
    font-size: 18px;
    color: #606266;
  }
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f5f7fa; 
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: #c0c4cc;
    border-radius: 4px;
  }
  
  .loading-more {
    text-align: center;
    padding: 10px;
    color: #909399;
    font-size: 13px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }
  
  .time-label {
    text-align: center;
    margin: 16px 0;
    span {
      display: inline-block;
      padding: 4px 10px;
      font-size: 12px;
      color: #fff;
      background-color: #dadce0;
      border-radius: 4px;
    }
  }
  
  .message-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 20px;
    
    .avatar {
      flex-shrink: 0;
    }
    
    .message-content {
      max-width: 65%;
      margin: 0 12px;
      
      .message-sender {
        font-size: 12px;
        color: #909399;
        margin-bottom: 4px;
        margin-left: 2px;
      }
      
      .bubble {
        position: relative;
        padding: 10px 14px;
        border-radius: 8px;
        font-size: 14px;
        line-height: 1.6;
        word-break: break-word;
        box-shadow: 0 1px 2px rgba(0,0,0,0.05);
        
        &.text {
          background-color: #fff;
          color: #303133;
          &::before {
            content: '';
            position: absolute;
            top: 12px;
            left: -6px;
            border-width: 6px 8px 6px 0;
            border-style: solid;
            border-color: transparent #fff transparent transparent;
          }
        }
        
        &.image {
          padding: 0;
          background: transparent;
          box-shadow: none;
          .el-image {
            max-width: 250px;
            border-radius: 8px;
            border: 1px solid #ebeef5;
          }
        }
      }
    }
    
    &.is-self {
      flex-direction: row-reverse;
      
      .message-content {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        
        .bubble.text {
          background-color: #95ec69; 
          color: #000;
          &::before {
            display: none;
          }
          &::after {
            content: '';
            position: absolute;
            top: 12px;
            right: -6px;
            border-width: 6px 0 6px 8px;
            border-style: solid;
            border-color: transparent transparent transparent #95ec69;
          }
        }
      }
    }
  }
}

.chat-footer {
  height: 180px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  
  .toolbar {
    height: 40px;
    padding: 0 16px;
    display: flex;
    align-items: center;
    
    .upload-btn {
      color: #606266;
      cursor: pointer;
      display: flex;
      align-items: center;
      transition: color 0.3s;
      
      &:hover {
        color: var(--el-color-primary);
      }
    }
  }
  
  .input-area {
    flex: 1;
    padding: 0 16px;
    
    :deep(.el-textarea__inner) {
      border: none !important;
      box-shadow: none !important;
      padding: 0;
      font-size: 14px;
      font-family: inherit;
      background-color: transparent;
      
      &::-webkit-scrollbar {
        width: 6px;
      }
      &::-webkit-scrollbar-thumb {
        background: #dcdfe6;
        border-radius: 4px;
      }
    }
  }
  
  .action-bar {
    height: 50px;
    padding: 0 20px;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 16px;
    
    .tips {
      font-size: 12px;
      color: #c0c4cc;
    }
    
    .send-btn {
      padding: 8px 24px;
      border-radius: 4px;
    }
  }
}
</style>