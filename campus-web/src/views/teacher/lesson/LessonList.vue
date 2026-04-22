<template>
  <div class="lesson-list">
    <div class="page-header">
      <h1 class="page-title">课程记录</h1>
    </div>
    
    <!-- 筛选器 -->
    <div class="filter-bar">
      <el-select v-model="filter.status" placeholder="课程状态" clearable @change="loadLessons">
        <el-option label="待确认/上课中" :value="0" />
        <el-option label="已完成" :value="1" />
        <el-option label="有争议" :value="2" />
      </el-select>
      
      <el-date-picker
        v-model="filter.dateRange"
        type="daterange"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        @change="loadLessons"
      />
    </div>
    
    <!-- 课程列表 -->
    <div v-loading="loading" class="lesson-container">
      <div v-if="lessons.length" class="lesson-cards">
        <div 
          v-for="lesson in lessons" 
          :key="lesson.id" 
          class="lesson-card"
          @click="viewDetail(lesson.id)"
        >
          <div class="lesson-left">
            <div class="lesson-date">
              <span class="day">{{ formatDay(lesson.lessonDate) }}</span>
              <span class="month">{{ formatMonth(lesson.lessonDate) }}</span>
              <span class="weekday">{{ formatWeekday(lesson.lessonDate) }}</span>
            </div>
          </div>
          
          <div class="lesson-main">
            <div class="lesson-header">
              <h4>{{ lesson.studentName }} - {{ lesson.subject }}</h4>
              <el-tag :type="getStatusType(lesson.status, lesson.contentSummary)" size="small">
                {{ getStatusText(lesson.status, lesson.contentSummary) }}
              </el-tag>
            </div>
            
            <div class="lesson-info">
              <p>
                <el-icon><Clock /></el-icon>
                {{ lesson.startTime }} - {{ lesson.endTime }}（{{ lesson.duration }}小时）
              </p>
              <p>
                <el-icon><Location /></el-icon>
                {{ lesson.address || '线上授课' }}
              </p>
            </div>
            
            <div class="lesson-actions">
              <el-button type="primary" size="small" @click.stop="viewDetail(lesson.id)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无课程记录" />
      
      <div v-if="total > pageSize" class="pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadLessons"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, Location } from '@element-plus/icons-vue'
import { getMyLessons, checkIn, checkOut } from '@shared/api/teaching'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const lessons = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filter = reactive({
  status: null,
  dateRange: null,
  orderId: null
})

const getStatusType = (status, contentSummary) => {
  if (status === 0 && !contentSummary) return 'info' // 上课中
  if (status === 0 && contentSummary) return 'warning' // 待确认
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

const getStatusText = (status, contentSummary) => {
  if (status === 0 && !contentSummary) return '上课中'
  if (status === 0 && contentSummary) return '待确认'
  if (status === 1) return '已完成'
  if (status === 2) return '有争议'
  return '未知'
}

const formatDay = (date) => dayjs(date).format('DD')
const formatMonth = (date) => dayjs(date).format('M月')
const formatWeekday = (date) => {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[dayjs(date).day()]
}

const loadLessons = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      pageSize: pageSize.value,
      status: filter.status,
      orderId: filter.orderId
    }
    
    if (filter.dateRange) {
      params.startDate = filter.dateRange[0]
      params.endDate = filter.dateRange[1]
    }
    
    const res = await getMyLessons(params)
    if (res.code === 200) {
      lessons.value = res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (id) => {
  router.push(`/teacher/lesson/${id}`)
}

onMounted(() => {
  // 从路由获取orderId筛选
  if (route.query.orderId) {
    filter.orderId = route.query.orderId
  }
  loadLessons()
})
</script>

<style lang="scss" scoped>
.lesson-list {
  .filter-bar {
    display: flex;
    gap: 16px;
    margin-bottom: 20px;
    flex-wrap: wrap;
    
    .el-select {
      width: 150px;
    }
  }
  
  .lesson-container {
    margin-top: 16px;
  }
  
  .lesson-cards {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  
  .lesson-card {
    display: flex;
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    cursor: pointer;
    transition: box-shadow 0.2s;
    
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .lesson-left {
      padding-right: 20px;
      border-right: 1px solid #ebeef5;
      margin-right: 20px;
      
      .lesson-date {
        text-align: center;
        min-width: 60px;
        
        .day {
          display: block;
          font-size: 28px;
          font-weight: 700;
          color: #409eff;
        }
        
        .month {
          display: block;
          font-size: 14px;
          color: #606266;
        }
        
        .weekday {
          display: block;
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
    
    .lesson-main {
      flex: 1;
      
      .lesson-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        
        h4 {
          font-size: 16px;
          font-weight: 600;
        }
      }
      
      .lesson-info {
        p {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 14px;
          color: #606266;
          margin-bottom: 6px;
          
          .el-icon {
            color: #909399;
          }
        }
      }
      
      .lesson-actions {
        margin-top: 12px;
      }
    }
  }
  
  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 24px;
  }
}

// 响应式
@media (max-width: 576px) {
  .lesson-list {
    .lesson-card {
      flex-direction: column;
      
      .lesson-left {
        border-right: none;
        border-bottom: 1px solid #ebeef5;
        padding-right: 0;
        padding-bottom: 16px;
        margin-right: 0;
        margin-bottom: 16px;
        
        .lesson-date {
          display: flex;
          gap: 8px;
          align-items: baseline;
          
          .day, .month, .weekday {
            display: inline;
          }
        }
      }
    }
  }
}
</style>
