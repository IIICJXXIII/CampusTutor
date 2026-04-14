<template>
  <div class="chat-list">
    <div class="page-header">
      <h1 class="page-title">消息</h1>
    </div>
    
    <div v-loading="loading" class="chat-container">
      <div v-if="conversations.length" class="conversation-list">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="conversation-item"
          @click="openChat(conv)"
        >
          <div class="avatar-wrapper">
            <el-avatar :size="48" :src="conv.avatar">
              {{ conv.name?.charAt(0) }}
            </el-avatar>
            <span v-if="conv.unread > 0" class="unread-badge">
              {{ conv.unread > 99 ? '99+' : conv.unread }}
            </span>
          </div>
          
          <div class="conv-content">
            <div class="conv-header">
              <h4>{{ conv.name }}</h4>
              <span class="time">{{ formatTime(conv.lastTime) }}</span>
            </div>
            <p class="last-message">{{ conv.lastMessage || '暂无消息' }}</p>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无消息" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useChatStore } from '@shared/stores'
import { getConversations } from '@shared/api/chat'
import dayjs from 'dayjs'

const router = useRouter()
const chatStore = useChatStore()

const loading = ref(false)
const conversations = ref([])

const formatTime = (time) => {
  if (!time) return ''
  const date = dayjs(time)
  const now = dayjs()
  
  if (date.isSame(now, 'day')) {
    return date.format('HH:mm')
  } else if (date.isSame(now.subtract(1, 'day'), 'day')) {
    return '昨天'
  } else if (date.isSame(now, 'year')) {
    return date.format('M/D')
  } else {
    return date.format('YYYY/M/D')
  }
}

const loadConversations = async () => {
  loading.value = true
  try {
    const res = await getConversations()
    if (res.code === 200) {
      conversations.value = res.data || []
      chatStore.setConversations(res.data)
    }
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const openChat = (conv) => {
  router.push(`/chat/${conv.targetId}`)
}

onMounted(() => {
  loadConversations()
})
</script>

<style lang="scss" scoped>
.chat-list {
  .chat-container {
    margin-top: 16px;
  }
  
  .conversation-list {
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
  }
  
  .conversation-item {
    display: flex;
    align-items: center;
    padding: 16px 20px;
    cursor: pointer;
    transition: background-color 0.2s;
    border-bottom: 1px solid #ebeef5;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:hover {
      background-color: #f5f7fa;
    }
    
    .avatar-wrapper {
      position: relative;
      margin-right: 12px;
      
      .unread-badge {
        position: absolute;
        top: -4px;
        right: -4px;
        min-width: 18px;
        height: 18px;
        padding: 0 6px;
        font-size: 12px;
        font-weight: 500;
        color: #fff;
        background: #f56c6c;
        border-radius: 9px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
    
    .conv-content {
      flex: 1;
      min-width: 0;
      
      .conv-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 6px;
        
        h4 {
          font-size: 15px;
          font-weight: 500;
        }
        
        .time {
          font-size: 12px;
          color: #909399;
        }
      }
      
      .last-message {
        font-size: 13px;
        color: #909399;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }
}
</style>
