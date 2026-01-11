<template>
  <div class="question-detail-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">错题详情</h1>
      <el-dropdown @command="handleCommand">
        <el-button link>
          <el-icon><MoreFilled /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="edit">编辑</el-dropdown-item>
            <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>
    
    <template v-else-if="question">
      <!-- 状态卡片 -->
      <div class="status-card" :class="{ solved: question.solved }">
        <el-icon v-if="question.solved"><CircleCheck /></el-icon>
        <el-icon v-else><Warning /></el-icon>
        <span>{{ question.solved ? '已掌握' : '待复习' }}</span>
      </div>
      
      <!-- 基本信息 -->
      <div class="info-card">
        <div class="info-row">
          <span class="label">科目</span>
          <el-tag>{{ question.subject }}</el-tag>
        </div>
        <div class="info-row">
          <span class="label">学生</span>
          <span>{{ question.studentName }}</span>
        </div>
        <div class="info-row">
          <span class="label">添加时间</span>
          <span>{{ formatDate(question.createTime) }}</span>
        </div>
        <div v-if="question.tags?.length" class="info-row">
          <span class="label">知识点</span>
          <div class="tags">
            <el-tag v-for="tag in question.tags" :key="tag" size="small" type="info">
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>
      
      <!-- 题目图片 -->
      <div v-if="question.imageUrl" class="image-card">
        <h3 class="card-title">题目图片</h3>
        <el-image
          :src="question.imageUrl"
          fit="contain"
          :preview-src-list="[question.imageUrl]"
          preview-teleported
        />
      </div>
      
      <!-- 题目内容 -->
      <div class="content-card">
        <h3 class="card-title">题目内容</h3>
        <div class="content-text">{{ question.content }}</div>
      </div>
      
      <!-- 错误原因 -->
      <div v-if="question.reason" class="content-card">
        <h3 class="card-title">错误原因</h3>
        <div class="content-text">{{ question.reason }}</div>
      </div>
      
      <!-- 正确答案 -->
      <div v-if="question.answer" class="content-card answer-card">
        <h3 class="card-title">正确答案</h3>
        <div class="content-text">{{ question.answer }}</div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button v-if="!question.solved" type="primary" size="large" @click="markSolved">
          标记为已掌握
        </el-button>
        <el-button v-else size="large" @click="markUnsolved">
          标记为待复习
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, MoreFilled, CircleCheck, Warning } from '@element-plus/icons-vue'
import { getWrongQuestionDetail, updateWrongQuestion, deleteWrongQuestion } from '@shared/api/wrongbook'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const question = ref(null)

const formatDate = (time) => dayjs(time).format('YYYY-MM-DD HH:mm')

const goBack = () => router.back()

const loadQuestion = async () => {
  loading.value = true
  try {
    const res = await getWrongQuestionDetail(route.params.id)
    if (res.code === 200) {
      question.value = res.data
    }
  } catch (error) {
    console.error('加载错题详情失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCommand = async (command) => {
  if (command === 'edit') {
    router.push(`/wrongbook/${route.params.id}/edit`)
  } else if (command === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除这道错题吗？', '删除确认')
      const res = await deleteWrongQuestion(route.params.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        router.back()
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
      }
    }
  }
}

const markSolved = async () => {
  try {
    const res = await updateWrongQuestion(route.params.id, { solved: true })
    if (res.code === 200) {
      question.value.solved = true
      ElMessage.success('已标记为掌握')
    }
  } catch (error) {
    console.error('更新失败:', error)
  }
}

const markUnsolved = async () => {
  try {
    const res = await updateWrongQuestion(route.params.id, { solved: false })
    if (res.code === 200) {
      question.value.solved = false
      ElMessage.success('已标记为待复习')
    }
  } catch (error) {
    console.error('更新失败:', error)
  }
}

onMounted(() => {
  loadQuestion()
})
</script>

<style lang="scss" scoped>
.question-detail-page {
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
    flex: 1;
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

.status-card {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  background: #fff3e0;
  color: #ff9800;
  
  &.solved {
    background: #e8f5e9;
    color: #4caf50;
  }
  
  .el-icon {
    font-size: 24px;
  }
}

.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .info-row {
    display: flex;
    align-items: center;
    padding: 10px 0;
    
    &:not(:last-child) {
      border-bottom: 1px solid #f5f5f5;
    }
    
    .label {
      width: 80px;
      color: #666;
      flex-shrink: 0;
    }
    
    .tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
  }
}

.image-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .el-image {
    width: 100%;
    max-height: 300px;
    border-radius: 8px;
  }
}

.content-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  &.answer-card {
    background: #e8f5e9;
  }
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 12px;
}

.content-text {
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 20px;
  background: #fff;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: center;
  
  .el-button {
    min-width: 200px;
  }
}
</style>
