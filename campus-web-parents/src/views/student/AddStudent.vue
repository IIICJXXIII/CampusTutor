<template>
  <div class="add-student-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">{{ isEdit ? '编辑孩子信息' : '添加孩子' }}</h1>
    </div>
    
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="student-form"
    >
      <div class="form-section">
        <h3 class="section-title">基本信息</h3>
        
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入孩子姓名" maxlength="20" />
        </el-form-item>
        
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="出生年月" prop="birthday">
          <el-date-picker
            v-model="form.birthday"
            type="month"
            placeholder="选择出生年月"
            format="YYYY年MM月"
            value-format="YYYY-MM"
          />
        </el-form-item>
      </div>
      
      <div class="form-section">
        <h3 class="section-title">学业信息</h3>
        
        <el-form-item label="就读学校" prop="school">
          <el-input v-model="form.school" placeholder="请输入学校名称" />
        </el-form-item>
        
        <el-form-item label="当前年级" prop="grade">
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
        
        <el-form-item label="需辅导科目" prop="subjects">
          <el-checkbox-group v-model="form.subjects">
            <el-checkbox value="语文">语文</el-checkbox>
            <el-checkbox value="数学">数学</el-checkbox>
            <el-checkbox value="英语">英语</el-checkbox>
            <el-checkbox value="物理">物理</el-checkbox>
            <el-checkbox value="化学">化学</el-checkbox>
            <el-checkbox value="生物">生物</el-checkbox>
            <el-checkbox value="历史">历史</el-checkbox>
            <el-checkbox value="地理">地理</el-checkbox>
            <el-checkbox value="政治">政治</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </div>
      
      <div class="form-section">
        <h3 class="section-title">补充信息</h3>
        
        <el-form-item label="学习情况" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请描述孩子的学习情况、优势科目、薄弱环节等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="性格特点" prop="character">
          <el-input
            v-model="form.character"
            type="textarea"
            :rows="3"
            placeholder="请描述孩子的性格特点，便于老师更好地沟通"
            maxlength="200"
          />
        </el-form-item>
      </div>
      
      <div class="form-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '添加孩子' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { addStudent, updateStudent, getStudentDetail } from '@shared/api/parent'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  name: '',
  gender: 1,
  birthday: '',
  school: '',
  grade: '',
  subjects: [],
  description: '',
  character: ''
})

const rules = {
  name: [
    { required: true, message: '请输入孩子姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  grade: [
    { required: true, message: '请选择年级', trigger: 'change' }
  ]
}

const loadStudent = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await getStudentDetail(route.params.id)
    if (res.code === 200 && res.data) {
      Object.assign(form, res.data)
    }
  } catch (error) {
    console.error('加载学生信息失败:', error)
    ElMessage.error('加载信息失败')
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
    const api = isEdit.value ? updateStudent : addStudent
    const data = { ...form }
    if (isEdit.value) {
      data.id = route.params.id
    }
    
    const res = await api(data)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      router.back()
    }
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadStudent()
})
</script>

<style lang="scss" scoped>
.add-student-page {
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

.student-form {
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

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}

:deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
