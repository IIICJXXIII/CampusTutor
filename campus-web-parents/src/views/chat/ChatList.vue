<template>
  <div class="chat-list-page">
    <div class="page-header">
      <h1 class="page-title">消息</h1>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="conversations.length === 0" class="empty-container">
      <el-empty description="暂无消息" />
    </div>
    
    <div v-else class="conversation-list">
      <div
        v-for="conv in conversations"
        :key="conv.id"
        class="conversation-item"
        :class="{ unread: conv.unreadCount > 0 }"
        @click="openChat(conv)"
      >
        <el-avatar :size="52" :src="conv.avatar">
          {{ conv.name?.charAt(0) }}
        </el-avatar>
        
        <div class="conv-content">
          <div class="conv-header">
            <span class="conv-name">{{ conv.name }}</span>
            <span class="conv-time">{{ formatTime(conv.lastTime) }}</span>
          </div>
          <div class="conv-message">
            <span class="message-text">{{ conv.lastMessage }}</span>
            <el-badge v-if="conv.unreadCount > 0" :value="conv.unreadCount" :max="99" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getConversations } from '@shared/api/chat'
import { smartTime } from '@shared/utils'

const router = useRouter()

const loading = ref(false)
const conversations = ref([])
let refreshTimer = null

const formatTime = (time) => smartTime(time)

const loadConversations = async () => {
  loading.value = true
  try {
    const res = await getConversations()
    if (res.code === 200) {
      conversations.value = res.data || []
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  } finally {
    loading.value = false
  }
}

const openChat = (conv) => {
  router.push({
    path: `/chat/${conv.targetUserId}`,
    query: { name: conv.name }
  })
}

onMounted(() => {
  loadConversations()
  // 定时刷新
  refreshTimer = setInterval(loadConversations, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
.chat-list-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.loading-container,
.empty-container {
  padding: 60px 0;
}

.conversation-list {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: background 0.2s;
  
  &:not(:last-child) {
    border-bottom: 1px solid #f5f5f5;
  }
  
  &:hover {
    background: #f9f9f9;
  }
  
  &.unread {
    background: #fafafa;
  }
  
  .conv-content {
    flex: 1;
    min-width: 0;
  }
  
  .conv-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
    
    .conv-name {
      font-weight: 600;
      font-size: 15px;
    }
    
    .conv-time {
      font-size: 12px;
      color: #999;
    }
  }
  
  .conv-message {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .message-text {
      flex: 1;
      font-size: 14px;
      color: #666;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}
</style>
