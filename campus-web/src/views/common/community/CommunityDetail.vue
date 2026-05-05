<template>
  <div class="community-detail-page">
    <van-nav-bar title="帖子详情" left-arrow @click-left="$router.back()" fixed placeholder />

    <!-- 帖子区域 -->
    <div class="post-section" v-loading="loading">
      <template v-if="post">
        <!-- 帖子头部 -->
        <div class="post-hero" :class="post.topicType === 1 ? 'hero-share' : 'hero-help'" />

        <div class="post-card">
          <div class="post-author-bar">
            <el-avatar :size="44" :src="post.authorAvatar || undefined" class="clickable-avatar" @click.stop="goToUser(post.userId)">
              {{ post.authorNickname?.charAt(0) }}
            </el-avatar>
            <div class="author-info" @click.stop="goToUser(post.userId)">
              <span class="author-name clickable-author">{{ post.authorNickname || '用户' }}</span>
              <span class="author-time">{{ formatTime(post.createTime) }}</span>
            </div>
            <div class="topic-badge" :class="post.topicType === 1 ? 'badge-share' : 'badge-help'">
              {{ post.topicType === 1 ? '经验分享' : '难题求助' }}
            </div>
          </div>

          <h1 class="post-title">{{ post.title }}</h1>

          <!-- 标签 -->
          <div class="post-tags" v-if="post.tags">
            <span v-for="tag in parseTags(post.tags)" :key="tag" class="tag-chip" :style="{ background: tagColor(tag).bg, color: tagColor(tag).fg }">
              {{ tag }}
            </span>
          </div>

          <div class="post-content">{{ post.content }}</div>

          <div class="post-actions">
            <div class="action-row">
              <span class="action-item" @click="handleLikePost">
                <svg viewBox="0 0 24 24" width="20" height="20" :fill="post.liked ? '#f56c6c' : 'none'" :stroke="post.liked ? '#f56c6c' : '#909399'" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                <span :class="{ liked: post.liked }">{{ post.likeCount || 0 }}</span>
              </span>
              <span class="action-item">
                <el-icon :size="20"><View /></el-icon>
                <span>{{ post.viewCount || 0 }}</span>
              </span>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 评论区 -->
    <div class="reply-section">
      <div class="reply-section-header">
        <h3>全部评论 <span class="reply-count-badge">{{ mainReplies.length }}</span></h3>
      </div>

      <div class="reply-list">
        <div v-for="reply in mainReplies" :key="reply.id" class="reply-item" :id="`reply-${reply.id}`">

          <div class="reply-main">
            <el-avatar :size="36" :src="reply.authorAvatar || undefined" class="clickable-avatar" @click.stop="goToUser(reply.userId)">
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
                  <svg viewBox="0 0 24 24" width="13" height="13" :fill="reply.liked ? '#f56c6c' : 'none'" :stroke="reply.liked ? '#f56c6c' : '#909399'" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                  {{ reply.likeCount || 0 }}
                </span>
                <span class="action-btn reply-btn" @click="openReplyInput(reply)">回复</span>
                <span v-if="isMyReply(reply)" class="action-btn delete-btn" @click="handleDelete(reply)">删除</span>
              </div>
            </div>
          </div>

          <!-- 子回复 -->
          <div v-if="reply.replyCount > 0" class="sub-reply-section">
            <div v-if="!expandedRoots[reply.id]" class="expand-btn" @click="expandSubReplies(reply)">
              <span>展开 {{ reply.replyCount }} 条回复</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template v-else>
              <div v-for="sub in subRepliesMap[reply.id] || []" :key="sub.id" class="sub-reply-item" :id="`reply-${sub.id}`">
                <el-avatar :size="28" :src="sub.authorAvatar || undefined" class="clickable-avatar" @click.stop="goToUser(sub.userId)">
                  {{ sub.authorNickname?.charAt(0) }}
                </el-avatar>
                <div class="sub-reply-body">
                  <div class="sub-reply-header">
                    <span class="sub-author clickable-author" @click.stop="goToUser(sub.userId)">{{ sub.authorNickname || '用户' }}</span>
                    <template v-if="sub.replyToNickname">
                      <span class="reply-to-arrow">→</span>
                      <span class="reply-to-name">@{{ sub.replyToNickname }}</span>
                    </template>
                    <span class="sub-time">{{ formatTime(sub.createTime) }}</span>
                  </div>
                  <p class="sub-content">{{ sub.content }}</p>
                  <div class="reply-actions sub-actions">
                    <span class="action-btn like-btn" :class="{ active: sub.liked }" @click="handleLikeReply(sub)">
                      <svg viewBox="0 0 24 24" width="12" height="12" :fill="sub.liked ? '#f56c6c' : 'none'" :stroke="sub.liked ? '#f56c6c' : '#909399'" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                      {{ sub.likeCount || 0 }}
                    </span>
                    <span class="action-btn reply-btn" @click="openReplyInput(sub)">回复</span>
                    <span v-if="isMyReply(sub)" class="action-btn delete-btn" @click="handleDelete(sub)">删除</span>
                  </div>
                </div>
              </div>
              <div v-if="hasMoreSub[reply.id]" class="expand-btn" @click="loadMoreSubReplies(reply)">加载更多回复 <el-icon><ArrowDown /></el-icon></div>
            </template>
          </div>
        </div>
        <el-empty v-if="mainReplies.length === 0" description="还没有评论，来说点什么吧" :image-size="80" />
      </div>

      <!-- 评论输入栏 -->
      <div class="reply-input-bar">
        <div v-if="replyingTo" class="replying-hint">
          <span>回复 <b>@{{ replyingTo.authorNickname || '用户' }}</b></span>
          <el-icon @click="cancelReply"><Close /></el-icon>
        </div>
        <div class="input-row">
          <el-avatar :size="32" :src="userStore.avatar" class="input-avatar">{{ userStore.nickname?.charAt(0) }}</el-avatar>
          <el-input v-model="replyContent" :placeholder="replyingTo ? `回复 @${replyingTo.authorNickname || '用户'}...` : '写评论...'" @keyup.enter="handleSendReply" size="large" />
          <el-button type="primary" :disabled="!replyContent.trim()" @click="handleSendReply" round>发送</el-button>
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
import { getCommunityPostDetail, likeCommunityPost, getCommunityReplies, createCommunityReply, getSubReplies, deleteCommunityReply, likeCommunityReply } from '@shared/api/community'
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

const tagColors = {
  '学习经验': { bg: '#e8f5e9', fg: '#2e7d32' },
  '考试技巧': { bg: '#fff3e0', fg: '#e65100' },
  '选课建议': { bg: '#e3f2fd', fg: '#1565c0' },
  '校园生活': { bg: '#fce4ec', fg: '#c62828' },
  '活动推荐': { bg: '#f3e5f5', fg: '#6a1b9a' },
  '求助问答': { bg: '#fff8e1', fg: '#f57f17' }
}
const tagColor = (tag) => tagColors[tag] || { bg: '#f5f5f5', fg: '#616161' }
const parseTags = (s) => s ? s.split(',').map(t => t.trim()).filter(Boolean) : []

const goToUser = (userId) => { if (userId) router.push(`/user/${userId}`) }
const scrollToReply = (replyId) => {
  nextTick(() => {
    const el = document.getElementById(`reply-${replyId}`)
    if (el) { el.scrollIntoView({ behavior: 'smooth', block: 'center' }); el.classList.add('reply-highlight'); setTimeout(() => el.classList.remove('reply-highlight'), 2000) }
  })
}

const formatTime = (time) => {
  if (!time) return ''
  const d = dayjs(time); const now = dayjs()
  if (now.diff(d, 'minute') < 1) return '刚刚'
  if (now.diff(d, 'hour') < 24) return d.fromNow()
  return d.format('MM-DD HH:mm')
}

const isMyReply = (reply) => currentUserId.value && reply.userId === Number(currentUserId.value)

onMounted(async () => {
  const id = route.params.id
  const targetReplyId = route.query.replyId
  loading.value = true
  try {
    const [postRes, replyRes] = await Promise.all([getCommunityPostDetail(id), getCommunityReplies(id, { page: 1, size: 50 })])
    if (postRes.code === 200) post.value = postRes.data
    if (replyRes.code === 200) mainReplies.value = replyRes.data?.records || []

    if (targetReplyId) {
      const replyId = Number(targetReplyId)
      const mainMatch = mainReplies.value.find(r => r.id === replyId)
      if (mainMatch) { scrollToReply(replyId) } else {
        for (const r of mainReplies.value) {
          if (r.replyCount > 0) { expandedRoots[r.id] = true; await loadSubReplies(r.id, null, 20); if ((subRepliesMap[r.id] || []).find(s => s.id === replyId)) { scrollToReply(replyId); break } }
        }
      }
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
})

const handleLikePost = async () => {
  if (!post.value) return
  try { const res = await likeCommunityPost(post.value.id); if (res.code === 200) { const liked = res.data.liked; post.value.liked = liked; post.value.likeCount = (post.value.likeCount || 0) + (liked ? 1 : -1) } } catch (e) { console.error(e) }
}

const handleLikeReply = async (reply) => {
  try { const res = await likeCommunityReply(reply.id); if (res.code === 200) { const liked = res.data.liked; reply.liked = liked; reply.likeCount = (reply.likeCount || 0) + (liked ? 1 : -1) } } catch (e) { console.error(e) }
}

const expandSubReplies = async (reply) => { expandedRoots[reply.id] = true; await loadSubReplies(reply.id, null, 3) }

const loadMoreSubReplies = async (reply) => {
  const existing = subRepliesMap[reply.id] || []
  const lastId = existing.length > 0 ? existing[existing.length - 1].id : null
  const res = await getSubReplies(reply.id, { lastId, size: 3 })
  if (res.code === 200) { const newReplies = res.data?.records || []; if (!subRepliesMap[reply.id]) subRepliesMap[reply.id] = []; subRepliesMap[reply.id].push(...newReplies); hasMoreSub[reply.id] = newReplies.length >= 3 }
}

const loadSubReplies = async (rootId, lastId, size) => {
  const res = await getSubReplies(rootId, { lastId, size })
  if (res.code === 200) { subRepliesMap[rootId] = res.data?.records || []; hasMoreSub[rootId] = subRepliesMap[rootId].length >= size }
}

const openReplyInput = (reply) => { replyingTo.value = reply; replyContent.value = '' }
const cancelReply = () => { replyingTo.value = null; replyContent.value = '' }

const handleSendReply = async () => {
  if (!replyContent.value.trim()) return
  const postId = route.params.id
  const data = { content: replyContent.value.trim() }
  if (replyingTo.value) {
    const t = replyingTo.value
    if (t.rootId && t.rootId > 0) { data.rootId = t.rootId; data.parentId = t.id; data.replyToId = t.id; data.replyToUserId = t.userId }
    else { data.rootId = t.id; data.parentId = t.id; data.replyToId = t.id; data.replyToUserId = t.userId }
  }
  try {
    const res = await createCommunityReply(postId, data)
    if (res.code === 200) {
      const nr = res.data
      if (nr.rootId && nr.rootId > 0) { if (!subRepliesMap[nr.rootId]) subRepliesMap[nr.rootId] = []; subRepliesMap[nr.rootId].push(nr); expandedRoots[nr.rootId] = true; const p = mainReplies.value.find(r => r.id === nr.rootId); if (p) p.replyCount = (p.replyCount || 0) + 1 }
      else { mainReplies.value.unshift(nr) }
      replyContent.value = ''; replyingTo.value = null; ElMessage.success('评论成功')
    }
  } catch (e) { ElMessage.error('评论失败') }
}

const handleDelete = async (reply) => {
  try { await ElMessageBox.confirm('确定删除该评论吗？', '删除评论', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }) } catch { return }
  const res = await deleteCommunityReply(reply.id)
  if (res.code === 200) {
    if (reply.rootId && reply.rootId > 0) { const l = subRepliesMap[reply.rootId]; if (l) { const i = l.findIndex(r => r.id === reply.id); if (i >= 0) l.splice(i, 1) }; const p = mainReplies.value.find(r => r.id === reply.rootId); if (p) p.replyCount = Math.max(0, (p.replyCount || 0) - 1) }
    else { const i = mainReplies.value.findIndex(r => r.id === reply.id); if (i >= 0) mainReplies.value.splice(i, 1) }
    ElMessage.success('已删除')
  }
}
</script>

<style lang="scss" scoped>
.community-detail-page { min-height: 100vh; background: #f5f7fa; padding-bottom: 100px; }

// 帖子区域
.post-section { background: #fff; }

.post-hero {
  height: 60px;
  &.hero-share { background: linear-gradient(135deg, #67C23A, #85ce61); }
  &.hero-help { background: linear-gradient(135deg, #E6A23C, #ebb563); }
}

.post-card { padding: 0 18px 18px; margin-top: -30px; }

.post-author-bar {
  display: flex; align-items: center; gap: 12px; padding-bottom: 16px;
  .author-info { flex: 1; cursor: pointer; }
  .author-name { display: block; font-size: 15px; font-weight: 600; color: #303133; }
  .author-time { font-size: 12px; color: #c0c4cc; }
}

.topic-badge {
  font-size: 12px; padding: 4px 10px; border-radius: 12px; font-weight: 500;
  &.badge-share { background: #e8f5e9; color: #2e7d32; }
  &.badge-help { background: #fff3e0; color: #e65100; }
}

.post-title { font-size: 21px; font-weight: 700; color: #1d2129; margin: 0 0 12px; line-height: 1.4; }

.post-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 14px; }
.tag-chip { font-size: 11px; padding: 3px 8px; border-radius: 4px; font-weight: 500; }

.post-content { font-size: 16px; color: #4e5969; line-height: 1.85; margin-bottom: 18px; white-space: pre-wrap; word-break: break-word; }

.post-actions { border-top: 1px solid #f0f2f5; padding-top: 14px; }
.action-row { display: flex; gap: 24px; }
.action-item { display: flex; align-items: center; gap: 6px; font-size: 14px; color: #909399; cursor: pointer; .liked { color: #f56c6c; font-weight: 600; } }

// 评论区
.reply-section { margin-top: 10px; background: #fff; padding: 0 18px 16px; }
.reply-section-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 0 12px; h3 { margin: 0; font-size: 16px; font-weight: 600; color: #1d2129; } }
.reply-count-badge { font-size: 13px; color: #909399; font-weight: 400; margin-left: 4px; }

.reply-item { padding: 14px 0; border-bottom: 1px solid #f5f5f5; &:last-child { border-bottom: none; } }
.reply-main { display: flex; gap: 10px; }
.reply-body { flex: 1; min-width: 0; }
.reply-header { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.reply-author { font-size: 14px; font-weight: 600; color: #303133; }
.reply-time { font-size: 11px; color: #c0c4cc; margin-left: auto; }
.reply-content { font-size: 15px; color: #4e5969; line-height: 1.6; margin: 4px 0 8px; word-break: break-word; }

.reply-actions { display: flex; gap: 16px; font-size: 12px; color: #c0c4cc; }
.action-btn { display: flex; align-items: center; gap: 3px; cursor: pointer; transition: color 0.2s; }
.action-btn:hover { color: #409EFF; }
.like-btn.active { color: #f56c6c; }
.delete-btn:hover { color: #f56c6c; }
.sub-actions { margin-top: 2px; font-size: 11px; }

.sub-reply-section { margin-left: 46px; margin-top: 8px; }
.expand-btn { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #409EFF; cursor: pointer; padding: 6px 0; &:hover { opacity: 0.8; } }
.sub-reply-item { display: flex; gap: 8px; padding: 10px; margin: 6px 0; background: #f7f8fa; border-radius: 10px; }
.sub-reply-body { flex: 1; min-width: 0; }
.sub-reply-header { display: flex; align-items: center; gap: 4px; flex-wrap: wrap; }
.sub-author { font-size: 13px; font-weight: 600; color: #303133; }
.reply-to-arrow { font-size: 10px; color: #c0c4cc; margin: 0 2px; }
.reply-to-name { font-size: 12px; color: #409EFF; font-weight: 500; }
.sub-time { font-size: 10px; color: #dcdfe6; margin-left: auto; }
.sub-content { font-size: 14px; color: #606266; line-height: 1.5; margin: 3px 0 4px; word-break: break-word; }

// 评论输入栏
.reply-input-bar {
  position: fixed; bottom: 0; left: 0; right: 0; background: #fff;
  padding: 10px 16px; padding-bottom: max(10px, env(safe-area-inset-bottom));
  box-shadow: 0 -2px 12px rgba(0,0,0,0.06); z-index: 100;
  .replying-hint { display: flex; align-items: center; justify-content: space-between; font-size: 12px; color: #409EFF; margin-bottom: 8px; }
  .input-row { display: flex; align-items: center; gap: 10px; }
  .input-avatar { flex-shrink: 0; }
  :deep(.el-input) { flex: 1; }
}

// 通用
.clickable-avatar { cursor: pointer; transition: transform 0.2s; &:hover { transform: scale(1.08); } }
.clickable-author { cursor: pointer; &:hover { color: #409EFF; } }
.reply-highlight { animation: highlightFade 2s ease-out; }
@keyframes highlightFade {
  0% { background-color: #ecf5ff; border-radius: 8px; }
  100% { background-color: transparent; }
}
</style>
