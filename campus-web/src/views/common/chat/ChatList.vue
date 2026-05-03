<template>
  <div class="chat-list-page">
    <div class="chat-wrapper">
      <div class="page-header">
        <div class="title-wrapper">
          <el-icon :size="24" color="#409EFC"><ChatDotRound /></el-icon>
          <h1 class="page-title">消息中心</h1>
        </div>
        <span class="sub-title" v-if="activeTab === 'chat'">共 {{ conversations.length }} 个会话</span>
        <el-button v-if="activeTab === 'notify'" text size="small" @click="markAllRead">
          全部已读
        </el-button>
      </div>

      <!-- Tab 切换 -->
      <div class="tab-bar">
        <div
          class="tab-item"
          :class="{ active: activeTab === 'chat' }"
          @click="switchTab('chat')"
        >
          <span>私聊消息</span>
          <span v-if="chatStore.unreadCount > 0" class="tab-badge">
            {{ chatStore.unreadCount > 99 ? '99+' : chatStore.unreadCount }}
          </span>
        </div>
        <div
          class="tab-item"
          :class="{ active: activeTab === 'notify' }"
          @click="switchTab('notify')"
        >
          <span>互动消息</span>
          <span v-if="notifyUnreadCount > 0" class="tab-badge notify-badge">
            {{ notifyUnreadCount > 99 ? '99+' : notifyUnreadCount }}
          </span>
        </div>
      </div>

      <!-- 私聊消息列表 -->
      <div v-show="activeTab === 'chat'" v-loading="loading" class="chat-container">
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
        <el-empty v-else description="暂无聊天记录" :image-size="120">
          <template #image>
            <el-icon :size="60" color="#C0C4CC"><Message /></el-icon>
          </template>
        </el-empty>
      </div>

      <!-- 互动消息列表 -->
      <div v-show="activeTab === 'notify'" v-loading="notifyLoading" class="chat-container">
        <div v-if="notifications.length" class="conversation-list">
          <div
            v-for="n in notifications"
            :key="n.id"
            class="conversation-item notify-item"
            :class="{ 'is-unread': !n.isRead }"
            @click="handleNotifyClick(n)"
          >
            <div class="avatar-wrapper">
              <el-avatar :size="44" :src="n.fromUserAvatar || undefined" class="user-avatar">
                {{ n.fromUserNickname?.charAt(0) || '?' }}
              </el-avatar>
              <span v-if="!n.isRead" class="unread-dot" />
            </div>
            <div class="conv-content">
              <div class="conv-header">
                <h4 class="nickname">
                  <span class="notify-from">{{ n.fromUserNickname || '未知用户' }}</span>
                  <span class="notify-action">{{ n.type === 1 ? '评论了你的帖子' : '回复了你的评论' }}</span>
                </h4>
                <span class="time">{{ formatTime(n.createTime) }}</span>
              </div>
              <div class="conv-footer">
                <p class="last-message notify-summary">
                  {{ n.contentSummary || '' }}
                </p>
                <span class="notify-post-title" v-if="n.postTitle">
                  来自：{{ n.postTitle }}
                </span>
              </div>
            </div>
            <el-icon class="notify-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
        <el-empty v-else description="暂无互动消息" :image-size="120">
          <template #image>
            <el-icon :size="60" color="#C0C4CC"><Bell /></el-icon>
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
import { ChatDotRound, Message, Bell, ArrowRight } from '@element-plus/icons-vue'
import { useChatStore } from '@shared/stores'
import { getConversations } from '@shared/api/chat'
import {
  getCommunityNotifications,
  markNotificationRead,
  markAllNotificationsRead
} from '@shared/api/community'
import dayjs from 'dayjs'

const router = useRouter()
const chatStore = useChatStore()

const loading = ref(false)
const conversations = ref([])
const activeTab = ref('chat')

// 互动消息状态
const notifyLoading = ref(false)
const notifications = ref([])
const notifyUnreadCount = ref(0)

const formatTime = (time) => {
  if (!time) return ''
  const date = dayjs(time)
  const now = dayjs()
  if (date.isSame(now, 'day')) return date.format('HH:mm')
  if (date.isSame(now.subtract(1, 'day'), 'day')) return '昨天'
  if (date.isSame(now, 'year')) return date.format('MM-DD')
  return date.format('YYYY-MM-DD')
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

const loadNotifications = async () => {
  notifyLoading.value = true
  try {
    const res = await getCommunityNotifications({ page: 1, size: 50 })
    if (res.code === 200) {
      notifications.value = res.data?.records || []
      notifyUnreadCount.value = notifications.value.filter(n => !n.isRead).length
    }
  } catch {
    // 静默处理
  } finally {
    notifyLoading.value = false
  }
}

const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'notify') {
    loadNotifications()
  }
}

const openChat = (conv) => {
  if (conv.targetUserId) {
    router.push(`/chat/${conv.targetUserId}`)
  } else {
    ElMessage.warning('无效的用户会话')
  }
}

const handleNotifyClick = async (n) => {
  // 标记已读
  if (!n.isRead) {
    try {
      await markNotificationRead(n.id)
      n.isRead = 1
      notifyUnreadCount.value = Math.max(0, notifyUnreadCount.value - 1)
    } catch { /* ignore */ }
  }
  // 跳转到帖子详情（带 replyId 定位）
  const query = n.replyId ? { replyId: n.replyId } : {}
  router.push({ path: `/community/${n.postId}`, query })
}

const markAllRead = async () => {
  try {
    await markAllNotificationsRead()
    notifications.value.forEach(n => { n.isRead = 1 })
    notifyUnreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadConversations()
  loadNotifications()
})
</script>

<style lang="scss" scoped>
.chat-list-page {
  min-height: calc(100vh - 64px);
  background-color: #f4f6f8;
  padding: 24px;
  display: flex;
  justify-content: center;
  align-items: flex-start;

  .chat-wrapper {
    width: 100%;
    max-width: 850px;
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
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
      .page-title { margin: 0; font-size: 20px; font-weight: 600; color: #1d2129; letter-spacing: 0.5px; }
    }
    .sub-title { font-size: 13px; color: #86909c; }
  }

  // Tab 切换栏
  .tab-bar {
    display: flex;
    border-bottom: 1px solid #f0f2f5;
    background: #fff;
    .tab-item {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      padding: 14px 0;
      font-size: 15px;
      color: #606266;
      cursor: pointer;
      position: relative;
      transition: color 0.2s;
      &.active {
        color: #409EFF;
        font-weight: 600;
        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 20%;
          right: 20%;
          height: 2px;
          background: #409EFF;
          border-radius: 1px;
        }
      }
      .tab-badge {
        min-width: 16px;
        height: 16px;
        padding: 0 5px;
        font-size: 10px;
        font-weight: bold;
        color: #fff;
        background: #f53f3f;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        &.notify-badge { background: #f97316; }
      }
    }
  }

  .chat-container { min-height: 400px; background-color: #ffffff; }
  .conversation-list { display: flex; flex-direction: column; }

  .conversation-item {
    display: flex;
    align-items: center;
    padding: 16px 24px;
    cursor: pointer;
    transition: all 0.2s ease;
    border-bottom: 1px solid #f2f3f5;
    &:last-child { border-bottom: none; }
    &:hover { background-color: #f7f8fa; transform: translateX(4px); }

    .avatar-wrapper {
      position: relative;
      margin-right: 16px;
      .user-avatar { border: 1px solid #f0f2f5; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
      .unread-badge {
        position: absolute; top: -4px; right: -4px; min-width: 18px; height: 18px;
        padding: 0 5px; font-size: 11px; font-weight: bold; color: #fff;
        background: #f53f3f; border-radius: 9px; display: flex; align-items: center;
        justify-content: center; box-shadow: 0 2px 4px rgba(245, 63, 63, 0.3);
        border: 2px solid #fff;
      }
      .unread-dot {
        position: absolute; top: 0; right: -2px; width: 10px; height: 10px;
        background: #f53f3f; border-radius: 50%; border: 2px solid #fff;
      }
    }

    .conv-content {
      flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: center; gap: 4px;
      .conv-header {
        display: flex; justify-content: space-between; align-items: center;
        .nickname { margin: 0; font-size: 15px; font-weight: 500; color: #1d2129; }
        .time { font-size: 12px; color: #86909c; white-space: nowrap; }
      }
      .conv-footer {
        display: flex; flex-direction: column; gap: 2px;
        .last-message {
          margin: 0; font-size: 13px; color: #86909c;
          white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
          &.is-unread { color: #4e5969; font-weight: 500; }
        }
      }
    }
  }

  // 互动消息专属样式
  .notify-item {
    .notify-from { color: #303133; font-weight: 600; }
    .notify-action { color: #606266; font-weight: 400; margin-left: 4px; }
    .notify-summary {
      color: #909399 !important;
      max-width: 400px;
    }
    .notify-post-title { font-size: 12px; color: #409EFF; }
    .notify-arrow { color: #c0c4cc; font-size: 16px; flex-shrink: 0; }
    &.is-unread {
      background: #f0f7ff;
    }
  }

  :deep(.el-empty) { padding: 60px 0; }
}
</style>
