<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createDemand, addStudent, getDemandDetail, updateDemand } from '@/api/demand'

const router = useRouter()
const route = useRoute()
const step = ref(1) // 1:学生信息, 2:教学需求, 3:授课偏好 
const isSubmitting = ref(false)

const demandId = computed(() => route.query.id)
const isEdit = computed(() => !!demandId.value)

const form = reactive({
  // Step 1: 学生信息
  studentName: '',
  studentId: null,
  grade: '小学三年级',
  weakSubjects: [],
  character: '内向',
  
  // Step 2: 教学需求
  target: '补差',
  frequency: '每周2次',
  remark: '',
  
  // Step 3: 授课偏好
  gender: '无要求',
  style: '鼓励型',
  budgetRange: [100, 200],
  latitude: 39.9042,
  longitude: 116.4074,
  address: '系统自动定位中...'
})

// 选项数据 - 年级命名与数据库tutor_profile.teach_grades保持一致
const gradeOptions = [
  { label: '小学一年级', value: '小学一年级' },
  { label: '小学二年级', value: '小学二年级' },
  { label: '小学三年级', value: '小学三年级' },
  { label: '小学四年级', value: '小学四年级' },
  { label: '小学五年级', value: '小学五年级' },
  { label: '小学六年级', value: '小学六年级' },
  { label: '初一', value: '初一' },
  { label: '初二', value: '初二' },
  { label: '初三', value: '初三' },
  { label: '高一', value: '高一' },
  { label: '高二', value: '高二' },
  { label: '高三', value: '高三' }
]

const subjectOptions = [
  { label: '语文', value: '语文' },
  { label: '数学', value: '数学' },
  { label: '英语', value: '英语' },
  { label: '物理', value: '物理' },
  { label: '化学', value: '化学' },
  { label: '生物', value: '生物' },
  { label: '历史', value: '历史' },
  { label: '地理', value: '地理' },
  { label: '政治', value: '政治' }
]

const charOptions = [
  { label: '内向安静', value: '内向' },
  { label: '活泼开朗', value: '活泼' },
  { label: '敏感细腻', value: '敏感' }
]

const targetOptions = [
  { label: '基础巩固', value: '补差', desc: '针对基础薄弱，逐步提升' },
  { label: '稳步提分', value: '提分', desc: '针对中等水平，冲击高分' },
  { label: '拔尖培优', value: '培优', desc: '针对优等生，竞赛培训' }
]

const styleOptions = [
  { label: '鼓励型', value: '鼓励型' },
  { label: '严厉型', value: '严厉型' },
  { label: '趣味型', value: '趣味型' },
  { label: '引导型', value: '引导型' }
]

const genderOptions = [
  { label: '无要求', value: '无要求' },
  { label: '男教师', value: '男' },
  { label: '女教师', value: '女' }
]

// 获取用户位置
const getUserLocation = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        form.latitude = position.coords.latitude
        form.longitude = position.coords.longitude
        form.address = `已获取定位 (${form.latitude.toFixed(4)}, ${form.longitude.toFixed(4)})`
      },
      () => {
        form.address = '使用默认位置'
      }
    )
  }
}

// 加载现有数据
const loadDemandData = async () => {
  if (!isEdit.value) {
    getUserLocation()
    return
  }
  
  try {
    const res = await getDemandDetail(demandId.value)
    if (res.code === 200 && res.data) {
      const data = res.data
      form.studentId = data.studentId
      form.studentName = data.studentName || '学生'
      form.grade = data.grade
      form.weakSubjects = data.subject ? [data.subject] : []
      form.budgetRange = [Math.max(0, (data.expectPrice || 100) - 50), data.expectPrice || 200]
      form.address = data.address
      form.latitude = data.latitude
      form.longitude = data.longitude
      form.remark = data.detail
      
      // 解析 remark 中的其他字段
      if (data.detail && data.detail.includes('性格:')) {
        const charMatch = data.detail.match(/性格: ([^,]+)/)
        if (charMatch) form.character = charMatch[1]
        
        const styleMatch = data.detail.match(/偏好风格: ([^,]+)/)
        if (styleMatch) form.style = styleMatch[1]
        
        const targetMatch = data.detail.match(/学习目标: ([^,]+)/)
        if (targetMatch) form.target = targetMatch[1]
        
        const freqMatch = data.detail.match(/频次: ([^,]+)/)
        if (freqMatch) form.frequency = freqMatch[1]
      }
    }
  } catch (error) {
    console.error('加载需求详情失败:', error)
  }
}

// 验证当前步骤
const validateStep = () => {
  if (step.value === 1) {
    if (!form.studentName.trim()) {
      ElMessage.warning('请填写学生姓名')
      return false
    }
    if (form.weakSubjects.length === 0) {
      ElMessage.warning('请至少选择一个薄弱科目')
      return false
    }
  }
  return true
}

// 下一步
const nextStep = () => {
  if (validateStep()) {
    step.value++
  }
}

// 提交处理
const handleSubmit = async () => {
  if (isSubmitting.value) return
  
  isSubmitting.value = true

  try {
    // 1. 获取学生ID
    let studentId = form.studentId
    if (!studentId) {
      try {
        const studentData = {
          studentName: form.studentName,
          gender: 0, 
          grade: form.grade,
          schoolName: '',
          weakSubjects: form.weakSubjects,
          studyDesc: form.character
        }
        const studentRes = await addStudent(studentData)
        studentId = studentRes.data
      } catch (e) {
        studentId = 1
      }
    }

    // 2. 创建或更新需求
    const demandData = {
      studentId: studentId,
      title: `${form.grade}${form.weakSubjects[0]}辅导`,
      subject: form.weakSubjects[0],
      grade: form.grade,
      teachMode: 1,
      expectPrice: form.budgetRange[1],
      latitude: form.latitude,
      longitude: form.longitude,
      address: form.address,
      detail: form.remark || `学习目标: ${form.target}, 频次: ${form.frequency}, 科目: ${form.weakSubjects.join(',')}, 性格: ${form.character}, 偏好风格: ${form.style}`
    }

    if (isEdit.value) {
      await updateDemand(demandId.value, demandData)
      ElMessage.success('需求更新成功！')
      router.push('/parent/demands')
    } else {
      await createDemand(demandData)
      ElMessage.success('需求发布成功！系统正在为您匹配老师...')
      router.push({
        path: '/teacher/list',
        query: {
          subject: form.weakSubjects[0],
          grade: form.grade,
          studentId: studentId
        }
      })
    }
    
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '发布失败')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  loadDemandData()
})
</script>

<template>
  <div class="demand-form-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">发布家教需求</h1>
      <p class="page-subtitle">填写信息，智能匹配最合适的老师</p>
    </div>

    <!-- 步骤条 -->
    <div class="step-container">
      <el-steps :active="step - 1" align-center finish-status="success">
        <el-step title="学生信息" />
        <el-step title="教学需求" />
        <el-step title="授课偏好" />
      </el-steps>
    </div>

    <!-- Step 1: 学生信息 -->
    <div v-if="step === 1" class="step-content">
      <el-card>
        <template #header>
          <div class="card-header">
            <el-icon><User /></el-icon>
            <span>学生基本信息</span>
          </div>
        </template>

        <el-form label-position="top" size="large">
          <el-form-item label="学生姓名" required>
            <el-input 
              v-model="form.studentName" 
              placeholder="请输入学生姓名"
              prefix-icon="User"
            />
          </el-form-item>

          <el-form-item label="就读年级" required>
            <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%">
              <el-option 
                v-for="item in gradeOptions" 
                :key="item.value" 
                :label="item.label" 
                :value="item.value" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="薄弱科目（可多选）" required>
            <el-checkbox-group v-model="form.weakSubjects" class="subject-group">
              <el-checkbox-button 
                v-for="item in subjectOptions" 
                :key="item.value" 
                :value="item.value"
              >
                {{ item.label }}
              </el-checkbox-button>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="性格特点">
            <el-radio-group v-model="form.character">
              <el-radio 
                v-for="item in charOptions" 
                :key="item.value" 
                :value="item.value"
                border
              >
                {{ item.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- Step 2: 教学需求 -->
    <div v-if="step === 2" class="step-content">
      <el-card>
        <template #header>
          <div class="card-header">
            <el-icon><Aim /></el-icon>
            <span>教学目标设定</span>
          </div>
        </template>

        <el-form label-position="top" size="large">
          <el-form-item label="教学目标">
            <div class="target-cards">
              <div 
                v-for="item in targetOptions" 
                :key="item.value"
                class="target-card"
                :class="{ active: form.target === item.value }"
                @click="form.target = item.value"
              >
                <div class="target-label">{{ item.label }}</div>
                <div class="target-desc">{{ item.desc }}</div>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="上课频次">
            <el-input 
              v-model="form.frequency" 
              placeholder="例如：每周2次，每次2小时"
            />
          </el-form-item>

          <el-form-item label="补充说明（可选）">
            <el-input
              v-model="form.remark"
              type="textarea"
              :rows="3"
              placeholder="描述学生的具体情况或特殊要求..."
            />
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- Step 3: 授课偏好 -->
    <div v-if="step === 3" class="step-content">
      <el-card>
        <template #header>
          <div class="card-header">
            <el-icon><Setting /></el-icon>
            <span>授课偏好设置</span>
          </div>
        </template>

        <el-form label-position="top" size="large">
          <el-form-item label="可接受价格区间（元/小时）">
            <el-slider
              v-model="form.budgetRange"
              range
              :min="50"
              :max="500"
              :step="10"
              :marks="{ 50: '50元', 250: '250元', 500: '500元' }"
            />
            <div class="budget-display">
              当前预算：<span class="budget-value">{{ form.budgetRange[0] }} - {{ form.budgetRange[1] }} 元/小时</span>
            </div>
          </el-form-item>

          <el-form-item label="教师性别偏好">
            <el-radio-group v-model="form.gender">
              <el-radio-button 
                v-for="item in genderOptions" 
                :key="item.value" 
                :value="item.value"
              >
                {{ item.label }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="教学风格偏好">
            <el-select v-model="form.style" placeholder="请选择" style="width: 100%">
              <el-option 
                v-for="item in styleOptions" 
                :key="item.value" 
                :label="item.label" 
                :value="item.value" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="上课地点">
            <el-input 
              v-model="form.address" 
              disabled
              prefix-icon="Location"
            />
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 底部按钮 -->
    <div class="bottom-actions">
      <el-button 
        v-if="step > 1" 
        size="large" 
        @click="step--"
      >
        上一步
      </el-button>
      
      <el-button 
        v-if="step < 3" 
        type="primary" 
        size="large"
        @click="nextStep"
      >
        下一步
        <el-icon><ArrowRight /></el-icon>
      </el-button>
      
      <el-button 
        v-if="step === 3" 
        type="primary" 
        size="large"
        :loading="isSubmitting"
        @click="handleSubmit"
      >
        {{ isSubmitting ? '提交中...' : '确认提交' }}
      </el-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.demand-form-page {
  min-height: 100vh;
  background: $bg-light;
  padding-bottom: 100px;
}

.page-header {
  background: linear-gradient(135deg, $warning-color 0%, #f97316 100%);
  padding: $spacing-xl $spacing-lg;
  color: #fff;

  .page-title {
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 4px;
  }

  .page-subtitle {
    font-size: 14px;
    opacity: 0.9;
  }
}

.step-container {
  background: #fff;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
}

.step-content {
  padding: $spacing-lg;

  .card-header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: 16px;
    font-weight: 600;
    color: $text-primary;
  }
}

.subject-group {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.target-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;

  .target-card {
    padding: $spacing-md;
    border: 2px solid $border-color;
    border-radius: 12px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: $warning-color;
    }

    &.active {
      border-color: $warning-color;
      background: rgba($warning-color, 0.05);

      .target-label {
        color: $warning-color;
      }
    }

    .target-label {
      font-size: 16px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 4px;
    }

    .target-desc {
      font-size: 12px;
      color: $text-muted;
    }
  }
}

.budget-display {
  margin-top: $spacing-md;
  text-align: center;
  color: $text-secondary;

  .budget-value {
    font-weight: 600;
    color: $warning-color;
  }
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: $spacing-md $spacing-lg;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  gap: $spacing-md;

  .el-button {
    flex: 1;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
  }
}

@media (max-width: 768px) {
  .target-cards {
    grid-template-columns: 1fr;
  }
}
</style>