import { createApp } from 'vue'
import './style.css' // <--- 必须要有这一行！
import App from './App.vue'
import router from './router'
//test
const app = createApp(App)
app.use(router)
app.mount('#app')