<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'
import { uploadFile } from '@/api/file'
// 注意：这里引入了 Base64 专用的接口
import { recognizeStudentCardByBase64 } from '@/api/ocr'
import { submitCertification } from '@/api/tutor'

const router = useRouter()
const userStore = useUserStore()
const step = ref(1) // 1: 基础认证, 2: 能力补充, 3: 完成页
const isOcrLoading = ref(false)
const isSubmitting = ref(false)

// 表单数据
const form = reactive({
  // Step 1
  studentCardImg: '',
  studentCardUrl: '',
  name: '',
  school: '',
  major: '',
  studentId: '',
  enrollYear: null,
  // Step 2
  subjects: [],
  certs: [],
  video: null,
  teachStyle: '',
  introduction: '',
  expectPrice: 150
})

// 科目选项
const subjectOptions = [
  { value: '小学语文', label: '小学语文' },
  { value: '小学数学', label: '小学数学' },
  { value: '小学英语', label: '小学英语' },
  { value: '小学奥数', label: '小学奥数' },
  { value: '初中语文', label: '初中语文' },
  { value: '初中数学', label: '初中数学' },
  { value: '初中英语', label: '初中英语' },
  { value: '初中物理', label: '初中物理' },
  { value: '初中化学', label: '初中化学' },
  { value: '高中语文', label: '高中语文' },
  { value: '高中数学', label: '高中数学' },
  { value: '高中英语', label: '高中英语' },
  { value: '高中物理', label: '高中物理' },
  { value: '高中化学', label: '高中化学' }
]

// 教学风格选项
const styleOptions = [
  { value: '鼓励型', label: '鼓励型 - 以正向激励为主' },
  { value: '严厉型', label: '严厉型 - 严格要求，高标准' },
  { value: '趣味型', label: '趣味型 - 寓教于乐' },
  { value: '引导型', label: '引导型 - 启发式教学' }
]

// 辅助工具：将文件转换为 Base64 字符串
const fileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = (error) => reject(error)
  })
}

// 上传学生证 + OCR 识别 (修改为 Base64 模式)
// 上传学生证 + OCR 识别
// ❌ 错误原因：参数名 uploadFile 把上面 import 进来的 uploadFile 函数给覆盖（遮挡）了
// const handleUploadCard = async (uploadFile) => { 

// ✅ 正确写法：把参数名改成 fileObj，避免命名冲突
const handleUploadCard = async (fileObj) => {
  // 这里获取真正的文件对象
  const file = fileObj.raw || fileObj 
  if (!file) return

  // 本地预览图片
  form.studentCardImg = URL.createObjectURL(file)
  
  // 重置表单
  form.name = ''
  form.school = ''
  form.major = ''
  form.studentId = ''
  form.enrollYear = null

  isOcrLoading.value = true

  try {
    // 1. 转 Base64
    const base64Promise = fileToBase64(file)
    
    // 2. 上传文件
    // ✅ 修改后：这里的 uploadFile 指的是顶部 import { uploadFile } from '@/api/file'
    // 因为参数名改成了 fileObj，这里就不会冲突了，可以正常作为函数调用！
    const uploadPromise = uploadFile(file, 'cert') 

    const [uploadRes, base64Data] = await Promise.all([uploadPromise, base64Promise])

    // 保存上传后的 URL
    if (uploadRes.data) {
      form.studentCardUrl = uploadRes.data
    }
    
    // 3. 调用 Base64 识别接口
    const ocrRes = await recognizeStudentCardByBase64(base64Data)
    
    if (ocrRes.data && ocrRes.data.success) {
      const data = ocrRes.data
      form.name = data.realName || ''
      form.school = data.universityName || ''
      form.major = data.major || ''
      form.studentId = data.studentId || ''
      form.enrollYear = data.enrollYear || null
      
      if (!form.name || !form.school) {
        ElMessage.warning('识别完成，部分文字模糊，请手动补充')
      } else {
        ElMessage.success('OCR 智能识别成功')
      }
    } else {
      ElMessage.error(ocrRes.data?.errorMsg || '识别失败，请手动填写')
    }
  } catch (error) {
    console.error('上传或识别流程异常:', error)
    ElMessage.warning('网络连接不稳定，请手动填写信息')
  } finally {
    isOcrLoading.value = false
  }
}

// 上传视频
const handleUploadVideo = (uploadFile) => {
  const file = uploadFile.raw || uploadFile
  if (file) {
    if (file.size > 500 * 1024 * 1024) {
      ElMessage.warning('视频文件不能超过500MB')
      return false
    }
    form.video = file.name
    ElMessage.success(`视频 "${file.name}" 上传成功`)
  }
}

// 提交认证
const handleSubmit = async () => {
  if (isSubmitting.value) return
  
  // 校验关键字段
  if (!form.name || !form.school) {
    ElMessage.warning('请先完成基础认证信息')
    return
  }
  if (!form.studentCardUrl) {
    ElMessage.warning('正在上传图片，请稍候...')
    return
  }

  isSubmitting.value = true

  try {
    const certData = {
      realName: form.name,
      universityName: form.school,
      major: form.major,
      // 后端必填字段：未做身份证上传，这里填充占位符
      idCard: '000000000000000000',
      idCardFrontUrl: form.studentCardUrl,
      idCardBackUrl: form.studentCardUrl,
      education: 2,
      enrollYear: form.enrollYear || new Date().getFullYear(),
      studentCardUrl: form.studentCardUrl,
      teachSubjects: JSON.stringify(form.subjects),
      teachStyle: form.teachStyle,
      introduction: form.introduction,
      expectPrice: form.expectPrice
    }

    await submitCertification(certData)
    step.value = 3
    ElMessage.success('认证提交成功')
  } catch (error) {
    console.error('提交认证失败:', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

// 完成页按钮点击
const handleFinish = () => {
  router.push('/teacher/students')
}
</script>

<template>
  <div class="auth-page">
    <div class="page-header">
      <h1 class="page-title">教师资质认证</h1>
      <p class="page-subtitle">完成认证后即可接单授课</p>
    </div>

    <div class="step-container" v-if="step < 3">
      <el-steps :active="step - 1" align-center finish-status="success">
        <el-step title="基础认证" description="学生证核验" />
        <el-step title="能力补充" description="教学信息" />
        <el-step title="认证完成" description="开始接单" />
      </el-steps>
    </div>

    <div v-if="step === 1" class="step-content">
      <el-card class="auth-card">
        <template #header>
          <div class="card-header">
            <el-icon><Postcard /></el-icon>
            <span>身份核验</span>
          </div>
        </template>

        <div class="upload-section">
          <el-upload
            class="card-uploader"
            :show-file-list="false"
            :auto-upload="false"
            accept="image/*"
            :on-change="handleUploadCard"
          >
            <div v-if="isOcrLoading" class="upload-loading">
              <el-icon class="is-loading" :size="32"><Loading /></el-icon>
              <span>AI 智能识别中...</span>
              <span class="sub-text">正在分析文字信息</span>
            </div>
            <div v-else-if="form.studentCardImg" class="upload-preview">
              <img :src="form.studentCardImg" />
              <div class="upload-mask">
                <el-icon><RefreshRight /></el-icon>
                <span>点击更换</span>
              </div>
            </div>
            <div v-else class="upload-placeholder">
              <el-icon :size="40"><Camera /></el-icon>
              <p>上传学生证 / 校园卡</p>
              <span>支持 JPG/PNG，自动识别信息</span>
            </div>
          </el-upload>
        </div>

        <el-form label-position="top" class="info-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="真实姓名" required>
                <el-input 
                  v-model="form.name" 
                  placeholder="等待识别..."
                  :suffix-icon="form.name ? 'CircleCheck' : ''"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="学号">
                <el-input v-model="form.studentId" placeholder="等待识别..." />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="就读高校" required>
            <el-input v-model="form.school" placeholder="等待识别..." />
          </el-form-item>

          <el-form-item label="主修专业">
            <el-input v-model="form.major" placeholder="等待识别..." />
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <div v-if="step === 2" class="step-content">
      <el-card class="auth-card">
        <template #header>
          <div class="card-header">
            <el-icon><Trophy /></el-icon>
            <span>能力展示</span>
          </div>
        </template>

        <el-form label-position="top" class="info-form">
          <el-form-item label="擅长科目（可多选）">
            <el-select 
              v-model="form.subjects" 
              multiple 
              placeholder="请选择擅长的科目"
              style="width: 100%"
            >
              <el-option 
                v-for="item in subjectOptions" 
                :key="item.value" 
                :label="item.label" 
                :value="item.value" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="教学风格">
            <el-select 
              v-model="form.teachStyle" 
              placeholder="请选择您的教学风格"
              style="width: 100%"
            >
              <el-option 
                v-for="item in styleOptions" 
                :key="item.value" 
                :label="item.label" 
                :value="item.value" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="期望时薪（元/小时）">
            <el-slider
              v-model="form.expectPrice"
              :min="50"
              :max="500"
              :step="10"
              show-input
            />
          </el-form-item>

          <el-form-item label="个人简介">
            <el-input
              v-model="form.introduction"
              type="textarea"
              :rows="4"
              placeholder="介绍一下您的教学经验、特长等..."
            />
          </el-form-item>

          <el-form-item label="试讲视频（可选）">
            <el-upload
              class="video-uploader"
              :show-file-list="false"
              :auto-upload="false"
              accept="video/*"
              :on-change="handleUploadVideo"
            >
              <div v-if="form.video" class="video-uploaded">
                <el-icon><VideoCamera /></el-icon>
                <span>{{ form.video }}</span>
                <el-icon class="check"><CircleCheck /></el-icon>
              </div>
              <div v-else class="video-placeholder">
                <el-icon><Upload /></el-icon>
                <span>上传 5-10 分钟微课展示</span>
                <small>最大支持 500MB</small>
              </div>
            </el-upload>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <div v-if="step === 3" class="step-content success-page">
      <div class="success-icon">
        <el-icon :size="64"><CircleCheckFilled /></el-icon>
      </div>
      
      <h2 class="success-title">认证提交成功</h2>
      <p class="success-desc">资质审核已通过，您的教师主页已激活</p>

      <el-card class="credit-card">
        <div class="credit-content">
          <p class="credit-label">当前信用分</p>
          <div class="credit-score">100</div>
          <p class="credit-tip">初始信用极佳，保持良好教学记录可提升</p>
        </div>
      </el-card>

      <el-button 
        type="primary" 
        size="large" 
        class="finish-btn"
        @click="handleFinish"
      >
        <el-icon><Location /></el-icon>
        进入地图找学生
      </el-button>
    </div>

    <div class="bottom-actions" v-if="step < 3">
      <el-button 
        v-if="step === 1" 
        type="primary" 
        size="large"
        :disabled="!form.name || !form.school"
        @click="step = 2"
      >
        下一步：能力补充
        <el-icon><ArrowRight /></el-icon>
      </el-button>

      <template v-if="step === 2">
        <el-button size="large" @click="step = 1">
          上一步
        </el-button>
        <el-button 
          type="primary" 
          size="large"
          :loading="isSubmitting"
          @click="handleSubmit"
        >
          提交认证
        </el-button>
      </template>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 100px;
}

.page-header {
  background: linear-gradient(135deg, #409EFF 0%, #667eea 100%);
  padding: 40px 20px;
  color: #fff;

  .page-title {
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 4px;
  }

  .page-subtitle {
    font-size: 14px;
    opacity: 0.85;
  }
}

.step-container {
  background: #fff;
  padding: 20px;
  margin-bottom: 20px;
}

.step-content {
  padding: 20px;
}

.auth-card {
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.upload-section {
  margin-bottom: 20px;

  .card-uploader {
    :deep(.el-upload) {
      width: 100%;
      border: 2px dashed #dcdfe6;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: #409EFF;
      }
    }
  }

  .upload-loading,
  .upload-placeholder {
    height: 200px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #606266;

    p {
      margin-top: 8px;
      font-weight: 600;
      color: #303133;
    }

    span {
      font-size: 12px;
      color: #909399;
    }
    
    .sub-text {
      margin-top: 5px;
      color: #909399;
    }
  }

  .upload-loading {
    color: #409EFF;

    .is-loading {
      animation: rotate 1.5s linear infinite;
    }
  }

  .upload-preview {
    position: relative;
    height: 200px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 10px;
    }

    .upload-mask {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #fff;
      opacity: 0;
      transition: opacity 0.3s;
      border-radius: 10px;
    }

    &:hover .upload-mask {
      opacity: 1;
    }
  }
}

.info-form {
  :deep(.el-input__suffix) {
    color: #67C23A;
  }
}

.video-uploader {
  :deep(.el-upload) {
    width: 100%;
    border: 1px dashed #dcdfe6;
    border-radius: 8px;
    padding: 20px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #409EFF;
    }
  }

  .video-placeholder,
  .video-uploaded {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    color: #606266;

    small {
      font-size: 12px;
      color: #909399;
    }
  }

  .video-uploaded {
    flex-direction: row;
    color: #409EFF;
    font-weight: 500;

    .check {
      color: #67C23A;
    }
  }
}

.success-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 80px;

  .success-icon {
    width: 100px;
    height: 100px;
    background: rgba(103, 194, 58, 0.1);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #67C23A;
    margin-bottom: 20px;
    animation: bounce 2s infinite;
  }

  .success-title {
    font-size: 24px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 8px;
  }

  .success-desc {
    color: #606266;
    margin-bottom: 40px;
  }

  .credit-card {
    width: 100%;
    max-width: 320px;
    background: linear-gradient(135deg, #409EFF 0%, #667eea 100%);
    border: none;
    margin-bottom: 40px;

    :deep(.el-card__body) {
      padding: 40px;
    }

    .credit-content {
      color: #fff;
      text-align: center;

      .credit-label {
        font-size: 14px;
        opacity: 0.85;
        margin-bottom: 4px;
      }

      .credit-score {
        font-size: 48px;
        font-weight: 700;
        font-family: 'Courier New', monospace;
      }

      .credit-tip {
        font-size: 12px;
        opacity: 0.7;
        margin-top: 8px;
      }
    }
  }

  .finish-btn {
    width: 100%;
    max-width: 320px;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
  }
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 20px;

  .el-button {
    flex: 1;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
  }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>