<template>
  <div class="community-detail-page">
    <el-page-header @back="goBack">
      <template #content>
        <span>帖子详情</span>
      </template>
    </el-page-header>

    <div v-loading="loading" class="detail-content">
      <div v-if="post" class="post-detail-card">
        <div class="post-header">
          <el-avatar :size="48" :src="post.authorAvatar">
            {{ post.authorName?.charAt(0) }}
          </el-avatar>
          <div class="post-meta">
            <span class="author-name">{{ post.authorName }}</span>
            <span class="post-time">{{ formatTime(post.createTime) }}</span>
          </div>
          <el-tag v-if="post.topicType === 1" type="success">经验分享</el-tag>
          <el-tag v-else type="warning">难题求助</el-tag>
        </div>

        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-content">{{ post.content }}</div>

        <div class="post-actions">
          <el-button :type="liked ? 'primary' : ''" @click="handleLike">
            <el-icon><Star /></el-icon> {{ post.likeCount || 0 }}
          </el-button>
          <span class="stat-item"><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
          <span class="stat-item"><el-icon><ChatDotRound /></el-icon> {{ post.replyCount || 0 }}</span>
        </div>
      </div>

      <div class="replies-section">
        <h3>评论 ({{ post?.replyCount || 0 }})</h3>

        <div class="reply-input">
          <el-input
            v-model="replyContent"
            placeholder="写下你的评论..."
            maxlength="512"
            show-word-limit
          >
            <template #append>
              <el-button :loading="replying" @click="handleReply">发送</el-button>
            </template>
          </el-input>
        </div>

        <div class="reply-list">
          <div v-for="reply in replies" :key="reply.id" class="reply-item">
            <el-avatar :size="32" :src="reply.authorAvatar">
              {{ reply.authorName?.charAt(0) }}
            </el-avatar>
            <div class="reply-body">
              <div class="reply-header">
                <span class="reply-author">{{ reply.authorName }}</span>
                <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
              </div>
              <p class="reply-content">{{ reply.content }}</p>
            </div>
          </div>

          <el-empty v-if="replies.length === 0" description="暂无评论" />
        </div>

        <div v-if="replyTotal > 10" class="pagination">
          <el-pagination
            v-model:current-page="replyPage"
            :page-size="10"
            :total="replyTotal"
            layout="prev, pager, next"
            @current-change="loadReplies"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, View, ChatDotRound } from '@element-plus/icons-vue'
import { getCommunityPostDetail, likeCommunityPost, getCommunityReplies, createCommunityReply } from '@shared/api/community'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const replying = ref(false)
const liked = ref(false)
const post = ref(null)
const replies = ref([])
const replyContent = ref('')
const replyPage = ref(1)
const replyTotal = ref(0)

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

const goBack = () => router.back()

const loadPost = async () => {
  loading.value = true
  try {
    const res = await getCommunityPostDetail(route.params.id)
    if (res.code === 200) {
      post.value = res.data
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadReplies = async () => {
  try {
    const res = await getCommunityReplies(route.params.id, {
      page: replyPage.value,
      size: 10
    })
    if (res.code === 200) {
      replies.value = res.data?.records || []
      replyTotal.value = res.data?.total || 0
    }
  } catch (e) {
    console.error('加载评论失败', e)
  }
}

const handleLike = async () => {
  try {
    const res = await likeCommunityPost(route.params.id)
    if (res.code === 200) {
      liked.value = true
      if (post.value) {
        post.value.likeCount = (post.value.likeCount || 0) + 1
      }
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  replying.value = true
  try {
    const res = await createCommunityReply(route.params.id, {
      content: replyContent.value
    })
    if (res.code === 200) {
      ElMessage.success('评论成功')
      replyContent.value = ''
      loadReplies()
      if (post.value) {
        post.value.replyCount = (post.value.replyCount || 0) + 1
      }
    }
  } catch (e) {
    ElMessage.error('评论失败')
  } finally {
    replying.value = false
  }
}

onMounted(() => {
  loadPost()
  loadReplies()
})
</script>

<style lang="scss" scoped>
.community-detail-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.detail-content {
  margin-top: 20px;
}

.post-detail-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .post-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;

    .post-meta {
      flex: 1;
      .author-name { font-weight: 600; display: block; }
      .post-time { font-size: 12px; color: #909399; }
    }
  }

  .post-title {
    font-size: 22px;
    font-weight: 700;
    margin: 0 0 16px;
  }

  .post-content {
    font-size: 15px;
    line-height: 1.8;
    color: #303133;
    white-space: pre-wrap;
    margin-bottom: 20px;
  }

  .post-actions {
    display: flex;
    align-items: center;
    gap: 16px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #909399;
      font-size: 14px;
    }
  }
}

.replies-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 20px;
  }

  .reply-input {
    margin-bottom: 24px;
  }

  .reply-list {
    .reply-item {
      display: flex;
      gap: 12px;
      padding: 12px 0;
      border-bottom: 1px solid #f5f5f5;

      &:last-child {
        border-bottom: none;
      }

      .reply-body {
        flex: 1;

        .reply-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 6px;

          .reply-author {
            font-weight: 500;
            font-size: 14px;
          }

          .reply-time {
            font-size: 12px;
            color: #909399;
          }
        }

        .reply-content {
          font-size: 14px;
          color: #303133;
          line-height: 1.6;
          margin: 0;
        }
      }
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
