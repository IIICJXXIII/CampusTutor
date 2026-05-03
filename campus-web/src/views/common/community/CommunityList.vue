<template>
  <div class="community-page">
    <van-nav-bar title="社区">
      <template #right>
        <el-button
          v-if="userStore.isLoggedIn"
          type="primary"
          size="small"
          class="publish-btn"
          @click="openPostDialog"
        >
          <el-icon><Plus /></el-icon>
          <span class="publish-btn-text">发布帖子</span>
        </el-button>
        <el-button
          v-else
          size="small"
          class="login-hint-btn"
          @click="goLogin"
        >
          <el-icon><User /></el-icon>
          <span class="publish-btn-text">登录发帖</span>
        </el-button>
      </template>
    </van-nav-bar>

    <div class="filter-bar">
      <el-radio-group v-model="topicType" size="small" @change="loadPosts">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="1">经验分享</el-radio-button>
        <el-radio-button :value="2">难题求助</el-radio-button>
      </el-radio-group>
      <el-button
        v-if="userStore.isLoggedIn"
        type="primary"
        size="small"
        round
        class="filter-bar-publish-btn"
        @click="openPostDialog"
      >
        <el-icon><Plus /></el-icon>发布帖子
      </el-button>
      <el-button
        v-else
        type="primary"
        size="small"
        round
        plain
        class="filter-bar-publish-btn"
        @click="goLogin"
      >
        <el-icon><Plus /></el-icon>登录发帖
      </el-button>
    </div>

    <div class="post-list" v-loading="loading">
      <el-empty v-if="!loading && posts.length === 0" description="暂无帖子" />

      <transition-group name="post-fade">
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
          <div class="post-tags" v-if="post.tags">
            <el-tag
              v-for="tag in parseTags(post.tags)"
              :key="tag"
              size="small"
              type="info"
              effect="plain"
              class="post-tag"
            >
              {{ tag }}
            </el-tag>
          </div>
          <div class="post-stats">
            <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ post.replyCount || 0 }}</span>
            <span @click.stop="handleLike(post)" class="like-btn" :class="{ active: post.liked }">
              <svg viewBox="0 0 24 24" width="14" height="14" :fill="post.liked ? '#f56c6c' : 'none'" :stroke="post.liked ? '#f56c6c' : 'currentColor'" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
              {{ post.likeCount || 0 }}
            </span>
          </div>
        </div>
      </transition-group>

      <div v-if="!loading && posts.length > 0" class="load-more">
        <el-button text @click="loadMorePosts" :loading="loadingMore">
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </el-button>
      </div>
    </div>

    <el-dialog
      v-model="showPostDialog"
      title="发布帖子"
      width="90%"
      :close-on-click-modal="false"
      class="post-dialog"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form
        ref="postFormRef"
        :model="postForm"
        :rules="formRules"
        label-position="top"
        class="post-form"
        @submit.prevent
      >
        <el-form-item label="帖子分类" prop="topicType">
          <el-radio-group v-model="postForm.topicType" class="topic-type-group">
            <el-radio :value="1">
              <el-icon><Promotion /></el-icon> 经验分享
            </el-radio>
            <el-radio :value="2">
              <el-icon><QuestionFilled /></el-icon> 难题求助
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="帖子标题" prop="title">
          <el-input
            v-model="postForm.title"
            placeholder="请输入标题（最多100个字符）"
            maxlength="100"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="帖子内容" prop="content">
          <div class="content-editor">
            <div class="editor-toolbar">
              <el-button-group size="small">
                <el-button @click="insertMarkdown('**', '**')" title="加粗">
                  <el-icon><EditPen /></el-icon>
                </el-button>
                <el-button @click="insertMarkdown('*', '*')" title="斜体">
                  <el-icon><Postcard /></el-icon>
                </el-button>
                <el-button @click="insertMarkdown('\n- ', '')" title="列表">
                  <el-icon><List /></el-icon>
                </el-button>
                <el-button @click="insertMarkdown('\n> ', '')" title="引用">
                  <el-icon><ChatLineSquare /></el-icon>
                </el-button>
              </el-button-group>
              <el-button
                size="small"
                :type="isPreview ? 'primary' : ''"
                @click="togglePreview"
              >
                <el-icon><View /></el-icon>
                {{ isPreview ? '编辑' : '预览' }}
              </el-button>
            </div>
            <div v-if="!isPreview" class="editor-area">
              <el-input
                ref="contentInputRef"
                v-model="postForm.content"
                type="textarea"
                :rows="8"
                placeholder="请输入帖子内容（支持Markdown格式）"
                maxlength="5000"
                show-word-limit
                resize="none"
              />
            </div>
            <div v-else class="preview-area">
              <div v-if="postForm.content" class="markdown-preview" v-html="renderedContent"></div>
              <el-empty v-else description="暂无内容可预览" :image-size="60" />
            </div>
          </div>
        </el-form-item>

        <el-form-item label="标签（最多5个）" prop="tags">
          <div class="tags-section">
            <div class="tag-checkboxes">
              <el-check-tag
                v-for="tag in COMMUNITY_TAGS"
                :key="tag"
                :checked="postForm.tags.includes(tag)"
                @change="toggleTag(tag)"
                class="tag-item"
                :class="{ 'tag-disabled': postForm.tags.length >= 5 && !postForm.tags.includes(tag) }"
              >
                {{ tag }}
              </el-check-tag>
            </div>
            <div class="custom-tag-input">
              <el-input
                v-model="customTagInput"
                placeholder="自定义标签，回车添加"
                size="small"
                :disabled="postForm.tags.length >= 5"
                @keyup.enter="addCustomTag"
                maxlength="10"
              >
                <template #append>
                  <el-button @click="addCustomTag" :disabled="postForm.tags.length >= 5 || !customTagInput.trim()">
                    添加
                  </el-button>
                </template>
              </el-input>
            </div>
            <div class="selected-tags" v-if="postForm.tags.length > 0">
              <el-tag
                v-for="tag in postForm.tags"
                :key="tag"
                closable
                size="small"
                @close="removeTag(tag)"
                class="selected-tag"
              >
                {{ tag }}
              </el-tag>
              <span class="tag-count">{{ postForm.tags.length }}/5</span>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleCreatePost">
            <el-icon v-if="!submitting"><Promotion /></el-icon>
            {{ submitting ? '发布中...' : '提交' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showLoginDialog"
      title="提示"
      width="80%"
      class="login-dialog"
      :show-close="true"
    >
      <div class="login-prompt">
        <el-icon :size="48" color="#409eff"><WarningFilled /></el-icon>
        <p>发布帖子需要先登录</p>
        <p class="login-sub">登录后即可参与社区互动</p>
      </div>
      <template #footer>
        <el-button @click="showLoginDialog = false">稍后再说</el-button>
        <el-button type="primary" @click="goLogin">去登录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  View, ChatDotRound, Plus, User,
  Promotion, QuestionFilled, EditPen, Postcard, List,
  ChatLineSquare, WarningFilled
} from '@element-plus/icons-vue'
import { getCommunityPosts, createCommunityPost, likeCommunityPost, COMMUNITY_TAGS } from '@shared/api/community'
import { useUserStore } from '@shared/stores'
import { marked } from 'marked'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const loadingMore = ref(false)
const posts = ref([])
const topicType = ref(null)
const currentPage = ref(1)
const hasMore = ref(true)
const showPostDialog = ref(false)
const showLoginDialog = ref(false)
const submitting = ref(false)
const isPreview = ref(false)
const customTagInput = ref('')
const postFormRef = ref(null)
const contentInputRef = ref(null)

const postForm = ref({
  topicType: 1,
  title: '',
  content: '',
  tags: []
})

const validateTitle = (rule, value, callback) => {
  if (!value || !value.trim()) {
    callback(new Error('请输入帖子标题'))
  } else if (value.trim().length > 100) {
    callback(new Error('标题不能超过100个字符'))
  } else {
    callback()
  }
}

const validateContent = (rule, value, callback) => {
  if (!value || !value.trim()) {
    callback(new Error('请输入帖子内容'))
  } else {
    callback()
  }
}

const validateTags = (rule, value, callback) => {
  if (value.length > 5) {
    callback(new Error('最多选择5个标签'))
  } else {
    callback()
  }
}

const formRules = {
  topicType: [
    { required: true, message: '请选择帖子分类', trigger: 'change' }
  ],
  title: [
    { required: true, validator: validateTitle, trigger: 'blur' }
  ],
  content: [
    { required: true, validator: validateContent, trigger: 'blur' }
  ],
  tags: [
    { validator: validateTags, trigger: 'change' }
  ]
}

const renderedContent = computed(() => {
  if (!postForm.value.content) return ''
  try {
    return marked(postForm.value.content)
  } catch {
    return postForm.value.content
  }
})

onMounted(() => {
  loadPosts()
})

const loadPosts = async () => {
  loading.value = true
  currentPage.value = 1
  hasMore.value = true
  try {
    const res = await getCommunityPosts({ topicType: topicType.value, page: 1, size: 20 })
    if (res.code === 200) {
      posts.value = res.data?.records || []
      hasMore.value = posts.value.length >= 20
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadMorePosts = async () => {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  currentPage.value++
  try {
    const res = await getCommunityPosts({
      topicType: topicType.value,
      page: currentPage.value,
      size: 20
    })
    if (res.code === 200) {
      const newPosts = res.data?.records || []
      posts.value.push(...newPosts)
      hasMore.value = newPosts.length >= 20
    }
  } catch (e) {
    console.error(e)
    currentPage.value--
  } finally {
    loadingMore.value = false
  }
}

const viewPost = (post) => {
  router.push(`/community/${post.id}`)
}

const parseTags = (tagsStr) => {
  if (!tagsStr) return []
  return tagsStr.split(',').filter(t => t.trim())
}

const handleLike = async (post) => {
  if (!userStore.isLoggedIn) {
    showLoginDialog.value = true
    return
  }
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

const openPostDialog = () => {
  if (!userStore.isLoggedIn) {
    showLoginDialog.value = true
    return
  }
  showPostDialog.value = true
}

const goLogin = () => {
  showLoginDialog.value = false
  router.push('/login')
}

const togglePreview = () => {
  isPreview.value = !isPreview.value
}

const insertMarkdown = (prefix, suffix) => {
  const textarea = contentInputRef.value?.$el?.querySelector('textarea')
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const content = postForm.value.content || ''
  const selectedText = content.substring(start, end)

  const newContent =
    content.substring(0, start) +
    prefix +
    (selectedText || '文本') +
    suffix +
    content.substring(end)

  postForm.value.content = newContent

  nextTick(() => {
    textarea.focus()
    const newPos = start + prefix.length + (selectedText || '文本').length
    textarea.setSelectionRange(newPos, newPos)
  })
}

const toggleTag = (tag) => {
  const idx = postForm.value.tags.indexOf(tag)
  if (idx >= 0) {
    postForm.value.tags.splice(idx, 1)
  } else {
    if (postForm.value.tags.length >= 5) {
      ElMessage.warning('最多选择5个标签')
      return
    }
    postForm.value.tags.push(tag)
  }
}

const addCustomTag = () => {
  const tag = customTagInput.value.trim()
  if (!tag) return
  if (postForm.value.tags.length >= 5) {
    ElMessage.warning('最多选择5个标签')
    return
  }
  if (postForm.value.tags.includes(tag)) {
    ElMessage.warning('标签已存在')
    return
  }
  if (tag.length > 10) {
    ElMessage.warning('标签长度不能超过10个字符')
    return
  }
  postForm.value.tags.push(tag)
  customTagInput.value = ''
}

const removeTag = (tag) => {
  const idx = postForm.value.tags.indexOf(tag)
  if (idx >= 0) {
    postForm.value.tags.splice(idx, 1)
  }
}

const resetForm = () => {
  postForm.value = {
    topicType: 1,
    title: '',
    content: '',
    tags: []
  }
  isPreview.value = false
  customTagInput.value = ''
  postFormRef.value?.resetFields()
}

const handleCancel = () => {
  const hasContent = postForm.value.title || postForm.value.content || postForm.value.tags.length > 0
  if (hasContent) {
    showPostDialog.value = false
  } else {
    showPostDialog.value = false
  }
}

const handleCreatePost = async () => {
  if (!postFormRef.value) return

  try {
    const valid = await postFormRef.value.validate()
    if (!valid) return
  } catch {
    ElMessage.warning('请检查表单填写是否完整')
    return
  }

  submitting.value = true
  try {
    const data = {
      title: postForm.value.title.trim(),
      content: postForm.value.content.trim(),
      topicType: postForm.value.topicType,
      tags: postForm.value.tags.length > 0 ? postForm.value.tags.join(',') : ''
    }

    const res = await createCommunityPost(data)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      showPostDialog.value = false
      loadPosts()
    }
  } catch (e) {
    const errorMsg = e?.message || '发布失败，请稍后重试'
    ElMessage.error(errorMsg)
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

.publish-btn,
.login-hint-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.publish-btn-text {
  @media (max-width: 360px) {
    display: none;
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  gap: 12px;

  .filter-bar-publish-btn {
    flex-shrink: 0;
    font-weight: 500;
  }
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
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &:active {
    transform: translateY(0);
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

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;

  .post-tag {
    font-size: 12px;
  }
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

.load-more {
  text-align: center;
  padding: 16px 0;
}

.post-fade-enter-active {
  transition: all 0.3s ease-out;
}

.post-fade-leave-active {
  transition: all 0.2s ease-in;
}

.post-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.post-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.post-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
    max-width: 600px;

    @media (max-width: 480px) {
      margin: 8px auto;
      width: 95% !important;
    }
  }

  :deep(.el-dialog__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
    margin-right: 0;
  }

  :deep(.el-dialog__body) {
    padding: 20px;
    max-height: 60vh;
    overflow-y: auto;
  }

  :deep(.el-dialog__footer) {
    padding: 12px 20px;
    border-top: 1px solid #f0f0f0;
  }
}

.post-form {
  .topic-type-group {
    display: flex;
    gap: 16px;
  }

  .content-editor {
    width: 100%;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    overflow: hidden;
    transition: border-color 0.2s;

    &:focus-within {
      border-color: #409eff;
    }
  }

  .editor-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    background: #f5f7fa;
    border-bottom: 1px solid #ebeef5;
  }

  .editor-area {
    :deep(.el-textarea__inner) {
      border: none;
      box-shadow: none;
      padding: 12px;
      font-size: 14px;
      line-height: 1.8;
    }
  }

  .preview-area {
    min-height: 200px;
    padding: 12px;
    background: #fff;
  }

  .markdown-preview {
    font-size: 14px;
    line-height: 1.8;
    color: #303133;
    word-break: break-word;

    :deep(h1), :deep(h2), :deep(h3) {
      margin: 12px 0 8px;
      font-weight: 600;
    }

    :deep(p) {
      margin: 8px 0;
    }

    :deep(ul), :deep(ol) {
      padding-left: 20px;
      margin: 8px 0;
    }

    :deep(blockquote) {
      margin: 8px 0;
      padding: 8px 16px;
      border-left: 4px solid #409eff;
      background: #f5f7fa;
      color: #606266;
    }

    :deep(strong) {
      font-weight: 600;
    }

    :deep(code) {
      background: #f5f7fa;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 13px;
    }
  }

  .tags-section {
    width: 100%;
  }

  .tag-checkboxes {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 12px;
  }

  .tag-item {
    transition: all 0.2s;

    &.tag-disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
  }

  .custom-tag-input {
    margin-bottom: 12px;

    :deep(.el-input-group__append) {
      padding: 0 8px;
    }
  }

  .selected-tags {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;

    .selected-tag {
      animation: tagIn 0.2s ease-out;
    }

    .tag-count {
      font-size: 12px;
      color: #909399;
      margin-left: 4px;
    }
  }
}

@keyframes tagIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.login-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
    max-width: 360px;
  }
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;

  p {
    margin: 12px 0 0;
    font-size: 16px;
    font-weight: 500;
    color: #303133;
  }

  .login-sub {
    font-size: 13px;
    color: #909399;
    font-weight: 400;
    margin-top: 4px;
  }
}

@media (max-width: 480px) {
  .filter-bar {
    :deep(.el-radio-button__inner) {
      padding: 6px 10px;
      font-size: 12px;
    }
  }

  .post-card {
    padding: 12px;
    border-radius: 8px;
  }

  .post-title {
    font-size: 15px;
  }

  .post-content {
    font-size: 13px;
  }

  .post-form {
    .editor-toolbar {
      padding: 6px 8px;

      :deep(.el-button) {
        padding: 4px 6px;
      }
    }
  }
}
</style>
