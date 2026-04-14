<template>
  <div class="edit-profile-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">编辑资料</h1>
      <el-button type="primary" :loading="saving" @click="saveProfile">
        保存
      </el-button>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>
    
    <div v-else class="profile-form">
      <!-- 头像 -->
      <div class="avatar-section">
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          accept="image/*"
          :before-upload="handleAvatarUpload"
        >
          <el-avatar :size="80" :src="form.avatar">
            {{ form.name?.charAt(0) }}
          </el-avatar>
          <div class="avatar-tip">点击更换头像</div>
        </el-upload>
      </div>
      
      <!-- 表单 -->
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="昵称" prop="name">
          <el-input v-model="form.name" placeholder="请输入昵称" />
        </el-form-item>
        
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" disabled>
            <template #append>
              <el-button @click="changePhone">更换</el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="联系微信">
          <el-input v-model="form.wechat" placeholder="选填，方便老师联系" />
        </el-form-item>
        
        <el-form-item label="所在地区">
          <el-cascader
            v-model="form.region"
            :options="regionOptions"
            placeholder="请选择地区"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="详细地址">
          <el-input
            v-model="form.address"
            type="textarea"
            :rows="2"
            placeholder="选填，方便安排上门辅导"
          />
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@shared/stores'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getUserInfo, updateUserInfo } from '@shared/api/user'
import { uploadFile } from '@shared/api/file'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)

const form = ref({
  name: '',
  gender: null,
  phone: '',
  wechat: '',
  avatar: '',
  region: [],
  address: ''
})

const rules = {
  name: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

// 简化的地区选项，实际应从API获取
const regionOptions = [
  {
    value: '北京',
    label: '北京',
    children: [
      { value: '朝阳区', label: '朝阳区' },
      { value: '海淀区', label: '海淀区' },
      { value: '东城区', label: '东城区' },
      { value: '西城区', label: '西城区' }
    ]
  },
  {
    value: '上海',
    label: '上海',
    children: [
      { value: '浦东新区', label: '浦东新区' },
      { value: '徐汇区', label: '徐汇区' },
      { value: '静安区', label: '静安区' }
    ]
  }
]

const goBack = () => {
  router.back()
}

const loadProfile = async () => {
  loading.value = true
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      const data = res.data || {}
      form.value = {
        name: data.name || '',
        gender: data.gender,
        phone: data.phone || '',
        wechat: data.wechat || '',
        avatar: data.avatar || '',
        region: data.region ? data.region.split(',') : [],
        address: data.address || ''
      }
    }
  } catch (error) {
    console.error('加载资料失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAvatarUpload = async (file) => {
  try {
    const res = await uploadFile(file)
    if (res.code === 200) {
      form.value.avatar = res.data.url
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    ElMessage.error('上传失败')
  }
  return false
}

const changePhone = () => {
  router.push('/settings/phone')
}

const saveProfile = async () => {
  try {
    await formRef.value?.validate()
    
    saving.value = true
    const data = {
      ...form.value,
      region: form.value.region?.join(',')
    }
    
    const res = await updateUserInfo(data)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      userStore.setUser({ ...userStore.user, ...data })
      router.back()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('保存失败:', error)
    }
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style lang="scss" scoped>
.edit-profile-page {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  
  .page-title {
    flex: 1;
    font-size: 20px;
    font-weight: 600;
    margin: 0;
  }
}

.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 12px;
}

.profile-form {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.avatar-section {
  text-align: center;
  margin-bottom: 24px;
  
  .avatar-uploader {
    cursor: pointer;
  }
  
  .el-avatar {
    margin-bottom: 8px;
  }
  
  .avatar-tip {
    font-size: 13px;
    color: #999;
  }
}
</style>
