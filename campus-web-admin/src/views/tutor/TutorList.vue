<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form card-shadow">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="姓名/学校/专业" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="认证状态">
          <el-select v-model="searchForm.certStatus" placeholder="全部" clearable style="width: 140px;">
            <el-option label="待提交" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="已通过" :value="2" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="学历">
          <el-select v-model="searchForm.education" placeholder="全部" clearable style="width: 120px;">
            <el-option label="专科" :value="1" />
            <el-option label="本科" :value="2" />
            <el-option label="硕士" :value="3" />
            <el-option label="博士" :value="4" />
          </el-select>
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
    
    <!-- 数据表格 -->
    <div class="table-container card-shadow">
      <div class="table-header">
        <span class="title">教师列表</span>
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
        <el-table-column prop="teachSubjects" label="授课科目" width="160">
          <template #default="{ row }">
            <template v-if="row.teachSubjects?.length">
              <el-tag v-for="s in row.teachSubjects.slice(0, 2)" :key="s" size="small" class="mr-1">
                {{ s }}
              </el-tag>
              <span v-if="row.teachSubjects.length > 2">...</span>
            </template>
            <span v-else class="text-gray">未设置</span>
          </template>
        </el-table-column>
        <el-table-column prop="certStatus" label="认证状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getCertStatusType(row.certStatus)">
              {{ getCertStatusText(row.certStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" :max="5" />
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleView(row)">查看</el-button>
            <el-button text type="primary" @click="handleEdit(row)">编辑</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogType === 'view' ? '教师详情' : '编辑教师'" width="700px">
      <el-descriptions :column="2" border v-if="dialogType === 'view'">
        <el-descriptions-item label="真实姓名">{{ currentTutor.realName }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ currentTutor.idCard }}</el-descriptions-item>
        <el-descriptions-item label="学校">{{ currentTutor.universityName }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentTutor.major }}</el-descriptions-item>
        <el-descriptions-item label="学历">{{ getEducationText(currentTutor.education) }}</el-descriptions-item>
        <el-descriptions-item label="入学年份">{{ currentTutor.enrollYear }}</el-descriptions-item>
        <el-descriptions-item label="授课科目" :span="2">
          <el-tag v-for="s in currentTutor.teachSubjects" :key="s" class="mr-1">{{ s }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="授课年级" :span="2">
          <el-tag v-for="g in currentTutor.teachGrades" :key="g" class="mr-1">{{ g }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="期望时薪">¥{{ currentTutor.expectPrice }}/小时</el-descriptions-item>
        <el-descriptions-item label="授课方式">
          <span v-if="currentTutor.canVisit">可上门</span>
          <span v-if="currentTutor.canOnline">{{ currentTutor.canVisit ? ' / ' : '' }}可线上</span>
        </el-descriptions-item>
        <el-descriptions-item label="认证状态">
          <el-tag :type="getCertStatusType(currentTutor.certStatus)">
            {{ getCertStatusText(currentTutor.certStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate v-model="currentTutor.rating" disabled show-score />
        </el-descriptions-item>
        <el-descriptions-item label="个人简介" :span="2">
          {{ currentTutor.introduction || '暂无' }}
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 证件图片 -->
      <div class="cert-images" v-if="dialogType === 'view'">
        <h4>认证材料</h4>
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="cert-item">
              <p>身份证正面</p>
              <el-image :src="currentTutor.idCardFrontUrl" fit="cover" :preview-src-list="[currentTutor.idCardFrontUrl]" />
            </div>
          </el-col>
          <el-col :span="8">
            <div class="cert-item">
              <p>身份证反面</p>
              <el-image :src="currentTutor.idCardBackUrl" fit="cover" :preview-src-list="[currentTutor.idCardBackUrl]" />
            </div>
          </el-col>
          <el-col :span="8">
            <div class="cert-item">
              <p>学生证</p>
              <el-image :src="currentTutor.studentCardUrl" fit="cover" :preview-src-list="[currentTutor.studentCardUrl]" />
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { tutorApi } from '@/api'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('view')
const currentTutor = ref({})

const searchForm = reactive({
  keyword: '',
  certStatus: null,
  education: null
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

const getCertStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const getCertStatusText = (status) => {
  const texts = { 0: '待提交', 1: '待审核', 2: '已通过', 3: '已拒绝' }
  return texts[status] || '未知'
}

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await tutorApi.getList({
      page: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      certStatus: searchForm.certStatus ?? undefined,
      education: searchForm.education ?? undefined
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('获取教师列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.certStatus = null
  searchForm.education = null
  handleSearch()
}

const handleView = (row) => {
  currentTutor.value = { ...row }
  dialogType.value = 'view'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  currentTutor.value = { ...row }
  dialogType.value = 'edit'
  dialogVisible.value = true
}
</script>

<style lang="scss" scoped>
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

.mr-1 {
  margin-right: 4px;
}

.text-gray {
  color: #909399;
}

.cert-images {
  margin-top: 20px;
  
  h4 {
    margin-bottom: 12px;
    color: #303133;
  }
  
  .cert-item {
    text-align: center;
    
    p {
      font-size: 12px;
      color: #909399;
      margin-bottom: 8px;
    }
    
    .el-image {
      width: 100%;
      height: 120px;
      border-radius: 4px;
      border: 1px solid #eee;
    }
  }
}
</style>
