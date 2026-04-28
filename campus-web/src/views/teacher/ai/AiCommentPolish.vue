<template>
  <div class="comment-polish-page">
    <el-page-header @back="$router.push('/teacher/ai/hub')">
      <template #content>AI 评语润色</template>
    </el-page-header>

    <div class="polish-container">
      <el-form :model="form" label-width="100px" class="polish-form">
        <el-form-item label="原始评语" required>
          <el-input
            v-model="form.originalComment"
            type="textarea"
            :rows="5"
            placeholder="请输入需要润色的评语，例如：这个学生钢琴节拍掌握还行，但指法有些粗糙"
          />
        </el-form-item>
        <el-form-item label="科目">
          <el-input v-model="form.subject" placeholder="选填" />
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input v-model="form.studentName" placeholder="选填" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="polish">
            润色评语
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="result" class="polish-result">
        <h3>润色结果</h3>
        <div class="result-text">{{ result }}</div>
        <el-button class="copy-btn" @click="copyResult">复制评语</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { polishComment } from '@shared/api/llm'

const form = ref({
  originalComment: '',
  subject: '',
  studentName: ''
})

const loading = ref(false)
const result = ref('')

const polish = async () => {
  if (!form.value.originalComment.trim()) {
    ElMessage.warning('请输入原始评语')
    return
  }
  loading.value = true
  try {
    const res = await polishComment(form.value)
    if (res.code === 200) {
      result.value = res.data
    } else {
      result.value = res.msg || '润色失败'
    }
  } catch (e) {
    ElMessage.error('润色失败，请稍后重试')
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
.comment-polish-page {
  max-width: 800px;
  margin: 0 auto;

  .polish-container {
    margin-top: 24px;
  }

  .polish-form {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 20px;
  }

  .polish-result {
    background: #fff;
    border-radius: 12px;
    padding: 24px;

    h3 {
      margin-bottom: 16px;
      font-size: 18px;
    }

    .result-text {
      background: #f5f7fa;
      border-radius: 8px;
      padding: 16px;
      line-height: 1.8;
      font-size: 14px;
      color: #303133;
      white-space: pre-wrap;
    }

    .copy-btn {
      margin-top: 16px;
    }
  }
}
</style>
