<template>
  <div class="report-list-page">
    <van-nav-bar title="学生报告" left-arrow @click-left="$router.back()" />

    <div class="student-selector" v-if="students.length > 0">
      <el-select v-model="selectedStudentId" placeholder="选择孩子" @change="loadReports" style="width: 100%">
        <el-option v-for="s in students" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="reportType" size="small" @change="loadReports">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="1">月度报告</el-radio-button>
        <el-radio-button :value="2">阶段总结</el-radio-button>
      </el-radio-group>
    </div>

    <div class="report-list" v-loading="loading">
      <el-empty v-if="!loading && reports.length === 0" description="暂无报告" />

      <div v-for="report in reports" :key="report.id" class="report-card" @click="viewDetail(report)">
        <div class="report-header">
          <el-tag :type="report.reportType === 1 ? 'primary' : 'success'" size="small">
            {{ report.reportType === 1 ? '月度报告' : '阶段总结' }}
          </el-tag>
          <span class="report-time">{{ report.createTime }}</span>
        </div>
        <div class="report-body" v-if="report.tutorComment">
          <p class="comment">{{ report.tutorComment }}</p>
        </div>
        <div class="report-footer">
          <span>查看详情</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { getStudentReports } from '@shared/api/report'
import { getStudentList } from '@shared/api/parent'

const router = useRouter()
const loading = ref(false)
const reports = ref([])
const students = ref([])
const selectedStudentId = ref(null)
const reportType = ref(null)

onMounted(async () => {
  try {
    const res = await getStudentList()
    if (res.code === 200) {
      const list = Array.isArray(res.data) ? res.data : (res.data?.records || [])
      students.value = list
      if (list.length > 0) {
        selectedStudentId.value = list[0].id
        loadReports()
      }
    }
  } catch (e) {
    console.error(e)
  }
})

const loadReports = async () => {
  if (!selectedStudentId.value) return
  loading.value = true
  try {
    const res = await getStudentReports(selectedStudentId.value, {
      reportType: reportType.value,
      page: 1,
      size: 20
    })
    if (res.code === 200) {
      reports.value = res.data?.records || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const viewDetail = (report) => {
  router.push(`/parent/reports/${report.id}`)
}
</script>

<style lang="scss" scoped>
.report-list-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.student-selector {
  padding: 12px 16px;
  background: #fff;
}

.filter-bar {
  padding: 12px 16px;
  background: #fff;
  margin-top: 8px;
}

.report-list {
  padding: 12px 16px;
}

.report-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-1px);
  }
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;

  .report-time {
    font-size: 12px;
    color: #909399;
  }
}

.report-body {
  .comment {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.report-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #667eea;
  margin-top: 8px;
}
</style>
