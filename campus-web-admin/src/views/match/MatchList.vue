<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form card-shadow">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="家长/教师姓名" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="匹配类型">
          <el-select v-model="searchForm.matchType" placeholder="全部" clearable style="width: 120px;">
            <el-option label="系统推荐" value="system" />
            <el-option label="手动匹配" value="manual" />
            <el-option label="教师申请" value="apply" />
          </el-select>
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="searchForm.result" placeholder="全部" clearable style="width: 120px;">
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
            <el-option label="待确认" value="pending" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker 
            v-model="searchForm.dateRange" 
            type="daterange" 
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card card-shadow">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总匹配次数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card card-shadow success">
          <div class="stat-value">{{ stats.success }}</div>
          <div class="stat-label">匹配成功</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card card-shadow failed">
          <div class="stat-value">{{ stats.failed }}</div>
          <div class="stat-label">匹配失败</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card card-shadow rate">
          <div class="stat-value">{{ stats.rate }}%</div>
          <div class="stat-label">成功率</div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 数据表格 -->
    <div class="table-container card-shadow">
      <div class="table-header">
        <span class="title">匹配记录</span>
      </div>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="demandId" label="需求ID" width="100" />
        <el-table-column prop="parentName" label="家长" width="100" />
        <el-table-column prop="subject" label="科目" width="80" />
        <el-table-column prop="tutorName" label="教师" width="100" />
        <el-table-column prop="matchType" label="匹配类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getMatchTypeStyle(row.matchType)" size="small">
              {{ getMatchTypeText(row.matchType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="matchScore" label="匹配度" width="100">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.matchScore" 
              :stroke-width="10"
              :color="getScoreColor(row.matchScore)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="result" label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="getResultType(row.result)" size="small">
              {{ getResultText(row.result) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" label="失败原因" show-overflow-tooltip />
        <el-table-column prop="createTime" label="匹配时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleView(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>
    
    <!-- 详情弹窗 -->
    <el-dialog v-model="dialogVisible" title="匹配详情" width="700px">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="detail-card">
            <h4>需求信息</h4>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="家长">{{ currentMatch.parentName }}</el-descriptions-item>
              <el-descriptions-item label="学员">{{ currentMatch.studentName }}</el-descriptions-item>
              <el-descriptions-item label="科目">{{ currentMatch.subject }}</el-descriptions-item>
              <el-descriptions-item label="年级">{{ currentMatch.grade }}</el-descriptions-item>
              <el-descriptions-item label="期望价格">¥{{ currentMatch.expectPrice }}/课时</el-descriptions-item>
              <el-descriptions-item label="上课地址">{{ currentMatch.address }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="detail-card">
            <h4>教师信息</h4>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="姓名">{{ currentMatch.tutorName }}</el-descriptions-item>
              <el-descriptions-item label="学校">{{ currentMatch.tutorSchool }}</el-descriptions-item>
              <el-descriptions-item label="教授科目">{{ currentMatch.tutorSubjects }}</el-descriptions-item>
              <el-descriptions-item label="时薪">¥{{ currentMatch.tutorPrice }}/课时</el-descriptions-item>
              <el-descriptions-item label="评分">
                <el-rate :model-value="currentMatch.tutorRating" disabled />
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-col>
      </el-row>
      
      <div class="match-result" style="margin-top: 20px;">
        <h4>匹配结果</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="匹配类型">
            <el-tag :type="getMatchTypeStyle(currentMatch.matchType)">
              {{ getMatchTypeText(currentMatch.matchType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="匹配度">
            <span style="font-size: 18px; font-weight: 600; color: #409eff;">
              {{ currentMatch.matchScore }}%
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="匹配结果">
            <el-tag :type="getResultType(currentMatch.result)">
              {{ getResultText(currentMatch.result) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="匹配时间">{{ currentMatch.createTime }}</el-descriptions-item>
          <el-descriptions-item label="失败原因" :span="2" v-if="currentMatch.result === 'failed'">
            {{ currentMatch.failReason }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

const loading = ref(false)
const dialogVisible = ref(false)
const currentMatch = ref({})

const stats = reactive({
  total: 256,
  success: 198,
  failed: 58,
  rate: 77.3
})

const searchForm = reactive({
  keyword: '',
  matchType: '',
  result: '',
  dateRange: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 模拟数据
const tableData = ref([
  { 
    id: 1, demandId: 101, parentName: '张妈妈', studentName: '张小明',
    subject: '数学', grade: '初三', expectPrice: 150, address: '海淀区中关村',
    tutorName: '李老师', tutorSchool: '北京大学', tutorSubjects: '数学、物理',
    tutorPrice: 150, tutorRating: 4.8,
    matchType: 'system', matchScore: 92, result: 'success',
    failReason: null, createTime: '2026-01-15 10:30:00'
  },
  { 
    id: 2, demandId: 102, parentName: '李爸爸', studentName: '李小华',
    subject: '英语', grade: '高一', expectPrice: 200, address: '朝阳区建国门',
    tutorName: '王老师', tutorSchool: '清华大学', tutorSubjects: '英语',
    tutorPrice: 180, tutorRating: 4.9,
    matchType: 'apply', matchScore: 88, result: 'success',
    failReason: null, createTime: '2026-01-14 15:00:00'
  },
  { 
    id: 3, demandId: 103, parentName: '王女士', studentName: '王小红',
    subject: '物理', grade: '高二', expectPrice: 180, address: '西城区西单',
    tutorName: '张老师', tutorSchool: '北京师范大学', tutorSubjects: '物理、化学',
    tutorPrice: 200, tutorRating: 4.5,
    matchType: 'manual', matchScore: 75, result: 'failed',
    failReason: '家长拒绝，价格不合适', createTime: '2026-01-13 09:00:00'
  },
  { 
    id: 4, demandId: 104, parentName: '赵先生', studentName: '赵小刚',
    subject: '语文', grade: '初一', expectPrice: 120, address: '东城区王府井',
    tutorName: '陈老师', tutorSchool: '中国人民大学', tutorSubjects: '语文',
    tutorPrice: 130, tutorRating: 4.7,
    matchType: 'system', matchScore: 85, result: 'pending',
    failReason: null, createTime: '2026-01-16 11:00:00'
  }
])

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = stats.total
    loading.value = false
  }, 500)
}

const getMatchTypeStyle = (type) => {
  const map = { 'system': 'primary', 'manual': 'warning', 'apply': 'success' }
  return map[type] || 'info'
}

const getMatchTypeText = (type) => {
  const map = { 'system': '系统推荐', 'manual': '手动匹配', 'apply': '教师申请' }
  return map[type] || type
}

const getResultType = (result) => {
  const map = { 'success': 'success', 'failed': 'danger', 'pending': 'warning' }
  return map[result] || 'info'
}

const getResultText = (result) => {
  const map = { 'success': '成功', 'failed': '失败', 'pending': '待确认' }
  return map[result] || result
}

const getScoreColor = (score) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.matchType = ''
  searchForm.result = ''
  searchForm.dateRange = null
  handleSearch()
}

const handleView = (row) => {
  currentMatch.value = { ...row }
  dialogVisible.value = true
}
</script>

<style lang="scss" scoped>
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  padding: 20px;
  text-align: center;
  border-radius: 8px;
  background: #fff;
  
  .stat-value {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
  }
  
  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-top: 8px;
  }
  
  &.success .stat-value { color: #67c23a; }
  &.failed .stat-value { color: #f56c6c; }
  &.rate .stat-value { color: #409eff; }
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  .title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-card {
  h4 {
    margin-bottom: 12px;
    color: #303133;
    font-size: 14px;
  }
}
</style>
