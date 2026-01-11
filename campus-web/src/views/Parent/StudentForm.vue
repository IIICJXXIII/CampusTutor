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

          <el-form-item label="家庭住址" prop="address">
            <el-input 
              v-model="form.address" 
              placeholder="请输入详细地址（如：xx区xx路xx小区）" 
              @keyup.enter="handleAddressSearch"
              clearable
            >
              <template #append>
                <el-button @click="handleAddressSearch" :loading="geoLoading" title="点击获取坐标">
                  <el-icon class="mr-1"><Location /></el-icon> 定位
                </el-button>
              </template>
            </el-input>
            
            <div class="mt-1 h-5 text-xs">
              <div v-if="form.latitude && form.longitude" class="flex items-center text-green-600">
                <el-icon class="mr-1"><CircleCheck /></el-icon>
                <span>已获取坐标: [{{ form.longitude }}, {{ form.latitude }}]</span>
              </div>
              <div v-else class="flex items-center text-gray-400">
                <el-icon class="mr-1"><Warning /></el-icon>
                <span>请输入地址并点击“定位”按钮以获取精确位置（用于计算家教距离）</span>
              </div>
            </div>
          </el-form-item>
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
import { ArrowLeft, Location, CircleCheck, Warning } from '@element-plus/icons-vue'
import { addStudent, updateStudent, getStudentDetail } from '@/api/parent'
import request from '@/utils/request' // 引入通用请求工具用于调用地图接口

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const submitting = ref(false)
const geoLoading = ref(false) // 地图定位loading状态

const studentId = computed(() => route.params.id)
const isEdit = computed(() => !!studentId.value)

const form = reactive({
  id: null,
  studentName: '',
  gender: 1,
  grade: '',
  schoolName: '',
  weakSubjects: [],
  studyDesc: '',
  // 新增字段
  address: '',
  latitude: null,
  longitude: null
})

const rules = {
  studentName: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
  address: [{ required: true, message: '请输入家庭住址并定位', trigger: 'blur' }]
}

// 核心功能：地址解析
const handleAddressSearch = async () => {
  if (!form.address) return ElMessage.warning('请先输入详细地址')
  
  geoLoading.value = true
  try {
    // 调用我们在后端 MapController 定义的接口
    const res = await request.get('/api/map/geocoder', {
      params: { address: form.address }
    })
    
    if (res.code === 200 && res.data.status === 0) {
      // 兼容后端 ResultData 或 Result 结构
      const resultData = res.data.result || res.data.resultData
      if (resultData && resultData.location) {
        form.latitude = resultData.location.lat
        form.longitude = resultData.location.lng
        ElMessage.success('地址定位成功')
      } else {
        ElMessage.warning('未能获取坐标，请尝试输入更标准的地址')
      }
    } else {
      ElMessage.error(res.message || '地址解析失败')
    }
  } catch (error) {
    console.error('定位失败:', error)
    ElMessage.error('定位服务异常')
  } finally {
    geoLoading.value = false
  }
}

const loadDetail = async () => {
  if (!isEdit.value) return
  try {
    const res = await getStudentDetail(studentId.value)
    if (res.code === 200 && res.data) {
      const data = res.data
      form.id = data.id
      form.studentName = data.name || data.studentName
      form.gender = data.gender
      form.grade = data.grade
      form.schoolName = data.school || data.schoolName || data.universityName
      
      // 回显地址信息
      form.address = data.address || ''
      form.latitude = data.latitude
      form.longitude = data.longitude
      
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
    
    // 校验坐标是否存在
    if (!form.latitude || !form.longitude) {
      ElMessage.warning('请点击地址栏右侧的“定位”按钮以获取准确位置')
      return
    }
    
    submitting.value = true
    try {
      // 转换数据格式以匹配后端实体
      const payload = {
        id: isEdit.value ? parseInt(studentId.value) : null,
        name: form.studentName, // 后端可能是 name
        gender: form.gender,
        grade: form.grade,
        universityName: form.schoolName, // 后端实体用了 universityName 存储学校
        address: form.address,
        latitude: form.latitude,
        longitude: form.longitude,
        subjects: JSON.stringify(form.weakSubjects), // 数组转字符串存储
        description: form.studyDesc // 后端可能是 description
      }
      
      // 如果后端接口需要特定字段名（如 studentName），请在这里调整 payload
      
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
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}
</style>