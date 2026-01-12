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
const certStatus = ref(null)

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
      certStatus.value = data.certStatus
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

  if (certStatus.value !== 2) {
    ElMessage.warning('认证通过后才能更新简历，请先完成/等待审核')
    router.push('/teacher/auth')
    return
  }

  loading.value = true
  try {
    await updateTutorProfile({
      realName: form.name,
      universityName: form.school,
      major: form.major,
      expectPrice: form.price,
      // 后端 DTO 期望 List<String>，直接传数组
      teachSubjects: form.subjects || [],
      teachGrades: form.grades || [],
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
    <div class="page-wrapper">
      <el-row :gutter="32">
        <!-- 左侧信息栏 -->
        <el-col :span="7" class="sidebar-col">
          <!-- 头像卡片 -->
          <el-card class="profile-card" shadow="hover">
            <div class="avatar-section">
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :before-upload="handleAvatarChange"
                accept="image/*"
              >
                <el-avatar :size="120" :src="avatarUrl" v-if="avatarUrl" />
                <div v-else class="avatar-placeholder">
                  <el-icon :size="40"><Camera /></el-icon>
                </div>
              </el-upload>
              <span class="avatar-tip">点击上传真实头像</span>
            </div>
            <div class="profile-info">
              <h2>{{ form.name || '设置姓名' }}</h2>
              <p>{{ form.school || '添加学校信息' }}</p>
              <el-tag v-if="certStatus === 2" type="success" effect="light">已认证</el-tag>
              <el-tag v-else-if="certStatus === 1" type="warning" effect="light">审核中</el-tag>
              <el-tag v-else type="info" effect="light">未认证</el-tag>
            </div>
          </el-card>

          <!-- 认证提示 -->
          <el-card class="tip-card" shadow="hover" v-if="certStatus !== 2">
            <div class="tip-icon">
              <el-icon :size="24"><Warning /></el-icon>
            </div>
            <h3>完成认证</h3>
            <p>认证通过后才能发布简历，让家长更信任你</p>
            <el-button type="primary" size="small" @click="$router.push('/teacher/auth')">去认证</el-button>
          </el-card>

          <!-- 填写提示 -->
          <el-card class="guide-card" shadow="hover">
            <template #header>
              <div class="card-title">
                <el-icon><InfoFilled /></el-icon>
                <span>填写指南</span>
              </div>
            </template>
            <div class="guide-content">
              <p>📝 真实姓名有助于建立信任</p>
              <p>🏫 填写准确的学校和专业</p>
              <p>💰 合理定价更容易获得订单</p>
              <p>✨ 详细的自我介绍更有竞争力</p>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧表单区 -->
        <el-col :span="17">
          <!-- 页面标题 -->
          <div class="page-header">
            <div class="header-left">
              <h1>发布家教信息</h1>
              <p>完善简历，让家长更容易找到你</p>
            </div>
            <el-switch v-model="form.isPublic" active-text="公开展示" />
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
            <!-- 基本信息卡片 -->
            <el-card class="form-card" shadow="hover">
              <template #header>
                <div class="form-header">
                  <el-icon><User /></el-icon>
                  <span>基本信息</span>
                </div>
              </template>
              
              <el-row :gutter="24">
                <el-col :span="8">
                  <el-form-item label="姓名 / 昵称" prop="name">
                    <el-input v-model="form.name" placeholder="例如：张同学" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="性别">
                    <el-radio-group v-model="form.gender">
                      <el-radio :value="1" border>男</el-radio>
                      <el-radio :value="2" border>女</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="联系电话">
                    <el-input v-model="form.phone" placeholder="手机号码" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
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
            </el-card>

            <!-- 授课信息卡片 -->
            <el-card class="form-card" shadow="hover">
              <template #header>
                <div class="form-header">
                  <el-icon><Reading /></el-icon>
                  <span>授课信息</span>
                </div>
              </template>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="擅长科目" prop="subjects">
                    <el-select v-model="form.subjects" multiple filterable placeholder="请选择擅长科目" style="width: 100%">
                      <el-option v-for="sub in subjectOptions" :key="sub" :label="sub" :value="sub" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="可授年级" prop="grades">
                    <el-select v-model="form.grades" multiple filterable placeholder="请选择可授年级" style="width: 100%">
                      <el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="期望时薪" prop="price">
                    <el-input-number v-model="form.price" :min="50" :max="1000" :step="10" controls-position="right" style="width: 100%">
                      <template #prefix>¥</template>
                    </el-input-number>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="可授课时间">
                    <el-input v-model="form.availableTime" placeholder="例如：周末全天、工作日晚上" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="教学经验">
                <el-input v-model="form.teachExperience" type="textarea" :rows="2" placeholder="例如：有2年家教经验，辅导过5名学生" />
              </el-form-item>
            </el-card>

            <!-- 自我介绍卡片 -->
            <el-card class="form-card" shadow="hover">
              <template #header>
                <div class="form-header">
                  <el-icon><EditPen /></el-icon>
                  <span>自我介绍</span>
                </div>
              </template>

              <el-form-item label="自我介绍 / 教学优势">
                <el-input v-model="form.intro" type="textarea" :rows="5" placeholder="请介绍您的教学经验、性格特点等..." show-word-limit :maxlength="500" />
              </el-form-item>

              <div class="form-actions">
                <el-button size="large" @click="$router.back()">取消</el-button>
                <el-button type="primary" size="large" @click="handlePublish" :loading="loading">
                  <el-icon><CircleCheck /></el-icon> 确认发布
                </el-button>
              </div>
            </el-card>
          </el-form>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.resume-page {
  min-height: calc(100vh - 114px);
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f4f8 50%, #f5f7fa 100%);
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

.profile-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 20px;
  text-align: center;

  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 16px;

    .avatar-uploader {
      cursor: pointer;
    }

    .avatar-placeholder {
      width: 120px;
      height: 120px;
      border-radius: 50%;
      background: linear-gradient(135deg, #e0e7ff 0%, #f0f4ff 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #409eff;
      transition: all 0.3s;
      border: 3px dashed #c0ccda;

      &:hover {
        border-color: #409eff;
        background: #e8f4ff;
      }
    }

    .avatar-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 8px;
    }
  }

  .profile-info {
    h2 {
      font-size: 20px;
      font-weight: 700;
      color: #303133;
      margin-bottom: 4px;
    }

    p {
      font-size: 13px;
      color: #909399;
      margin-bottom: 12px;
    }
  }
}

.tip-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #fff7e6 0%, #fff 100%);
  text-align: center;

  :deep(.el-card__body) {
    padding: 24px;
  }

  .tip-icon {
    width: 48px;
    height: 48px;
    background: #ffc107;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 12px;
    color: #fff;
  }

  h3 {
    font-size: 16px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 8px;
  }

  p {
    font-size: 13px;
    color: #606266;
    margin-bottom: 16px;
  }
}

.guide-card {
  border-radius: 16px;
  border: none;
  background: linear-gradient(135deg, #f0f9ff 0%, #fff 100%);

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 700;
    font-size: 15px;
    color: #303133;

    .el-icon {
      color: #409eff;
    }
  }

  .guide-content {
    p {
      margin: 0 0 10px 0;
      font-size: 13px;
      color: #606266;
      line-height: 1.6;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

/* 右侧内容 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 24px 28px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);

  .header-left {
    h1 {
      font-size: 24px;
      font-weight: 800;
      color: #1a1a2e;
      margin: 0 0 4px 0;
    }

    p {
      font-size: 14px;
      color: #909399;
      margin: 0;
    }
  }
}

.form-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);

  .form-header {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    font-weight: 700;
    color: #303133;

    .el-icon {
      font-size: 20px;
      color: #409eff;
    }
  }

  :deep(.el-card__body) {
    padding: 28px;
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f2f5;

  .el-button {
    min-width: 140px;
    height: 48px;
    font-size: 15px;
    font-weight: 600;
    border-radius: 12px;

    &.el-button--primary {
      background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
      border: none;
      box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 12px 32px rgba(64, 158, 255, 0.4);
      }
    }
  }
}

/* 表单增强 */
:deep(.el-form-item) {
  margin-bottom: 20px;

  .el-form-item__label {
    font-weight: 600;
    color: #303133;
    font-size: 14px;
  }
}

:deep(.el-input__wrapper), :deep(.el-textarea__inner) {
  border-radius: 10px;

  &:focus-within {
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  }
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-radio-group) {
  display: flex;
  gap: 16px;
}

:deep(.el-radio.is-bordered) {
  padding: 12px 24px;
  border-radius: 10px;
  margin-right: 0;

  &.is-checked {
    border-color: #409eff;
    background: #f0f9ff;
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
  .resume-page {
    padding: 16px 0;
  }

  .page-wrapper {
    padding: 0 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .form-actions {
    flex-direction: column-reverse;

    .el-button {
      width: 100%;
      min-width: auto;
    }
  }
}
</style>
