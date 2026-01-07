<template>
  <div class="tutor-list-page">
    <div class="page-container">
      <el-row :gutter="24">
        <el-col :span="6" class="filter-col">
          <el-card class="filter-card" shadow="hover">
            <template #header>
              <div class="filter-header">
                <span>筛选条件</span>
                <el-button link type="primary" @click="resetFilter" size="small">重置</el-button>
              </div>
            </template>
            
            <el-form :model="filterForm" label-position="top" size="default">
              <el-form-item label="科目">
                <el-select v-model="filterForm.subject" placeholder="请选择科目" clearable>
                  <el-option v-for="item in subjects" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>

              <el-form-item label="年级">
                <el-select v-model="filterForm.grade" placeholder="请选择年级" clearable>
                  <el-option v-for="item in grades" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>

              <el-form-item label="期望时薪 (元)">
                <div class="price-range">
                  <el-input-number v-model="filterForm.minPrice" :min="0" :step="10" placeholder="最低" controls-position="right" />
                  <span class="separator">-</span>
                  <el-input-number v-model="filterForm.maxPrice" :min="0" :step="10" placeholder="最高" controls-position="right" />
                </div>
              </el-form-item>

              <el-form-item label="授课方式">
                <el-radio-group v-model="filterForm.teachMode">
                  <el-radio :label="null">不限</el-radio>
                  <el-radio :label="1">线下上门</el-radio>
                  <el-radio :label="2">在线网课</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="教员性别">
                <el-radio-group v-model="filterForm.gender">
                  <el-radio :label="null">不限</el-radio>
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-button type="primary" class="search-btn" @click="handleSearch" :loading="loading">
                立即搜索
              </el-button>
            </el-form>
          </el-card>
        </el-col>

        <el-col :span="18">
          <div class="toolbar">
            <div class="total-count">共找到 <b>{{ total }}</b> 位老师</div>
            <el-radio-group v-model="filterForm.sortBy" @change="handleSortChange" size="default">
              <el-radio-button label="score">✨ 智能推荐</el-radio-button>
              <el-radio-button label="distance">📍 距离最近</el-radio-button>
              <el-radio-button label="rating">⭐ 评分最高</el-radio-button>
              <el-radio-button label="price">💰 价格最低</el-radio-button>
            </el-radio-group>
          </div>

          <div v-loading="loading" class="tutor-list">
            <div v-if="tutors.length === 0 && !loading" class="empty-state">
              <el-empty description="暂无符合条件的教员，试试减少筛选条件" />
            </div>

            <el-card 
              v-for="tutor in tutors" 
              :key="tutor.id" 
              class="tutor-card" 
              shadow="hover"
              @click="goDetail(tutor.id)"
            >
              <div class="card-body">
                <div class="avatar-section">
                  <el-avatar :size="80" :src="tutor.avatarUrl || defaultAvatar(tutor.id)" shape="square" />
                  <div class="match-badge" v-if="tutor.matchScore >= 80">
                    <el-tag type="danger" effect="dark" size="small">极力推荐</el-tag>
                  </div>
                </div>

                <div class="info-section">
                  <div class="header-row">
                    <div class="name-box">
                      <span class="name">{{ tutor.realName }}</span>
                      <el-tag size="small" effect="plain" class="edu-tag">
                        {{ tutor.universityName }} · {{ tutor.major }}
                      </el-tag>
                    </div>
                    <div class="price-box">
                      <span class="price">¥{{ tutor.expectPrice }}</span>
                      <span class="unit">/小时</span>
                    </div>
                  </div>

                  <div class="tags-row">
                    <el-tag 
                      v-for="tag in tutor.matchTags" 
                      :key="tag" 
                      type="success" 
                      size="small" 
                      class="match-tag"
                    >
                      <el-icon><Check /></el-icon> {{ tag }}
                    </el-tag>
                    <el-tag v-if="tutor.teachMode === 1 || tutor.teachMode === 3" type="info" size="small">可上门</el-tag>
                    <el-tag v-if="tutor.teachMode === 2 || tutor.teachMode === 3" type="info" size="small">可网课</el-tag>
                  </div>

                  <p class="intro-text line-clamp-2">{{ tutor.introduction || '暂无自我介绍...' }}</p>
                  
                  <div class="stats-row">
                    <span class="stat-item"><el-icon><StarFilled /></el-icon> {{ tutor.rating || 5.0 }}分</span>
                    <span class="stat-item"><el-icon><MapLocation /></el-icon> {{ formatDistance(tutor.distance) }}</span>
                    <span class="stat-item">已授 {{ tutor.orderCount || 0 }} 课时</span>
                  </div>
                </div>

                <div class="score-section">
                  <el-tooltip placement="left" effect="light">
                    <template #content>
                      <div class="score-tooltip">
                        <div class="tooltip-title">智能匹配维度</div>
                        <div class="score-row"><span>科目匹配:</span> <span>{{ formatScore(tutor.subjectScore) }}</span></div>
                        <div class="score-row"><span>年级匹配:</span> <span>{{ formatScore(tutor.gradeScore) }}</span></div>
                        <div class="score-row"><span>距离评分:</span> <span>{{ formatScore(tutor.distanceScore) }}</span></div>
                        <div class="score-row"><span>价格评分:</span> <span>{{ formatScore(tutor.priceScore) }}</span></div>
                        <div class="score-row"><span>综合信誉:</span> <span>{{ formatScore(tutor.ratingScore) }}</span></div>
                      </div>
                    </template>
                    <div class="score-circle">
                      <el-progress 
                        type="dashboard" 
                        :percentage="formatScore(tutor.matchScore)" 
                        :width="80" 
                        :color="getScoreColor(tutor.matchScore)"
                      >
                        <template #default="{ percentage }">
                          <span class="score-val">{{ percentage }}</span>
                          <span class="score-label">匹配度</span>
                        </template>
                      </el-progress>
                    </div>
                  </el-tooltip>
                  <el-button type="primary" size="small" plain round>查看详情</el-button>
                </div>
              </div>
            </el-card>

            <div class="pagination-wrapper">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="total"
                :page-size="filterForm.size"
                @current-change="handlePageChange"
              />
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchTutors } from '@/api/match'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const tutors = ref([])
const total = ref(0)

// 筛选表单数据
const filterForm = reactive({
  subject: '',
  grade: '',
  minPrice: null,
  maxPrice: null,
  teachMode: null, // null不限, 1上门, 2网课
  gender: null,
  sortBy: 'score', // 默认智能推荐
  sortOrder: 'desc',
  // 模拟当前用户位置 (北京天安门附近)，实际项目应从 store 或浏览器获取
  latitude: 39.9042, 
  longitude: 116.4074,
  radius: 20, // 搜索半径 20km
  page: 1,
  size: 10
})

// 选项数据
const subjects = ['数学', '英语', '语文', '物理', '化学', '生物', '地理', '历史', '政治']
const grades = ['小学一年级', '小学二年级', '小学三年级', '小学四年级', '小学五年级', '小学六年级', 
                '初一', '初二', '初三', '高一', '高二', '高三']

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    // 构造请求参数，去除空值
    const params = { ...filterForm }
    
    // 特殊处理：如果是"价格最低"，则由后端升序排列
    if (params.sortBy === 'price') {
      params.sortOrder = 'asc'
    } else {
      params.sortOrder = 'desc' // 默认降序(高分在前)
    }

    const res = await searchTutors(params)
    if (res.code === 200) {
      tutors.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('搜索失败', error)
    ElMessage.error('获取教员列表失败')
  } finally {
    loading.value = false
  }
}

// 事件处理
const handleSearch = () => {
  filterForm.page = 1
  fetchList()
}

const resetFilter = () => {
  filterForm.subject = ''
  filterForm.grade = ''
  filterForm.minPrice = null
  filterForm.maxPrice = null
  filterForm.teachMode = null
  filterForm.gender = null
  handleSearch()
}

const handleSortChange = () => {
  filterForm.page = 1
  fetchList()
}

const handlePageChange = (page) => {
  filterForm.page = page
  fetchList()
}

const goDetail = (id) => {
  router.push(`/teacher/${id}`) // 假设详情页路由为 /teacher/:id
}

// 工具函数
const defaultAvatar = (seed) => `https://api.dicebear.com/7.x/miniavs/svg?seed=${seed}`

const formatScore = (score) => {
  return score ? Math.round(score) : 0
}

const formatDistance = (dist) => {
  if (!dist && dist !== 0) return '距离未知'
  if (dist < 1) return `${(dist * 1000).toFixed(0)}m`
  return `${dist.toFixed(1)}km`
}

const getScoreColor = (score) => {
  if (score >= 80) return '#67c23a' // Success Green
  if (score >= 60) return '#e6a23c' // Warning Orange
  return '#909399' // Info Grey
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.tutor-list-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 40px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 筛选侧边栏 */
.filter-card {
  border-radius: 8px;
  border: none;
  position: sticky;
  top: 20px;

  .filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }

  .price-range {
    display: flex;
    align-items: center;
    gap: 5px;
    
    .el-input-number {
      width: 100%;
    }
    .separator {
      color: #909399;
    }
  }

  .search-btn {
    width: 100%;
    margin-top: 10px;
    font-weight: 600;
  }
}

/* 顶部工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  background: #fff;
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);

  .total-count {
    color: #606266;
    b {
      color: #409eff;
      font-size: 16px;
    }
  }
}

/* 教员卡片 */
.tutor-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: none;
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  }

  .card-body {
    display: flex;
    gap: 20px;
  }
}

.avatar-section {
  position: relative;
  flex-shrink: 0;

  .match-badge {
    position: absolute;
    bottom: -6px;
    left: 50%;
    transform: translateX(-50%);
    white-space: nowrap;
  }
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 8px;

    .name-box {
      .name {
        font-size: 18px;
        font-weight: 700;
        margin-right: 10px;
        color: #303133;
      }
    }

    .price-box {
      .price {
        font-size: 20px;
        font-weight: 700;
        color: #f56c6c;
      }
      .unit {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .tags-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 8px;

    .match-tag {
      font-weight: 500;
    }
  }

  .intro-text {
    font-size: 13px;
    color: #606266;
    margin-bottom: 12px;
    line-height: 1.5;
    height: 40px; /* limit height for 2 lines */
  }

  .line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .stats-row {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: #909399;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.score-section {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-left: 20px;
  border-left: 1px dashed #e4e7ed;
  gap: 12px;

  .score-circle {
    cursor: help;
    
    .score-val {
      display: block;
      font-size: 18px;
      font-weight: 700;
      color: #303133;
    }
    .score-label {
      font-size: 10px;
      color: #909399;
    }
  }
}

/* Tooltip 自定义样式 */
.score-tooltip {
  padding: 4px;
  
  .tooltip-title {
    font-weight: 600;
    margin-bottom: 8px;
    border-bottom: 1px solid rgba(255,255,255,0.2);
    padding-bottom: 4px;
  }
  
  .score-row {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    margin-bottom: 4px;
    gap: 16px;
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-col {
    display: none; /* 移动端暂隐藏左侧筛选 */
  }
  .card-body {
    flex-direction: column;
  }
  .score-section {
    flex-direction: row;
    border-left: none;
    border-top: 1px dashed #e4e7ed;
    padding-top: 12px;
    padding-left: 0;
    justify-content: space-between;
  }
}
</style>