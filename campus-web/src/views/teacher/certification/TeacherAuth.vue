<template>
  <div class="teacher-auth">
    <div class="page-header">
      <h1 class="page-title">资质认证</h1>
      <p class="page-desc">完成认证后可接收家教订单</p>
    </div>
    
    <!-- 认证状态 -->
    <div v-if="certStatus > 0" class="status-card">
      <el-result
        :icon="getStatusIcon()"
        :title="getStatusTitle()"
        :sub-title="getStatusSubTitle()"
      >
        <template #extra>
          <el-button v-if="certStatus === 3" type="primary" @click="resubmit">
            重新提交
          </el-button>
          <el-button v-if="certStatus === 2" type="primary" @click="goToResume">
            完善简历
          </el-button>
        </template>
      </el-result>
    </div>
    
    <!-- 认证表单 -->
    <div v-else class="auth-container">
      <el-steps :active="currentStep" finish-status="success" align-center class="steps">
        <el-step title="学生证认证" />
        <el-step title="身份证认证" />
        <el-step title="确认信息" />
      </el-steps>
      
      <!-- 步骤1: 学生证 -->
      <div v-show="currentStep === 0" class="step-content">
        <div class="upload-section">
          <h3>上传学生证照片</h3>
          <p class="tip">请上传清晰的学生证正面照片，系统将自动识别信息</p>
          
          <el-upload
            class="upload-area"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :data="{ type: 'cert' }"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :on-success="handleStudentCardUpload"
            accept="image/*,.pdf"
          >
            <div v-if="!form.studentCardUrl" class="upload-placeholder">
              <el-icon :size="48"><Plus /></el-icon>
              <p>点击上传学生证</p>
            </div>
            <img v-else :src="form.studentCardUrl" class="preview-image" />
          </el-upload>
          
          <div v-if="studentCardInfo.name" class="ocr-result">
            <h4>识别结果</h4>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="姓名">{{ studentCardInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="学号">{{ studentCardInfo.studentId }}</el-descriptions-item>
              <el-descriptions-item label="学校">{{ studentCardInfo.school }}</el-descriptions-item>
              <el-descriptions-item label="专业">{{ studentCardInfo.major }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
        
        <div class="step-actions">
          <el-button type="primary" :disabled="!form.studentCardUrl" @click="nextStep">
            下一步
          </el-button>
        </div>
      </div>
      
      <!-- 步骤2: 身份证 -->
      <div v-show="currentStep === 1" class="step-content">
        <div class="upload-section">
          <h3>上传身份证照片</h3>
          
          <div class="id-card-uploads">
            <div class="id-card-item">
              <p class="label">身份证正面 (人像面)</p>
              <el-upload
                class="upload-area small"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :data="{ type: 'cert' }"
                :show-file-list="false"
                :before-upload="beforeUpload"
                :on-success="handleIdCardFrontUpload"
                accept="image/*,.jpg,.jpeg,.png,.gif,.webp,.bmp,.tiff,.tif"
              >
                <div v-if="!form.idCardFrontUrl" class="upload-placeholder">
                  <el-icon :size="32"><Plus /></el-icon>
                  <p>正面</p>
                </div>
                <img v-else :src="form.idCardFrontUrl" class="preview-image" />
              </el-upload>
            </div>
            
            <div class="id-card-item">
              <p class="label">身份证背面 (国徽面)</p>
              <el-upload
                class="upload-area small"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :data="{ type: 'cert' }"
                :show-file-list="false"
                :before-upload="beforeUpload"
                :on-success="handleIdCardBackUpload"
                accept="image/*,.jpg,.jpeg,.png,.gif,.webp,.bmp,.tiff,.tif"
              >
                <div v-if="!form.idCardBackUrl" class="upload-placeholder">
                  <el-icon :size="32"><Plus /></el-icon>
                  <p>背面</p>
                </div>
                <img v-else :src="form.idCardBackUrl" class="preview-image" />
              </el-upload>
            </div>
          </div>
          
          <div v-if="idCardInfo.name" class="ocr-result">
            <h4>识别结果</h4>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="姓名">{{ idCardInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ idCardInfo.gender }}</el-descriptions-item>
              <el-descriptions-item label="身份证号">{{ maskIdCard(idCardInfo.idNumber) }}</el-descriptions-item>
              <el-descriptions-item label="有效期">{{ idCardInfo.validDate }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
        
        <div class="step-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" :disabled="!form.idCardFrontUrl || !form.idCardBackUrl" @click="nextStep">
            下一步
          </el-button>
        </div>
      </div>
      
      <!-- 步骤3: 确认信息 -->
      <div v-show="currentStep === 2" class="step-content">
        <div class="confirm-section">
          <h3>确认认证信息</h3>
          
          <el-form :model="form" label-width="100px">
            <el-form-item label="真实姓名" required>
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="性别" required>
              <el-radio-group v-model="form.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="学校" required>
              <el-input v-model="form.school" placeholder="请输入学校名称" />
            </el-form-item>
            <el-form-item label="专业" required>
              <el-input v-model="form.major" placeholder="请输入专业" />
            </el-form-item>
            <el-form-item label="学历" required>
              <el-select v-model="form.education" placeholder="请选择学历">
                <el-option label="本科" :value="1" />
                <el-option label="硕士" :value="2" />
                <el-option label="博士" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="入学年份" required>
              <el-date-picker
                v-model="form.enrollYear"
                type="year"
                placeholder="选择入学年份"
                value-format="YYYY"
              />
            </el-form-item>
            <el-form-item label="身份证号" required>
              <el-input v-model="form.idNumber" placeholder="请输入身份证号" />
            </el-form-item>
          </el-form>
          
          <el-checkbox v-model="agreed" class="agreement">
            我已阅读并同意《教员服务协议》和《隐私政策》
          </el-checkbox>
        </div>
        
        <div class="step-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" :loading="submitting" :disabled="!agreed" @click="handleSubmit">
            提交认证
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore, useTutorStore } from '@shared/stores'
import { getCertification, submitCertification } from '@shared/api/tutor'
import { recognizeStudentCard, recognizeIdCardFront, recognizeIdCardBack } from '@shared/api/ocr'

const router = useRouter()
const userStore = useUserStore()
const tutorStore = useTutorStore()

const currentStep = ref(0)
const certStatus = ref(0) // 0待提交 1待审核 2已通过 3已拒绝
const submitting = ref(false)
const agreed = ref(false)

const uploadUrl = '/api/file/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const form = reactive({
  studentCardUrl: '',
  idCardFrontUrl: '',
  idCardBackUrl: '',
  realName: '',
  gender: 1,
  school: '',
  major: '',
  enrollYear: '',
  idNumber: '',
  education: 1,           // 学历：1本科 2硕士 3博士
  certificateUrls: []     // 资质证书URLs
})

const studentCardInfo = reactive({
  name: '',
  studentId: '',
  school: '',
  major: ''
})

const idCardInfo = reactive({
  name: '',
  gender: '',
  idNumber: '',
  validDate: ''
})

const getStatusIcon = () => {
  const map = { 1: 'info', 2: 'success', 3: 'error' }
  return map[certStatus.value] || 'info'
}

const getStatusTitle = () => {
  const map = { 1: '认证审核中', 2: '认证已通过', 3: '认证被拒绝' }
  return map[certStatus.value] || ''
}

const getStatusSubTitle = () => {
  const map = {
    1: '您的认证资料正在审核中，请耐心等待',
    2: '恭喜您已通过认证，可以开始接单啦！',
    3: '很抱歉，您的认证未通过，请检查资料后重新提交'
  }
  return map[certStatus.value] || ''
}

const maskIdCard = (id) => {
  if (!id) return ''
  return id.replace(/^(.{6})(.*)(.{4})$/, '$1********$3')
}

const beforeUpload = (file) => {
  // 允许的图片 MIME 类型
  const allowedImageTypes = [
    'image/jpeg',
    'image/png',
    'image/gif',
    'image/webp',
    'image/bmp',
    'image/x-ms-bmp',
    'image/tiff',
    'image/svg+xml'
  ];
  // 允许的扩展名（备用检查）
  const allowedExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.tiff', '.tif', '.svg', '.pdf'];
  
  const isImage = file.type.startsWith('image/');
  const isAllowedType = allowedImageTypes.includes(file.type) || file.type === 'application/pdf';
  const fileName = file.name.toLowerCase();
  const hasAllowedExtension = allowedExtensions.some(ext => fileName.endsWith(ext));
  const isLt10M = file.size / 1024 / 1024 < 10;
  
  if (!isAllowedType && !hasAllowedExtension) {
    ElMessage.error(`不支持的文件格式。请上传以下格式：${allowedExtensions.join(', ')}`);
    return false;
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB');
    return false;
  }
  return true;
}

const handleStudentCardUpload = async (response) => {
  if (response.code === 200) {
    let imageUrl = null
    if (response.data && typeof response.data === 'string') {
      imageUrl = response.data
      form.studentCardUrl = response.data
    } else if (response.data && response.data.url) {
      imageUrl = response.data.url
      form.studentCardUrl = response.data.url
    } else if (response.url) {
      imageUrl = response.url
      form.studentCardUrl = response.url
    } else {
      return
    }
    
    try {
      const res = await recognizeStudentCard(imageUrl)
      if (res.code === 200 && res.data) {
        Object.assign(studentCardInfo, res.data)
        form.realName = res.data.name || form.realName
        form.school = res.data.school || form.school
        form.major = res.data.major || form.major
      }
    } catch (error) {
      // OCR识别失败
    }
  }
}

const handleIdCardFrontUpload = async (response) => {
  if (response.code === 200) {
    let imageUrl = null
    if (response.data && typeof response.data === 'string') {
      imageUrl = response.data
      form.idCardFrontUrl = response.data
    } else if (response.data && response.data.url) {
      imageUrl = response.data.url
      form.idCardFrontUrl = response.data.url
    } else if (response.url) {
      imageUrl = response.url
      form.idCardFrontUrl = response.url
    } else {
      return
    }
    
    try {
      const res = await recognizeIdCardFront(imageUrl)
      if (res.code === 200 && res.data) {
        Object.assign(idCardInfo, res.data)
        form.realName = res.data.name || form.realName
        form.gender = res.data.gender === '男' ? 1 : 2
        form.idNumber = res.data.idNumber || form.idNumber
      }
    } catch (error) {
      // OCR识别失败
    }
  }
}

const handleIdCardBackUpload = async (response) => {
  if (response.code === 200) {
    let imageUrl = null
    if (response.data && typeof response.data === 'string') {
      imageUrl = response.data
      form.idCardBackUrl = response.data
    } else if (response.data && response.data.url) {
      imageUrl = response.data.url
      form.idCardBackUrl = response.data.url
    } else if (response.url) {
      imageUrl = response.url
      form.idCardBackUrl = response.url
    } else {
      return
    }
    
    try {
      const res = await recognizeIdCardBack(imageUrl)
      if (res.code === 200 && res.data) {
        idCardInfo.validDate = res.data.validDate
      }
    } catch (error) {
      // OCR识别失败
    }
  }
}

const nextStep = () => {
  currentStep.value++
}

const prevStep = () => {
  currentStep.value--
}

const handleSubmit = async () => {
  if (!form.realName || !form.school || !form.idNumber) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  submitting.value = true
  try {
    // 按照后端 TutorCertRequest 的字段名发送数据
    const res = await submitCertification({
      realName: form.realName,
      idCard: form.idNumber,                    // 后端字段名是 idCard
      idCardFrontUrl: form.idCardFrontUrl,
      idCardBackUrl: form.idCardBackUrl,
      universityName: form.school,              // 后端字段名是 universityName
      major: form.major,
      education: form.education || 1,           // 后端必填字段：学历(1本科 2硕士 3博士)
      enrollYear: parseInt(form.enrollYear) || new Date().getFullYear(),  // 后端需要Integer类型
      studentCardUrl: form.studentCardUrl,
      certificateUrls: form.certificateUrls || []  // 可选的资质证书
    })
    
    if (res.code === 200) {
      ElMessage.success('提交成功，等待审核')
      certStatus.value = 1
      tutorStore.setCertStatus(1)
    }
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const resubmit = () => {
  certStatus.value = 0
  currentStep.value = 0
}

const goToResume = () => {
  router.push('/teacher/resume')
}

const loadCertStatus = async () => {
  try {
    const res = await getCertification()
    if (res.code === 200 && res.data) {
      certStatus.value = res.data.certStatus || 0
      tutorStore.setCertStatus(certStatus.value)
      
      if (res.data.realName) {
        // 后端字段名映射到前端表单字段名
        const data = res.data
        form.studentCardUrl = data.studentCardUrl || ''
        form.idCardFrontUrl = data.idCardFrontUrl || ''
        form.idCardBackUrl = data.idCardBackUrl || ''
        form.realName = data.realName || ''
        form.gender = data.gender || 1
        form.school = data.universityName || data.school || ''
        form.major = data.major || ''
        form.enrollYear = data.enrollYear ? String(data.enrollYear) : ''
        form.idNumber = data.idCard || data.idNumber || ''
        form.education = data.education || 1
        form.certificateUrls = data.certificateUrls || []
      }
    }
  } catch (error) {
    // 获取认证状态失败
  }
}

onMounted(() => {
  loadCertStatus()
})
</script>

<style lang="scss" scoped>
.teacher-auth {
  max-width: 800px;
  margin: 0 auto;
  
  .status-card {
    background: #fff;
    border-radius: 12px;
    padding: 48px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
  
  .auth-container {
    background: #fff;
    border-radius: 12px;
    padding: 32px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    
    .steps {
      margin-bottom: 32px;
    }
    
    .step-content {
      .upload-section, .confirm-section {
        h3 {
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 12px;
        }
        
        .tip {
          font-size: 14px;
          color: #909399;
          margin-bottom: 24px;
        }
      }
      
      .upload-area {
        width: 300px;
        height: 200px;
        border: 2px dashed #dcdfe6;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: border-color 0.2s;
        overflow: hidden;
        
        &:hover {
          border-color: #409eff;
        }
        
        &.small {
          width: 200px;
          height: 130px;
        }
        
        .upload-placeholder {
          text-align: center;
          color: #909399;
          
          p {
            margin-top: 8px;
            font-size: 14px;
          }
        }
        
        .preview-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .id-card-uploads {
        display: flex;
        gap: 24px;
        
        .id-card-item {
          .label {
            margin-bottom: 8px;
            font-size: 14px;
            color: #606266;
          }
        }
      }
      
      .ocr-result {
        margin-top: 24px;
        
        h4 {
          font-size: 14px;
          font-weight: 600;
          margin-bottom: 12px;
          color: #67c23a;
        }
      }
      
      .agreement {
        margin-top: 24px;
      }
    }
    
    .step-actions {
      margin-top: 32px;
      display: flex;
      justify-content: center;
      gap: 16px;
    }
  }
}
</style>
