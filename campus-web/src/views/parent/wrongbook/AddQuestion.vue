<template>
  <div class="add-question-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">添加错题</h1>
    </div>
    
    <div class="upload-section">
      <h3 class="section-title">上传错题图片</h3>
      <el-upload
        class="image-uploader"
        :show-file-list="false"
        accept="image/*"
        :before-upload="handleUpload"
      >
        <div v-if="imageUrl" class="preview-image">
          <el-image :src="imageUrl" fit="contain" />
          <div class="image-actions">
            <el-button size="small" @click.stop="recognizeText">
              <el-icon><View /></el-icon>
              识别文字
            </el-button>
            <el-button size="small" type="danger" @click.stop="imageUrl = ''">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
        <div v-else class="upload-placeholder">
          <el-icon class="upload-icon"><Plus /></el-icon>
          <div class="upload-text">点击上传错题图片</div>
          <div class="upload-tip">支持拍照或从相册选择</div>
        </div>
      </el-upload>
    </div>
    
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="选择学生" prop="studentId">
        <el-select v-model="form.studentId" placeholder="请选择学生" style="width: 100%">
          <el-option
            v-for="s in students"
            :key="s.id"
            :label="s.name"
            :value="s.id"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="科目" prop="subject">
        <el-select v-model="form.subject" placeholder="请选择科目" style="width: 100%">
          <el-option label="钢琴/乐器陪练" value="钢琴/乐器陪练" />
          <el-option label="美术/书法" value="美术/书法" />
          <el-option label="声乐/视唱练耳" value="声乐/视唱练耳" />
          <el-option label="中考体育专项" value="中考体育专项" />
          <el-option label="羽毛球/网球陪练" value="羽毛球/网球陪练" />
          <el-option label="篮球/足球指导" value="篮球/足球指导" />
          <el-option label="少儿编程(Scratch/Python)" value="少儿编程(Scratch/Python)" />
          <el-option label="机器人/3D打印" value="机器人/3D打印" />
          <el-option label="科学实验/航模" value="科学实验/航模" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="题目内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="请输入或粘贴题目内容，也可以通过OCR识别"
        />
        <div v-if="recognizing" class="recognizing-tip">
          <el-icon class="is-loading"><Loading /></el-icon>
          正在识别中...
        </div>
      </el-form-item>
      
      <el-form-item label="错误原因">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="2"
          placeholder="选填，记录错误原因便于复习"
        />
      </el-form-item>
      
      <el-form-item label="正确答案">
        <el-input
          v-model="form.answer"
          type="textarea"
          :rows="3"
          placeholder="选填，记录正确答案"
        />
      </el-form-item>
      
      <el-form-item label="知识点标签">
        <el-select
          v-model="form.tags"
          multiple
          filterable
          allow-create
          placeholder="添加知识点标签"
          style="width: 100%"
        >
          <el-option label="函数" value="函数" />
          <el-option label="方程" value="方程" />
          <el-option label="几何" value="几何" />
          <el-option label="阅读理解" value="阅读理解" />
          <el-option label="语法" value="语法" />
        </el-select>
      </el-form-item>
    </el-form>
    
    <div class="action-section">
      <el-button size="large" @click="goBack">取消</el-button>
      <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
        保存错题
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, View, Delete, Loading } from '@element-plus/icons-vue'
import { addWrongQuestion } from '@shared/api/wrongbook'
import { getStudentList } from '@shared/api/parent'
import { uploadFile } from '@shared/api/file'
import { recognizeGeneral } from '@shared/api/ocr'

const router = useRouter()

const formRef = ref(null)
const students = ref([])
const imageUrl = ref('')
const recognizing = ref(false)
const submitting = ref(false)

const form = ref({
  studentId: '',
  subject: '',
  content: '',
  reason: '',
  answer: '',
  tags: []
})

const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  subject: [{ required: true, message: '请选择科目', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }]
}

const goBack = () => router.back()

const loadStudents = async () => {
  try {
    const res = await getStudentList()
    if (res.code === 200) {
      students.value = res.data || []
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
  }
}

const handleUpload = async (file) => {
  try {
    const res = await uploadFile(file)
    if (res.code === 200) {
      imageUrl.value = res.data.url
      ElMessage.success('上传成功')
    }
  } catch (error) {
    ElMessage.error('上传失败')
  }
  return false
}

const recognizeText = async () => {
  if (!imageUrl.value) {
    ElMessage.warning('请先上传图片')
    return
  }
  
  recognizing.value = true
  try {
    const res = await recognizeGeneral(imageUrl.value)
    if (res.code === 200 && res.data?.text) {
      form.value.content = res.data.text
      ElMessage.success('识别成功')
    } else {
      ElMessage.warning('未能识别到文字')
    }
  } catch (error) {
    console.error('OCR识别失败:', error)
    ElMessage.error('识别失败')
  } finally {
    recognizing.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    submitting.value = true
    const data = {
      ...form.value,
      imageUrl: imageUrl.value
    }
    
    const res = await addWrongQuestion(data)
    if (res.code === 200) {
      ElMessage.success('添加成功')
      router.back()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('添加失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadStudents()
})
</script>

<style lang="scss" scoped>
.add-question-page {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
  padding-bottom: 100px;
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

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px;
}

.upload-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .image-uploader {
    width: 100%;
  }
  
  .upload-placeholder {
    border: 2px dashed #dcdfe6;
    border-radius: 8px;
    padding: 40px;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      border-color: #409eff;
    }
    
    .upload-icon {
      font-size: 48px;
      color: #c0c4cc;
    }
    
    .upload-text {
      margin-top: 12px;
      font-size: 14px;
      color: #606266;
    }
    
    .upload-tip {
      margin-top: 4px;
      font-size: 12px;
      color: #909399;
    }
  }
  
  .preview-image {
    .el-image {
      width: 100%;
      max-height: 300px;
      border-radius: 8px;
    }
    
    .image-actions {
      display: flex;
      justify-content: center;
      gap: 12px;
      margin-top: 12px;
    }
  }
}

.el-form {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.recognizing-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  font-size: 13px;
  color: #409eff;
}

.action-section {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  
  .el-button {
    min-width: 120px;
  }
}
</style>
