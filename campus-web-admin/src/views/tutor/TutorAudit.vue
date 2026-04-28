<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-item">
          <div class="stat-number warning">{{ pendingCount }}</div>
          <div class="stat-label">待审核</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-item">
          <div class="stat-number success">{{ todayApproved }}</div>
          <div class="stat-label">今日通过</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-item">
          <div class="stat-number danger">{{ todayRejected }}</div>
          <div class="stat-label">今日拒绝</div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 待审核列表 -->
    <div class="table-container card-shadow">
      <div class="table-header">
        <span class="title">待审核认证列表</span>
        <el-button type="primary" @click="fetchData">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="universityName" label="学校" width="160" />
        <el-table-column prop="major" label="专业" width="140" />
        <el-table-column prop="education" label="学历" width="100">
          <template #default="{ row }">
            {{ getEducationText(row.education) }}
          </template>
        </el-table-column>
        <el-table-column prop="enrollYear" label="入学年份" width="100" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleReview(row)">
              <el-icon><View /></el-icon>审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>
    
    <!-- 审核弹窗 -->
    <el-dialog v-model="reviewVisible" title="认证审核" width="900px" top="5vh">
      <el-row :gutter="20">
        <!-- 左侧：基本信息 -->
        <el-col :span="12">
          <h4 class="section-title">基本信息</h4>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="真实姓名">{{ currentTutor.realName }}</el-descriptions-item>
            <el-descriptions-item label="身份证号">{{ currentTutor.idCard }}</el-descriptions-item>
            <el-descriptions-item label="学校">{{ currentTutor.universityName }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ currentTutor.major }}</el-descriptions-item>
            <el-descriptions-item label="学历">{{ getEducationText(currentTutor.education) }}</el-descriptions-item>
            <el-descriptions-item label="入学年份">{{ currentTutor.enrollYear }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
        
        <!-- 右侧：证件照片 -->
        <el-col :span="12">
          <h4 class="section-title">认证材料</h4>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="身份证正面" name="idFront">
              <el-image 
                :src="currentTutor.idCardFrontUrl" 
                fit="contain" 
                class="cert-image"
                :preview-src-list="[currentTutor.idCardFrontUrl]"
              />
            </el-tab-pane>
            <el-tab-pane label="身份证反面" name="idBack">
              <el-image 
                :src="currentTutor.idCardBackUrl" 
                fit="contain" 
                class="cert-image"
                :preview-src-list="[currentTutor.idCardBackUrl]"
              />
            </el-tab-pane>
            <el-tab-pane label="学生证" name="studentCard">
              <el-image 
                :src="currentTutor.studentCardUrl" 
                fit="contain" 
                class="cert-image"
                :preview-src-list="[currentTutor.studentCardUrl]"
              />
            </el-tab-pane>
            <el-tab-pane label="其他证书" name="certificates" v-if="currentTutor.certificateUrls?.length">
              <div class="cert-list">
                <el-image 
                  v-for="(url, index) in currentTutor.certificateUrls" 
                  :key="index"
                  :src="url" 
                  fit="cover" 
                  class="cert-thumb"
                  :preview-src-list="currentTutor.certificateUrls"
                  :initial-index="index"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
      
      <!-- 审核操作 -->
      <el-divider />
      <div class="review-actions">
        <el-form :model="reviewForm" label-width="80px">
          <el-form-item label="审核结果">
            <el-radio-group v-model="reviewForm.result">
              <el-radio-button label="approve">
                <el-icon><Check /></el-icon> 通过
              </el-radio-button>
              <el-radio-button label="reject">
                <el-icon><Close /></el-icon> 拒绝
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="拒绝原因" v-if="reviewForm.result === 'reject'">
            <el-input 
              v-model="reviewForm.rejectReason" 
              type="textarea" 
              :rows="3"
              placeholder="请输入拒绝原因，将通知给教师"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReview" :loading="submitting">
          提交审核结果
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { tutorApi, statsApi } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const reviewVisible = ref(false)
const activeTab = ref('idFront')
const currentTutor = ref({})

const pendingCount = ref(0)
const todayApproved = ref(0)
const todayRejected = ref(0)

const reviewForm = reactive({
  result: 'approve',
  rejectReason: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const getEducationText = (education) => {
  const texts = { 1: '专科', 2: '本科', 3: '硕士', 4: '博士' }
  return texts[education] || '未知'
}

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await tutorApi.getPendingList({
      page: pagination.page,
      size: pagination.size
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
    pendingCount.value = pagination.total
  } catch (error) {
    console.error('获取待审核列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReview = (row) => {
  currentTutor.value = { ...row }
  reviewForm.result = 'approve'
  reviewForm.rejectReason = ''
  activeTab.value = 'idFront'
  reviewVisible.value = true
}

const handleSubmitReview = async () => {
  if (reviewForm.result === 'reject' && !reviewForm.rejectReason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  submitting.value = true
  
  try {
    if (reviewForm.result === 'approve') {
      await tutorApi.approve(currentTutor.value.id)
      ElMessage.success('认证已通过')
      todayApproved.value++
    } else {
      await tutorApi.reject(currentTutor.value.id, reviewForm.rejectReason)
      ElMessage.success('认证已拒绝')
      todayRejected.value++
    }
    
    reviewVisible.value = false
    pendingCount.value--
    fetchData()
  } catch (error) {
    console.error('审核操作失败:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.stat-row {
  margin-bottom: 20px;
  
  .stat-item {
    text-align: center;
    padding: 20px;
    
    .stat-number {
      font-size: 36px;
      font-weight: 600;
      
      &.warning { color: #E6A23C; }
      &.success { color: #67C23A; }
      &.danger { color: #F56C6C; }
    }
    
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 8px;
    }
  }
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

.section-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.cert-image {
  width: 100%;
  height: 250px;
  border: 1px solid #eee;
  border-radius: 4px;
}

.cert-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  
  .cert-thumb {
    width: 100px;
    height: 100px;
    border-radius: 4px;
    cursor: pointer;
  }
}

.review-actions {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
}
</style>
