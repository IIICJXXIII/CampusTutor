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
          <el-tag :type="post.topicType === 1 ? 'success' : 'warning'" size="small">
            {{ post.topicType === 1 ? '经验分享' : '难题求助' }}
          </el-tag>
        </div>

        <h2 class="post-title">{{ post.title }}</h2>
        <div class="post-tags" v-if="post.tags">
          <span v-for="tag in parseTags(post.tags)" :key="tag" class="tag-chip" :style="{ background: tagColor(tag).bg, color: tagColor(tag).fg }">{{ tag }}</span>
        </div>
        <div class="post-content">{{ post.content }}</div>

        <div class="post-stats">
          <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
          <span class="like-btn" :class="{ active: post.liked }" @click="handleLikePost">
            <svg viewBox="0 0 24 24" width="16" height="16" :fill="post.liked ? '#f56c6c' : 'none'" :stroke="post.liked ? '#f56c6c' : '#909399'" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
            {{ post.likeCount || 0 }}
          </span>
        </div>
      </template>
    </div>

    <div class="reply-section">
      <h3>评论 ({{ mainReplies.length }})</h3>

      <div class="reply-list">
        <div v-for="reply in mainReplies" :key="reply.id" class="reply-item" :id="`reply-${reply.id}`">
          <div class="reply-main">
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
                  <svg viewBox="0 0 24 24" width="12" height="12" :fill="reply.liked ? '#f56c6c' : 'none'" :stroke="reply.liked ? '#f56c6c' : '#909399'" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                  {{ reply.likeCount || 0 }}
                </span>
                <span class="action-btn" @click="openReplyInput(reply)">回复</span>
                <span v-if="isMyReply(reply)" class="action-btn delete-btn" @click="handleDelete(reply)">删除</span>
              </div>
            </div>
          </div>

          <div v-if="reply.replyCount > 0" class="sub-reply-section">
            <div v-if="!expandedRoots[reply.id]" class="expand-hint" @click="expandSubReplies(reply)">
              <span>展开 {{ reply.replyCount }} 条回复</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template v-else>
              <div v-for="sub in subRepliesMap[reply.id] || []" :key="sub.id" class="sub-reply-item" :id="`reply-${sub.id}`">
                <el-avatar :size="24" :src="sub.authorAvatar || undefined" class="clickable-avatar" @click.stop="goToUser(sub.userId)">
                  {{ sub.authorNickname?.charAt(0) }}
                </el-avatar>
                <div class="sub-reply-body">
                  <span class="sub-author clickable-author" @click.stop="goToUser(sub.userId)">{{ sub.authorNickname || '用户' }}</span>
                  <template v-if="sub.replyToNickname">
                    <span class="reply-to-text">回复</span>
                    <span class="reply-to-name">@{{ sub.replyToNickname }}</span>
                  </template>
                  <span class="sub-content">：{{ sub.content }}</span>
                  <div class="reply-actions sub-actions">
                    <span class="action-btn like-btn" :class="{ active: sub.liked }" @click="handleLikeReply(sub)">
                      <svg viewBox="0 0 24 24" width="11" height="11" :fill="sub.liked ? '#f56c6c' : 'none'" :stroke="sub.liked ? '#f56c6c' : '#909399'" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                      {{ sub.likeCount || 0 }}
                    </span>
                    <span class="action-btn" @click="openReplyInput(sub)">回复</span>
                    <span v-if="isMyReply(sub)" class="action-btn delete-btn" @click="handleDelete(sub)">删除</span>
                  </div>
                </div>
              </div>
              <div v-if="hasMoreSub[reply.id]" class="expand-hint" @click="loadMoreSubReplies(reply)">继续展开更多 <el-icon><ArrowDown /></el-icon></div>
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
          <el-input v-model="replyContent" :placeholder="replyingTo ? `回复 @${replyingTo.authorNickname || '用户'}...` : '写评论...'" @keyup.enter="handleSendReply" />
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
  '学习经验': { bg: '#e8f5e9', fg: '#2e7d32' }, '考试技巧': { bg: '#fff3e0', fg: '#e65100' },
  '选课建议': { bg: '#e3f2fd', fg: '#1565c0' }, '校园生活': { bg: '#fce4ec', fg: '#c62828' },
  '活动推荐': { bg: '#f3e5f5', fg: '#6a1b9a' }, '求助问答': { bg: '#fff8e1', fg: '#f57f17' }
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
  const existing = subRepliesMap[reply.id] || []; const lastId = existing.length > 0 ? existing[existing.length - 1].id : null
  const res = await getSubReplies(reply.id, { lastId, size: 3 })
  if (res.code === 200) { const nr = res.data?.records || []; if (!subRepliesMap[reply.id]) subRepliesMap[reply.id] = []; subRepliesMap[reply.id].push(...nr); hasMoreSub[reply.id] = nr.length >= 3 }
}
const loadSubReplies = async (rootId, lastId, size) => {
  const res = await getSubReplies(rootId, { lastId, size })
  if (res.code === 200) { subRepliesMap[rootId] = res.data?.records || []; hasMoreSub[rootId] = subRepliesMap[rootId].length >= size }
}
const openReplyInput = (reply) => { replyingTo.value = reply; replyContent.value = '' }
const cancelReply = () => { replyingTo.value = null; replyContent.value = '' }
const handleSendReply = async () => {
  if (!replyContent.value.trim()) return
  const postId = route.params.id; const data = { content: replyContent.value.trim() }
  if (replyingTo.value) { const t = replyingTo.value; if (t.rootId && t.rootId > 0) { data.rootId = t.rootId; data.parentId = t.id; data.replyToId = t.id; data.replyToUserId = t.userId } else { data.rootId = t.id; data.parentId = t.id; data.replyToId = t.id; data.replyToUserId = t.userId } }
  try {
    const res = await createCommunityReply(postId, data)
    if (res.code === 200) { const nr = res.data; if (nr.rootId && nr.rootId > 0) { if (!subRepliesMap[nr.rootId]) subRepliesMap[nr.rootId] = []; subRepliesMap[nr.rootId].push(nr); expandedRoots[nr.rootId] = true; const p = mainReplies.value.find(r => r.id === nr.rootId); if (p) p.replyCount = (p.replyCount || 0) + 1 } else { mainReplies.value.unshift(nr) }; replyContent.value = ''; replyingTo.value = null; ElMessage.success('评论成功') }
  } catch (e) { ElMessage.error('评论失败') }
}
const handleDelete = async (reply) => {
  try { await ElMessageBox.confirm('确定删除该评论吗？', '删除评论', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }) } catch { return }
  const res = await deleteCommunityReply(reply.id)
  if (res.code === 200) { if (reply.rootId && reply.rootId > 0) { const l = subRepliesMap[reply.rootId]; if (l) { const i = l.findIndex(r => r.id === reply.id); if (i >= 0) l.splice(i, 1) }; const p = mainReplies.value.find(r => r.id === reply.rootId); if (p) p.replyCount = Math.max(0, (p.replyCount || 0) - 1) } else { const i = mainReplies.value.findIndex(r => r.id === reply.id); if (i >= 0) mainReplies.value.splice(i, 1) }; ElMessage.success('已删除') }
}
</script>

<style lang="scss" scoped>
.community-detail-page { min-height: 100vh; background: #f5f7fa; padding-bottom: 80px; }

.post-detail { background: #fff; padding: 16px; }
.post-header { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; .post-meta { flex: 1; .author { display: block; font-size: 15px; font-weight: 500; color: #303133; } .time { font-size: 12px; color: #c0c4cc; } } }
.post-title { font-size: 18px; font-weight: 600; color: #303133; margin: 0 0 10px; }
.post-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px; }
.tag-chip { font-size: 11px; padding: 3px 8px; border-radius: 4px; font-weight: 500; }
.post-content { font-size: 15px; color: #606266; line-height: 1.8; margin-bottom: 16px; white-space: pre-wrap; word-break: break-word; }
.post-stats { display: flex; gap: 20px; font-size: 13px; color: #909399; padding-top: 12px; border-top: 1px solid #f0f0f0; span { display: flex; align-items: center; gap: 4px; cursor: pointer; } .like-btn { transition: color 0.2s; &.active { color: #f56c6c; } } }

.reply-section { background: #fff; margin-top: 10px; padding: 16px; h3 { font-size: 16px; font-weight: 600; color: #303133; margin: 0 0 16px; } }
.reply-item { margin-bottom: 18px; }
.reply-main { display: flex; gap: 10px; }
.reply-body { flex: 1; min-width: 0; .reply-header { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; } .reply-author { font-size: 14px; font-weight: 500; color: #303133; } .reply-time { font-size: 11px; color: #c0c4cc; } .reply-content { font-size: 14px; color: #606266; line-height: 1.6; margin: 4px 0 6px; word-break: break-word; } }
.reply-actions { display: flex; gap: 16px; font-size: 12px; color: #c0c4cc; .action-btn { display: flex; align-items: center; gap: 3px; cursor: pointer; transition: color 0.2s; &:hover { color: #409eff; } } .like-btn.active { color: #f56c6c; } .delete-btn:hover { color: #f56c6c; } &.sub-actions { margin-top: 2px; font-size: 11px; } }

.sub-reply-section { margin-left: 42px; margin-top: 8px; padding: 10px 12px; background: #f7f8fa; border-radius: 8px; }
.expand-hint { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #409eff; cursor: pointer; padding: 4px 0; &:hover { opacity: 0.8; } }
.sub-reply-item { display: flex; gap: 8px; padding: 6px 0; &:not(:last-child) { border-bottom: none; } }
.sub-reply-body { flex: 1; font-size: 13px; line-height: 1.5; color: #606266; .sub-author { font-weight: 500; color: #303133; } .reply-to-text { color: #909399; margin: 0 2px; } .reply-to-name { color: #409eff; font-weight: 500; } .sub-content { color: #606266; } }

.reply-input-bar { position: sticky; bottom: 0; background: #fff; padding: 10px 16px; border-radius: 12px 12px 0 0; box-shadow: 0 -2px 8px rgba(0,0,0,0.06); .replying-hint { display: flex; align-items: center; justify-content: space-between; font-size: 12px; color: #409eff; margin-bottom: 6px; } .input-row { display: flex; gap: 8px; .el-input { flex: 1; } } }

.clickable-avatar { cursor: pointer; transition: transform 0.2s; &:hover { transform: scale(1.08); } }
.clickable-author { cursor: pointer; &:hover { color: #409EFF; } }
.reply-highlight { animation: highlightFade 2s ease-out; }
@keyframes highlightFade { 0% { background-color: #ecf5ff; } 100% { background-color: transparent; } }
</style>
