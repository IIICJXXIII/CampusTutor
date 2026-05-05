<template>
  <div class="user-profile-page">
    <van-nav-bar title="用户资料" left-arrow @click-left="$router.back()" />
    <div class="profile-card" v-if="profile" v-loading="loading">
      <div class="profile-header">
        <el-avatar :size="72" :src="profile.avatarUrl || undefined" class="profile-avatar">{{ profile.nickname?.charAt(0) || '?' }}</el-avatar>
        <h2 class="profile-name">{{ profile.nickname || '未知用户' }}</h2>
        <el-tag size="small" :type="profile.role === 1 ? 'success' : profile.role === 2 ? 'warning' : 'info'">{{ roleMap[profile.role] || '用户' }}</el-tag>
      </div>
      <div class="profile-info">
        <div class="info-item" v-if="profile.gender"><span class="info-label">性别</span><span class="info-value">{{ profile.gender === 1 ? '男' : '女' }}</span></div>
        <div class="info-item" v-if="profile.region"><span class="info-label">地区</span><span class="info-value">{{ profile.region }}</span></div>
        <div class="info-item" v-if="profile.address"><span class="info-label">地址</span><span class="info-value">{{ profile.address }}</span></div>
        <div class="info-item" v-if="profile.createTime"><span class="info-label">加入时间</span><span class="info-value">{{ formatTime(profile.createTime) }}</span></div>
      </div>
    </div>
    <el-empty v-else-if="!loading" description="用户不存在" :image-size="80" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@shared/api/request'
import dayjs from 'dayjs'
const route = useRoute()
const profile = ref(null)
const loading = ref(false)
const roleMap = { 0: '管理员', 1: '教员', 2: '家长' }
const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD') : ''
onMounted(async () => {
  const userId = route.params.id
  if (!userId) return
  loading.value = true
  try { const res = await request.get(`/user/${userId}`); if (res.code === 200) profile.value = res.data } catch { profile.value = null } finally { loading.value = false }
})
</script>

<style lang="scss" scoped>
.user-profile-page { min-height: 100vh; background: #f5f7fa; }
.profile-card { margin: 16px; background: #fff; border-radius: 12px; padding: 24px 20px; }
.profile-header { display: flex; flex-direction: column; align-items: center; padding-bottom: 20px; border-bottom: 1px solid #f0f0f0; }
.profile-header .profile-avatar { margin-bottom: 12px; }
.profile-header .profile-name { font-size: 20px; font-weight: 600; color: #303133; margin: 0 0 8px; }
.profile-info { padding-top: 16px; }
.info-item { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #f5f5f5; }
.info-item:last-child { border-bottom: none; }
.info-label { font-size: 14px; color: #909399; }
.info-value { font-size: 14px; color: #303133; }
</style>
