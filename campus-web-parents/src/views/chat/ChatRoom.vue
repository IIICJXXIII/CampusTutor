<template>
  <div class="chat-room-page">
    <div class="chat-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <div class="chat-title">{{ chatName }}</div>
      <el-button link @click="showUserInfo">
        <el-icon><User /></el-icon>
      </el-button>
    </div>
    
    <div ref="messageContainer" class="message-container" @scroll="handleScroll">
      <div v-if="loadingMore" class="loading-more">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
      </div>
      
      <div
        v-for="(msg, index) in messages"
        :key="msg.id"
        class="message-wrapper"
      >
        <!-- 时间分隔线 -->
        <div v-if="showTimeDivider(index)" class="time-divider">
          {{ formatTimeDivider(msg.createTime) }}
        </div>
        
        <!-- 消息 -->
        <div class="message-item" :class="{ 'is-self': msg.senderId === userId }">
          <el-avatar 
            v-if="msg.senderId !== userId" 
            :size="40" 
            :src="targetAvatar"
          >
            {{ chatName?.charAt(0) }}
          </el-avatar>
          
          <div class="message-content">
            <!-- 文本消息 -->
            <div v-if="msg.type === 'text'" class="message-bubble">
              {{ msg.content }}
            </div>
            <!-- 图片消息 -->
            <div v-else-if="msg.type === 'image'" class="message-image">
              <el-image
                :src="msg.content"
                fit="cover"
                :preview-src-list="[msg.content]"
                preview-teleported
              />
            </div>
          </div>
          
          <el-avatar 
            v-if="msg.senderId === userId" 
            :size="40" 
            :src="userAvatar"
          >
            {{ userName?.charAt(0) }}
          </el-avatar>
        </div>
      </div>
    </div>
    
    <div class="input-bar">
      <el-button circle @click="showEmojiPicker = !showEmojiPicker">
        <el-icon><Smile /></el-icon>
      </el-button>
      
      <el-upload
        :show-file-list="false"
        accept="image/*"
        :before-upload="handleUploadImage"
      >
        <el-button circle>
          <el-icon><Picture /></el-icon>
        </el-button>
      </el-upload>
      
      <el-input
        v-model="inputText"
        placeholder="输入消息..."
        @keyup.enter="sendMessage"
      />
      
      <el-button type="primary" :disabled="!inputText.trim()" @click="sendMessage">
        发送
      </el-button>
    </div>
    
    <!-- 表情选择器 -->
    <div v-if="showEmojiPicker" class="emoji-picker">
      <div
        v-for="emoji in emojis"
        :key="emoji"
        class="emoji-item"
        @click="insertEmoji(emoji)"
      >
        {{ emoji }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@shared/stores'
import { ElMessage } from 'element-plus'
import { ArrowLeft, User, Loading, Smile, Picture } from '@element-plus/icons-vue'
import { getChatHistory, sendMessage as sendApi } from '@shared/api/chat'
import { uploadFile } from '@shared/api/file'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const messageContainer = ref(null)
const messages = ref([])
const inputText = ref('')
const loadingMore = ref(false)
const showEmojiPicker = ref(false)
const hasMore = ref(true)
const page = ref(1)
const targetAvatar = ref('')

const userId = computed(() => userStore.user?.id)
const userName = computed(() => userStore.user?.name)
const userAvatar = computed(() => userStore.user?.avatar)
const chatName = computed(() => route.query.name || '聊天')

const emojis = ['😀', '😊', '😄', '🤣', '😂', '😅', '😆', '😁', '🙂', '😉', 
  '😍', '🥰', '😘', '😋', '😎', '🤔', '😐', '😑', '😶', '🙄',
  '😏', '😬', '😌', '😔', '😢', '😭', '😤', '😡', '🤬', '😱',
  '👍', '👎', '👌', '✌️', '🤞', '🤝', '👏', '🙏', '❤️', '💔']

const goBack = () => {
  router.back()
}

const showUserInfo = () => {
  router.push(`/teachers/${route.params.targetUserId}`)
}

const loadMessages = async (loadMore = false) => {
  if (loadingMore.value || (!hasMore.value && loadMore)) return
  
  loadingMore.value = true
  try {
    const res = await getChatHistory(route.params.targetUserId, {
      page: loadMore ? page.value + 1 : 1,
      size: 20
    })
    
    if (res.code === 200) {
      const newMessages = res.data?.records || []
      if (loadMore) {
        messages.value = [...newMessages.reverse(), ...messages.value]
        page.value++
      } else {
        messages.value = newMessages.reverse()
        scrollToBottom()
      }
      hasMore.value = newMessages.length >= 20
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  } finally {
    loadingMore.value = false
  }
}

const handleScroll = () => {
  if (messageContainer.value && messageContainer.value.scrollTop < 50 && hasMore.value) {
    loadMessages(true)
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  if (!inputText.value.trim()) return
  
  try {
    const res = await sendApi({
      targetUserId: route.params.targetUserId,
      type: 'text',
      content: inputText.value.trim()
    })
    
    if (res.code === 200) {
      messages.value.push({
        id: Date.now(),
        senderId: userId.value,
        content: inputText.value.trim(),
        type: 'text',
        createTime: new Date().toISOString()
      })
      inputText.value = ''
      showEmojiPicker.value = false
      scrollToBottom()
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败')
  }
}

const handleUploadImage = async (file) => {
  try {
    const res = await uploadFile(file)
    if (res.code === 200) {
      await sendApi({
        targetUserId: route.params.targetUserId,
        type: 'image',
        content: res.data.url
      })
      messages.value.push({
        id: Date.now(),
        senderId: userId.value,
        content: res.data.url,
        type: 'image',
        createTime: new Date().toISOString()
      })
      scrollToBottom()
    }
  } catch (error) {
    ElMessage.error('图片发送失败')
  }
  return false
}

const insertEmoji = (emoji) => {
  inputText.value += emoji
  showEmojiPicker.value = false
}

const showTimeDivider = (index) => {
  if (index === 0) return true
  const current = dayjs(messages.value[index].createTime)
  const prev = dayjs(messages.value[index - 1].createTime)
  return current.diff(prev, 'minute') > 5
}

const formatTimeDivider = (time) => {
  const msgTime = dayjs(time)
  const now = dayjs()
  
  if (now.isSame(msgTime, 'day')) {
    return msgTime.format('HH:mm')
  } else if (now.subtract(1, 'day').isSame(msgTime, 'day')) {
    return `昨天 ${msgTime.format('HH:mm')}`
  } else if (now.isSame(msgTime, 'year')) {
    return msgTime.format('MM-DD HH:mm')
  }
  return msgTime.format('YYYY-MM-DD HH:mm')
}

// WebSocket 连接
let ws = null

const connectWebSocket = () => {
  const wsUrl = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080'}/ws/chat?userId=${userId.value}`
  ws = new WebSocket(wsUrl)
  
  ws.onmessage = (event) => {
    const data = JSON.parse(event.data)
    if (data.senderId === parseInt(route.params.targetUserId)) {
      messages.value.push(data)
      scrollToBottom()
    }
  }
  
  ws.onerror = (error) => {
    console.error('WebSocket error:', error)
  }
  
  ws.onclose = () => {
    // 断线重连
    setTimeout(connectWebSocket, 3000)
  }
}

onMounted(() => {
  loadMessages()
  if (userId.value) {
    connectWebSocket()
  }
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
})
</script>

<style lang="scss" scoped>
.chat-room-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  .chat-title {
    font-size: 17px;
    font-weight: 600;
  }
}

.message-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  
  .loading-more {
    text-align: center;
    padding: 12px;
    color: #999;
    font-size: 13px;
  }
}

.message-wrapper {
  margin-bottom: 16px;
}

.time-divider {
  text-align: center;
  padding: 8px 0;
  font-size: 12px;
  color: #999;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  
  &.is-self {
    flex-direction: row-reverse;
    
    .message-bubble {
      background: #409eff;
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
    line-height: 1.5;
    word-break: break-word;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  }
  
  .message-image {
    .el-image {
      max-width: 200px;
      max-height: 200px;
      border-radius: 8px;
      overflow: hidden;
    }
  }
}

.input-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  
  .el-input {
    flex: 1;
  }
}

.emoji-picker {
  position: fixed;
  bottom: 60px;
  left: 0;
  right: 0;
  background: #fff;
  padding: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
  
  .emoji-item {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    cursor: pointer;
    border-radius: 6px;
    
    &:hover {
      background: #f5f5f5;
    }
  }
}
</style>
