<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { 
  Camera, Upload, CheckCircle, ChevronRight, 
  Loader2, FileText, Video, MapPin 
} from 'lucide-vue-next';
import { store } from '../../store.js'; // 引入全局状态以便更新身份状态

const router = useRouter();
const step = ref(1); // 1: 基础认证, 2: 能力补充, 3: 完成页
const isOcrLoading = ref(false); // 控制OCR加载动画

// 表单数据
const form = reactive({
  // Step 1
  studentCardImg: '',
  name: '',
  school: '',
  major: '',
  studentId: '',
  // Step 2
  subjects: [],
  certs: [],
  video: null
});

// --- 交互逻辑 ---

// 1. 模拟上传学生证 + OCR 识别
const handleUploadCard = (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // 1.1 本地预览图片
  const imgUrl = URL.createObjectURL(file);
  form.studentCardImg = imgUrl;

  // 1.2 模拟 OCR 识别过程 (2秒动画)
  isOcrLoading.value = true;
  
  setTimeout(() => {
    isOcrLoading.value = false;
    // 1.3 自动填充数据 (高亮效果由 CSS 动画处理)
    form.name = '张同学';
    form.school = '北京师范大学';
    form.major = '数学与应用数学';
    form.studentId = '2021001052';
    alert('OCR 识别成功！已自动填充信息。');
  }, 2000);
};

// 2. 模拟上传视频
const handleUploadVideo = (event) => {
  const file = event.target.files[0];
  if (file) {
    if (file.size > 500 * 1024 * 1024) return alert('文件超过500MB');
    form.video = file.name;
    alert(`视频 "${file.name}" 上传成功`);
  }
};

// 3. 提交认证
const handleSubmit = () => {
  // 模拟提交请求
  setTimeout(() => {
    // 1. 更新步骤显示
    step.value = 3; 
    
    // 2. ★ 核心：更新全局状态为“已认证”
    store.setCertification(true); 
    
  }, 1000);
};

// 4. 完成页按钮点击
const handleFinish = () => {
  // 认证完了，去完善简历（发布信息）
  router.push('/teacher/resume'); 
};
</script>

<template>
  <div class="min-h-screen bg-brand-gray pb-safe font-sans flex flex-col">
    
    <div class="bg-white p-4 sticky top-0 z-10 border-b flex items-center justify-center">
      <h1 class="font-bold text-lg text-gray-800">教师资质认证</h1>
    </div>

    <div v-if="step < 3" class="bg-white p-4 mb-3 shadow-sm">
      <div class="flex items-center justify-between text-xs font-bold mb-2">
        <span class="flex items-center gap-1" :class="step >= 1 ? 'text-brand-blue' : 'text-gray-400'">
          <div class="w-5 h-5 rounded-full flex items-center justify-center border" 
               :class="step >= 1 ? 'bg-brand-blue text-white border-brand-blue' : 'border-gray-300'">1</div>
          基础认证
        </span>
        <span class="flex items-center gap-1" :class="step >= 2 ? 'text-brand-blue' : 'text-gray-400'">
          <div class="w-5 h-5 rounded-full flex items-center justify-center border"
               :class="step >= 2 ? 'bg-brand-blue text-white border-brand-blue' : 'border-gray-300'">2</div>
          能力补充
        </span>
      </div>
      <div class="w-full h-1.5 bg-gray-100 rounded-full overflow-hidden">
        <div class="h-full bg-brand-blue transition-all duration-500 ease-out" 
             :style="{ width: step === 1 ? '50%' : '100%' }"></div>
      </div>
    </div>

    <div v-if="step === 1" class="flex-1 p-4 space-y-4 animate-fade-in">
      <div class="bg-white p-6 rounded-xl shadow-sm">
        <h3 class="font-bold text-lg mb-4 flex items-center gap-2">
          <div class="w-1 h-4 bg-brand-blue rounded"></div>
          身份核验
        </h3>

        <div class="relative group cursor-pointer">
          <input type="file" accept="image/*" @change="handleUploadCard" class="absolute inset-0 opacity-0 z-10 cursor-pointer" />
          
          <div class="border-2 border-dashed border-blue-200 rounded-xl p-8 bg-blue-50/50 flex flex-col items-center justify-center transition-colors group-hover:border-brand-blue group-hover:bg-blue-50 h-48">
            
            <div v-if="isOcrLoading" class="flex flex-col items-center text-brand-blue animate-pulse">
              <Loader2 :size="32" class="animate-spin mb-2" />
              <span class="text-xs font-bold">AI 智能识别中...</span>
            </div>

            <div v-else-if="form.studentCardImg" class="w-full h-full absolute inset-0 p-2">
              <img :src="form.studentCardImg" class="w-full h-full object-cover rounded-lg" />
              <div class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity rounded-lg">
                <span class="text-white text-xs font-bold border border-white px-3 py-1 rounded-full">点击更换</span>
              </div>
            </div>

            <div v-else class="flex flex-col items-center text-gray-400 group-hover:text-brand-blue">
              <div class="w-12 h-12 bg-white rounded-full flex items-center justify-center shadow-sm mb-3">
                <Camera :size="24" />
              </div>
              <p class="text-sm font-bold text-gray-600 group-hover:text-brand-blue">上传学生证 / 校园卡</p>
              <p class="text-[10px] mt-1">支持 JPG/PNG，自动识别信息</p>
            </div>
          </div>
        </div>

        <div class="mt-6 space-y-4">
          <div class="relative">
            <label class="text-xs text-gray-500 mb-1 block">真实姓名</label>
            <input v-model="form.name" type="text" 
                   class="w-full border-b border-gray-200 py-2 outline-none focus:border-brand-blue bg-transparent font-bold text-gray-800 transition-all duration-1000"
                   :class="{'bg-yellow-50 px-2': form.name && !isOcrLoading}" 
                   placeholder="等待识别..." />
            <CheckCircle v-if="form.name" :size="16" class="absolute right-0 bottom-3 text-green-500" />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-xs text-gray-500 mb-1 block">就读高校</label>
              <input v-model="form.school" class="w-full border-b border-gray-200 py-2 outline-none focus:border-brand-blue bg-transparent text-sm" placeholder="等待识别..." />
            </div>
            <div>
              <label class="text-xs text-gray-500 mb-1 block">学号</label>
              <input v-model="form.studentId" class="w-full border-b border-gray-200 py-2 outline-none focus:border-brand-blue bg-transparent text-sm" placeholder="等待识别..." />
            </div>
          </div>

          <div>
             <label class="text-xs text-gray-500 mb-1 block">主修专业</label>
             <input v-model="form.major" class="w-full border-b border-gray-200 py-2 outline-none focus:border-brand-blue bg-transparent text-sm" placeholder="等待识别..." />
          </div>
        </div>
      </div>
    </div>

    <div v-if="step === 2" class="flex-1 p-4 space-y-4 animate-fade-in">
      <div class="bg-white p-6 rounded-xl shadow-sm space-y-6">
        <h3 class="font-bold text-lg flex items-center gap-2">
          <div class="w-1 h-4 bg-brand-orange rounded"></div>
          能力展示
        </h3>

        <div>
          <label class="block text-sm font-bold text-gray-700 mb-2">擅长科目 (可多选)</label>
          <input type="text" class="w-full border border-gray-200 rounded-lg p-3 text-sm focus:ring-2 focus:ring-brand-blue outline-none" placeholder="例如：小学奥数, 初中英语" />
        </div>

        <div>
          <div class="flex justify-between mb-2">
            <label class="text-sm font-bold text-gray-700">获奖证书</label>
            <span class="text-[10px] bg-gray-100 text-gray-500 px-2 py-0.5 rounded">可选</span>
          </div>
          <div class="border border-dashed border-gray-300 rounded-lg p-4 flex items-center justify-center gap-2 text-gray-400 cursor-pointer hover:bg-gray-50 hover:border-gray-400 transition-colors">
            <FileText :size="18" />
            <span class="text-xs">点击上传 (JPG/PDF)</span>
          </div>
        </div>

        <div>
          <div class="flex justify-between mb-2">
            <label class="text-sm font-bold text-gray-700">试讲视频</label>
            <span class="text-[10px] bg-gray-100 text-gray-500 px-2 py-0.5 rounded">可选</span>
          </div>
          <div class="relative group">
            <input type="file" accept="video/*" @change="handleUploadVideo" class="absolute inset-0 opacity-0 cursor-pointer z-10" />
            <div class="border border-dashed border-gray-300 rounded-lg p-6 flex flex-col items-center justify-center gap-2 text-gray-400 group-hover:bg-gray-50 transition-colors">
              
              <div v-if="form.video" class="flex items-center gap-2 text-brand-blue">
                <Video :size="20" />
                <span class="text-sm font-bold truncate max-w-[200px]">{{ form.video }}</span>
                <CheckCircle :size="14" />
              </div>

              <div v-else class="flex flex-col items-center">
                <Upload :size="20" class="mb-1" />
                <span class="text-xs">上传 5-10 分钟微课展示</span>
                <span class="text-[10px] text-gray-300 mt-1">最大支持 500MB</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="step === 3" class="flex-1 flex flex-col items-center justify-center bg-white p-8 text-center animate-fade-in">
      <div class="w-24 h-24 bg-green-50 rounded-full flex items-center justify-center mb-6 animate-bounce-small">
        <CheckCircle :size="48" class="text-green-500" />
      </div>
      
      <h2 class="text-2xl font-bold text-gray-900 mb-2">认证提交成功</h2>
      <p class="text-gray-500 text-sm mb-8">
        资质审核已通过，您的教师主页已激活
      </p>

      <div class="bg-gradient-to-r from-brand-blue to-blue-500 text-white p-6 rounded-2xl w-full shadow-lg shadow-blue-200 mb-8 relative overflow-hidden">
        <div class="absolute top-0 right-0 w-20 h-20 bg-white/10 rounded-full -mr-5 -mt-5"></div>
        <div class="relative z-10">
          <p class="text-blue-100 text-xs mb-1">当前信用分</p>
          <div class="text-4xl font-bold font-mono">100</div>
          <p class="text-[10px] opacity-80 mt-2">初始信用极佳，保持良好教学记录可提升</p>
        </div>
      </div>

      <button @click="handleFinish" class="w-full bg-black text-white py-4 rounded-xl font-bold shadow-xl active:scale-95 transition-transform flex items-center justify-center gap-2">
        <MapPin :size="18" /> 进入地图找学生
      </button>
    </div>

    <div v-if="step < 3" class="bg-white p-4 border-t sticky bottom-0">
      <button v-if="step === 1" @click="step = 2" 
              :disabled="!form.name"
              class="w-full bg-brand-blue text-white py-3 rounded-xl font-bold shadow-lg disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-1">
        下一步：能力补充 <ChevronRight :size="16" />
      </button>

      <div v-if="step === 2" class="flex gap-3">
        <button @click="step = 1" class="flex-1 bg-gray-100 text-gray-600 py-3 rounded-xl font-bold">
          上一步
        </button>
        <button @click="handleSubmit" class="flex-[2] bg-brand-blue text-white py-3 rounded-xl font-bold shadow-lg">
          提交认证
        </button>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* 简单的加载动画 */
@keyframes bounce-small {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
.animate-bounce-small {
  animation: bounce-small 2s infinite;
}
</style>