<template>
  <div class="community-page">
    <div class="community-header">
      <h1>社区</h1>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>发帖
      </el-button>
    </div>

    <div class="topic-tabs">
      <el-radio-group v-model="currentTopic" @change="loadPosts">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="1">经验分享</el-radio-button>
        <el-radio-button :value="2">难题求助</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="loading" class="post-list">
      <div v-for="post in posts" :key="post.id" class="post-card" @click="goToDetail(post.id)">
        <div class="post-header">
          <el-avatar :size="36" :src="post.authorAvatar">
            {{ post.authorName?.charAt(0) }}
          </el-avatar>
          <div class="post-meta">
            <span class="author-name">{{ post.authorName }}</span>
            <span class="post-time">{{ formatTime(post.createTime) }}</span>
          </div>
          <el-tag v-if="post.topicType === 1" size="small" type="success">经验分享</el-tag>
          <el-tag v-else size="small" type="warning">难题求助</el-tag>
        </div>
        <h3 class="post-title">{{ post.title }}</h3>
        <p class="post-content-preview">{{ post.content?.substring(0, 120) }}{{ post.content?.length > 120 ? '...' : '' }}</p>
        <div class="post-stats">
          <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
          <span><el-icon><ChatDotRound /></el-icon> {{ post.replyCount || 0 }}</span>
          <span><el-icon><Star /></el-icon> {{ post.likeCount || 0 }}</span>
        </div>
      </div>

      <el-empty v-if="!loading && posts.length === 0" description="暂无帖子" />
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="10"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadPosts"
      />
    </div>

    <el-dialog v-model="showCreateDialog" title="发布帖子" width="560px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="类型">
          <el-radio-group v-model="createForm.topicType">
            <el-radio :value="1">经验分享</el-radio>
            <el-radio :value="2">难题求助</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="createForm.title" placeholder="请输入标题" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="createForm.content"
            type="textarea"
            :rows="6"
            placeholder="分享您的经验或提出问题..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, View, ChatDotRound, Star } from '@element-plus/icons-vue'
import { getCommunityPosts, createCommunityPost } from '@shared/api/community'

const router = useRouter()
const loading = ref(false)
const creating = ref(false)
const posts = ref([])
const total = ref(0)
const currentPage = ref(1)
const currentTopic = ref(null)
const showCreateDialog = ref(false)
const createForm = reactive({
  topicType: 1,
  title: '',
  content: ''
})

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

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await getCommunityPosts({
      topicType: currentTopic.value,
      page: currentPage.value,
      size: 10
    })
    if (res.code === 200) {
      posts.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/community/${id}`)
}

const handleCreate = async () => {
  if (!createForm.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!createForm.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  creating.value = true
  try {
    const res = await createCommunityPost({
      title: createForm.title,
      content: createForm.content,
      topicType: createForm.topicType
    })
    if (res.code === 200) {
      ElMessage.success('发布成功')
      showCreateDialog.value = false
      createForm.title = ''
      createForm.content = ''
      loadPosts()
    }
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style lang="scss" scoped>
.community-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.community-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h1 {
    font-size: 24px;
    font-weight: 700;
    margin: 0;
  }
}

.topic-tabs {
  margin-bottom: 20px;
}

.post-list {
  min-height: 300px;
}

.post-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }

  .post-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;

    .post-meta {
      flex: 1;

      .author-name {
        font-weight: 500;
        display: block;
      }

      .post-time {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .post-title {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 8px;
    color: #303133;
  }

  .post-content-preview {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    margin: 0 0 12px;
  }

  .post-stats {
    display: flex;
    gap: 20px;
    color: #909399;
    font-size: 13px;

    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
