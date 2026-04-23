<template>
  <div class="community-page">
    <van-nav-bar title="社区">
      <template #right>
        <el-button type="primary" size="small" @click="showPostDialog = true">发帖</el-button>
      </template>
    </van-nav-bar>

    <div class="filter-bar">
      <el-radio-group v-model="topicType" size="small" @change="loadPosts">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="1">经验分享</el-radio-button>
        <el-radio-button :value="2">难题求助</el-radio-button>
      </el-radio-group>
    </div>

    <div class="post-list" v-loading="loading">
      <el-empty v-if="!loading && posts.length === 0" description="暂无帖子" />

      <div v-for="post in posts" :key="post.id" class="post-card" @click="viewPost(post)">
        <div class="post-header">
          <el-avatar :size="32" :src="post.authorAvatar || undefined">
            {{ post.authorNickname?.charAt(0) }}
          </el-avatar>
          <div class="post-meta">
            <span class="author">{{ post.authorNickname || '用户' }}</span>
            <span class="time">{{ post.createTime }}</span>
          </div>
          <el-tag :type="post.topicType === 1 ? 'primary' : 'warning'" size="small">
            {{ post.topicType === 1 ? '经验分享' : '难题求助' }}
          </el-tag>
        </div>
        <h3 class="post-title">{{ post.title }}</h3>
        <p class="post-content" v-if="post.content">{{ post.content }}</p>
        <div class="post-stats">
          <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
          <span><el-icon><ChatDotRound /></el-icon> {{ post.replyCount || 0 }}</span>
          <span @click.stop="handleLike(post)" class="like-btn" :class="{ active: post.liked }">
            <el-icon><StarFilled v-if="post.liked" /><Star v-else /></el-icon> {{ post.likeCount || 0 }}
          </span>
        </div>
      </div>
    </div>

    <el-dialog v-model="showPostDialog" title="发布帖子" width="90%" :close-on-click-modal="false">
      <el-form :model="postForm" label-position="top">
        <el-form-item label="类型">
          <el-radio-group v-model="postForm.topicType">
            <el-radio :value="1">经验分享</el-radio>
            <el-radio :value="2">难题求助</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="postForm.title" placeholder="请输入标题" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="postForm.content" type="textarea" :rows="4" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPostDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreatePost">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, ChatDotRound, Star, StarFilled } from '@element-plus/icons-vue'
import { getCommunityPosts, createCommunityPost, likeCommunityPost } from '@shared/api/community'

const router = useRouter()
const loading = ref(false)
const posts = ref([])
const topicType = ref(null)
const showPostDialog = ref(false)
const submitting = ref(false)
const postForm = ref({ topicType: 1, title: '', content: '' })

onMounted(() => {
  loadPosts()
})

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await getCommunityPosts({ topicType: topicType.value, page: 1, size: 20 })
    if (res.code === 200) {
      posts.value = res.data?.records || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const viewPost = (post) => {
  router.push(`/community/${post.id}`)
}

const handleLike = async (post) => {
  try {
    const res = await likeCommunityPost(post.id)
    if (res.code === 200) {
      const liked = res.data.liked
      post.liked = liked
      post.likeCount = (post.likeCount || 0) + (liked ? 1 : -1)
    }
  } catch (e) {
    console.error(e)
  }
}

const handleCreatePost = async () => {
  if (!postForm.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  submitting.value = true
  try {
    const res = await createCommunityPost(postForm.value)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      showPostDialog.value = false
      postForm.value = { topicType: 1, title: '', content: '' }
      loadPosts()
    }
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.community-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.filter-bar {
  padding: 12px 16px;
  background: #fff;
}

.post-list {
  padding: 12px 16px;
}

.post-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-1px);
  }
}

.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;

  .post-meta {
    flex: 1;

    .author {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }

    .time {
      font-size: 12px;
      color: #909399;
    }
  }
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px;
}

.post-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0 0 10px;
}

.post-stats {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .like-btn {
    transition: color 0.2s;
    &.active { color: #f56c6c; }
  }
}
</style>
