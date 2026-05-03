<template>
  <div class="community-detail-page">
    <van-nav-bar title="帖子详情" left-arrow @click-left="$router.back()" />

    <div class="post-detail" v-loading="loading">
      <template v-if="post">
        <div class="post-header">
          <el-avatar :size="40" :src="post.authorAvatar || undefined" class="clickable-avatar" @click.stop="goToUser(post.userId)">
            {{ post.authorNickname?.charAt(0) }}
          </el-avatar>
          <div class="post-meta">
            <span class="author clickable-author" @click.stop="goToUser(post.userId)">{{ post.authorNickname || '用户' }}</span>
            <span class="time">{{ formatTime(post.createTime) }}</span>
          </div>
          <el-tag :type="post.topicType === 1 ? 'primary' : 'warning'" size="small">
            {{ post.topicType === 1 ? '经验分享' : '难题求助' }}
          </el-tag>
        </div>

        <h2 class="post-title">{{ post.title }}</h2>
        <div class="post-content">{{ post.content }}</div>

        <div class="post-stats">
          <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
          <span class="like-btn" :class="{ active: post.liked }" @click="handleLikePost">
            <svg viewBox="0 0 24 24" width="14" height="14" :fill="post.liked ? '#f56c6c' : 'none'" :stroke="post.liked ? '#f56c6c' : 'currentColor'" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
            {{ post.likeCount || 0 }}
          </span>
        </div>
      </template>
    </div>

    <div class="reply-section">
      <h3>评论 ({{ mainReplies.length }})</h3>

      <div class="reply-list">
        <div v-for="reply in mainReplies" :key="reply.id" class="reply-item">
          <div class="reply-main" :id="`reply-${reply.id}`">
            <el-avatar :size="32" :src="reply.authorAvatar || undefined" class="clickable-avatar" @click.stop="goToUser(reply.userId)">
              {{ reply.authorNickname?.charAt(0) }}
            </el-avatar>
            <div class="reply-body">
              <div class="reply-header">
                <span class="reply-author clickable-author" @click.stop="goToUser(reply.userId)">{{ reply.authorNickname || '用户' }}</span>
                <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
              </div>
              <p class="reply-content">{{ reply.content }}</p>
              <div class="reply-actions">
                <span class="action-btn like-btn" :class="{ active: reply.liked }" @click="handleLikeReply(reply)">
                  <svg viewBox="0 0 24 24" width="13" height="13" :fill="reply.liked ? '#f56c6c' : 'none'" :stroke="reply.liked ? '#f56c6c' : 'currentColor'" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                  {{ reply.likeCount || 0 }}
                </span>
                <span class="action-btn" @click="openReplyInput(reply)">回复</span>
                <span v-if="isMyReply(reply)" class="action-btn delete-btn" @click="handleDelete(reply)">删除</span>
              </div>
            </div>
          </div>

          <div v-if="reply.replyCount > 0" class="sub-reply-section">
            <div v-if="!expandedRoots[reply.id]" class="expand-hint" @click="expandSubReplies(reply)">
              <span>点击展开 {{ reply.replyCount }} 条回复</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template v-else>
              <div v-for="sub in subRepliesMap[reply.id] || []" :key="sub.id" class="sub-reply-item" :id="`reply-${sub.id}`">
                <el-avatar :size="24" :src="sub.authorAvatar || undefined" class="clickable-avatar" @click.stop="goToUser(sub.userId)">
                  {{ sub.authorNickname?.charAt(0) }}
                </el-avatar>
                <div class="sub-reply-body">
                  <span class="sub-author">{{ sub.authorNickname || '用户' }}</span>
                  <template v-if="sub.replyToNickname">
                    <span class="reply-to-text">回复</span>
                    <span class="reply-to-name">@{{ sub.replyToNickname }}</span>
                  </template>
                  <span class="sub-content">：{{ sub.content }}</span>
                  <div class="reply-actions sub-actions">
                    <span class="action-btn like-btn" :class="{ active: sub.liked }" @click="handleLikeReply(sub)">
                      <svg viewBox="0 0 24 24" width="12" height="12" :fill="sub.liked ? '#f56c6c' : 'none'" :stroke="sub.liked ? '#f56c6c' : 'currentColor'" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                      {{ sub.likeCount || 0 }}
                    </span>
                    <span class="action-btn" @click="openReplyInput(sub)">回复</span>
                    <span v-if="isMyReply(sub)" class="action-btn delete-btn" @click="handleDelete(sub)">删除</span>
                  </div>
                </div>
              </div>
              <div v-if="hasMoreSub[reply.id]" class="expand-hint" @click="loadMoreSubReplies(reply)">
                <span>继续展开更多回复</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
            </template>
          </div>
        </div>
        <el-empty v-if="mainReplies.length === 0" description="暂无评论" :image-size="60" />
      </div>

      <div class="reply-input-bar">
        <div v-if="replyingTo" class="replying-hint">
          <span>回复 @{{ replyingTo.authorNickname || '用户' }}</span>
          <el-icon @click="cancelReply"><Close /></el-icon>
        </div>
        <div class="input-row">
          <el-input
            v-model="replyContent"
            :placeholder="replyingTo ? `回复 @${replyingTo.authorNickname || '用户'}...` : '写评论...'"
            @keyup.enter="handleSendReply"
          />
          <el-button type="primary" :disabled="!replyContent.trim()" @click="handleSendReply">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, ArrowDown, Close } from '@element-plus/icons-vue'
import {
  getCommunityPostDetail, likeCommunityPost,
  getCommunityReplies, createCommunityReply,
  getSubReplies, deleteCommunityReply, likeCommunityReply
} from '@shared/api/community'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { useUserStore } from '@shared/stores'
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const post = ref(null)
const mainReplies = ref([])
const subRepliesMap = reactive({})
const expandedRoots = reactive({})
const hasMoreSub = reactive({})
const replyContent = ref('')
const replyingTo = ref(null)

const currentUserId = computed(() => userStore.userId)

// 跳转到用户资料页
const goToUser = (userId) => {
  if (userId) {
    router.push(`/user/${userId}`)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const d = dayjs(time)
  const now = dayjs()
  if (now.diff(d, 'minute') < 1) return '刚刚'
  if (now.diff(d, 'hour') < 24) return d.fromNow()
  if (now.diff(d, 'day') < 7) return d.fromNow()
  return d.format('MM-DD HH:mm')
}

const isMyReply = (reply) => {
  return currentUserId.value && reply.userId === Number(currentUserId.value)
}

// 滚动到指定评论并高亮
const scrollToReply = (replyId) => {
  nextTick(() => {
    const el = document.getElementById(`reply-${replyId}`)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' })
      el.classList.add('reply-highlight')
      setTimeout(() => el.classList.remove('reply-highlight'), 2000)
    }
  })
}

onMounted(async () => {
  const id = route.params.id
  const targetReplyId = route.query.replyId
  loading.value = true
  try {
    const [postRes, replyRes] = await Promise.all([
      getCommunityPostDetail(id),
      getCommunityReplies(id, { page: 1, size: 50 })
    ])
    if (postRes.code === 200) {
      post.value = postRes.data
    }
    if (replyRes.code === 200) {
      mainReplies.value = replyRes.data?.records || []
    }

    // 处理深度链接：滚动到指定评论
    if (targetReplyId) {
      const replyId = Number(targetReplyId)
      // 检查是否在主回复列表中
      const mainMatch = mainReplies.value.find(r => r.id === replyId)
      if (mainMatch) {
        scrollToReply(replyId)
      } else {
        // 需要查找子回复：遍历主回复查找匹配的rootId
        // 先尝试从任意主回复的replyCount判断是否可能有子回复
        for (const mainReply of mainReplies.value) {
          if (mainReply.replyCount > 0) {
            expandedRoots[mainReply.id] = true
            await loadSubReplies(mainReply.id, null, 20)
            const subMatch = (subRepliesMap[mainReply.id] || []).find(s => s.id === replyId)
            if (subMatch) {
              scrollToReply(replyId)
              break
            }
          }
        }
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

const handleLikePost = async () => {
  if (!post.value) return
  try {
    const res = await likeCommunityPost(post.value.id)
    if (res.code === 200) {
      const liked = res.data.liked
      post.value.liked = liked
      post.value.likeCount = (post.value.likeCount || 0) + (liked ? 1 : -1)
    }
  } catch (e) {
    console.error(e)
  }
}

const handleLikeReply = async (reply) => {
  try {
    const res = await likeCommunityReply(reply.id)
    if (res.code === 200) {
      const liked = res.data.liked
      reply.liked = liked
      reply.likeCount = (reply.likeCount || 0) + (liked ? 1 : -1)
    }
  } catch (e) {
    console.error(e)
  }
}

const expandSubReplies = async (reply) => {
  expandedRoots[reply.id] = true
  await loadSubReplies(reply.id, null, 3)
}

const loadMoreSubReplies = async (reply) => {
  const existing = subRepliesMap[reply.id] || []
  const lastId = existing.length > 0 ? existing[existing.length - 1].id : null
  const res = await getSubReplies(reply.id, { lastId, size: 3 })
  if (res.code === 200) {
    const newReplies = res.data?.records || []
    if (!subRepliesMap[reply.id]) subRepliesMap[reply.id] = []
    subRepliesMap[reply.id].push(...newReplies)
    hasMoreSub[reply.id] = newReplies.length >= 3
  }
}

const loadSubReplies = async (rootId, lastId, size) => {
  const res = await getSubReplies(rootId, { lastId, size })
  if (res.code === 200) {
    subRepliesMap[rootId] = res.data?.records || []
    hasMoreSub[rootId] = subRepliesMap[rootId].length >= size
  }
}

const openReplyInput = (reply) => {
  replyingTo.value = reply
  replyContent.value = ''
}

const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

const handleSendReply = async () => {
  if (!replyContent.value.trim()) return
  const postId = route.params.id
  const data = { content: replyContent.value.trim() }

  if (replyingTo.value) {
    const target = replyingTo.value
    if (target.rootId && target.rootId > 0) {
      data.rootId = target.rootId
      data.parentId = target.id
      data.replyToId = target.id
      data.replyToUserId = target.userId
    } else {
      data.rootId = target.id
      data.parentId = target.id
      data.replyToId = target.id
      data.replyToUserId = target.userId
    }
  }

  try {
    const res = await createCommunityReply(postId, data)
    if (res.code === 200) {
      const newReply = res.data
      if (newReply.rootId && newReply.rootId > 0) {
        if (!subRepliesMap[newReply.rootId]) subRepliesMap[newReply.rootId] = []
        subRepliesMap[newReply.rootId].push(newReply)
        const parent = mainReplies.value.find(r => r.id === newReply.rootId)
        if (parent) parent.replyCount = (parent.replyCount || 0) + 1
        expandedRoots[newReply.rootId] = true
      } else {
        mainReplies.value.unshift(newReply)
      }
      replyContent.value = ''
      replyingTo.value = null
      ElMessage.success('评论成功')
    }
  } catch (e) {
    ElMessage.error('评论失败')
  }
}

const handleDelete = async (reply) => {
  try {
    await ElMessageBox.confirm('确定删除该评论吗？', '删除评论', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteCommunityReply(reply.id)
    if (res.code === 200) {
      if (reply.rootId && reply.rootId > 0) {
        const list = subRepliesMap[reply.rootId]
        if (list) {
          const idx = list.findIndex(r => r.id === reply.id)
          if (idx >= 0) list.splice(idx, 1)
        }
        const parent = mainReplies.value.find(r => r.id === reply.rootId)
        if (parent) parent.replyCount = Math.max(0, (parent.replyCount || 0) - 1)
      } else {
        const idx = mainReplies.value.findIndex(r => r.id === reply.id)
        if (idx >= 0) mainReplies.value.splice(idx, 1)
      }
      ElMessage.success('已删除')
    }
  } catch (e) { /* cancelled */ }
}
</script>

<style lang="scss" scoped>
.community-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
}

.clickable-avatar {
  cursor: pointer;
  transition: transform 0.2s;
  &:hover { transform: scale(1.08); }
}

.clickable-author {
  cursor: pointer;
  &:hover { color: #409EFF; }
}

.post-detail {
  background: #fff;
  padding: 16px;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;

  .post-meta {
    flex: 1;
    .author { display: block; font-size: 15px; font-weight: 500; color: #303133; }
    .time { font-size: 12px; color: #909399; }
  }
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px;
}

.post-content {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  margin-bottom: 16px;
}

.post-stats {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
  }

  .like-btn {
    transition: color 0.2s;
    &.active { color: #f56c6c; }
  }
}

.reply-section {
  background: #fff;
  margin-top: 12px;
  padding: 16px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 16px;
  }
}

.reply-item {
  margin-bottom: 20px;
}

.reply-main {
  display: flex;
  gap: 10px;
}

.reply-body {
  flex: 1;

  .reply-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
  }

  .reply-author {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
  }

  .reply-time {
    font-size: 11px;
    color: #c0c4cc;
  }

  .reply-content {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    margin: 4px 0 6px;
  }
}

.reply-actions {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;

  .action-btn {
    display: flex;
    align-items: center;
    gap: 3px;
    cursor: pointer;
    transition: color 0.2s;

    &:hover { color: #409eff; }
  }

  .like-btn.active { color: #f56c6c; }
  .delete-btn:hover { color: #f56c6c; }

  &.sub-actions {
    margin-top: 2px;
    font-size: 11px;
  }
}

.sub-reply-section {
  margin-left: 42px;
  margin-top: 8px;
  padding: 10px 12px;
  background: #f7f8fa;
  border-radius: 8px;
}

.expand-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #409eff;
  cursor: pointer;
  padding: 4px 0;

  &:hover { opacity: 0.8; }
}

.sub-reply-item {
  display: flex;
  gap: 8px;
  padding: 6px 0;

  &:not(:last-child) {
    border-bottom: none;
  }
}

.sub-reply-body {
  flex: 1;
  font-size: 13px;
  line-height: 1.5;
  color: #606266;

  .sub-author {
    font-weight: 500;
    color: #303133;
  }

  .reply-to-text {
    color: #909399;
    margin: 0 2px;
  }

  .reply-to-name {
    color: #409eff;
    font-weight: 500;
  }

  .sub-content {
    color: #606266;
  }
}

.reply-highlight {
  animation: highlightFade 2s ease-out;
}

@keyframes highlightFade {
  0% { background-color: #ecf5ff; }
  100% { background-color: transparent; }
}

.reply-input-bar {
  position: sticky;
  bottom: 0;
  background: #fff;
  padding: 10px 16px;
  border-radius: 12px 12px 0 0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);

  .replying-hint {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 12px;
    color: #409eff;
    margin-bottom: 6px;
    cursor: pointer;
  }

  .input-row {
    display: flex;
    gap: 8px;

    .el-input { flex: 1; }
  }
}
</style>
