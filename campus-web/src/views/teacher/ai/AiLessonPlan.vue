<template>
  <div class="lesson-plan-page">
    <el-page-header @back="$router.push('/teacher/ai/hub')">
      <template #content>AI 课程规划</template>
    </el-page-header>

    <div class="plan-container">
      <el-form :model="form" label-width="100px" class="plan-form">
        <el-form-item label="科目" required>
          <el-select v-model="form.subject" placeholder="选择科目">
            <el-option v-for="s in subjects" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生水平" required>
          <el-select v-model="form.studentLevel" placeholder="选择水平">
            <el-option label="基础薄弱" value="beginner" />
            <el-option label="中等水平" value="intermediate" />
            <el-option label="成绩优秀" value="advanced" />
          </el-select>
        </el-form-item>
        <el-form-item label="总课时数" required>
          <el-input-number v-model="form.totalHours" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="学生信息">
          <el-input
            v-model="form.studentInfo"
            type="textarea"
            :rows="3"
            placeholder="年级、薄弱环节、学习目标等（选填）"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="generate">
            生成教学计划
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="result" class="plan-result">
        <h3>教学计划</h3>
        <div class="markdown-body" v-html="renderedResult"></div>
        <el-button class="copy-btn" @click="copyResult">复制内容</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { generateLessonPlan } from '@shared/api/llm'

const subjects = ['钢琴/乐器陪练', '美术/书法', '声乐/视唱练耳', '中考体育专项', '羽毛球/网球陪练', '篮球/足球指导', '少儿编程(Scratch/Python)', '机器人/3D打印', '科学实验/航模']

const form = ref({
  subject: '',
  studentLevel: '',
  totalHours: 10,
  studentInfo: ''
})

const loading = ref(false)
const result = ref('')

const renderedResult = computed(() => {
  if (!result.value) return ''
  return result.value
    .replace(/\n/g, '<br>')
    .replace(/### (.+)/g, '<h4>$1</h4>')
    .replace(/## (.+)/g, '<h3>$1</h3>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
})

const generate = async () => {
  if (!form.value.subject || !form.value.studentLevel) {
    ElMessage.warning('请填写科目和学生水平')
    return
  }
  loading.value = true
  try {
    const res = await generateLessonPlan(form.value)
    if (res.data?.code === 200) {
      result.value = res.data.data
    } else {
      result.value = res.data?.data || res.data?.msg || '生成失败'
    }
  } catch (e) {
    ElMessage.error('生成失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const copyResult = () => {
  navigator.clipboard.writeText(result.value).then(() => {
    ElMessage.success('已复制到剪贴板')
  })
}
</script>

<style lang="scss" scoped>
.lesson-plan-page {
  max-width: 800px;
  margin: 0 auto;

  .plan-container {
    margin-top: 24px;
  }

  .plan-form {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 20px;
  }

  .plan-result {
    background: #fff;
    border-radius: 12px;
    padding: 24px;

    h3 {
      margin-bottom: 16px;
      font-size: 18px;
    }

    .markdown-body {
      line-height: 1.8;
      color: #303133;
      font-size: 14px;
    }

    .copy-btn {
      margin-top: 16px;
    }
  }
}
</style>
