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
    <div class="page-wrapper">
      <el-row :gutter="32">
        <!-- 左侧信息栏 -->
        <el-col :span="7" class="sidebar-col">
          <!-- 标题卡片 -->
          <el-card class="title-card" shadow="hover">
            <div class="title-icon">
              <el-icon :size="32"><Edit /></el-icon>
            </div>
            <h1>发布家教需求</h1>
            <p>填写信息，智能匹配最合适的老师</p>
          </el-card>

          <!-- 进度卡片 -->
          <el-card class="progress-card" shadow="hover">
            <template #header>
              <div class="card-title">
                <el-icon><List /></el-icon>
                <span>填写进度</span>
              </div>
            </template>
            <el-steps :active="step - 1" direction="vertical" finish-status="success">
              <el-step title="学生信息" description="基本信息和薄弱科目" />
              <el-step title="教学需求" description="目标和频次设定" />
              <el-step title="授课偏好" description="价格和教师要求" />
            </el-steps>
          </el-card>

          <!-- 当前步骤提示 -->
          <el-card class="tip-card" shadow="hover">
            <template #header>
              <div class="card-title">
                <el-icon><InfoFilled /></el-icon>
                <span>当前步骤</span>
              </div>
            </template>
            <div class="tip-content" v-if="step === 1">
              <p>👦 请填写学生的基本信息</p>
              <p>📚 选择需要辅导的科目</p>
              <p>😊 描述孩子的性格特点</p>
            </div>
            <div class="tip-content" v-else-if="step === 2">
              <p>🎯 选择合适的教学目标</p>
              <p>📅 设定每周的上课频次</p>
              <p>📝 补充特殊要求或情况</p>
            </div>
            <div class="tip-content" v-else>
              <p>💰 设定可接受的价格范围</p>
              <p>👨‍🏫 选择教师性别偏好</p>
              <p>🎭 选择喜欢的教学风格</p>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧表单区 -->
        <el-col :span="17">
          <!-- Step 1: 学生信息 -->
          <el-card v-if="step === 1" class="form-card" shadow="hover">
            <template #header>
              <div class="form-header">
                <div class="header-left">
                  <el-icon><User /></el-icon>
                  <span>学生基本信息</span>
                </div>
                <el-tag type="info">Step 1/3</el-tag>
              </div>
            </template>

            <el-form label-position="top" size="large">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="学生姓名" required>
                    <el-input v-model="form.studentName" placeholder="请输入学生姓名" prefix-icon="User" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="就读年级" required>
                    <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%">
                      <el-option v-for="item in gradeOptions" :key="item.value" :label="item.label" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="薄弱科目（可多选）" required>
                <el-checkbox-group v-model="form.weakSubjects" class="subject-group">
                  <el-checkbox-button v-for="item in subjectOptions" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </el-checkbox-button>
                </el-checkbox-group>
              </el-form-item>

              <el-form-item label="性格特点">
                <el-radio-group v-model="form.character">
                  <el-radio v-for="item in charOptions" :key="item.value" :value="item.value" border>
                    {{ item.label }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>

              <div class="form-actions">
                <div></div>
                <el-button type="primary" size="large" @click="nextStep">
                  下一步 <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </el-form>
          </el-card>

          <!-- Step 2: 教学需求 -->
          <el-card v-if="step === 2" class="form-card" shadow="hover">
            <template #header>
              <div class="form-header">
                <div class="header-left">
                  <el-icon><Aim /></el-icon>
                  <span>教学目标设定</span>
                </div>
                <el-tag type="info">Step 2/3</el-tag>
              </div>
            </template>

            <el-form label-position="top" size="large">
              <el-form-item label="教学目标">
                <div class="target-cards">
                  <div v-for="item in targetOptions" :key="item.value" class="target-card" :class="{ active: form.target === item.value }" @click="form.target = item.value">
                    <div class="target-label">{{ item.label }}</div>
                    <div class="target-desc">{{ item.desc }}</div>
                  </div>
                </div>
              </el-form-item>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="上课频次">
                    <el-input v-model="form.frequency" placeholder="例如：每周<unknown>次，每次<unknown>小时" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="上课地点">
                    <el-input v-model="form.address" disabled prefix-icon="Location" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="补充说明（可选）">
                <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="描述学生的具体情况或特殊要求..." />
              </el-form-item>

              <div class="form-actions">
                <el-button size="large" @click="step--">上一步</el-button>
                <el-button type="primary" size="large" @click="nextStep">
                  下一步 <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </el-form>
          </el-card>

          <!-- Step 3: 授课偏好 -->
          <el-card v-if="step === 3" class="form-card" shadow="hover">
            <template #header>
              <div class="form-header">
                <div class="header-left">
                  <el-icon><Setting /></el-icon>
                  <span>授课偏好设置</span>
                </div>
                <el-tag type="info">Step 3/3</el-tag>
              </div>
            </template>

            <el-form label-position="top" size="large">
              <el-form-item label="可接受价格区间（元/小时）">
                <el-slider v-model="form.budgetRange" range :min="50" :max="500" :step="10" :marks="{ 50: '50元', 150: '150元', 300: '300元', 500: '500元' }" />
                <div class="budget-display">
                  当前预算：<span class="budget-value">{{ form.budgetRange[0] }} - {{ form.budgetRange[1] }} 元/小时</span>
                </div>
              </el-form-item>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="教师性别偏好">
                    <el-radio-group v-model="form.gender">
                      <el-radio-button v-for="item in genderOptions" :key="item.value" :value="item.value">
                        {{ item.label }}
                      </el-radio-button>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="教学风格偏好">
                    <el-select v-model="form.style" placeholder="请选择" style="width: 100%">
                      <el-option v-for="item in styleOptions" :key="item.value" :label="item.label" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="form-actions">
                <el-button size="large" @click="step--">上一步</el-button>
                <el-button type="primary" size="large" :loading="isSubmitting" @click="handleSubmit">
                  <el-icon><Check /></el-icon> {{ isSubmitting ? '提交中...' : '确认提交' }}
                </el-button>
              </div>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.demand-form-page {
  min-height: calc(100vh - 114px);
  background: linear-gradient(135deg, #fef9f3 0%, #fff5eb 50%, #f5f7fa 100%);
  padding: 32px 0;
}

.page-wrapper {
  max-width: 1300px;
  margin: 0 auto;
  padding: 0 32px;
}

/* 左侧边栏 */
.sidebar-col {
  position: sticky;
  top: 100px;
  height: fit-content;
}

.title-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #ff9500 0%, #ff6b00 100%);
  color: #fff;
  text-align: center;

  :deep(.el-card__body) {
    padding: 32px 24px;
  }

  .title-icon {
    width: 64px;
    height: 64px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
  }

  h1 {
    font-size: 24px;
    font-weight: 800;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    opacity: 0.9;
    margin: 0;
  }
}

.progress-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 20px;

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 700;
    font-size: 15px;
    color: #303133;

    .el-icon {
      color: #ff6b00;
    }
  }

  :deep(.el-steps) {
    .el-step__head.is-finish {
      color: #ff6b00;
      border-color: #ff6b00;
    }
    .el-step__title.is-finish {
      color: #ff6b00;
    }
    .el-step__head.is-process {
      color: #ff6b00;
      border-color: #ff6b00;
    }
    .el-step__title.is-process {
      color: #ff6b00;
      font-weight: 600;
    }
    .el-step__description {
      font-size: 12px;
      color: #909399;
    }
  }
}

.tip-card {
  border-radius: 16px;
  border: none;
  background: linear-gradient(135deg, #fff5eb 0%, #fff 100%);

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 700;
    font-size: 15px;
    color: #303133;

    .el-icon {
      color: #ff6b00;
    }
  }

  .tip-content {
    p {
      margin: 0 0 12px 0;
      font-size: 13px;
      color: #606266;
      line-height: 1.6;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

/* 右侧表单卡片 */
.form-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 40px rgba(255, 107, 0, 0.08);

  .form-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 18px;
      font-weight: 700;
      color: #303133;

      .el-icon {
        font-size: 22px;
        color: #ff6b00;
      }
    }
  }

  :deep(.el-card__body) {
    padding: 32px;
  }
}

.form-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 2px solid #fef0e5;

  .el-button {
    min-width: 140px;
    height: 48px;
    font-size: 15px;
    font-weight: 600;
    border-radius: 12px;
    
    &.el-button--primary {
      background: linear-gradient(135deg, #ff9500 0%, #ff6b00 100%);
      border: none;
      box-shadow: 0 8px 24px rgba(255, 107, 0, 0.3);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 12px 32px rgba(255, 107, 0, 0.4);
      }
    }

    &.el-button--default {
      border: 2px solid #e4e7ed;
      
      &:hover {
        border-color: #ff6b00;
        color: #ff6b00;
      }
    }
  }
}

.subject-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  :deep(.el-checkbox-button) {
    .el-checkbox-button__inner {
      border-radius: 20px;
      padding: 10px 20px;
      border: 2px solid #e4e7ed;
      background: #fff;
      font-weight: 500;
      transition: all 0.3s;

      &:hover {
        border-color: #ff9500;
        color: #ff6b00;
      }
    }

    &.is-checked .el-checkbox-button__inner {
      background: linear-gradient(135deg, #ff9500 0%, #ff6b00 100%);
      border-color: transparent;
      box-shadow: 0 4px 12px rgba(255, 107, 0, 0.3);
    }
  }
}

.target-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;

  .target-card {
    padding: 24px 16px;
    border: 2px solid #f0f2f5;
    border-radius: 14px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
    background: #fff;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: linear-gradient(90deg, #ff9500, #ff6b00);
      transform: scaleX(0);
      transition: transform 0.3s;
    }

    &:hover {
      border-color: #ffd4b3;
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(255, 107, 0, 0.1);

      &::before {
        transform: scaleX(1);
      }
    }

    &.active {
      border-color: #ff6b00;
      background: linear-gradient(180deg, #fff5eb 0%, #fff 100%);

      &::before {
        transform: scaleX(1);
      }

      .target-label {
        color: #ff6b00;
      }
    }

    .target-label {
      font-size: 17px;
      font-weight: 700;
      color: #303133;
      margin-bottom: 6px;
    }

    .target-desc {
      font-size: 12px;
      color: #909399;
    }
  }
}

.budget-display {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
  color: #606266;
  padding: 12px;
  background: #fef9f3;
  border-radius: 10px;

  .budget-value {
    font-weight: 700;
    font-size: 16px;
    color: #ff6b00;
  }
}

/* 表单增强 */
:deep(.el-form-item) {
  margin-bottom: 24px;

  .el-form-item__label {
    font-weight: 600;
    color: #303133;
    font-size: 14px;
  }
}

:deep(.el-input__wrapper), :deep(.el-textarea__inner) {
  border-radius: 10px;
  
  &:focus-within {
    box-shadow: 0 0 0 2px rgba(255, 107, 0, 0.2);
  }
}

:deep(.el-slider) {
  .el-slider__bar {
    background: linear-gradient(90deg, #ff9500, #ff6b00);
  }
  .el-slider__button {
    border-color: #ff6b00;
  }
}

:deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

:deep(.el-radio-button) {
  .el-radio-button__inner {
    border-radius: 10px !important;
    border: 2px solid #e4e7ed !important;
    padding: 12px 24px;
    
    &:hover {
      color: #ff6b00;
    }
  }

  &.is-active .el-radio-button__inner {
    background: linear-gradient(135deg, #ff9500 0%, #ff6b00 100%) !important;
    border-color: transparent !important;
    box-shadow: 0 4px 12px rgba(255, 107, 0, 0.3);
  }
}

/* 响应式 */
@media (max-width: 1024px) {
  .sidebar-col {
    display: none;
  }

  :deep(.el-col-17) {
    max-width: 100% !important;
    flex: 0 0 100% !important;
  }
}

@media (max-width: 768px) {
  .demand-form-page {
    padding: 16px 0;
  }

  .page-wrapper {
    padding: 0 16px;
  }

  .target-cards {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column-reverse;
    gap: 12px;

    .el-button {
      width: 100%;
      min-width: auto;
    }
  }
}
</style>