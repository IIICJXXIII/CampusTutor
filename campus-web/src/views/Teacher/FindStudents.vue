<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDemandList } from '@/api/demand'

const router = useRouter()
const viewMode = ref('list')
const loading = ref(false)
const searchQuery = ref('')

// 筛选条件
const filterForm = ref({
  subject: '',
  gradeLevel: '',
  distance: ''
})

// 学生需求数据
const students = ref([
  {
    id: 1,
    grade: '小学三年级',
    subject: '数学',
    price: 180,
    tags: ['急需', '1.2km'],
    location: '阳光花园',
    desc: '孩子计算基础薄弱，需要耐心。',
    publishTime: '2024-01-15',
    top: '30%',
    left: '40%'
  },
  {
    id: 2,
    grade: '初中二年级',
    subject: '物理',
    price: 220,
    tags: ['考前冲刺', '3.5km'],
    location: '万达广场',
    desc: '期中考试物理不及格，急需提升。',
    publishTime: '2024-01-14',
    top: '50%',
    left: '70%'
  },
  {
    id: 3,
    grade: '高中英语',
    subject: '口语',
    price: 250,
    tags: ['线上', '0km'],
    location: '线上教学',
    desc: '准备出国留学，重点练习雅思。',
    publishTime: '2024-01-13',
    top: '60%',
    left: '20%'
  }
])

// 科目选项
const subjectOptions = ['语文', '数学', '英语', '物理', '化学', '生物', '历史', '地理', '政治']
const gradeOptions = ['小学', '初中', '高中']
const distanceOptions = ['1km内', '3km内', '5km内', '不限']

// 获取标签类型
const getTagType = (tag) => {
  if (tag.includes('急')) return 'danger'
  if (tag.includes('冲刺')) return 'warning'
  if (tag.includes('线上')) return 'success'
  return 'info'
}

// 获取需求列表
const fetchStudents = async () => {
  loading.value = true
  try {
    const res = await getDemandList({
      page: 1,
      pageSize: 20,
      status: 1
    })
    if (res.data && res.data.records) {
      const records = res.data.records.map((item, idx) => ({
        id: item.id,
        grade: `${item.gradeLevel || '小学'}${item.gradeName || '三年级'}`,
        subject: item.subjects || '数学',
        price: item.maxPrice || 180,
        tags: buildTags(item),
        location: item.teachLocation || '待定',
        desc: item.description || '暂无描述',
        publishTime: item.createTime,
        top: `${30 + idx * 15}%`,
        left: `${20 + idx * 20}%`
      }))
      if (records.length > 0) {
        students.value = records
      }
    }
  } catch (error) {
    console.error('获取需求失败:', error)
  } finally {
    loading.value = false
  }
}

const buildTags = (item) => {
  const tags = []
  if (item.urgency === 1) tags.push('急需')
  if (item.teachMode === 'online') tags.push('线上')
  if (item.distance) tags.push(`${item.distance}km`)
  return tags.length ? tags : ['新发布']
}

const handleViewDetail = (item) => {
  router.push(`/student/${item.id}`)
}

const handleRefresh = () => {
  fetchStudents()
  ElMessage.success('已刷新')
}

onMounted(() => {
  fetchStudents()
})
</script>

<template>
  <div class="find-students-page" v-loading="loading">
    <!-- 搜索栏 -->
    <div class="search-header">
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索生源需求..."
          prefix-icon="Search"
          clearable
        />
        <el-button 
          :type="viewMode === 'map' ? 'primary' : 'default'"
          @click="viewMode = viewMode === 'list' ? 'map' : 'list'"
        >
          <el-icon class="mr-1">
            <component :is="viewMode === 'list' ? 'MapLocation' : 'List'" />
          </el-icon>
          {{ viewMode === 'list' ? '地图' : '列表' }}
        </el-button>
      </div>
      
      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-select v-model="filterForm.subject" placeholder="科目" clearable size="small">
          <el-option v-for="s in subjectOptions" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="filterForm.gradeLevel" placeholder="年级段" clearable size="small">
          <el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g" />
        </el-select>
        <el-select v-model="filterForm.distance" placeholder="距离" clearable size="small">
          <el-option v-for="d in distanceOptions" :key="d" :label="d" :value="d" />
        </el-select>
      </div>
    </div>

    <!-- 列表视图 -->
    <div v-if="viewMode === 'list'" class="list-view">
      <el-card 
        v-for="item in students" 
        :key="item.id" 
        class="student-card"
        shadow="hover"
        @click="handleViewDetail(item)"
      >
        <div class="card-header">
          <div class="title-section">
            <h3 class="grade-subject">{{ item.grade }} · {{ item.subject }}</h3>
            <span class="price">¥{{ item.price }}/时</span>
          </div>
          <div class="tags-section">
            <el-tag 
              v-for="tag in item.tags" 
              :key="tag" 
              :type="getTagType(tag)"
              size="small"
              effect="plain"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
        
        <div class="card-body">
          <p class="desc">"{{ item.desc }}"</p>
        </div>
        
        <div class="card-footer">
          <span class="location">
            <el-icon><Location /></el-icon>
            {{ item.location }}
          </span>
          <el-button type="primary" text size="small">
            查看详情
            <el-icon class="ml-1"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </el-card>

      <el-empty v-if="students.length === 0" description="暂无生源需求" />
    </div>

    <!-- 地图视图 -->
    <div v-else class="map-view">
      <div class="map-container">
        <!-- 地图背景 -->
        <div class="map-grid"></div>
        
        <!-- 当前位置 -->
        <div class="current-location">
          <div class="pulse-ring"></div>
          <div class="location-dot"></div>
          <span class="location-label">我的位置</span>
        </div>

        <!-- 学生需求标记 -->
        <div 
          v-for="item in students" 
          :key="item.id"
          class="map-marker"
          :style="{ top: item.top, left: item.left }"
          @click="handleViewDetail(item)"
        >
          <div class="marker-content">
            <div class="marker-price">¥{{ item.price }}</div>
            <div class="marker-info">
              <div class="marker-subject">{{ item.subject }}</div>
              <div class="marker-location">{{ item.location }}</div>
            </div>
          </div>
          <div class="marker-arrow"></div>
        </div>
      </div>

      <!-- 底部信息栏 -->
      <div class="map-footer">
        <div class="footer-info">
          <div class="info-icon">
            <el-icon :size="24"><Promotion /></el-icon>
          </div>
          <div class="info-text">
            <p class="info-title">附近发现 {{ students.length }} 个需求</p>
            <p class="info-sub">定位精准度: <span class="status-good">高</span></p>
          </div>
        </div>
        <el-button type="primary" @click="handleRefresh">
          <el-icon class="mr-1"><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.find-students-page {
  min-height: 100vh;
  background: $bg-light;
  display: flex;
  flex-direction: column;
}

.search-header {
  background: #fff;
  padding: $spacing-md $spacing-lg;
  box-shadow: $shadow-sm;
  position: sticky;
  top: 0;
  z-index: 10;

  .search-bar {
    display: flex;
    gap: $spacing-sm;
    margin-bottom: $spacing-sm;

    .el-input {
      flex: 1;
    }
  }

  .filter-bar {
    display: flex;
    gap: $spacing-sm;
    overflow-x: auto;

    .el-select {
      width: 100px;
    }
  }
}

.list-view {
  padding: $spacing-md $spacing-lg;
  flex: 1;

  .student-card {
    margin-bottom: $spacing-md;
    border-radius: 12px;
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-2px);
    }

    .card-header {
      .title-section {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: $spacing-sm;

        .grade-subject {
          font-size: 16px;
          font-weight: 600;
          color: $text-primary;
        }

        .price {
          font-size: 18px;
          font-weight: 700;
          color: $warning-color;
        }
      }

      .tags-section {
        display: flex;
        flex-wrap: wrap;
        gap: $spacing-xs;
      }
    }

    .card-body {
      padding: $spacing-sm 0;

      .desc {
        font-size: 14px;
        color: $text-secondary;
        background: $bg-light;
        padding: $spacing-sm $spacing-md;
        border-radius: 8px;
        line-height: 1.6;
      }
    }

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: $spacing-sm;
      border-top: 1px solid $border-color;

      .location {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: $text-muted;
      }
    }
  }
}

.map-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;

  .map-container {
    flex: 1;
    background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 100%);
    position: relative;
    overflow: hidden;

    .map-grid {
      position: absolute;
      inset: 0;
      opacity: 0.3;
      background-image: radial-gradient($primary-color 1px, transparent 1px);
      background-size: 20px 20px;
    }

    .current-location {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      display: flex;
      flex-direction: column;
      align-items: center;
      z-index: 5;

      .pulse-ring {
        position: absolute;
        width: 60px;
        height: 60px;
        border-radius: 50%;
        background: rgba($primary-color, 0.2);
        animation: pulse 2s infinite;
      }

      .location-dot {
        width: 16px;
        height: 16px;
        background: $primary-color;
        border-radius: 50%;
        border: 3px solid #fff;
        box-shadow: $shadow-md;
        z-index: 1;
      }

      .location-label {
        margin-top: 4px;
        font-size: 10px;
        font-weight: 600;
        background: rgba(255, 255, 255, 0.9);
        padding: 2px 8px;
        border-radius: 10px;
      }
    }

    .map-marker {
      position: absolute;
      cursor: pointer;
      z-index: 10;
      transition: all 0.3s;

      &:hover {
        z-index: 50;
        transform: scale(1.05);
      }

      .marker-content {
        background: #fff;
        border-radius: 12px;
        padding: $spacing-sm;
        box-shadow: $shadow-lg;
        border: 2px solid rgba($warning-color, 0.3);
        display: flex;
        align-items: center;
        gap: $spacing-sm;
        min-width: 130px;

        .marker-price {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background: rgba($warning-color, 0.1);
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
          font-weight: 700;
          color: $warning-color;
        }

        .marker-info {
          .marker-subject {
            font-size: 13px;
            font-weight: 600;
            color: $text-primary;
          }

          .marker-location {
            font-size: 11px;
            color: $text-muted;
          }
        }
      }

      .marker-arrow {
        position: absolute;
        bottom: -6px;
        left: 50%;
        transform: translateX(-50%) rotate(45deg);
        width: 12px;
        height: 12px;
        background: #fff;
        border-right: 2px solid rgba($warning-color, 0.3);
        border-bottom: 2px solid rgba($warning-color, 0.3);
      }
    }
  }

  .map-footer {
    position: absolute;
    bottom: $spacing-lg;
    left: $spacing-lg;
    right: $spacing-lg;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 16px;
    padding: $spacing-md $spacing-lg;
    box-shadow: $shadow-xl;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .footer-info {
      display: flex;
      align-items: center;
      gap: $spacing-md;

      .info-icon {
        width: 48px;
        height: 48px;
        background: $primary-color;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }

      .info-text {
        .info-title {
          font-size: 14px;
          font-weight: 600;
          color: $text-primary;
        }

        .info-sub {
          font-size: 12px;
          color: $text-muted;

          .status-good {
            color: $success-color;
            font-weight: 600;
          }
        }
      }
    }
  }
}

@keyframes pulse {
  0% {
    transform: scale(0.5);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}
</style>
