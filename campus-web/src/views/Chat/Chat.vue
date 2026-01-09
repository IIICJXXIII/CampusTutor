<template>
  <div class="chat-container">
    <!-- 左侧会话列表 -->
    <div class="session-panel">
      <div class="session-header">
        <h3>消息</h3>
        <el-badge :value="totalUnread" :hidden="totalUnread === 0" class="unread-badge">
          <el-icon><Bell /></el-icon>
        </el-badge>
      </div>
      
      <div class="session-list">
        <div 
          v-for="session in sessions" 
          :key="session.targetUserId"
          class="session-item"
          :class="{ active: activeUserId === session.targetUserId }"
          @click="selectSession(session)"
        >
          <el-avatar :size="48" :src="session.targetAvatar || defaultAvatar">
            {{ session.targetNickname?.charAt(0) }}
          </el-avatar>
          <div class="session-info">
            <div class="session-top">
              <span class="session-name">{{ session.targetNickname || '未知用户' }}</span>
              <span class="session-time">{{ formatTime(session.lastTime) }}</span>
            </div>
            <div class="session-bottom">
              <span class="session-message">{{ session.lastMessage || '暂无消息' }}</span>
              <el-badge 
                v-if="session.unreadCount > 0" 
                :value="session.unreadCount" 
                class="item-badge"
              />
            </div>
          </div>
        </div>
        
        <el-empty v-if="sessions.length === 0" description="暂无会话" />
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-panel">
      <template v-if="activeUserId">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="chat-user-info">
            <el-avatar :size="40" :src="activeSession?.targetAvatar || defaultAvatar">
              {{ activeSession?.targetNickname?.charAt(0) }}
            </el-avatar>
            <div class="chat-user-detail">
              <span class="chat-user-name">{{ activeSession?.targetNickname }}</span>
              <span class="chat-user-role">{{ getRoleName(activeSession?.targetRole) }}</span>
            </div>
          </div>
          <div class="chat-status" :class="{ online: isOnline }">
            {{ isOnline ? '在线' : '离线' }}
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef">
          <div 
            v-for="msg in messages" 
            :key="msg.id"
            class="message-item"
            :class="{ 'message-self': msg.senderId === currentUserId }"
          >
            <el-avatar 
              :size="36" 
              :src="msg.senderId === currentUserId ? currentUserAvatar : activeSession?.targetAvatar"
            >
              {{ msg.senderId === currentUserId ? currentUserName?.charAt(0) : activeSession?.targetNickname?.charAt(0) }}
            </el-avatar>
            <div class="message-content">
              <div class="message-bubble">{{ msg.content }}</div>
              <div class="message-time">{{ formatMessageTime(msg.createTime) }}</div>
            </div>
          </div>
          
          <el-empty v-if="messages.length === 0" description="暂无消息，开始聊天吧" />
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keyup.enter.ctrl="sendMessage"
          />
          <div class="input-actions">
            <span class="input-tip">Ctrl + Enter 发送</span>
            <el-button type="primary" @click="sendMessage" :disabled="!inputMessage.trim()">
              发送
            </el-button>
          </div>
        </div>
      </template>

      <template v-else>
        <div class="chat-empty">
          <el-empty description="选择一个会话开始聊天" />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Bell } from '@element-plus/icons-vue';
import { getSessionList, getChatHistory, markAsRead, getChatUserInfo } from '@/api/chat';
import websocket from '@/utils/websocket';

const route = useRoute();
const router = useRouter();

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 状态
const sessions = ref([]);
const messages = ref([]);
const activeUserId = ref(null);
const activeSession = ref(null);
const inputMessage = ref('');
const isOnline = ref(false);
const messageListRef = ref(null);

// 当前用户信息
const currentUserId = ref(null);
const currentUserName = ref('');
const currentUserAvatar = ref('');

// 计算总未读数
const totalUnread = computed(() => {
  return sessions.value.reduce((sum, s) => sum + (s.unreadCount || 0), 0);
});

// 初始化
onMounted(async () => {
  // 获取当前用户信息
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
  currentUserId.value = Number(userInfo.id); // 确保是数字类型
  currentUserName.value = userInfo.nickname || userInfo.username;
  currentUserAvatar.value = userInfo.avatar || userInfo.avatarUrl;

  // 加载会话列表
  await loadSessions();

  // 连接 WebSocket
  websocket.connect();

  // 注册消息处理器
  websocket.onMessage(handleWebSocketMessage);

  // 注册连接状态处理器
  websocket.onConnectionChange((status) => {
    isOnline.value = status === 'connected';
  });

  // 如果 URL 中有目标用户ID，直接打开聊天
  if (route.params.targetUserId) {
    await openChatWithUser(Number(route.params.targetUserId));
  }
});

onUnmounted(() => {
  // 组件卸载时不断开 WebSocket，保持全局连接
});

// 监听路由变化
watch(() => route.params.targetUserId, async (newUserId) => {
  if (newUserId) {
    await openChatWithUser(Number(newUserId));
  }
});

// 加载会话列表
async function loadSessions() {
  try {
    const res = await getSessionList();
    sessions.value = res.data || [];
  } catch (error) {
    console.error('加载会话列表失败:', error);
  }
}

// 选择会话
async function selectSession(session) {
  activeUserId.value = session.targetUserId;
  activeSession.value = session;

  // 加载聊天历史
  await loadChatHistory(session.targetUserId);

  // 标记已读
  if (session.unreadCount > 0) {
    await markAsRead(session.targetUserId);
    session.unreadCount = 0;
    websocket.sendReadNotification(session.targetUserId);
  }

  // 更新路由
  router.replace({ path: `/chat/${session.targetUserId}` });
}

// 通过用户ID打开聊天
async function openChatWithUser(userId) {
  // 先在会话列表中查找
  let session = sessions.value.find(s => s.targetUserId === userId);

  if (!session) {
    // 会话不存在，获取用户信息创建新会话
    try {
      const res = await getChatUserInfo(userId);
      session = {
        targetUserId: userId,
        targetNickname: res.data.nickname,
        targetAvatar: res.data.avatar,
        targetRole: res.data.role,
        lastMessage: '',
        lastTime: null,
        unreadCount: 0
      };
      sessions.value.unshift(session);
    } catch (error) {
      ElMessage.error('获取用户信息失败');
      return;
    }
  }

  await selectSession(session);
}

// 加载聊天历史
async function loadChatHistory(targetUserId) {
  try {
    const res = await getChatHistory(targetUserId);
    // 倒序排列，最新的在下面
    messages.value = (res.data || []).reverse();
    
    // 滚动到底部
    await nextTick();
    scrollToBottom();
  } catch (error) {
    console.error('加载聊天历史失败:', error);
  }
}

// 发送消息
function sendMessage() {
  const content = inputMessage.value.trim();
  if (!content || !activeUserId.value) return;

  const sent = websocket.sendChatMessage(activeUserId.value, content);
  
  if (!sent) {
    ElMessage.warning('消息发送失败，请检查网络连接');
    return;
  }

  inputMessage.value = '';
}

// 处理 WebSocket 消息
function handleWebSocketMessage(msg) {
  switch (msg.type) {
    case 'message':
    case 'sent':
      handleNewMessage(msg.data);
      break;
    case 'read':
      // 对方已读通知
      break;
    case 'connected':
      console.log('WebSocket 连接成功');
      break;
    case 'pong':
      // 心跳响应
      break;
  }
}

// 处理新消息
function handleNewMessage(msgData) {
  // 判断是否是当前会话的消息
  const isSelf = msgData.senderId === currentUserId.value;
  const isCurrentChat = isSelf 
    ? msgData.receiverId === activeUserId.value
    : msgData.senderId === activeUserId.value;

  if (isCurrentChat) {
    // 添加到当前消息列表
    messages.value.push(msgData);
    nextTick(() => scrollToBottom());

    // 如果是收到的消息，标记已读
    if (!isSelf) {
      markAsRead(msgData.senderId);
      websocket.sendReadNotification(msgData.senderId);
    }
  }

  // 更新会话列表
  updateSessionList(msgData, isSelf);
}

// 更新会话列表
function updateSessionList(msgData, isSelf) {
  const targetId = isSelf ? msgData.receiverId : msgData.senderId;
  const sessionIndex = sessions.value.findIndex(s => s.targetUserId === targetId);

  if (sessionIndex > -1) {
    const session = sessions.value[sessionIndex];
    session.lastMessage = msgData.content;
    session.lastTime = msgData.createTime;
    
    // 如果不是当前会话的消息，且不是自己发送的，增加未读数
    if (!isSelf && targetId !== activeUserId.value) {
      session.unreadCount = (session.unreadCount || 0) + 1;
    }

    // 将会话移到顶部
    sessions.value.splice(sessionIndex, 1);
    sessions.value.unshift(session);
  } else if (!isSelf) {
    // 仅当收到别人的消息时才创建新会话
    // 自己发送的消息不需要在这里创建会话（会话应该已经存在于 openChatWithUser 中创建）
    getChatUserInfo(targetId).then(res => {
      sessions.value.unshift({
        targetUserId: targetId,
        targetNickname: res.data.nickname,
        targetAvatar: res.data.avatar,
        targetRole: res.data.role,
        lastMessage: msgData.content,
        lastTime: msgData.createTime,
        unreadCount: 1
      });
    });
  }
  // 如果是自己发送且会话不存在，不做任何操作（会话会在 selectSession 或 openChatWithUser 中创建）
}

// 滚动到底部
function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
  }
}

// 格式化时间
function formatTime(time) {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前';
  
  return date.toLocaleDateString();
}

function formatMessageTime(time) {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleString('zh-CN', { 
    month: '2-digit', 
    day: '2-digit', 
    hour: '2-digit', 
    minute: '2-digit' 
  });
}

// 获取角色名称
function getRoleName(role) {
  const roles = { 0: '管理员', 1: '教员', 2: '家长' };
  return roles[role] || '用户';
}
</script>

<style scoped lang="scss">
.chat-container {
  display: flex;
  height: calc(100vh - 60px);
  background: #f5f7fa;
}

// 会话列表面板
.session-panel {
  width: 320px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.session-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  
  h3 {
    margin: 0;
    font-size: 18px;
    color: #303133;
  }
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  
  &:hover {
    background: #f5f7fa;
  }
  
  &.active {
    background: #ecf5ff;
  }
}

.session-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
}

.session-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.session-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.session-time {
  font-size: 12px;
  color: #909399;
}

.session-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-message {
  font-size: 13px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

// 聊天面板
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.chat-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
}

.chat-user-info {
  display: flex;
  align-items: center;
}

.chat-user-detail {
  margin-left: 12px;
  
  .chat-user-name {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    display: block;
  }
  
  .chat-user-role {
    font-size: 12px;
    color: #909399;
  }
}

.chat-status {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  background: #f0f0f0;
  color: #909399;
  
  &.online {
    background: #e1f3d8;
    color: #67c23a;
  }
}

// 消息列表
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  
  &.message-self {
    flex-direction: row-reverse;
    
    .message-content {
      align-items: flex-end;
    }
    
    .message-bubble {
      background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
      color: #fff;
    }
  }
}

.message-content {
  display: flex;
  flex-direction: column;
  margin: 0 10px;
  max-width: 60%;
}

.message-bubble {
  padding: 10px 14px;
  background: #f4f4f5;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.message-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}

// 输入区域
.input-area {
  padding: 16px 20px;
  border-top: 1px solid #e4e7ed;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.input-tip {
  font-size: 12px;
  color: #909399;
}
</style>
