<template>
  <el-dialog
    v-model="visible"
    title="获取您的位置信息"
    width="420px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    align-center
  >
    <div class="location-dialog-content">
      <el-icon :size="48" class="location-icon"><Location /></el-icon>
      <p class="location-desc">
        为了给您推荐附近的家教需求、自动填充地址信息，我们需要获取您的地理位置。
      </p>
      <p class="location-privacy">
        您的位置数据仅用于平台服务，不会泄露给第三方。您可以随时在设置中修改授权状态。
      </p>
    </div>
    <template #footer>
      <el-button @click="handleDeny">拒绝</el-button>
      <el-button type="primary" :loading="locating" @click="handleAccept">
        {{ locating ? '定位中...' : '同意授权' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { updateUserAddress } from '@shared/api/user'
import { reverseGeocode } from '@shared/api/map'
import { useUserStore } from '@shared/stores'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'located', 'denied'])

const userStore = useUserStore()
const visible = ref(props.modelValue)
const locating = ref(false)

watch(() => props.modelValue, (val) => { visible.value = val })
watch(visible, (val) => { emit('update:modelValue', val) })

const handleAccept = () => {
  locating.value = true
  localStorage.setItem('locationPermission', 'granted')
  fetchLocation()
}

const handleDeny = () => {
  visible.value = false
  localStorage.setItem('locationPermission', 'denied')
  userStore.setUserInfo({
    ...userStore.userInfo,
    longitude: null,
    latitude: null,
    address: ''
  })
  emit('denied')
}

const fetchLocation = () => {
  if (!navigator.geolocation) {
    locating.value = false
    visible.value = false
    ElMessage.warning('您的设备不支持定位功能')
    emit('denied')
    return
  }

  navigator.geolocation.getCurrentPosition(
    async (position) => {
      const { longitude, latitude } = position.coords
      let address = `${longitude.toFixed(6)}, ${latitude.toFixed(6)}`

      try {
        const res = await reverseGeocode(latitude, longitude)
        if (res.code === 200 && res.data) {
          address = res.data.formattedAddress || res.data.address || address
        }
      } catch (e) {
        console.warn('逆地理编码失败:', e)
      }

      try {
        await updateUserAddress({ longitude, latitude, address })
      } catch (e) {
        console.warn('更新用户地址失败:', e)
      }

      userStore.setUserInfo({
        ...userStore.userInfo,
        longitude,
        latitude,
        address
      })
      emit('located', { longitude, latitude, address })

      locating.value = false
      visible.value = false
    },
    (error) => {
      locating.value = false
      visible.value = false
      if (error.code === error.PERMISSION_DENIED) {
        ElMessage.warning('您拒绝了位置授权')
        localStorage.setItem('locationPermission', 'denied')
      } else {
        ElMessage.info('定位失败，您可以稍后在设置中重新授权')
      }
      emit('denied')
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
  )
}
</script>

<style lang="scss" scoped>
.location-dialog-content {
  text-align: center;
  padding: 10px 0;

  .location-icon {
    color: #667eea;
    margin-bottom: 16px;
  }

  .location-desc {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    margin-bottom: 8px;
  }

  .location-privacy {
    font-size: 12px;
    color: #909399;
    line-height: 1.5;
  }
}
</style>
