<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const isUploading = ref(false)
const activeSubject = ref('全部')

// 科目筛选
const subjects = ['全部', '数学', '英语', '物理', '化学', '语文']

// 错题数据
const questions = ref([
  {
    id: 1,
    subject: '数学',
    img: 'https://api.dicebear.com/7.x/shapes/svg?seed=math1',
    tags: ['二次函数', '抛物线'],
    date: '3月1日',
    mastered: false
  },
  {
    id: 2,
    subject: '英语',
    img: 'https://api.dicebear.com/7.x/shapes/svg?seed=eng1',
    tags: ['虚拟语气', '语法填空'],
    date: '2月28日',
    mastered: true
  },
  {
    id: 3,
    subject: '物理',
    img: 'https://api.dicebear.com/7.x/shapes/svg?seed=phy1',
    tags: ['力学', '牛顿定律'],
    date: '2月25日',
    mastered: false
  }
])

// 筛选后的题目
const filteredQuestions = computed(() => {
  if (activeSubject.value === '全部') {
    return questions.value
  }
  return questions.value.filter(q => q.subject === activeSubject.value)
})

// 模拟拍照上传+OCR
const handleUpload = () => {
  isUploading.value = true
  
  setTimeout(() => {
    questions.value.unshift({
      id: Date.now(),
      subject: '物理',
      img: 'https://api.dicebear.com/7.x/shapes/svg?seed=phy' + Date.now(),
      tags: ['力学', 'OCR自动识别'],
      date: '刚刚',
      mastered: false
    })
    isUploading.value = false
    ElMessage.success('OCR识别完成！已自动归类标签')
  }, 1500)
}

// 查看题目详情
const handleViewQuestion = (question) => {
  ElMessage.info(`查看题目：${question.subject}`)
}

// 标记掌握
const handleToggleMastered = (question) => {
  question.mastered = !question.mastered
  ElMessage.success(question.mastered ? '已标记为掌握' : '已取消掌握标记')
}

// 删除题目
const handleDeleteQuestion = (question, index) => {
  questions.value.splice(index, 1)
  ElMessage.success('已删除')
}
</script>

<template>
  <div class="wrong-book-page" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h1 class="page-title">智能错题本</h1>
      </div>
      <el-button text>
        <el-icon><Search /></el-icon>
      </el-button>
    </div>

    <!-- 科目筛选 -->
    <div class="subject-tabs">
      <el-scrollbar>
        <div class="tabs-wrapper">
          <el-button 
            v-for="sub in subjects" 
            :key="sub"
            :type="activeSubject === sub ? 'primary' : 'default'"
            size="small"
            round
            @click="activeSubject = sub"
          >
            {{ sub }}
          </el-button>
        </div>
      </el-scrollbar>
    </div>

    <!-- 错题网格 -->
    <div class="questions-grid">
      <el-card 
        v-for="(q, index) in filteredQuestions" 
        :key="q.id"
        class="question-card"
        shadow="hover"
        @click="handleViewQuestion(q)"
      >
        <!-- 题目图片 -->
        <div class="question-image">
          <el-image :src="q.img" fit="cover" />
          <div v-if="q.mastered" class="mastered-overlay">
            <el-tag type="success" effect="dark" size="small">已掌握</el-tag>
          </div>
        </div>
        
        <!-- 题目信息 -->
        <div class="question-info">
          <div class="info-header">
            <span class="subject-name">{{ q.subject }}</span>
            <span class="question-date">{{ q.date }}</span>
          </div>
          <div class="tags-wrapper">
            <el-tag 
              v-for="t in q.tags" 
              :key="t"
              size="small"
              effect="plain"
            >
              #{{ t }}
            </el-tag>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="question-actions" @click.stop>
          <el-button 
            :type="q.mastered ? 'success' : 'default'"
            size="small"
            circle
            @click="handleToggleMastered(q)"
          >
            <el-icon><Check /></el-icon>
          </el-button>
          <el-button 
            type="danger" 
            size="small"
            circle
            @click="handleDeleteQuestion(q, index)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty 
      v-if="filteredQuestions.length === 0" 
      description="暂无错题"
      class="empty-state"
    >
      <el-button type="primary" @click="handleUpload">
        <el-icon class="mr-1"><Camera /></el-icon>
        拍照添加
      </el-button>
    </el-empty>

    <!-- 提示文字 -->
    <div v-if="questions.length < 5" class="hint-text">
      点击右下角相机，体验 AI 搜题功能
    </div>

    <!-- 悬浮按钮 -->
    <div class="fab-button" @click="handleUpload">
      <el-icon v-if="!isUploading" :size="24"><Camera /></el-icon>
      <el-icon v-else :size="24" class="is-loading"><Loading /></el-icon>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.wrong-book-page {
  min-height: 100vh;
  background: $bg-light;
  padding-bottom: 100px;
}

.page-header {
  background: #fff;
  padding: $spacing-md $spacing-lg;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid $border-color;

  .header-left {
    display: flex;
    align-items: center;
    gap: $spacing-sm;

    .page-title {
      font-size: 18px;
      font-weight: 600;
    }
  }
}

.subject-tabs {
  background: #fff;
  padding: $spacing-md $spacing-lg;
  border-bottom: 1px solid $border-color;

  .tabs-wrapper {
    display: flex;
    gap: $spacing-sm;
    white-space: nowrap;
  }
}

.questions-grid {
  padding: $spacing-lg;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.question-card {
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;

  :deep(.el-card__body) {
    padding: 0;
  }

  .question-image {
    height: 120px;
    background: $bg-light;
    position: relative;
    overflow: hidden;

    .el-image {
      width: 100%;
      height: 100%;
      opacity: 0.8;
      transition: transform 0.3s;
    }

    &:hover .el-image {
      transform: scale(1.05);
    }

    .mastered-overlay {
      position: absolute;
      inset: 0;
      background: rgba($success-color, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .question-info {
    padding: $spacing-md;

    .info-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: $spacing-sm;

      .subject-name {
        font-size: 14px;
        font-weight: 600;
        color: $text-primary;
      }

      .question-date {
        font-size: 12px;
        color: $text-muted;
      }
    }

    .tags-wrapper {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;

      .el-tag {
        font-size: 10px;
      }
    }
  }

  .question-actions {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-xs;
    padding: 0 $spacing-md $spacing-md;
  }
}

.empty-state {
  padding: $spacing-xl;
}

.hint-text {
  text-align: center;
  font-size: 12px;
  color: $text-muted;
  padding: $spacing-lg;
}

.fab-button {
  position: fixed;
  bottom: 80px;
  right: $spacing-lg;
  width: 56px;
  height: 56px;
  background: $primary-color;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: $shadow-xl;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 20;

  &:hover {
    transform: scale(1.1);
  }

  &:active {
    transform: scale(0.95);
  }
}
</style>
