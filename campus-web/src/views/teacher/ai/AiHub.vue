<template>
  <div class="ai-hub-page">
    <el-page-header @back="$router.back()">
      <template #content>AI 工具中心</template>
    </el-page-header>

    <div class="hub-grid">
      <div class="hub-card" @click="$router.push('/ai')">
        <el-icon :size="40" color="#409eff"><ChatDotRound /></el-icon>
        <h3>AI 对话助手</h3>
        <p>与 AI 互动，获取教学建议和答疑</p>
      </div>

      <div class="hub-card" @click="$router.push('/teacher/ai/lesson-plan')">
        <el-icon :size="40" color="#67c23a"><Document /></el-icon>
        <h3>课程规划生成</h3>
        <p>根据学生情况自动生成教学计划</p>
      </div>

      <div class="hub-card" @click="$router.push('/teacher/ai/comment-polish')">
        <el-icon :size="40" color="#e6a23c"><EditPen /></el-icon>
        <h3>评语智能润色</h3>
        <p>将简单评语润色为专业、温暖的反馈</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ChatDotRound, Document, EditPen } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.ai-hub-page {
  max-width: 800px;
  margin: 0 auto;

  .hub-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 20px;
    margin-top: 24px;
  }

  .hub-card {
    background: #fff;
    border-radius: 12px;
    padding: 32px 24px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid #ebeef5;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }

    h3 {
      margin: 16px 0 8px;
      font-size: 16px;
    }

    p {
      color: #909399;
      font-size: 13px;
      margin: 0;
    }
  }
}
</style>
