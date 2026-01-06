<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form card-shadow">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="订单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="教师">
          <el-input v-model="searchForm.tutorName" placeholder="教师姓名" clearable style="width: 120px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="待上课" value="pending" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker 
            v-model="searchForm.date" 
            type="date" 
            placeholder="选择日期"
            style="width: 160px;"
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
    
    <!-- 数据表格 -->
    <div class="table-container card-shadow">
      <div class="table-header">
        <span class="title">课时记录列表</span>
      </div>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="lessonNo" label="课时序号" width="100" />
        <el-table-column prop="tutorName" label="教师" width="100" />
        <el-table-column prop="studentName" label="学员" width="100" />
        <el-table-column prop="subject" label="科目" width="80" />
        <el-table-column prop="lessonDate" label="上课日期" width="120" />
        <el-table-column label="上课时间" width="140">
          <template #default="{ row }">
            {{ row.startTime }} - {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="80">
          <template #default="{ row }">
            {{ row.duration }}分钟
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hasCheckIn" label="签到照片" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.checkInPhoto" type="success" size="small">已上传</el-tag>
            <el-tag v-else type="info" size="small">未上传</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleView(row)">详情</el-button>
            <el-button text type="warning" v-if="row.checkInPhoto" @click="viewPhoto(row)">
              照片
            </el-button>
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
    
    <!-- 课时详情弹窗 -->
    <el-dialog v-model="dialogVisible" title="课时详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ currentLesson.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="课时序号">第 {{ currentLesson.lessonNo }} 课时</el-descriptions-item>
        <el-descriptions-item label="教师">{{ currentLesson.tutorName }}</el-descriptions-item>
        <el-descriptions-item label="学员">{{ currentLesson.studentName }}</el-descriptions-item>
        <el-descriptions-item label="科目">{{ currentLesson.subject }}</el-descriptions-item>
        <el-descriptions-item label="课时单价">¥{{ currentLesson.price }}</el-descriptions-item>
        <el-descriptions-item label="上课日期">{{ currentLesson.lessonDate }}</el-descriptions-item>
        <el-descriptions-item label="上课时间">
          {{ currentLesson.startTime }} - {{ currentLesson.endTime }}
        </el-descriptions-item>
        <el-descriptions-item label="上课时长">{{ currentLesson.duration }} 分钟</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentLesson.status)">
            {{ getStatusText(currentLesson.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上课地址" :span="2">{{ currentLesson.address }}</el-descriptions-item>
        <el-descriptions-item label="课后评价" :span="2">
          <div v-if="currentLesson.rating">
            <el-rate :model-value="currentLesson.rating" disabled />
            <p style="margin-top: 8px; color: #606266;">{{ currentLesson.comment }}</p>
          </div>
          <span v-else>暂无评价</span>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 签到照片 -->
      <div v-if="currentLesson.checkInPhoto" style="margin-top: 20px;">
        <h4 style="margin-bottom: 12px;">签到照片</h4>
        <el-image 
          :src="currentLesson.checkInPhoto" 
          :preview-src-list="[currentLesson.checkInPhoto]"
          style="width: 200px; height: 150px; border-radius: 8px;"
          fit="cover"
        />
      </div>
    </el-dialog>
    
    <!-- 照片预览 -->
    <el-dialog v-model="photoDialogVisible" title="签到照片" width="600px">
      <div class="photo-preview">
        <el-image 
          :src="currentPhoto" 
          :preview-src-list="[currentPhoto]"
          style="max-width: 100%; border-radius: 8px;"
        />
        <div class="photo-info">
          <p><strong>上传时间：</strong>{{ currentLesson.checkInTime }}</p>
          <p><strong>上课地点：</strong>{{ currentLesson.address }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

const loading = ref(false)
const dialogVisible = ref(false)
const photoDialogVisible = ref(false)
const currentLesson = ref({})
const currentPhoto = ref('')

const searchForm = reactive({
  orderNo: '',
  tutorName: '',
  status: '',
  date: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 模拟数据
const tableData = ref([
  { 
    id: 1, orderNo: 'ORD20260115001', lessonNo: 1, 
    tutorName: '李老师', studentName: '张小明', subject: '数学',
    lessonDate: '2026-01-16', startTime: '14:00', endTime: '16:00',
    duration: 120, price: 150, status: 'completed',
    address: '北京市海淀区中关村大街1号',
    checkInPhoto: 'https://picsum.photos/400/300?random=1',
    checkInTime: '2026-01-16 14:05:00',
    rating: 5, comment: '讲解清晰，孩子很喜欢'
  },
  { 
    id: 2, orderNo: 'ORD20260115001', lessonNo: 2, 
    tutorName: '李老师', studentName: '张小明', subject: '数学',
    lessonDate: '2026-01-18', startTime: '14:00', endTime: '16:00',
    duration: 120, price: 150, status: 'completed',
    address: '北京市海淀区中关村大街1号',
    checkInPhoto: 'https://picsum.photos/400/300?random=2',
    checkInTime: '2026-01-18 14:02:00',
    rating: 4, comment: '不错，继续保持'
  },
  { 
    id: 3, orderNo: 'ORD20260115001', lessonNo: 3, 
    tutorName: '李老师', studentName: '张小明', subject: '数学',
    lessonDate: '2026-01-20', startTime: '14:00', endTime: '16:00',
    duration: 120, price: 150, status: 'completed',
    address: '北京市海淀区中关村大街1号',
    checkInPhoto: 'https://picsum.photos/400/300?random=3',
    checkInTime: '2026-01-20 14:00:00',
    rating: null, comment: null
  },
  { 
    id: 4, orderNo: 'ORD20260115001', lessonNo: 4, 
    tutorName: '李老师', studentName: '张小明', subject: '数学',
    lessonDate: '2026-01-22', startTime: '14:00', endTime: '16:00',
    duration: 120, price: 150, status: 'pending',
    address: '北京市海淀区中关村大街1号',
    checkInPhoto: null
  },
  { 
    id: 5, orderNo: 'ORD20260114001', lessonNo: 1, 
    tutorName: '王老师', studentName: '李小华', subject: '英语',
    lessonDate: '2026-01-15', startTime: '10:00', endTime: '12:00',
    duration: 120, price: 200, status: 'ongoing',
    address: '北京市朝阳区建国路88号',
    checkInPhoto: 'https://picsum.photos/400/300?random=5',
    checkInTime: '2026-01-15 10:00:00'
  }
])

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = 50
    loading.value = false
  }, 500)
}

const getStatusType = (status) => {
  const map = {
    'pending': 'warning',
    'ongoing': 'primary',
    'completed': 'success',
    'cancelled': 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'pending': '待上课',
    'ongoing': '进行中',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return map[status] || status
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.tutorName = ''
  searchForm.status = ''
  searchForm.date = null
  handleSearch()
}

const handleView = (row) => {
  currentLesson.value = { ...row }
  dialogVisible.value = true
}

const viewPhoto = (row) => {
  currentLesson.value = { ...row }
  currentPhoto.value = row.checkInPhoto
  photoDialogVisible.value = true
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

.photo-preview {
  text-align: center;
  
  .photo-info {
    margin-top: 16px;
    text-align: left;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;
    
    p {
      margin: 8px 0;
      color: #606266;
    }
  }
}
</style>
