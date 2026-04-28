<template>
  <div class="my-resume">
    <div class="page-header">
      <h1 class="page-title">我的简历</h1>
      <p class="page-desc">完善简历信息，提高匹配成功率</p>
    </div>
    
    <div class="resume-container">
      <!-- 基本信息 -->
      <div class="section-card">
        <div class="section-header">
          <h3>基本信息</h3>
          <el-button type="primary" link @click="editSection('basic')">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
        </div>
        <div class="section-body">
          <div class="avatar-row">
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
            >
              <el-avatar :size="80" :src="profile.avatar">
                <el-icon :size="32"><User /></el-icon>
              </el-avatar>
              <div class="avatar-tip">点击更换头像</div>
            </el-upload>
            <div class="basic-info">
              <h2>{{ profile.realName || userStore.nickname }}</h2>
              <p>{{ profile.school }} · {{ profile.major }}</p>
              <div class="tags">
                <el-tag v-if="profile.certStatus === 2" type="success">已认证</el-tag>
                <el-tag v-for="tag in profile.tags" :key="tag">{{ tag }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 教学信息 -->
      <div class="section-card">
        <div class="section-header">
          <h3>教学信息</h3>
          <el-button type="primary" link @click="editSection('teaching')">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
        </div>
        <div class="section-body">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="擅长科目">
              <el-tag v-for="s in profile.subjects" :key="s" class="subject-tag">{{ s }}</el-tag>
              <span v-if="!profile.subjects?.length" class="no-data">未设置</span>
            </el-descriptions-item>
            <el-descriptions-item label="可教年级">
              <span>{{ profile.grades?.join('、') || '未设置' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="教学经验">{{ profile.experience || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="期望薪资">
              {{ profile.expectedSalary ? `¥${profile.expectedSalary}/小时` : '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="可授课区域" :span="2">
              {{ profile.teachingArea || '未设置' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      
      <!-- 自我介绍 -->
      <div class="section-card">
        <div class="section-header">
          <h3>自我介绍</h3>
          <el-button type="primary" link @click="editSection('intro')">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
        </div>
        <div class="section-body">
          <p class="intro-text">{{ profile.introduction || '暂未填写自我介绍' }}</p>
        </div>
      </div>
      
      <!-- 教学成果 -->
      <div class="section-card">
        <div class="section-header">
          <h3>教学成果</h3>
          <el-button type="primary" link @click="editSection('achievements')">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
        </div>
        <div class="section-body">
          <p class="achievements-text">{{ profile.achievements || '暂未填写教学成果' }}</p>
        </div>
      </div>
      
      <!-- 排课设置 -->
      <div class="section-card">
        <div class="section-header">
          <h3>排课时间</h3>
          <el-button type="primary" link @click="goToSchedule">
            <el-icon><Setting /></el-icon>设置
          </el-button>
        </div>
        <div class="section-body">
          <div v-if="schedules.length" class="schedule-preview">
            <div v-for="day in schedules" :key="day.dayOfWeek" class="schedule-day">
              <span class="day-label">{{ getDayName(day.dayOfWeek) }}</span>
              <span class="time-slots">{{ day.timeSlots?.join('、') || '全天' }}</span>
            </div>
          </div>
          <el-empty v-else description="暂未设置可授课时间" :image-size="60" />
        </div>
      </div>
    </div>
    
    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="editDialogTitle" width="600px">
      <el-form :model="editForm" label-width="100px">
        <!-- 基本信息 -->
        <template v-if="editType === 'basic'">
          <el-form-item label="昵称">
            <el-input v-model="editForm.nickname" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="editForm.phone" disabled />
          </el-form-item>
        </template>
        
        <!-- 教学信息 -->
        <template v-if="editType === 'teaching'">
          <el-form-item label="擅长科目">
            <el-checkbox-group v-model="editForm.subjects">
              <el-checkbox label="钢琴/乐器陪练" />
              <el-checkbox label="美术/书法" />
              <el-checkbox label="声乐/视唱练耳" />
              <el-checkbox label="中考体育专项" />
              <el-checkbox label="羽毛球/网球陪练" />
              <el-checkbox label="篮球/足球指导" />
              <el-checkbox label="少儿编程(Scratch/Python)" />
              <el-checkbox label="机器人/3D打印" />
              <el-checkbox label="科学实验/航模" />
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="可教年级">
            <el-checkbox-group v-model="editForm.grades">
              <el-checkbox label="小学" />
              <el-checkbox label="初中" />
              <el-checkbox label="高中" />
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="教学经验">
            <el-select v-model="editForm.experience" placeholder="请选择">
              <el-option label="无经验" value="无经验" />
              <el-option label="1年以下" value="1年以下" />
              <el-option label="1-2年" value="1-2年" />
              <el-option label="2-3年" value="2-3年" />
              <el-option label="3年以上" value="3年以上" />
            </el-select>
          </el-form-item>
          <el-form-item label="期望薪资">
            <el-input-number v-model="editForm.expectedSalary" :min="50" :max="500" :step="10" />
            <span class="unit">元/小时</span>
          </el-form-item>
          <el-form-item label="可授课区域">
            <el-input v-model="editForm.teachingArea" placeholder="如：海淀区、朝阳区" />
          </el-form-item>
        </template>
        
        <!-- 自我介绍 -->
        <template v-if="editType === 'intro'">
          <el-form-item label="自我介绍">
            <el-input
              v-model="editForm.introduction"
              type="textarea"
              :rows="6"
              placeholder="介绍一下您的教学风格、教学理念等"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </template>
        
        <!-- 教学成果 -->
        <template v-if="editType === 'achievements'">
          <el-form-item label="教学成果">
            <el-input
              v-model="editForm.achievements"
              type="textarea"
              :rows="6"
              placeholder="描述您的教学成果，如学生成绩提升情况等"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </template>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Edit, Setting } from '@element-plus/icons-vue'
import { useUserStore, useTutorStore } from '@shared/stores'
import { getTutorProfile, updateTutorProfile, getScheduleConfig } from '@shared/api/tutor'

const router = useRouter()
const userStore = useUserStore()
const tutorStore = useTutorStore()

const profile = ref({})
const schedules = ref([])
const editDialogVisible = ref(false)
const editType = ref('')
const saving = ref(false)

const uploadUrl = '/api/file/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const editForm = reactive({
  nickname: '',
  phone: '',
  subjects: [],
  grades: [],
  experience: '',
  expectedSalary: 100,
  teachingArea: '',
  introduction: '',
  achievements: ''
})

const editDialogTitle = computed(() => {
  const map = {
    basic: '编辑基本信息',
    teaching: '编辑教学信息',
    intro: '编辑自我介绍',
    achievements: '编辑教学成果'
  }
  return map[editType.value] || '编辑'
})

const getDayName = (day) => {
  const names = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return names[day] || ''
}

const loadProfile = async () => {
  try {
    const res = await getTutorProfile()
    if (res.code === 200) {
      const data = res.data || {}
      // 后端字段 -> 前端展示字段映射
      const parseJson = (val, fallback) => {
        if (!val) return fallback
        if (Array.isArray(val)) return val
        try { return JSON.parse(val) } catch { return fallback }
      }
      profile.value = {
        ...data,
        realName: data.realName,
        school: data.universityName,
        universityName: data.universityName,
        major: data.major,
        avatar: data.avatarUrl || data.avatar,
        subjects: parseJson(data.teachSubjects, []),
        grades: parseJson(data.teachGrades, []),
        experience: data.teachStyle || data.experience,
        expectedSalary: data.expectPrice,
        teachingArea: data.address,
        introduction: data.introduction,
        achievements: data.achievements,
        certStatus: data.certStatus,
        teachStyle: data.teachStyle
      }
      tutorStore.setProfile(res.data)
    }
  } catch (error) {
    console.error('加载简历失败', error)
  }
}

const loadSchedule = async () => {
  try {
    const res = await getScheduleConfig()
    if (res.code === 200) {
      schedules.value = res.data || []
    }
  } catch (error) {
    console.error('加载排课失败', error)
  }
}

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    profile.value.avatar = response.data.url
    saveProfileField('avatar', response.data.url)
  }
}

const editSection = (type) => {
  editType.value = type
  
  // 初始化表单数据
  Object.assign(editForm, {
    nickname: profile.value.nickname || userStore.nickname,
    phone: userStore.phone,
    subjects: profile.value.subjects || [],
    grades: profile.value.grades || [],
    experience: profile.value.experience || '',
    expectedSalary: profile.value.expectedSalary || 100,
    teachingArea: profile.value.teachingArea || '',
    introduction: profile.value.introduction || '',
    achievements: profile.value.achievements || ''
  })
  
  editDialogVisible.value = true
}

const saveProfile = async () => {
  saving.value = true
  try {
    // 将前端字段名映射为后端 TutorProfileUpdateRequest 字段名
    const data = {
      realName: editForm.nickname || profile.value.realName,
      universityName: profile.value.school || profile.value.universityName,
      major: profile.value.major,
      teachSubjects: editForm.subjects,          // 后端: teachSubjects
      teachGrades: editForm.grades,              // 后端: teachGrades
      teachStyle: profile.value.teachStyle,
      introduction: editForm.introduction,
      expectPrice: editForm.expectedSalary,       // 后端: expectPrice
      canVisit: 1,
      canOnline: 1,
      address: editForm.teachingArea
    }
    
    const res = await updateTutorProfile(data)
    
    if (res.code === 200) {
      ElMessage.success('保存成功')
      editDialogVisible.value = false
      loadProfile()
    }
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const saveProfileField = async (field, value) => {
  try {
    await updateTutorProfile({
      ...profile.value,
      [field]: value
    })
  } catch (error) {
    console.error('保存失败', error)
  }
}

const goToSchedule = () => {
  router.push('/teacher/schedule')
}

onMounted(() => {
  loadProfile()
  loadSchedule()
})
</script>

<style lang="scss" scoped>
.my-resume {
  max-width: 800px;
  margin: 0 auto;
  
  .section-card {
    background: #fff;
    border-radius: 12px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    overflow: hidden;
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 24px;
      border-bottom: 1px solid #ebeef5;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
      }
    }
    
    .section-body {
      padding: 24px;
    }
  }
  
  .avatar-row {
    display: flex;
    align-items: center;
    gap: 24px;
    
    .avatar-uploader {
      cursor: pointer;
      text-align: center;
      
      .avatar-tip {
        font-size: 12px;
        color: #909399;
        margin-top: 8px;
      }
    }
    
    .basic-info {
      h2 {
        font-size: 20px;
        font-weight: 600;
        margin-bottom: 8px;
      }
      
      p {
        font-size: 14px;
        color: #606266;
        margin-bottom: 12px;
      }
      
      .tags {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .subject-tag {
    margin-right: 8px;
    margin-bottom: 4px;
  }
  
  .no-data {
    color: #909399;
    font-size: 14px;
  }
  
  .intro-text, .achievements-text {
    font-size: 14px;
    color: #606266;
    line-height: 1.8;
  }
  
  .schedule-preview {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    
    .schedule-day {
      background: #f5f7fa;
      padding: 8px 16px;
      border-radius: 8px;
      
      .day-label {
        font-weight: 500;
        margin-right: 8px;
      }
      
      .time-slots {
        font-size: 13px;
        color: #606266;
      }
    }
  }
  
  .unit {
    margin-left: 8px;
    color: #909399;
  }
}
</style>
