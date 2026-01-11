<template>
  <div class="student-form-page p-4 pb-20">
    <div class="flex items-center mb-6">
      <el-button link @click="router.back()" class="mr-2">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="text-xl font-bold">{{ isEdit ? '编辑孩子信息' : '添加孩子' }}</h1>
    </div>
    
    <el-card class="form-container" shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="student-form"
      >
        <div class="section mb-6">
          <h3 class="font-bold mb-4 border-l-4 border-blue-500 pl-3">基本信息</h3>
          
          <el-form-item label="姓名" prop="studentName">
            <el-input v-model="form.studentName" placeholder="请输入孩子姓名" maxlength="20" />
          </el-form-item>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="form.gender">
                  <el-radio :value="1">男</el-radio>
                  <el-radio :value="2">女</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="当前年级" prop="grade">
                <el-select v-model="form.grade" placeholder="请选择年级" class="w-full">
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
            </el-col>
          </el-row>
        </div>
        
        <div class="section mb-6">
          <h3 class="font-bold mb-4 border-l-4 border-blue-500 pl-3">学业信息</h3>
          
          <el-form-item label="就读学校" prop="schoolName">
            <el-input v-model="form.schoolName" placeholder="（选填）请输入学校名称" />
          </el-form-item>
          
          <el-form-item label="需辅导/薄弱科目" prop="weakSubjects">
            <el-checkbox-group v-model="form.weakSubjects">
              <el-checkbox-button value="语文">语文</el-checkbox-button>
              <el-checkbox-button value="数学">数学</el-checkbox-button>
              <el-checkbox-button value="英语">英语</el-checkbox-button>
              <el-checkbox-button value="物理">物理</el-checkbox-button>
              <el-checkbox-button value="化学">化学</el-checkbox-button>
              <el-checkbox-button value="生物">生物</el-checkbox-button>
              <el-checkbox-button value="历史">历史</el-checkbox-button>
              <el-checkbox-button value="地理">地理</el-checkbox-button>
              <el-checkbox-button value="政治">政治</el-checkbox-button>
            </el-checkbox-group>
          </el-form-item>
        </div>
        
        <div class="section">
          <h3 class="font-bold mb-4 border-l-4 border-blue-500 pl-3">补充说明</h3>
          
          <el-form-item label="学习情况" prop="studyDesc">
            <el-input
              v-model="form.studyDesc"
              type="textarea"
              :rows="4"
              placeholder="请描述孩子的学习情况、薄弱环节等，以便老师更好地了解情况"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </div>
      </el-form>
    </el-card>

    <div class="bottom-bar fixed bottom-0 left-0 right-0 p-4 bg-white border-t flex gap-4">
      <el-button class="flex-grow" size="large" @click="router.back()">取消</el-button>
      <el-button
        class="flex-grow"
        size="large"
        type="primary"
        :loading="submitting"
        @click="handleSubmit"
      >
        提交
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { addStudent, updateStudent, getStudentDetail } from '@/api/parent'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const submitting = ref(false)

const studentId = computed(() => route.params.id)
const isEdit = computed(() => !!studentId.value)

const form = reactive({
  id: null,
  studentName: '',
  gender: 1,
  grade: '',
  schoolName: '',
  weakSubjects: [],
  studyDesc: ''
})

const rules = {
  studentName: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }]
}

const loadDetail = async () => {
  if (!isEdit.value) return
  try {
    const res = await getStudentDetail(studentId.value)
    if (res.code === 200 && res.data) {
      const data = res.data
      form.id = data.id
      // 这里的映射需要注意后端字段名
      form.studentName = data.name || data.studentName
      form.gender = data.gender
      form.grade = data.grade
      form.schoolName = data.school || data.schoolName
      
      // 处理科目列表
      if (data.subjects) {
        if (Array.isArray(data.subjects)) {
          form.weakSubjects = data.subjects
        } else {
          try {
            form.weakSubjects = JSON.parse(data.subjects)
          } catch (e) {
            form.weakSubjects = data.subjects.split(',').filter(s => s)
          }
        }
      } else if (data.weakSubjects) {
        form.weakSubjects = Array.isArray(data.weakSubjects) ? data.weakSubjects : JSON.parse(data.weakSubjects)
      }
      
      form.studyDesc = data.studyDesc || data.description
    }
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载信息失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 转换数据格式以匹配 StudentRequest
      const payload = {
        ...form,
        id: isEdit.value ? parseInt(studentId.value) : null
      }
      
      const res = isEdit.value ? await updateStudent(payload) : await addStudent(payload)
      
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
        router.back()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (error) {
      console.error('保存失败:', error)
      ElMessage.error('网络错误，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.form-container {
  border-radius: 12px;
}
.bottom-bar {
  z-index: 100;
}
</style>
