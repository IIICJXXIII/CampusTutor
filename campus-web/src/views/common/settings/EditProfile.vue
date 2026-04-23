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
      <div class="avatar-section">
        <el-upload
          class="avatar-uploader"
          action="/api/file/upload?folder=avatar"
          :headers="uploadHeaders"
          :show-file-list="false"
          accept="image/jpeg,image/png,image/gif,image/webp"
          :before-upload="beforeAvatarUpload"
          :on-success="handleAvatarSuccess"
          :on-error="handleAvatarError"
        >
          <el-avatar :size="80" :src="form.avatar">
            {{ form.name?.charAt(0) }}
          </el-avatar>
          <div class="avatar-tip">点击更换头像</div>
        </el-upload>
      </div>
      
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@shared/stores'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getCurrentUser, updateUserInfo } from '@shared/api/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

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

const regionOptions = [
  {
    value: '北京', label: '北京',
    children: [
      { value: '朝阳区', label: '朝阳区' },
      { value: '海淀区', label: '海淀区' },
      { value: '东城区', label: '东城区' },
      { value: '西城区', label: '西城区' },
      { value: '丰台区', label: '丰台区' },
      { value: '通州区', label: '通州区' }
    ]
  },
  {
    value: '上海', label: '上海',
    children: [
      { value: '浦东新区', label: '浦东新区' },
      { value: '徐汇区', label: '徐汇区' },
      { value: '静安区', label: '静安区' },
      { value: '黄浦区', label: '黄浦区' },
      { value: '长宁区', label: '长宁区' }
    ]
  },
  {
    value: '湖南省', label: '湖南省',
    children: [
      { value: '长沙市', label: '长沙市', children: [
        { value: '岳麓区', label: '岳麓区' }, { value: '芙蓉区', label: '芙蓉区' },
        { value: '天心区', label: '天心区' }, { value: '开福区', label: '开福区' },
        { value: '雨花区', label: '雨花区' }, { value: '望城区', label: '望城区' },
        { value: '长沙县', label: '长沙县' }, { value: '浏阳市', label: '浏阳市' },
        { value: '宁乡市', label: '宁乡市' }
      ]},
      { value: '株洲市', label: '株洲市', children: [
        { value: '天元区', label: '天元区' }, { value: '荷塘区', label: '荷塘区' },
        { value: '芦淞区', label: '芦淞区' }, { value: '石峰区', label: '石峰区' },
        { value: '渌口区', label: '渌口区' }, { value: '醴陵市', label: '醴陵市' }
      ]},
      { value: '湘潭市', label: '湘潭市', children: [
        { value: '岳塘区', label: '岳塘区' }, { value: '雨湖区', label: '雨湖区' },
        { value: '湘乡市', label: '湘乡市' }, { value: '韶山市', label: '韶山市' },
        { value: '湘潭县', label: '湘潭县' }
      ]},
      { value: '衡阳市', label: '衡阳市', children: [
        { value: '蒸湘区', label: '蒸湘区' }, { value: '珠晖区', label: '珠晖区' },
        { value: '雁峰区', label: '雁峰区' }, { value: '石鼓区', label: '石鼓区' },
        { value: '常宁市', label: '常宁市' }, { value: '耒阳市', label: '耒阳市' }
      ]},
      { value: '邵阳市', label: '邵阳市', children: [
        { value: '大祥区', label: '大祥区' }, { value: '双清区', label: '双清区' },
        { value: '北塔区', label: '北塔区' }, { value: '武冈市', label: '武冈市' },
        { value: '邵东市', label: '邵东市' }
      ]},
      { value: '岳阳市', label: '岳阳市', children: [
        { value: '岳阳楼区', label: '岳阳楼区' }, { value: '云溪区', label: '云溪区' },
        { value: '君山区', label: '君山区' }, { value: '汨罗市', label: '汨罗市' },
        { value: '临湘市', label: '临湘市' }
      ]},
      { value: '常德市', label: '常德市', children: [
        { value: '武陵区', label: '武陵区' }, { value: '鼎城区', label: '鼎城区' },
        { value: '津市市', label: '津市市' }, { value: '澧县', label: '澧县' }
      ]},
      { value: '张家界市', label: '张家界市', children: [
        { value: '永定区', label: '永定区' }, { value: '武陵源区', label: '武陵源区' },
        { value: '慈利县', label: '慈利县' }, { value: '桑植县', label: '桑植县' }
      ]},
      { value: '益阳市', label: '益阳市', children: [
        { value: '赫山区', label: '赫山区' }, { value: '资阳区', label: '资阳区' },
        { value: '沅江市', label: '沅江市' }
      ]},
      { value: '郴州市', label: '郴州市', children: [
        { value: '北湖区', label: '北湖区' }, { value: '苏仙区', label: '苏仙区' },
        { value: '资兴市', label: '资兴市' }
      ]},
      { value: '永州市', label: '永州市', children: [
        { value: '冷水滩区', label: '冷水滩区' }, { value: '零陵区', label: '零陵区' }
      ]},
      { value: '怀化市', label: '怀化市', children: [
        { value: '鹤城区', label: '鹤城区' }, { value: '洪江市', label: '洪江市' }
      ]},
      { value: '娄底市', label: '娄底市', children: [
        { value: '娄星区', label: '娄星区' }, { value: '冷水江市', label: '冷水江市' },
        { value: '涟源市', label: '涟源市' }
      ]},
      { value: '湘西土家族苗族自治州', label: '湘西土家族苗族自治州', children: [
        { value: '吉首市', label: '吉首市' }, { value: '凤凰县', label: '凤凰县' },
        { value: '花垣县', label: '花垣县' }, { value: '龙山县', label: '龙山县' }
      ]}
    ]
  }
]

const goBack = () => {
  router.back()
}

const loadProfile = async () => {
  loading.value = true
  try {
    const res = await getCurrentUser()
    if (res.code === 200) {
      const data = res.data || {}
      form.value = {
        name: data.nickname || data.name || '',
        gender: data.gender,
        phone: data.phone || data.username || '',
        wechat: data.wechat || '',
        avatar: data.avatarUrl || data.avatar || '',
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

const beforeAvatarUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('仅支持 JPG/PNG/GIF/WEBP 格式的图片')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    form.value.avatar = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

const handleAvatarError = () => {
  ElMessage.error('头像上传失败，请重试')
}

const changePhone = () => {
  router.push('/settings/phone')
}

const saveProfile = async () => {
  try {
    await formRef.value?.validate()
    
    saving.value = true
    const data = {
      nickname: form.value.name,
      gender: form.value.gender,
      avatarUrl: form.value.avatar,
      wechat: form.value.wechat,
      region: form.value.region?.join(','),
      address: form.value.address
    }
    
    const res = await updateUserInfo(data)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      userStore.setUserInfo({
        ...userStore.userInfo,
        nickname: data.nickname,
        avatar: data.avatarUrl,
        wechat: data.wechat,
        region: data.region,
        address: data.address,
        gender: data.gender
      })
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
