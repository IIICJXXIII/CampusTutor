<template>
  <div class="chat-list-page">
    <div class="chat-wrapper">
      <div class="page-header">
        <div class="title-wrapper">
          <el-icon :size="24" color="#409EFC"><ChatDotRound /></el-icon>
          <h1 class="page-title">消息中心</h1>
        </div>
        <span class="sub-title">共 {{ conversations.length }} 个会话</span>
      </div>
      
      <div v-loading="loading" class="chat-container">
        <div v-if="conversations.length" class="conversation-list">
          <div
            v-for="conv in conversations"
            :key="conv.targetUserId"
            class="conversation-item"
            @click="openChat(conv)"
          >
            <div class="avatar-wrapper">
              <el-avatar :size="50" :src="conv.targetAvatar" class="user-avatar">
                {{ conv.targetNickname?.charAt(0) || 'U' }}
              </el-avatar>
              <transition name="el-zoom-in-center">
                <span v-if="conv.unreadCount > 0" class="unread-badge">
                  {{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}
                </span>
              </transition>
            </div>
            
            <div class="conv-content">
              <div class="conv-header">
                <h4 class="nickname">{{ conv.targetNickname || '未知用户' }}</h4>
                <span class="time">{{ formatTime(conv.lastTime) }}</span>
              </div>
              <div class="conv-footer">
                <p class="last-message" :class="{'is-unread': conv.unreadCount > 0}">
                  {{ conv.lastMessage || '暂无新消息' }}
                </p>
              </div>
            </div>
          </div>
        </div>
        
        <el-empty 
          v-else 
          description="暂无聊天记录，快去寻找心仪的教员吧" 
          :image-size="160"
        >
          <template #image>
            <el-icon :size="60" color="#C0C4CC"><Message /></el-icon>
          </template>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Message } from '@element-plus/icons-vue' // 引入了图标
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
    return date.format('MM-DD')
  } else {
    return date.format('YYYY-MM-DD')
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
  if (conv.targetUserId) {
    router.push(`/chat/${conv.targetUserId}`)
  } else {
    ElMessage.warning('无效的用户会话')
  }
}

onMounted(() => {
  loadConversations()
})
</script>

<style lang="scss" scoped>
.chat-list-page {
  /* 整个页面加个浅灰底色，凸显中间的白色卡片 */
  min-height: calc(100vh - 64px); // 根据你的顶部导航栏高度调整
  background-color: #f4f6f8;
  padding: 24px;
  display: flex;
  justify-content: center;
  align-items: flex-start;

  .chat-wrapper {
    width: 100%;
    max-width: 850px; // 限制最大宽度，居中显示，桌面端体验更好
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04); // 柔和的阴影
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 1px solid #f0f2f5;
    background: #fafbfc;
    
    .title-wrapper {
      display: flex;
      align-items: center;
      gap: 10px;
      
      .page-title {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #1d2129;
        letter-spacing: 0.5px;
      }
    }

    .sub-title {
      font-size: 13px;
      color: #86909c;
    }
  }

  .chat-container {
    min-height: 400px;
    background-color: #ffffff;
  }
  
  .conversation-list {
    display: flex;
    flex-direction: column;
  }
  
  .conversation-item {
    display: flex;
    align-items: center;
    padding: 18px 24px;
    cursor: pointer;
    transition: all 0.2s ease;
    border-bottom: 1px solid #f2f3f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    /* 悬浮态特效 */
    &:hover {
      background-color: #f7f8fa;
      transform: translateX(4px); // 轻微的右移特效
    }
    
    .avatar-wrapper {
      position: relative;
      margin-right: 16px;
      
      .user-avatar {
        border: 1px solid #f0f2f5;
        box-shadow: 0 2px 8px rgba(0,0,0,0.05);
      }
      
      .unread-badge {
        position: absolute;
        top: -4px;
        right: -4px;
        min-width: 18px;
        height: 18px;
        padding: 0 5px;
        font-size: 11px;
        font-weight: bold;
        color: #fff;
        background: #f53f3f;
        border-radius: 9px;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2px 4px rgba(245, 63, 63, 0.3);
        border: 2px solid #fff; // 描边使其在头像上更突出
      }
    }
    
    .conv-content {
      flex: 1;
      min-width: 0; // 配合 overflow hidden 防止撑破
      display: flex;
      flex-direction: column;
      justify-content: center;
      gap: 6px;
      
      .conv-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .nickname {
          margin: 0;
          font-size: 16px;
          font-weight: 500;
          color: #1d2129;
        }
        
        .time {
          font-size: 12px;
          color: #86909c;
        }
      }
      
      .conv-footer {
        display: flex;
        align-items: center;

        .last-message {
          margin: 0;
          font-size: 14px;
          color: #86909c;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          
          /* 有未读消息时，最后一条消息颜色加深 */
          &.is-unread {
            color: #4e5969;
            font-weight: 500;
          }
        }
      }
    }
  }

  // 修改 el-empty 的默认内边距
  :deep(.el-empty) {
    padding: 60px 0;
  }
}
</style>