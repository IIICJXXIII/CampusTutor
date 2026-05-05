<template>
  <div class="community-page">
    <van-nav-bar title="社区" fixed placeholder>
      <template #right>
        <el-button type="primary" size="small" round @click="openPostDialog">
          <el-icon><Plus /></el-icon>发帖
        </el-button>
      </template>
    </van-nav-bar>

    <!-- 顶部横幅 -->
    <div class="community-banner">
      <div class="banner-content">
        <h2 class="banner-title">校园社区</h2>
        <p class="banner-desc">分享经验，互帮互助，共建温暖校园</p>
      </div>
      <div class="banner-stats">
        <div class="stat-item">
          <span class="stat-num">{{ totalPosts }}</span>
          <span class="stat-label">帖子</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">2</span>
          <span class="stat-label">话题</span>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-scroll">
        <span
          v-for="item in filterItems"
          :key="item.value"
          class="filter-chip"
          :class="{ active: topicType === item.value }"
          @click="switchFilter(item.value)"
        >{{ item.label }}</span>
      </div>
      <el-button
        v-if="userStore.isLoggedIn"
        type="primary" size="small" round
        class="filter-bar-publish-btn"
        @click="openPostDialog"
      ><el-icon><Plus /></el-icon>发布</el-button>
    </div>

    <!-- 帖子列表 -->
    <div class="post-list" v-loading="loading">
      <el-empty v-if="!loading && posts.length === 0" description="还没有帖子，快来发布第一条吧" :image-size="100" />

      <div v-for="post in posts" :key="post.id" class="post-card" @click="viewPost(post)">
        <!-- 左侧色条 -->
        <div class="post-accent" :class="post.topicType === 1 ? 'accent-share' : 'accent-help'" />

        <div class="post-body">
          <div class="post-header">
            <el-avatar :size="36" :src="post.authorAvatar || undefined" class="post-avatar">
              {{ post.authorNickname?.charAt(0) }}
            </el-avatar>
            <div class="post-meta">
              <span class="author">{{ post.authorNickname || '用户' }}</span>
              <span class="time">{{ formatTime(post.createTime) }}</span>
            </div>
            <div class="post-topic-badge" :class="post.topicType === 1 ? 'badge-share' : 'badge-help'">
              {{ post.topicType === 1 ? '经验分享' : '难题求助' }}
            </div>
            <span v-if="post.viewCount > 50" class="hot-badge">🔥热</span>
          </div>

          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-content" v-if="post.content">{{ post.content }}</p>

          <!-- 标签 -->
          <div class="post-tags" v-if="post.tags">
            <span v-for="tag in parseTags(post.tags)" :key="tag" class="tag-chip" :style="{ background: tagColor(tag).bg, color: tagColor(tag).fg }">
              {{ tag }}
            </span>
          </div>

          <div class="post-footer">
            <div class="post-stats">
              <span class="stat-item"><el-icon><View /></el-icon>{{ post.viewCount || 0 }}</span>
              <span class="stat-item"><el-icon><ChatDotRound /></el-icon>{{ post.replyCount || 0 }}</span>
              <span class="stat-item like-btn" :class="{ active: post.liked }" @click.stop="handleLike(post)">
                <svg viewBox="0 0 24 24" width="15" height="15" :fill="post.liked ? '#f56c6c' : 'none'" :stroke="post.liked ? '#f56c6c' : 'currentColor'" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                {{ post.likeCount || 0 }}
              </span>
            </div>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div v-if="posts.length > 0" class="load-more" @click="loadMorePosts">
        <span v-if="loadingMore">加载中...</span>
        <span v-else>加载更多</span>
      </div>
    </div>

    <!-- 发布弹窗 -->
    <el-dialog v-model="showPostDialog" title="发布帖子" width="90%" :close-on-click-modal="false" destroy-on-close class="post-dialog">
      <el-form :model="postForm" label-position="top" @submit.prevent>
        <el-form-item label="话题类型">
          <div class="topic-selector">
            <div
              class="topic-card"
              :class="{ active: postForm.topicType === 1 }"
              @click="postForm.topicType = 1"
            >
              <el-icon :size="24" color="#67C23A"><Promotion /></el-icon>
              <span>经验分享</span>
            </div>
            <div
              class="topic-card"
              :class="{ active: postForm.topicType === 2 }"
              @click="postForm.topicType = 2"
            >
              <el-icon :size="24" color="#E6A23C"><QuestionFilled /></el-icon>
              <span>难题求助</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="选择标签">
          <div class="tag-selector">
            <span
              v-for="tag in COMMUNITY_TAGS"
              :key="tag"
              class="tag-option"
              :class="{ selected: selectedTags.includes(tag) }"
              @click="toggleTag(tag)"
            >{{ tag }}</span>
          </div>
        </el-form-item>

        <el-form-item label="帖子标题">
          <el-input v-model="postForm.title" placeholder="起一个吸引人的标题吧~" maxlength="128" show-word-limit size="large" />
        </el-form-item>

        <el-form-item label="帖子内容">
          <el-input v-model="postForm.content" type="textarea" :rows="6" placeholder="分享你的经验或问题..." maxlength="5000" show-word-limit resize="none" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPostDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreatePost" round>{{ submitting ? '发布中...' : '立即发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, ChatDotRound, Plus, Promotion, QuestionFilled, ArrowRight } from '@element-plus/icons-vue'
import { getCommunityPosts, createCommunityPost, likeCommunityPost, COMMUNITY_TAGS } from '@shared/api/community'
import { useUserStore } from '@shared/stores'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const loadingMore = ref(false)
const posts = ref([])
const topicType = ref(null)
const currentPage = ref(1)
const hasMore = ref(true)
const showPostDialog = ref(false)
const submitting = ref(false)
const selectedTags = ref([])
const postForm = ref({ topicType: 1, title: '', content: '', tags: '' })

const totalPosts = computed(() => posts.value.length)

const filterItems = [
  { label: '全部', value: null },
  { label: '✨ 经验分享', value: 1 },
  { label: '❓ 难题求助', value: 2 }
]

const tagColors = {
  '学习经验': { bg: '#e8f5e9', fg: '#2e7d32' },
  '考试技巧': { bg: '#fff3e0', fg: '#e65100' },
  '选课建议': { bg: '#e3f2fd', fg: '#1565c0' },
  '校园生活': { bg: '#fce4ec', fg: '#c62828' },
  '活动推荐': { bg: '#f3e5f5', fg: '#6a1b9a' },
  '求助问答': { bg: '#fff8e1', fg: '#f57f17' }
}

const tagColor = (tag) => tagColors[tag] || { bg: '#f5f5f5', fg: '#616161' }

const parseTags = (tagsStr) => {
  if (!tagsStr) return []
  return tagsStr.split(',').map(t => t.trim()).filter(Boolean)
}

const toggleTag = (tag) => {
  const idx = selectedTags.value.indexOf(tag)
  if (idx >= 0) selectedTags.value.splice(idx, 1)
  else if (selectedTags.value.length < 5) selectedTags.value.push(tag)
}

const formatTime = (time) => {
  if (!time) return ''
  const d = dayjs(time)
  const now = dayjs()
  if (now.diff(d, 'minute') < 1) return '刚刚'
  if (now.diff(d, 'hour') < 24) return d.fromNow()
  return d.format('MM-DD HH:mm')
}

const openPostDialog = () => {
  postForm.value = { topicType: 1, title: '', content: '', tags: '' }
  selectedTags.value = []
  showPostDialog.value = true
}

const goLogin = () => router.push('/login')

const switchFilter = (val) => {
  topicType.value = val
  currentPage.value = 1
  loadPosts()
}

onMounted(() => { loadPosts() })

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await getCommunityPosts({ topicType: topicType.value, page: 1, size: 20 })
    if (res.code === 200) {
      posts.value = res.data?.records || []
      hasMore.value = posts.value.length >= 20
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const loadMorePosts = async () => {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  currentPage.value++
  try {
    const res = await getCommunityPosts({ topicType: topicType.value, page: currentPage.value, size: 20 })
    if (res.code === 200) {
      const newPosts = res.data?.records || []
      posts.value.push(...newPosts)
      hasMore.value = newPosts.length >= 20
    }
  } catch (e) { console.error(e); currentPage.value-- } finally { loadingMore.value = false }
}

const viewPost = (post) => router.push(`/community/${post.id}`)

const handleLike = async (post) => {
  try {
    const res = await likeCommunityPost(post.id)
    if (res.code === 200) {
      const liked = res.data.liked
      post.liked = liked
      post.likeCount = (post.likeCount || 0) + (liked ? 1 : -1)
    }
  } catch (e) { console.error(e) }
}

const handleCreatePost = async () => {
  if (!postForm.value.title) { ElMessage.warning('请输入标题'); return }
  if (!postForm.value.content) { ElMessage.warning('请输入内容'); return }
  submitting.value = true
  try {
    const data = { ...postForm.value, tags: selectedTags.value.join(',') }
    const res = await createCommunityPost(data)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      showPostDialog.value = false
      loadPosts()
    }
  } catch (e) { ElMessage.error('发布失败') } finally { submitting.value = false }
}
</script>

<style lang="scss" scoped>
.community-page {
  min-height: 100vh;
  background: #f5f7fa;
}

// ====== 顶部横幅 ======
.community-banner {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  padding: 20px 20px 24px;
  color: #fff;
  .banner-content {
    .banner-title { margin: 0 0 6px; font-size: 22px; font-weight: 700; }
    .banner-desc { margin: 0; font-size: 13px; opacity: 0.85; }
  }
  .banner-stats {
    display: flex; gap: 28px; margin-top: 16px;
    .stat-item { display: flex; flex-direction: column; }
    .stat-num { font-size: 20px; font-weight: 700; }
    .stat-label { font-size: 11px; opacity: 0.75; margin-top: 2px; }
  }
}

// ====== 筛选栏 ======
.filter-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px; background: #fff; position: sticky; top: 46px; z-index: 10;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04); gap: 10px;
}
.filter-scroll { display: flex; gap: 8px; overflow-x: auto; flex: 1; -webkit-overflow-scrolling: touch; &::-webkit-scrollbar { display: none; } }
.filter-chip {
  white-space: nowrap; padding: 6px 14px; border-radius: 20px; font-size: 13px;
  background: #f5f7fa; color: #606266; cursor: pointer; transition: all 0.2s;
  &.active { background: #ecf5ff; color: #409EFF; font-weight: 600; }
}
.filter-bar-publish-btn { flex-shrink: 0; }

// ====== 帖子列表 ======
.post-list { padding: 12px 16px; }

.post-card {
  display: flex; background: #fff; border-radius: 14px; margin-bottom: 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04); cursor: pointer; overflow: hidden;
  transition: all 0.25s;
  &:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); }
  &:active { transform: translateY(0); }
}

.post-accent {
  width: 4px; flex-shrink: 0;
  &.accent-share { background: linear-gradient(180deg, #67C23A, #85ce61); }
  &.accent-help { background: linear-gradient(180deg, #E6A23C, #ebb563); }
}

.post-body { flex: 1; padding: 16px 16px 12px 14px; min-width: 0; }

.post-header {
  display: flex; align-items: center; gap: 10px; margin-bottom: 10px;
  .post-avatar { flex-shrink: 0; }
  .post-meta { flex: 1; min-width: 0;
    .author { display: block; font-size: 14px; font-weight: 600; color: #303133; }
    .time { font-size: 11px; color: #c0c4cc; }
  }
}

.post-topic-badge {
  font-size: 11px; padding: 3px 8px; border-radius: 10px; font-weight: 500; white-space: nowrap;
  &.badge-share { background: #e8f5e9; color: #2e7d32; }
  &.badge-help { background: #fff3e0; color: #e65100; }
}

.hot-badge { font-size: 11px; margin-left: 4px; }

.post-title {
  font-size: 17px; font-weight: 600; color: #1d2129; margin: 0 0 8px; line-height: 1.4;
}

.post-content {
  font-size: 14px; color: #86909c; line-height: 1.65;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; margin: 0 0 8px;
}

.post-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 10px; }
.tag-chip { font-size: 11px; padding: 3px 8px; border-radius: 4px; font-weight: 500; }

.post-footer {
  display: flex; align-items: center; justify-content: space-between;
  border-top: 1px solid #f5f5f5; padding-top: 10px;
  .post-stats { display: flex; gap: 18px; font-size: 13px; color: #c0c4cc; }
  .stat-item { display: flex; align-items: center; gap: 3px; }
  .like-btn { &.active { color: #f56c6c; } }
  .arrow-icon { color: #dcdfe6; }
}

.load-more { text-align: center; padding: 16px 0; color: #909399; font-size: 13px; cursor: pointer; }

// ====== 发布弹窗 ======
.post-dialog {
  :deep(.el-dialog) { border-radius: 16px; max-width: 520px; }
  :deep(.el-dialog__header) { padding: 20px 20px 0; border-bottom: none; }
  :deep(.el-dialog__body) { padding: 16px 20px 20px; }
  :deep(.el-dialog__footer) { padding: 0 20px 20px; }
}

.topic-selector { display: flex; gap: 12px; width: 100%; }
.topic-card {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 16px; border: 2px solid #ebeef5; border-radius: 12px; cursor: pointer;
  transition: all 0.2s;
  span { font-size: 14px; font-weight: 500; color: #606266; }
  &.active { border-color: #409EFF; background: #ecf5ff; span { color: #409EFF; } }
}

.tag-selector { display: flex; flex-wrap: wrap; gap: 8px; }
.tag-option {
  padding: 5px 12px; border-radius: 16px; font-size: 12px;
  background: #f5f7fa; color: #909399; cursor: pointer; transition: all 0.2s;
  &.selected { background: #ecf5ff; color: #409EFF; font-weight: 500; }
}
</style>
