<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTutorProfile, updateTutorProfile } from '@/api/tutor'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const avatarUrl = ref('')

// 表单数据
const form = reactive({
  name: '',
  school: '',
  major: '',
  price: 150,
  subjects: [],
  grades: [], // 可授年级
  intro: '',
  isPublic: true,
  phone: '',
  gender: 1,
  teachExperience: '',
  availableTime: ''
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  school: [{ required: true, message: '请输入就读高校', trigger: 'blur' }],
  subjects: [{ required: true, message: '请选择擅长科目', trigger: 'change' }],
  price: [{ required: true, message: '请输入期望时薪', trigger: 'blur' }]
}

const formRef = ref(null)

// 科目选项
const subjectOptions = [
  '语文', '数学', '英语', '物理', '化学', '生物', '历史', '地理', '政治'
]

// 年级选项 - 与数据库tutor_profile.teach_grades和前端搜索保持一致
const gradeOptions = [
  '小学一年级', '小学二年级', '小学三年级', '小学四年级', '小学五年级', '小学六年级',
  '初一', '初二', '初三',
  '高一', '高二', '高三'
]

// 获取教师信息
const fetchProfile = async () => {
  loading.value = true
  try {
    const res = await getTutorProfile()
    if (res.data) {
      const data = res.data
      form.name = data.realName || userStore.userInfo?.username || ''
      form.school = data.universityName || ''
      form.major = data.major || ''
      form.price = data.expectPrice || 150
      form.intro = data.introduction || ''
      form.phone = data.phone || userStore.userInfo?.phone || ''
      form.gender = data.gender || 1
      form.teachExperience = data.teachExperience || ''
      form.availableTime = data.availableTime || ''
      avatarUrl.value = data.avatarUrl || ''
      
      if (data.teachSubjects) {
        try {
          form.subjects = JSON.parse(data.teachSubjects)
        } catch {
          // 兼容逗号分隔格式
          form.subjects = data.teachSubjects.split(',').map(s => s.trim()).filter(Boolean)
        }
      }
      if (data.teachGrades) {
        try {
          form.grades = JSON.parse(data.teachGrades)
        } catch {
          // 兼容逗号分隔格式
          form.grades = data.teachGrades.split(',').map(s => s.trim()).filter(Boolean)
        }
      }
    }
  } catch (error) {
    console.error('获取简历失败:', error)
  } finally {
    loading.value = false
  }
}

// 上传头像
const handleAvatarChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarUrl.value = e.target.result
  }
  reader.readAsDataURL(file.raw)
  return false
}

// 发布简历
const handlePublish = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请完善必填信息')
    return
  }

  loading.value = true
  try {
    await updateTutorProfile({
      realName: form.name,
      universityName: form.school,
      major: form.major,
      expectPrice: form.price,
      teachSubjects: JSON.stringify(form.subjects),
      teachGrades: JSON.stringify(form.grades),
      introduction: form.intro,
      phone: form.phone,
      gender: form.gender,
      teachExperience: form.teachExperience,
      availableTime: form.availableTime
    })
    ElMessage.success('发布成功！家长现在可以看到您的信息了')
    router.push('/find-students')
  } catch (error) {
    ElMessage.error(error.message || '发布失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<template>
  <div class="resume-page" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">发布家教信息</h1>
      <p class="page-subtitle">完善简历，让家长更容易找到你</p>
    </div>

    <!-- 表单区域 -->
    <div class="form-container">
      <el-card shadow="never">
        <el-form 
          ref="formRef"
          :model="form" 
          :rules="rules" 
          label-position="top"
        >
          <!-- 头像上传 -->
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="handleAvatarChange"
              accept="image/*"
            >
              <el-avatar :size="100" :src="avatarUrl" v-if="avatarUrl" />
              <div v-else class="avatar-placeholder">
                <el-icon :size="32"><Camera /></el-icon>
              </div>
            </el-upload>
            <span class="avatar-tip">点击上传真实头像</span>
          </div>

          <!-- 基本信息 -->
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="姓名 / 昵称" prop="name">
                <el-input v-model="form.name" placeholder="例如：张同学" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别">
                <el-radio-group v-model="form.gender">
                  <el-radio :value="1">男</el-radio>
                  <el-radio :value="2">女</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="就读高校" prop="school">
                <el-input v-model="form.school" placeholder="学校名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="专业">
                <el-input v-model="form.major" placeholder="专业名称" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="期望时薪" prop="price">
                <el-input-number 
                  v-model="form.price" 
                  :min="50" 
                  :max="1000" 
                  :step="10"
                  controls-position="right"
                >
                  <template #prefix>¥</template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话">
                <el-input v-model="form.phone" placeholder="手机号码" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="擅长科目" prop="subjects">
            <el-select
              v-model="form.subjects"
              multiple
              filterable
              placeholder="请选择擅长科目"
              style="width: 100%"
            >
              <el-option
                v-for="sub in subjectOptions"
                :key="sub"
                :label="sub"
                :value="sub"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="可授年级" prop="grades">
            <el-select
              v-model="form.grades"
              multiple
              filterable
              placeholder="请选择可授年级"
              style="width: 100%"
            >
              <el-option
                v-for="g in gradeOptions"
                :key="g"
                :label="g"
                :value="g"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="教学经验">
            <el-input 
              v-model="form.teachExperience" 
              type="textarea" 
              :rows="2"
              placeholder="例如：有2年家教经验，辅导过5名学生"
            />
          </el-form-item>

          <el-form-item label="可授课时间">
            <el-input 
              v-model="form.availableTime" 
              placeholder="例如：周末全天、工作日晚上"
            />
          </el-form-item>

          <el-form-item label="自我介绍 / 教学优势">
            <el-input 
              v-model="form.intro" 
              type="textarea" 
              :rows="4"
              placeholder="请介绍您的教学经验、性格特点等..."
              show-word-limit
              :maxlength="500"
            />
          </el-form-item>

          <el-form-item>
            <div class="switch-row">
              <span class="switch-label">公开展示我的信息</span>
              <el-switch v-model="form.isPublic" />
            </div>
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              class="submit-btn"
              @click="handlePublish"
              :loading="loading"
            >
              <el-icon class="mr-1"><CircleCheck /></el-icon>
              确认发布
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.resume-page {
  min-height: 100vh;
  background: $bg-light;
  padding-bottom: $spacing-xl;
}

.page-header {
  background: linear-gradient(135deg, $primary-color 0%, #667eea 100%);
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

.form-container {
  padding: $spacing-lg;
  margin-top: -$spacing-md;

  .el-card {
    border-radius: 16px;
  }

  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: $spacing-xl;

    .avatar-uploader {
      cursor: pointer;
    }

    .avatar-placeholder {
      width: 100px;
      height: 100px;
      border-radius: 50%;
      background: $bg-light;
      display: flex;
      align-items: center;
      justify-content: center;
      color: $text-muted;
      transition: all 0.3s;

      &:hover {
        background: #e5e7eb;
      }
    }

    .avatar-tip {
      font-size: 12px;
      color: $text-muted;
      margin-top: $spacing-sm;
    }
  }

  .switch-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding: $spacing-sm 0;

    .switch-label {
      font-size: 14px;
      font-weight: 500;
      color: $text-primary;
    }
  }

  .submit-btn {
    width: 100%;
    height: 50px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
  }

  :deep(.el-input-number) {
    width: 100%;
  }
}
</style>
