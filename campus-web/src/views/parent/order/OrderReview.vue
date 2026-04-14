<template>
  <div class="order-review-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">评价订单</h1>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>
    
    <template v-else>
      <!-- 教师信息 -->
      <div class="tutor-card">
        <el-avatar :size="56" :src="orderInfo.tutorAvatar">
          {{ orderInfo.tutorName?.charAt(0) }}
        </el-avatar>
        <div class="tutor-info">
          <div class="tutor-name">{{ orderInfo.tutorName }}</div>
          <div class="subject">{{ orderInfo.subject }} | {{ orderInfo.lessonCount }}次课</div>
        </div>
      </div>
      
      <!-- 评分 -->
      <div class="rating-section">
        <div class="rating-item">
          <span class="rating-label">教学质量</span>
          <el-rate v-model="form.teachingScore" show-score />
        </div>
        <div class="rating-item">
          <span class="rating-label">服务态度</span>
          <el-rate v-model="form.attitudeScore" show-score />
        </div>
        <div class="rating-item">
          <span class="rating-label">守时守约</span>
          <el-rate v-model="form.punctualityScore" show-score />
        </div>
      </div>
      
      <!-- 标签选择 -->
      <div class="tags-section">
        <h3 class="section-title">选择标签</h3>
        <div class="tag-list">
          <el-tag
            v-for="tag in availableTags"
            :key="tag"
            :effect="selectedTags.includes(tag) ? 'dark' : 'plain'"
            :type="selectedTags.includes(tag) ? 'primary' : ''"
            class="tag-item"
            @click="toggleTag(tag)"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
      
      <!-- 评价内容 -->
      <div class="content-section">
        <h3 class="section-title">评价内容</h3>
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="5"
          placeholder="请分享您对这位老师的真实评价，帮助其他家长做出选择..."
          maxlength="500"
          show-word-limit
        />
      </div>
      
      <!-- 上传图片 -->
      <div class="image-section">
        <h3 class="section-title">上传图片（选填）</h3>
        <el-upload
          v-model:file-list="imageList"
          list-type="picture-card"
          accept="image/*"
          :limit="4"
          :before-upload="beforeImageUpload"
          :on-remove="handleImageRemove"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </div>
      
      <!-- 匿名选项 -->
      <div class="anonymous-section">
        <el-checkbox v-model="form.anonymous">
          匿名评价
        </el-checkbox>
        <span class="anonymous-tip">匿名后教师将看不到您的个人信息</span>
      </div>
      
      <!-- 提交按钮 -->
      <div class="submit-section">
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          :disabled="!canSubmit"
          @click="submitReview"
        >
          提交评价
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { getOrderDetail } from '@shared/api/order'
import { submitReview as submitApi } from '@shared/api/review'
import { uploadFile } from '@shared/api/file'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const submitting = ref(false)
const orderInfo = ref({})
const imageList = ref([])

const form = ref({
  teachingScore: 5,
  attitudeScore: 5,
  punctualityScore: 5,
  content: '',
  anonymous: false
})

const availableTags = [
  '讲解清晰', '耐心细致', '善于引导', '知识丰富',
  '认真负责', '准时守约', '方法独特', '效果显著',
  '性格开朗', '沟通顺畅'
]

const selectedTags = ref([])

const canSubmit = computed(() => {
  return form.value.teachingScore > 0 &&
         form.value.attitudeScore > 0 &&
         form.value.punctualityScore > 0
})

const goBack = () => {
  router.back()
}

const loadOrder = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    if (res.code === 200) {
      orderInfo.value = res.data || {}
    }
  } catch (error) {
    console.error('加载订单失败:', error)
  } finally {
    loading.value = false
  }
}

const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    if (selectedTags.value.length < 5) {
      selectedTags.value.push(tag)
    } else {
      ElMessage.warning('最多选择5个标签')
    }
  }
}

const beforeImageUpload = async (file) => {
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  
  try {
    const res = await uploadFile(file)
    if (res.code === 200) {
      imageList.value.push({
        name: file.name,
        url: res.data.url
      })
    }
  } catch (error) {
    ElMessage.error('上传失败')
  }
  return false
}

const handleImageRemove = (file) => {
  const index = imageList.value.findIndex(f => f.url === file.url)
  if (index > -1) {
    imageList.value.splice(index, 1)
  }
}

const submitReview = async () => {
  if (!canSubmit.value) {
    ElMessage.warning('请完成评分')
    return
  }
  
  submitting.value = true
  try {
    const data = {
      orderId: route.params.id,
      teachingScore: form.value.teachingScore,
      attitudeScore: form.value.attitudeScore,
      punctualityScore: form.value.punctualityScore,
      content: form.value.content,
      tags: selectedTags.value,
      images: imageList.value.map(f => f.url),
      anonymous: form.value.anonymous
    }
    
    const res = await submitApi(data)
    if (res.code === 200) {
      ElMessage.success('评价提交成功')
      router.back()
    }
  } catch (error) {
    console.error('提交评价失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadOrder()
})
</script>

<style lang="scss" scoped>
.order-review-page {
  padding: 20px;
  max-width: 700px;
  margin: 0 auto;
  padding-bottom: 100px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  
  .page-title {
    font-size: 20px;
    font-weight: 600;
    margin: 0;
  }
}

.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 12px;
}

.tutor-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .tutor-info {
    .tutor-name {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 4px;
    }
    
    .subject {
      font-size: 14px;
      color: #666;
    }
  }
}

.rating-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .rating-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 0;
    
    &:not(:last-child) {
      border-bottom: 1px solid #f5f5f5;
    }
    
    .rating-label {
      font-size: 15px;
    }
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px;
}

.tags-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .tag-item {
    cursor: pointer;
  }
}

.content-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.image-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.anonymous-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .anonymous-tip {
    font-size: 13px;
    color: #999;
  }
}

.submit-section {
  text-align: center;
  
  .el-button {
    min-width: 200px;
  }
}
</style>
