<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTutorProfile } from '@/api/tutor'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const showBookingDialog = ref(false)

// 教师数据
const teacher = ref({
  id: route.params.id,
  name: '张老师',
  title: '北京师范大学 · 英语硕士',
  avatar: '',
  tags: ['实名认证', '英语专八', '3年教龄', '幽默风趣'],
  rating: 4.9,
  renewRate: 98,
  totalHours: 300,
  price: 200,
  intro: '擅长引导式教学，主攻初中英语语法与口语。曾帮助3名学生在期末考试中提分30+。',
  schedule: ['周六上午 09:00-11:00', '周日晚上 19:00-21:00'],
  subjects: ['英语', '口语']
})

// 预约表单
const bookingForm = ref({
  studentName: '王小明',
  grade: '三年级',
  subject: '英语',
  time: '周六上午 09:00'
})

// 获取教师详情
const fetchTeacherDetail = async () => {
  loading.value = true
  try {
    const res = await getTutorProfile(route.params.id)
    if (res.data) {
      teacher.value = {
        ...teacher.value,
        ...res.data,
        userId: res.data.userId, // 保存用户ID
        name: res.data.realName || teacher.value.name,
        title: `${res.data.universityName || ''} · ${res.data.major || ''}`,
        tags: buildTags(res.data),
        price: res.data.expectPrice || teacher.value.price,
        subjects: parseJson(res.data.teachSubjects, []),
        grades: parseJson(res.data.teachGrades, [])
      }
      // 更新预约表单的默认科目
      if (teacher.value.subjects.length > 0) {
        bookingForm.value.subject = teacher.value.subjects[0]
      }
      if (teacher.value.grades.length > 0) {
        bookingForm.value.grade = teacher.value.grades[0]
      }
    }
  } catch (error) {
    console.error('获取教师详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 解析JSON字符串
const parseJson = (str, defaultVal) => {
  if (!str) return defaultVal
  try {
    return JSON.parse(str)
  } catch {
    return defaultVal
  }
}

const buildTags = (data) => {
  const tags = []
  if (data.certStatus === 2) tags.push('实名认证')
  if (data.teachSubjects) {
    try {
      const subjects = JSON.parse(data.teachSubjects)
      tags.push(...subjects.slice(0, 2))
    } catch {}
  }
  if (data.teachStyle) tags.push(data.teachStyle)
  return tags.length ? tags : ['新入驻']
}

const handleBook = () => {
  showBookingDialog.value = false
  router.push({
    path: `/booking/${teacher.value.id}`,
    query: {
      teacherId: teacher.value.userId, // 用户ID
      tutorProfileId: teacher.value.id, // 教员档案ID
      teacherName: teacher.value.name,
      subject: bookingForm.value.subject,
      grade: bookingForm.value.grade,
      price: teacher.value.price
    }
  })
}

onMounted(() => {
  fetchTeacherDetail()
})
</script>

<template>
  <div class="teacher-profile-page" v-loading="loading">
    <!-- 教师头部信息 -->
    <div class="profile-header">
      <el-avatar :size="80" :src="teacher.avatar || `https://api.dicebear.com/7.x/miniavs/svg?seed=${teacher.id}`" />
      <div class="header-info">
        <h1 class="teacher-name">{{ teacher.name }}</h1>
        <p class="teacher-title">{{ teacher.title }}</p>
      </div>
    </div>

    <!-- 标签 -->
    <div class="tag-section">
      <el-tag 
        v-for="tag in teacher.tags" 
        :key="tag" 
        type="primary" 
        effect="plain"
        size="small"
      >
        {{ tag }}
      </el-tag>
    </div>

    <!-- 数据统计 -->
    <el-card class="stats-card" shadow="never">
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">
            {{ teacher.rating }}
            <el-icon class="star"><Star /></el-icon>
          </div>
          <div class="stat-label">评分</div>
        </div>
        <el-divider direction="vertical" />
        <div class="stat-item">
          <div class="stat-value">{{ teacher.renewRate }}%</div>
          <div class="stat-label">续课率</div>
        </div>
        <el-divider direction="vertical" />
        <div class="stat-item">
          <div class="stat-value">{{ teacher.totalHours }}+</div>
          <div class="stat-label">授课时</div>
        </div>
      </div>
    </el-card>

    <!-- 教学经历 -->
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Trophy /></el-icon>
          <span>教学经历</span>
        </div>
      </template>
      <p class="intro-text">{{ teacher.intro }}</p>
    </el-card>

    <!-- 可约时间 -->
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Clock /></el-icon>
          <span>可约时间</span>
        </div>
      </template>
      <div class="schedule-list">
        <div v-for="time in teacher.schedule" :key="time" class="schedule-item">
          <el-icon><Calendar /></el-icon>
          <span>{{ time }}</span>
        </div>
      </div>
    </el-card>

    <!-- 底部操作栏 -->
    <div class="bottom-bar">
      <div class="price-info">
        <span class="price-value">¥{{ teacher.price }}</span>
        <span class="price-unit">/小时</span>
      </div>
      <el-button type="primary" size="large" @click="showBookingDialog = true">
        立即预约试听
      </el-button>
    </div>

    <!-- 预约弹窗 -->
    <el-dialog 
      v-model="showBookingDialog" 
      title="确认预约信息" 
      width="400px"
      :append-to-body="true"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="学生">{{ bookingForm.studentName }} ({{ bookingForm.grade }})</el-descriptions-item>
        <el-descriptions-item label="科目">{{ bookingForm.subject }}</el-descriptions-item>
        <el-descriptions-item label="时段">
          <el-tag type="primary">{{ bookingForm.time }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showBookingDialog = false">取消</el-button>
        <el-button type="primary" @click="handleBook">确认发送请求</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.teacher-profile-page {
  min-height: 100vh;
  background: $bg-light;
  padding-bottom: 100px;
}

.profile-header {
  background: linear-gradient(135deg, $primary-color 0%, #667eea 100%);
  padding: $spacing-xl $spacing-lg;
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  color: #fff;

  .header-info {
    .teacher-name {
      font-size: 24px;
      font-weight: 700;
      margin-bottom: 4px;
    }

    .teacher-title {
      font-size: 14px;
      opacity: 0.9;
    }
  }
}

.tag-section {
  background: #fff;
  padding: $spacing-md $spacing-lg;
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-xs;
}

.stats-card {
  margin: $spacing-md $spacing-lg;
  border-radius: 12px;

  .stats-grid {
    display: flex;
    align-items: center;
    justify-content: center;

    .stat-item {
      flex: 1;
      text-align: center;

      .stat-value {
        font-size: 20px;
        font-weight: 700;
        color: $text-primary;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;

        .star {
          color: #facc15;
        }
      }

      .stat-label {
        font-size: 12px;
        color: $text-muted;
        margin-top: 4px;
      }
    }
  }
}

.info-card {
  margin: $spacing-md $spacing-lg;
  border-radius: 12px;

  .card-header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-weight: 600;
    color: $text-primary;
  }

  .intro-text {
    font-size: 14px;
    color: $text-secondary;
    line-height: 1.8;
  }

  .schedule-list {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;

    .schedule-item {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      padding: $spacing-sm $spacing-md;
      background: $bg-light;
      border-radius: 8px;
      font-size: 14px;
      color: $text-secondary;
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: $spacing-md $spacing-lg;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;

  .price-info {
    .price-value {
      font-size: 28px;
      font-weight: 700;
      color: $danger-color;
    }

    .price-unit {
      font-size: 12px;
      color: $text-muted;
    }
  }

  .el-button {
    height: 48px;
    padding: 0 32px;
    font-size: 16px;
    font-weight: 600;
  }
}
</style>
