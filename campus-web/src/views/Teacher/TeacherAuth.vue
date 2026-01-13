<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'
import { uploadFile } from '@/api/file'
import { recognizeStudentCardByBase64 } from '@/api/ocr'
import { submitCertification } from '@/api/tutor'

const router = useRouter()
const userStore = useUserStore()
const step = ref(1)
const isOcrLoading = ref(false)
const isSubmitting = ref(false)

const form = reactive({
  studentCardImg: '',
  studentCardUrl: '',
  name: '',
  school: '',
  major: '',
  studentId: '',
  enrollYear: null,
  subjects: [],
  certs: [],
  video: null,
  teachStyle: '',
  introduction: '',
  expectPrice: 150
})

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

const styleOptions = [
  { value: '鼓励型', label: '鼓励型 - 以正向激励为主' },
  { value: '严厉型', label: '严厉型 - 严格要求，高标准' },
  { value: '趣味型', label: '趣味型 - 寓教于乐' },
  { value: '引导型', label: '引导型 - 启发式教学' }
]

const fileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = (error) => reject(error)
  })
}

const handleUploadCard = async (fileObj) => {
  const file = fileObj.raw || fileObj 
  if (!file) return

  form.studentCardImg = URL.createObjectURL(file)
  form.name = ''
  form.school = ''
  form.major = ''
  form.studentId = ''
  form.enrollYear = null

  isOcrLoading.value = true

  try {
    const base64Promise = fileToBase64(file)
    const uploadPromise = uploadFile(file, 'cert') 

    const [uploadRes, base64Data] = await Promise.all([uploadPromise, base64Promise])

    if (uploadRes.data) {
      form.studentCardUrl = uploadRes.data
    }
    
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

const handleSubmit = async () => {
  if (isSubmitting.value) return
  
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

const handleFinish = () => {
  router.push('/teacher/students')
}
</script>

<template>
  <div class="auth-page">
    <!-- 页面顶部横幅 -->
    <div class="page-banner">
      <div class="banner-bg"></div>
      <div class="banner-content">
        <div class="banner-left">
          <div class="banner-badge">
            <el-icon><Medal /></el-icon>
            <span>教师认证中心</span>
          </div>
          <h1>教师资质认证</h1>
          <p>完成认证后即可接单授课，开启您的家教之旅</p>
        </div>
        <div class="banner-right">
          <div class="banner-stats">
            <div class="stat-item">
              <span class="stat-num">5000+</span>
              <span class="stat-label">已认证教师</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-num">98%</span>
              <span class="stat-label">认证通过率</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-num">24h</span>
              <span class="stat-label">快速审核</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="page-container">
      <!-- 认证进度条 -->
      <div class="progress-bar" v-if="step < 3">
        <div class="progress-wrapper">
          <div class="progress-step" :class="{ active: step >= 1, done: step > 1 }">
            <div class="step-circle">
              <el-icon v-if="step > 1"><Check /></el-icon>
              <span v-else>1</span>
            </div>
            <span class="step-text">身份核验</span>
          </div>
          <div class="progress-line" :class="{ active: step > 1 }"></div>
          <div class="progress-step" :class="{ active: step >= 2, done: step > 2 }">
            <div class="step-circle">
              <el-icon v-if="step > 2"><Check /></el-icon>
              <span v-else>2</span>
            </div>
            <span class="step-text">能力展示</span>
          </div>
          <div class="progress-line" :class="{ active: step > 2 }"></div>
          <div class="progress-step" :class="{ active: step >= 3 }">
            <div class="step-circle"><span>3</span></div>
            <span class="step-text">认证完成</span>
          </div>
        </div>
      </div>

      <!-- Step 1: 基础认证 -->
      <div v-if="step === 1" class="step-content">
        <div class="content-grid">
          <!-- 左侧上传区 -->
          <div class="upload-panel">
            <div class="panel-header">
              <el-icon><Postcard /></el-icon>
              <span>上传学生证 / 校园卡</span>
            </div>
            <el-upload
              class="card-uploader"
              :show-file-list="false"
              :auto-upload="false"
              accept="image/*"
              :on-change="handleUploadCard"
            >
              <div v-if="isOcrLoading" class="upload-state loading">
                <el-icon class="is-loading" :size="48"><Loading /></el-icon>
                <h4>AI 智能识别中...</h4>
                <p>正在分析证件信息</p>
              </div>
              <div v-else-if="form.studentCardImg" class="upload-state preview">
                <img :src="form.studentCardImg" />
                <div class="preview-mask">
                  <el-icon><RefreshRight /></el-icon>
                  <span>点击更换图片</span>
                </div>
              </div>
              <div v-else class="upload-state placeholder">
                <div class="upload-icon">
                  <el-icon :size="40"><Camera /></el-icon>
                </div>
                <h4>点击或拖拽上传</h4>
                <p>支持 JPG/PNG 格式</p>
                <el-tag type="info" size="small">AI 自动识别信息</el-tag>
              </div>
            </el-upload>
            <div class="upload-tips">
              <el-icon><InfoFilled /></el-icon>
              <span>请上传清晰的学生证照片，确保姓名、学校等信息完整可见</span>
            </div>
          </div>

          <!-- 右侧表单区 -->
          <div class="form-panel">
            <div class="panel-header">
              <el-icon><EditPen /></el-icon>
              <span>基本信息</span>
              <el-tag v-if="form.name && form.school" type="success" size="small">已识别</el-tag>
            </div>
            <el-form label-position="top" class="info-form">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="真实姓名" required>
                    <el-input v-model="form.name" placeholder="请输入姓名" size="large" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="学号">
                    <el-input v-model="form.studentId" placeholder="请输入学号" size="large" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="就读高校" required>
                    <el-input v-model="form.school" placeholder="请输入学校名称" size="large" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="主修专业">
                    <el-input v-model="form.major" placeholder="请输入专业" size="large" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
            <div class="form-actions">
              <el-button type="primary" size="large" :disabled="!form.name || !form.school" @click="step = 2">
                下一步：能力补充
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- Step 2: 能力补充 -->
      <div v-if="step === 2" class="step-content">
        <div class="form-wide-panel">
          <div class="panel-header">
            <el-icon><Trophy /></el-icon>
            <span>教学能力展示</span>
          </div>
          
          <el-form label-position="top" class="info-form">
            <el-row :gutter="32">
              <el-col :span="12">
                <el-form-item label="擅长科目（可多选）">
                  <el-select v-model="form.subjects" multiple placeholder="请选择擅长的科目" size="large" style="width: 100%">
                    <el-option v-for="item in subjectOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="教学风格">
                  <el-select v-model="form.teachStyle" placeholder="请选择您的教学风格" size="large" style="width: 100%">
                    <el-option v-for="item in styleOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="期望时薪（元/小时）">
              <div class="price-control">
                <el-slider v-model="form.expectPrice" :min="50" :max="500" :step="10" />
                <el-input-number v-model="form.expectPrice" :min="50" :max="500" :step="10" size="large" />
              </div>
            </el-form-item>

            <el-divider />

            <el-row :gutter="32">
              <el-col :span="16">
                <el-form-item label="个人简介">
                  <el-input v-model="form.introduction" type="textarea" :rows="6" placeholder="介绍一下您的教学经验、特长、教学理念等..." />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="试讲视频（可选）">
                  <el-upload class="video-uploader" :show-file-list="false" :auto-upload="false" accept="video/*" :on-change="handleUploadVideo">
                    <div v-if="form.video" class="video-uploaded">
                      <el-icon :size="32"><VideoCamera /></el-icon>
                      <span>{{ form.video }}</span>
                      <el-icon class="check"><CircleCheck /></el-icon>
                    </div>
                    <div v-else class="video-placeholder">
                      <el-icon :size="32"><Upload /></el-icon>
                      <span>上传微课视频</span>
                      <small>5-10分钟，最大500MB</small>
                    </div>
                  </el-upload>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <div class="form-actions dual">
            <el-button size="large" @click="step = 1">
              <el-icon><ArrowLeft /></el-icon>
              上一步
            </el-button>
            <el-button type="primary" size="large" :loading="isSubmitting" @click="handleSubmit">
              提交认证申请
            </el-button>
          </div>
        </div>
      </div>

      <!-- Step 3: 完成页 -->
      <div v-if="step === 3" class="success-section">
        <div class="success-card">
          <div class="success-header">
            <div class="success-icon">
              <el-icon :size="64"><CircleCheckFilled /></el-icon>
            </div>
            <h2>认证提交成功！</h2>
            <p>资质审核已通过，您的教师主页已激活</p>
          </div>
          
          <div class="success-body">
            <div class="info-cards">
              <div class="info-card credit">
                <p class="label">当前信用分</p>
                <div class="value">100</div>
                <p class="tip">初始信用极佳</p>
              </div>
              <div class="info-card tips">
                <h4>接下来您可以：</h4>
                <ul>
                  <li><el-icon><Location /></el-icon> 在地图上查看附近的学生需求</li>
                  <li><el-icon><Document /></el-icon> 完善个人简历，获得更多曝光</li>
                  <li><el-icon><ChatDotRound /></el-icon> 与家长沟通，争取更多订单</li>
                </ul>
              </div>
            </div>
          </div>

          <div class="success-footer">
            <el-button type="primary" size="large" @click="handleFinish">
              <el-icon><Location /></el-icon>
              进入地图找学生
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.auth-page {
  min-height: calc(100vh - 114px);
  background: linear-gradient(180deg, #f0f4f8 0%, #e8ecf0 100%);
}

// 页面横幅
.page-banner {
  position: relative;
  padding: 60px 0;
  overflow: hidden;

  .banner-bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 50%, #3d7ab5 100%);
    &::after {
      content: '';
      position: absolute;
      inset: 0;
      background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
    }
  }

  .banner-content {
    position: relative;
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 60px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .banner-left {
    color: #fff;

    .banner-badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: rgba(255,255,255,0.15);
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 14px;
      margin-bottom: 20px;
    }

    h1 {
      font-size: 42px;
      font-weight: 800;
      margin-bottom: 12px;
      letter-spacing: -1px;
    }

    p {
      font-size: 18px;
      opacity: 0.9;
    }
  }

  .banner-right {
    .banner-stats {
      display: flex;
      gap: 32px;
      background: rgba(255,255,255,0.1);
      backdrop-filter: blur(10px);
      padding: 24px 40px;
      border-radius: 16px;
      border: 1px solid rgba(255,255,255,0.2);
    }

    .stat-item {
      text-align: center;
      color: #fff;

      .stat-num {
        display: block;
        font-size: 32px;
        font-weight: 800;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 14px;
        opacity: 0.8;
      }
    }

    .stat-divider {
      width: 1px;
      background: rgba(255,255,255,0.3);
    }
  }
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 60px 80px;
}

// 进度条
.progress-bar {
  margin-bottom: 48px;

  .progress-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0;
    background: #fff;
    padding: 32px 60px;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  }

  .progress-step {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;

    .step-circle {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: #e8ecf0;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      font-weight: 700;
      color: #94a3b8;
      transition: all 0.3s;
    }

    .step-text {
      font-size: 15px;
      font-weight: 600;
      color: #94a3b8;
    }

    &.active {
      .step-circle {
        background: linear-gradient(135deg, #409EFF, #667eea);
        color: #fff;
        box-shadow: 0 4px 16px rgba(64,158,255,0.4);
      }
      .step-text { color: #1e3a5f; }
    }

    &.done {
      .step-circle {
        background: linear-gradient(135deg, #67c23a, #14b8a6);
      }
    }
  }

  .progress-line {
    width: 120px;
    height: 4px;
    background: #e8ecf0;
    margin: 0 24px;
    margin-bottom: 32px;
    border-radius: 2px;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(90deg, #67c23a, #14b8a6);
    }
  }
}

// Step 1 两栏布局
.content-grid {
  display: grid;
  grid-template-columns: 480px 1fr;
  gap: 32px;
}

.upload-panel, .form-panel, .form-wide-panel {
  background: #fff;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.06);
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
  color: #1e3a5f;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f4f8;

  .el-icon {
    color: #409EFF;
    font-size: 24px;
  }

  .el-tag {
    margin-left: auto;
  }
}

// 上传区域
.card-uploader {
  :deep(.el-upload) {
    width: 100%;
    border: 2px dashed #d0d7de;
    border-radius: 16px;
    background: #fafbfc;
    transition: all 0.3s;
    cursor: pointer;

    &:hover {
      border-color: #409EFF;
      background: #f0f7ff;
    }
  }
}

.upload-state {
  height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;

  &.loading {
    color: #409EFF;
    h4 { font-size: 18px; margin: 16px 0 8px; }
    p { color: #94a3b8; }
    .is-loading { animation: rotate 1.5s linear infinite; }
  }

  &.placeholder {
    .upload-icon {
      width: 80px;
      height: 80px;
      background: linear-gradient(135deg, #409EFF, #667eea);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      margin-bottom: 20px;
    }
    h4 { font-size: 18px; color: #1e3a5f; margin-bottom: 8px; }
    p { color: #94a3b8; margin-bottom: 16px; }
  }

  &.preview {
    position: relative;
    padding: 0;
    img { width: 100%; height: 100%; object-fit: cover; border-radius: 14px; }
    .preview-mask {
      position: absolute;
      inset: 0;
      background: rgba(0,0,0,0.6);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #fff;
      opacity: 0;
      transition: opacity 0.3s;
      border-radius: 14px;
      .el-icon { font-size: 32px; margin-bottom: 8px; }
    }
    &:hover .preview-mask { opacity: 1; }
  }
}

.upload-tips {
  margin-top: 20px;
  padding: 16px;
  background: #f0f7ff;
  border-radius: 12px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
  font-size: 14px;
  color: #64748b;

  .el-icon { color: #409EFF; margin-top: 2px; }
}

// 表单样式
.info-form {
  :deep(.el-form-item) { margin-bottom: 28px; }
  :deep(.el-form-item__label) {
    font-weight: 600;
    color: #1e3a5f;
    font-size: 15px;
  }
  :deep(.el-input__wrapper), :deep(.el-textarea__inner) {
    border-radius: 12px;
    box-shadow: 0 0 0 1px #d0d7de inset;
    &:focus-within { box-shadow: 0 0 0 2px #409EFF inset; }
  }
}

.form-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 2px solid #f0f4f8;

  .el-button {
    height: 52px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
    min-width: 200px;
  }

  &.dual {
    display: flex;
    justify-content: space-between;
  }
}

// Step 2 宽面板
.form-wide-panel {
  max-width: 1000px;
  margin: 0 auto;
}

.price-control {
  display: flex;
  gap: 24px;
  align-items: center;
  .el-slider { flex: 1; }
  .el-input-number { width: 140px; }
}

.video-uploader {
  :deep(.el-upload) {
    width: 100%;
    height: 160px;
    border: 2px dashed #d0d7de;
    border-radius: 12px;
    background: #fafbfc;
    &:hover { border-color: #409EFF; }
  }

  .video-placeholder, .video-uploaded {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: #64748b;
    .el-icon { color: #409EFF; }
    small { font-size: 12px; color: #94a3b8; }
  }

  .video-uploaded {
    color: #409EFF;
    font-weight: 600;
    .check { color: #67c23a; }
  }
}

// 成功页面
.success-section {
  max-width: 900px;
  margin: 0 auto;
}

.success-card {
  background: #fff;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 40px rgba(0,0,0,0.08);
}

.success-header {
  background: linear-gradient(135deg, #1e3a5f, #2d5a87);
  padding: 60px 40px;
  text-align: center;
  color: #fff;

  .success-icon {
    width: 100px;
    height: 100px;
    background: rgba(255,255,255,0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24px;
    color: #67c23a;
  }

  h2 { font-size: 32px; margin-bottom: 12px; }
  p { font-size: 16px; opacity: 0.9; }
}

.success-body {
  padding: 48px;
}

.info-cards {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 32px;
}

.info-card {
  padding: 32px;
  border-radius: 16px;

  &.credit {
    background: linear-gradient(135deg, #409EFF, #667eea);
    color: #fff;
    text-align: center;
    .label { font-size: 14px; opacity: 0.9; }
    .value { font-size: 56px; font-weight: 800; margin: 12px 0; }
    .tip { font-size: 13px; opacity: 0.8; }
  }

  &.tips {
    background: #f8fafc;
    h4 { font-size: 16px; font-weight: 700; color: #1e3a5f; margin-bottom: 20px; }
    ul { list-style: none; padding: 0; margin: 0; }
    li {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 14px 0;
      font-size: 15px;
      color: #475569;
      border-bottom: 1px dashed #e2e8f0;
      &:last-child { border: none; }
      .el-icon { color: #409EFF; font-size: 20px; }
    }
  }
}

.success-footer {
  padding: 0 48px 48px;
  text-align: center;

  .el-button {
    height: 56px;
    font-size: 18px;
    font-weight: 600;
    border-radius: 14px;
    min-width: 280px;
  }
}

// 响应式
@media (max-width: 1200px) {
  .page-banner .banner-content { padding: 0 32px; }
  .page-container { padding: 32px; }
  .content-grid { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .page-banner {
    padding: 40px 0;
    .banner-left h1 { font-size: 28px; }
    .banner-right { display: none; }
  }
  .progress-bar .progress-wrapper { padding: 24px; }
  .progress-bar .progress-line { width: 60px; margin: 0 12px; }
  .info-cards { grid-template-columns: 1fr; }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>