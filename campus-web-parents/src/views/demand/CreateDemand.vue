<template>
  <div class="create-demand-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">发布需求</h1>
    </div>
    
    <!-- 步骤条 -->
    <el-steps :active="currentStep" align-center class="step-bar">
      <el-step title="基本信息" />
      <el-step title="详细要求" />
      <el-step title="确认发布" />
    </el-steps>
    
    <!-- 步骤1：基本信息 -->
    <div v-show="currentStep === 0" class="form-container">
      <el-form ref="step1Ref" :model="form" :rules="rules1" label-width="100px">
        <div class="form-section">
          <h3 class="section-title">选择孩子</h3>
          
          <el-form-item label="关联学生" prop="studentId">
            <el-select v-model="form.studentId" placeholder="请选择要辅导的孩子">
              <el-option
                v-for="student in students"
                :key="student.id"
                :label="`${student.name} (${student.grade})`"
                :value="student.id"
              />
            </el-select>
            <el-button type="primary" link @click="addStudent">
              <el-icon><Plus /></el-icon>
              添加孩子
            </el-button>
          </el-form-item>
        </div>
        
        <div class="form-section">
          <h3 class="section-title">辅导信息</h3>
          
          <el-form-item label="需求标题" prop="title">
            <el-input v-model="form.title" placeholder="如：初三数学一对一辅导" maxlength="50" show-word-limit />
          </el-form-item>
          
          <el-form-item label="辅导科目" prop="subject">
            <el-select v-model="form.subject" placeholder="请选择科目">
              <el-option label="语文" value="语文" />
              <el-option label="数学" value="数学" />
              <el-option label="英语" value="英语" />
              <el-option label="物理" value="物理" />
              <el-option label="化学" value="化学" />
              <el-option label="生物" value="生物" />
              <el-option label="历史" value="历史" />
              <el-option label="地理" value="地理" />
              <el-option label="政治" value="政治" />
              <el-option label="综合" value="综合" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="学生年级" prop="grade">
            <el-select v-model="form.grade" placeholder="请选择年级">
              <el-option-group label="小学">
                <el-option label="一年级" value="小学一年级" />
                <el-option label="二年级" value="小学二年级" />
                <el-option label="三年级" value="小学三年级" />
                <el-option label="四年级" value="小学四年级" />
                <el-option label="五年级" value="小学五年级" />
                <el-option label="六年级" value="小学六年级" />
              </el-option-group>
              <el-option-group label="初中">
                <el-option label="初一" value="初一" />
                <el-option label="初二" value="初二" />
                <el-option label="初三" value="初三" />
              </el-option-group>
              <el-option-group label="高中">
                <el-option label="高一" value="高一" />
                <el-option label="高二" value="高二" />
                <el-option label="高三" value="高三" />
              </el-option-group>
            </el-select>
          </el-form-item>
          
          <el-form-item label="期望薪资" prop="salary">
            <el-input-number v-model="form.salary" :min="30" :max="500" :step="10" />
            <span class="unit">元/小时</span>
          </el-form-item>
        </div>
      </el-form>
    </div>
    
    <!-- 步骤2：详细要求 -->
    <div v-show="currentStep === 1" class="form-container">
      <el-form ref="step2Ref" :model="form" :rules="rules2" label-width="100px">
        <div class="form-section">
          <h3 class="section-title">上课时间</h3>
          
          <el-form-item label="上课频率" prop="frequency">
            <el-select v-model="form.frequency" placeholder="请选择上课频率">
              <el-option label="每周1次" value="每周1次" />
              <el-option label="每周2次" value="每周2次" />
              <el-option label="每周3次" value="每周3次" />
              <el-option label="每周4-5次" value="每周4-5次" />
              <el-option label="面议" value="面议" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="每次时长" prop="duration">
            <el-select v-model="form.duration" placeholder="请选择每次时长">
              <el-option label="1小时" :value="1" />
              <el-option label="1.5小时" :value="1.5" />
              <el-option label="2小时" :value="2" />
              <el-option label="2.5小时" :value="2.5" />
              <el-option label="3小时" :value="3" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="可用时段">
            <el-checkbox-group v-model="form.availableTime">
              <el-checkbox value="工作日白天">工作日白天</el-checkbox>
              <el-checkbox value="工作日晚上">工作日晚上</el-checkbox>
              <el-checkbox value="周末白天">周末白天</el-checkbox>
              <el-checkbox value="周末晚上">周末晚上</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </div>
        
        <div class="form-section">
          <h3 class="section-title">上课地点</h3>
          
          <el-form-item label="授课方式" prop="teachingMode">
            <el-radio-group v-model="form.teachingMode">
              <el-radio value="线下">线下上门</el-radio>
              <el-radio value="线上">线上授课</el-radio>
              <el-radio value="都可以">线上线下均可</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item v-if="form.teachingMode !== '线上'" label="上课地址" prop="address">
            <el-input v-model="form.address" placeholder="请输入详细地址" />
          </el-form-item>
          
          <el-form-item v-if="form.teachingMode !== '线上'" label="所在区域" prop="district">
            <el-input v-model="form.district" placeholder="如：海淀区中关村" />
          </el-form-item>
        </div>
        
        <div class="form-section">
          <h3 class="section-title">教师要求</h3>
          
          <el-form-item label="性别要求">
            <el-radio-group v-model="form.genderRequirement">
              <el-radio value="不限">不限</el-radio>
              <el-radio value="男">男教师</el-radio>
              <el-radio value="女">女教师</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="其他要求" prop="requirements">
            <el-input
              v-model="form.requirements"
              type="textarea"
              :rows="4"
              placeholder="请描述您对老师的其他要求，如教学经验、教学风格等"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </div>
        
        <!-- AI 解析 -->
        <div class="ai-assist">
          <el-button type="success" plain @click="aiParseDemand" :loading="aiParsing">
            <el-icon><MagicStick /></el-icon>
            AI 智能填写
          </el-button>
          <span class="ai-tip">输入需求描述，AI 帮您智能填充表单</span>
        </div>
      </el-form>
    </div>
    
    <!-- 步骤3：确认发布 -->
    <div v-show="currentStep === 2" class="form-container">
      <div class="confirm-section">
        <h3 class="section-title">需求确认</h3>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="需求标题" :span="2">
            {{ form.title }}
          </el-descriptions-item>
          <el-descriptions-item label="关联学生">
            {{ getStudentName(form.studentId) }}
          </el-descriptions-item>
          <el-descriptions-item label="辅导科目">
            {{ form.subject }}
          </el-descriptions-item>
          <el-descriptions-item label="学生年级">
            {{ form.grade }}
          </el-descriptions-item>
          <el-descriptions-item label="期望薪资">
            {{ form.salary }}元/小时
          </el-descriptions-item>
          <el-descriptions-item label="上课频率">
            {{ form.frequency }}
          </el-descriptions-item>
          <el-descriptions-item label="每次时长">
            {{ form.duration }}小时
          </el-descriptions-item>
          <el-descriptions-item label="授课方式">
            {{ form.teachingMode }}
          </el-descriptions-item>
          <el-descriptions-item label="性别要求">
            {{ form.genderRequirement }}
          </el-descriptions-item>
          <el-descriptions-item v-if="form.address" label="上课地址" :span="2">
            {{ form.district }} {{ form.address }}
          </el-descriptions-item>
          <el-descriptions-item v-if="form.requirements" label="其他要求" :span="2">
            {{ form.requirements }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="publish-options">
          <el-checkbox v-model="publishImmediately">创建后立即上架</el-checkbox>
          <div class="publish-tip">上架后教师可以查看并申请此需求</div>
        </div>
      </div>
    </div>
    
    <!-- 底部按钮 -->
    <div class="form-actions">
      <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
      <el-button v-if="currentStep < 2" type="primary" @click="nextStep">下一步</el-button>
      <el-button v-if="currentStep === 2" type="primary" :loading="submitting" @click="handleSubmit">
        {{ publishImmediately ? '发布需求' : '保存草稿' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, MagicStick } from '@element-plus/icons-vue'
import { createDemand, publishDemand } from '@shared/api/demand'
import { getStudentList } from '@shared/api/parent'
import { parseDemand } from '@shared/api/llm'

const router = useRouter()
const route = useRoute()

const currentStep = ref(0)
const step1Ref = ref(null)
const step2Ref = ref(null)
const submitting = ref(false)
const aiParsing = ref(false)
const publishImmediately = ref(true)
const students = ref([])

const form = reactive({
  studentId: null,
  title: '',
  subject: '',
  grade: '',
  salary: 80,
  frequency: '',
  duration: 2,
  availableTime: [],
  teachingMode: '线下',
  address: '',
  district: '',
  genderRequirement: '不限',
  requirements: ''
})

const rules1 = {
  title: [{ required: true, message: '请输入需求标题', trigger: 'blur' }],
  subject: [{ required: true, message: '请选择辅导科目', trigger: 'change' }],
  grade: [{ required: true, message: '请选择学生年级', trigger: 'change' }],
  salary: [{ required: true, message: '请设置期望薪资', trigger: 'blur' }]
}

const rules2 = {
  frequency: [{ required: true, message: '请选择上课频率', trigger: 'change' }],
  duration: [{ required: true, message: '请选择每次时长', trigger: 'change' }],
  teachingMode: [{ required: true, message: '请选择授课方式', trigger: 'change' }]
}

const loadStudents = async () => {
  try {
    const res = await getStudentList()
    if (res.code === 200) {
      students.value = res.data || []
      // 如果URL有studentId参数，自动选中
      if (route.query.studentId) {
        form.studentId = parseInt(route.query.studentId)
        const student = students.value.find(s => s.id === form.studentId)
        if (student) {
          form.grade = student.grade
        }
      }
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
  }
}

const getStudentName = (id) => {
  const student = students.value.find(s => s.id === id)
  return student ? student.name : '未关联'
}

const goBack = () => {
  router.back()
}

const addStudent = () => {
  router.push('/students/add')
}

const prevStep = () => {
  currentStep.value--
}

const nextStep = async () => {
  if (currentStep.value === 0) {
    const valid = await step1Ref.value.validate().catch(() => false)
    if (!valid) return
  } else if (currentStep.value === 1) {
    const valid = await step2Ref.value.validate().catch(() => false)
    if (!valid) return
  }
  currentStep.value++
}

const aiParseDemand = async () => {
  const description = form.requirements
  if (!description || description.length < 10) {
    ElMessage.warning('请先在"其他要求"中输入需求描述')
    return
  }
  
  aiParsing.value = true
  try {
    // 后端API参数为 text 字符串
    const res = await parseDemand(description)
    if (res.code === 200 && res.data) {
      const parsed = res.data
      if (parsed.subject) form.subject = parsed.subject
      if (parsed.grade) form.grade = parsed.grade
      if (parsed.salary) form.salary = parsed.salary
      if (parsed.frequency) form.frequency = parsed.frequency
      if (parsed.teachingMode) form.teachingMode = parsed.teachingMode
      ElMessage.success('AI 解析完成，请检查填充的内容')
    }
  } catch (error) {
    console.error('AI 解析失败:', error)
    ElMessage.error('AI 解析失败，请手动填写')
  } finally {
    aiParsing.value = false
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const res = await createDemand({
      ...form,
      availableTime: form.availableTime.join(',')
    })
    
    if (res.code === 200) {
      const demandId = res.data?.id || res.data
      
      if (publishImmediately.value && demandId) {
        await publishDemand(demandId)
        ElMessage.success('需求发布成功')
      } else {
        ElMessage.success('需求保存成功')
      }
      
      router.replace('/demands')
    }
  } catch (error) {
    console.error('创建需求失败:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadStudents()
})
</script>

<style lang="scss" scoped>
.create-demand-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
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

.step-bar {
  margin-bottom: 32px;
}

.form-container {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.form-section {
  margin-bottom: 24px;
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #eee;
  }
}

.unit {
  margin-left: 8px;
  color: #666;
}

.ai-assist {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f0f9eb;
  border-radius: 8px;
  
  .ai-tip {
    font-size: 13px;
    color: #67c23a;
  }
}

.confirm-section {
  .section-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 20px;
  }
}

.publish-options {
  margin-top: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  
  .publish-tip {
    font-size: 13px;
    color: #999;
    margin-top: 8px;
  }
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}
</style>
