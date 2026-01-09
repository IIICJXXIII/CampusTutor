<template>
  <div class="edit-demand-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">编辑需求</h1>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <el-form
      v-else
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="demand-form"
    >
      <div class="form-section">
        <h3 class="section-title">基本信息</h3>
        
        <el-form-item label="关联学生" prop="studentId">
          <el-select v-model="form.studentId" placeholder="请选择要辅导的孩子">
            <el-option
              v-for="student in students"
              :key="student.id"
              :label="`${student.name} (${student.grade})`"
              :value="student.id"
            />
          </el-select>
        </el-form-item>
        
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
      
      <div class="form-section">
        <h3 class="section-title">上课安排</h3>
        
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
            placeholder="请描述您对老师的其他要求"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </div>
      
      <div class="form-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          保存修改
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getDemandDetail, updateDemand } from '@shared/api/demand'
import { getStudentList } from '@shared/api/parent'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const students = ref([])

const form = reactive({
  id: null,
  studentId: null,
  title: '',
  subject: '',
  grade: '',
  salary: 80,
  frequency: '',
  duration: 2,
  teachingMode: '线下',
  address: '',
  district: '',
  genderRequirement: '不限',
  requirements: ''
})

const rules = {
  title: [{ required: true, message: '请输入需求标题', trigger: 'blur' }],
  subject: [{ required: true, message: '请选择辅导科目', trigger: 'change' }],
  grade: [{ required: true, message: '请选择学生年级', trigger: 'change' }],
  salary: [{ required: true, message: '请设置期望薪资', trigger: 'blur' }],
  frequency: [{ required: true, message: '请选择上课频率', trigger: 'change' }],
  duration: [{ required: true, message: '请选择每次时长', trigger: 'change' }],
  teachingMode: [{ required: true, message: '请选择授课方式', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const [demandRes, studentsRes] = await Promise.all([
      getDemandDetail(route.params.id),
      getStudentList()
    ])
    
    if (demandRes.code === 200 && demandRes.data) {
      Object.assign(form, demandRes.data)
    }
    
    if (studentsRes.code === 200) {
      students.value = studentsRes.data || []
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    const res = await updateDemand(form)
    if (res.code === 200) {
      ElMessage.success('修改成功')
      router.back()
    }
  } catch (error) {
    console.error('修改失败:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.edit-demand-page {
  padding: 20px;
  max-width: 700px;
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

.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 12px;
}

.demand-form {
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

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}
</style>
