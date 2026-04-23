<template>
  <div class="report-detail-page">
    <van-nav-bar title="报告详情" left-arrow @click-left="$router.back()" />

    <div class="report-content" v-loading="loading">
      <el-empty v-if="!loading && !report" description="报告不存在" />

      <template v-if="report">
        <div class="report-meta">
          <el-tag :type="report.reportType === 1 ? 'primary' : 'success'">
            {{ report.reportType === 1 ? '月度报告' : '阶段总结' }}
          </el-tag>
          <span class="time">{{ report.createTime }}</span>
        </div>

        <div class="section" v-if="report.scoreChartData">
          <h3>成绩变化趋势</h3>
          <div class="chart-container" ref="chartRef"></div>
        </div>

        <div class="section" v-if="report.tutorComment">
          <h3>老师评语</h3>
          <div class="comment-box">
            <p>{{ report.tutorComment }}</p>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getReportDetail } from '@shared/api/report'

const route = useRoute()
const loading = ref(false)
const report = ref(null)
const chartRef = ref(null)

onMounted(async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getReportDetail(id)
    if (res.code === 200) {
      report.value = res.data
      if (report.value?.scoreChartData) {
        await nextTick()
        renderChart()
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

const renderChart = () => {
  try {
    const chartData = typeof report.value.scoreChartData === 'string'
      ? JSON.parse(report.value.scoreChartData)
      : report.value.scoreChartData
    if (chartRef.value && chartData) {
      chartRef.value.innerHTML = '<div style="padding: 20px; text-align: center; color: #909399;">成绩趋势图数据区域</div>'
    }
  } catch (e) {
    console.error('Chart render error:', e)
  }
}
</script>

<style lang="scss" scoped>
.report-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.report-content {
  padding: 16px;
}

.report-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;

  .time {
    font-size: 13px;
    color: #909399;
  }
}

.section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 12px;
  }
}

.chart-container {
  min-height: 200px;
  background: #fafafa;
  border-radius: 8px;
}

.comment-box {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;

  p {
    font-size: 14px;
    color: #606266;
    line-height: 1.8;
    margin: 0;
  }
}
</style>
