import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import App from './App.vue'
import router from './router'
import { setRequestErrorHandler } from '@shared/api/request'
import '@shared/styles/index.scss'
import './styles/parent.scss'
import './styles/teacher.scss'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

setRequestErrorHandler((msg) => ElMessage.error(msg))

app.config.errorHandler = (err, vm, info) => {
  console.error('Unhandled error:', err, info)
  if (err?.message?.includes('Failed to fetch dynamically imported module') ||
      err?.message?.includes('Importing a module script failed')) {
    ElMessage.error('页面加载失败，正在刷新...')
    setTimeout(() => window.location.reload(true), 1500)
  } else {
    ElMessage.error('系统异常，请稍后重试')
  }
}

window.addEventListener('unhandledrejection', (event) => {
  console.error('Unhandled promise rejection:', event.reason)
  if (event.reason?.message?.includes('Failed to fetch dynamically imported module') ||
      event.reason?.message?.includes('Importing a module script failed')) {
    event.preventDefault()
    ElMessage.error('资源加载失败，正在刷新...')
    setTimeout(() => window.location.reload(true), 1500)
  }
})

try {
  app.mount('#app')
  const loading = document.getElementById('app-loading')
  if (loading) loading.remove()
} catch (e) {
  console.error('Vue app mount failed:', e)
  const loading = document.getElementById('app-loading')
  if (loading) {
    loading.querySelector('.spinner').style.display = 'none'
    loading.querySelector('p').textContent = '应用启动失败，请刷新页面'
    loading.querySelector('.error-box').style.display = 'block'
  }
}