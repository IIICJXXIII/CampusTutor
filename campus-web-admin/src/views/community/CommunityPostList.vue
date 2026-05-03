<template>
  <div class="page-container">
    <!-- 操作栏 -->
    <div class="action-bar card-shadow">
      <el-form :model="searchForm" inline>
        <el-form-item label="标题关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索帖子标题" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="话题类型">
          <el-select v-model="searchForm.topicType" placeholder="全部" clearable style="width: 140px;">
            <el-option label="经验分享" :value="1" />
            <el-option label="难题求助" :value="2" />
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
      <el-button type="success" size="default" @click="handlePublish">
        <el-icon><Edit /></el-icon>发布帖子
      </el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container card-shadow">
      <div class="table-header">
        <span class="title">社区帖子列表</span>
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="1">正常</el-radio-button>
          <el-radio-button label="0">已隐藏</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="filteredData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="authorNickname" label="作者" width="100" />
        <el-table-column prop="topicType" label="话题类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.topicType === 1 ? 'success' : 'warning'" size="small">
              {{ row.topicType === 1 ? '经验分享' : '难题求助' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" width="180">
          <template #default="{ row }">
            <template v-if="row.tags">
              <el-tag
                v-for="(tag, idx) in parseTags(row.tags)"
                :key="idx"
                size="small"
                style="margin: 2px;"
              >{{ tag }}</el-tag>
            </template>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="80" />
        <el-table-column prop="likeCount" label="点赞" width="70" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '已隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleView(row)">查看</el-button>
            <el-button text type="danger" v-if="row.status === 1" @click="handleDelete(row)">
              删除
            </el-button>
            <el-button text type="success" v-if="row.status === 0" @click="handleRestore(row)">
              恢复
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

    <!-- 发布帖子弹窗 -->
    <el-dialog v-model="publishDialogVisible" title="发布帖子" width="680px" :close-on-click-modal="false">
      <el-form ref="publishFormRef" :model="publishForm" :rules="publishRules" label-width="90px">
        <el-form-item label="帖子标题" prop="title">
          <el-input v-model="publishForm.title" placeholder="请输入帖子标题（2-128字）" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="话题类型" prop="topicType">
          <el-radio-group v-model="publishForm.topicType">
            <el-radio :value="1">经验分享</el-radio>
            <el-radio :value="2">难题求助</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择标签" prop="tags">
          <el-checkbox-group v-model="selectedTags">
            <el-checkbox
              v-for="tag in availableTags"
              :key="tag"
              :label="tag"
            >{{ tag }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="帖子内容" prop="content">
          <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="8"
            placeholder="请输入帖子内容，支持换行（1-5000字）"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="submitPublish">
          {{ publishing ? '发布中...' : '确认发布' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看帖子详情弹窗 -->
    <el-dialog v-model="detailVisible" title="帖子详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="帖子ID">{{ currentPost.id }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ currentPost.authorNickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="话题类型">
          <el-tag :type="currentPost.topicType === 1 ? 'success' : 'warning'" size="small">
            {{ currentPost.topicType === 1 ? '经验分享' : '难题求助' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标签">
          <template v-if="currentPost.tags">
            <el-tag v-for="(tag, idx) in parseTags(currentPost.tags)" :key="idx" size="small" style="margin: 2px;">{{ tag }}</el-tag>
          </template>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ currentPost.viewCount }}</el-descriptions-item>
        <el-descriptions-item label="点赞数">{{ currentPost.likeCount }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ currentPost.createTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentPost.status === 1 ? 'success' : 'danger'" size="small">
            {{ currentPost.status === 1 ? '正常' : '已隐藏' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="帖子标题" :span="2">{{ currentPost.title }}</el-descriptions-item>
        <el-descriptions-item label="帖子内容" :span="2">
          <div style="white-space: pre-wrap; max-height: 300px; overflow-y: auto;">{{ currentPost.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { communityApi } from '@/api'

const loading = ref(false)
const publishing = ref(false)
const publishDialogVisible = ref(false)
const detailVisible = ref(false)
const currentPost = ref({})
const viewMode = ref('all')
const publishFormRef = ref(null)

// 可用标签
const availableTags = [
  '学习经验',
  '考试技巧',
  '选课建议',
  '校园生活',
  '活动推荐',
  '求助问答'
]

const searchForm = reactive({
  keyword: '',
  topicType: ''
})

const selectedTags = ref([])

const publishForm = reactive({
  title: '',
  topicType: 1,
  content: ''
})

const publishRules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 2, max: 128, message: '标题长度应在2-128字之间', trigger: 'blur' }
  ],
  topicType: [
    { required: true, message: '请选择话题类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 1, max: 5000, message: '内容不能超过5000字', trigger: 'blur' }
  ]
}

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref([])

const filteredData = computed(() => {
  if (viewMode.value === 'all') return tableData.value
  return tableData.value.filter(item => item.status === Number(viewMode.value))
})

// 解析逗号分隔的标签
const parseTags = (tags) => {
  if (!tags) return []
  return tags.split(',').map(t => t.trim()).filter(Boolean)
}

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await communityApi.getList({
      topicType: searchForm.topicType || undefined,
      page: pagination.page,
      size: pagination.size
    })
    if (res.code === 200) {
      tableData.value = res.data?.records || []
      pagination.total = res.data?.total || 0
    }
  } catch {
    // API不可用时使用空数据
    tableData.value = []
    pagination.total = 0
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
  searchForm.topicType = ''
  handleSearch()
}

const handlePublish = () => {
  publishForm.title = ''
  publishForm.topicType = 1
  publishForm.content = ''
  selectedTags.value = []
  publishDialogVisible.value = true
}

const submitPublish = async () => {
  const valid = await publishFormRef.value?.validate().catch(() => false)
  if (!valid) return

  publishing.value = true
  try {
    const res = await communityApi.create({
      title: publishForm.title,
      content: publishForm.content,
      topicType: publishForm.topicType,
      tags: selectedTags.value.join(',')
    })
    if (res.code === 200) {
      ElMessage.success('帖子发布成功')
      publishDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.msg || '发布失败')
    }
  } catch {
    ElMessage.error('发布失败，请稍后重试')
  } finally {
    publishing.value = false
  }
}

const handleView = (row) => {
  currentPost.value = { ...row }
  detailVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该帖子吗？删除后帖子将隐藏。', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await communityApi.delete(row.id)
    ElMessage.success('帖子已删除')
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

const handleRestore = async (row) => {
  try {
    await communityApi.restore(row.id)
    ElMessage.success('帖子已恢复')
    fetchData()
  } catch {
    ElMessage.error('恢复失败')
  }
}
</script>

<style lang="scss" scoped>
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
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
</style>
