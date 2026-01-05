<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Camera, CheckCircle, Upload, ChevronRight, Loader2 } from 'lucide-vue-next';

const router = useRouter();
const step = ref(1); // 当前步骤 1 或 2
const isOcrLoading = ref(false); // OCR 加载状态

// 表单数据
const form = reactive({
  // 步骤1：基础认证
  studentCardImg: '',
  name: '',
  school: '',
  major: '',
  studentId: '',
  
  // 步骤2：能力补充
  certs: [], // 证书
  video: null, // 试讲视频
  subjects: [], // 可授科目
  intro: ''
});

// 模拟 OCR 识别 (FR 1)
const handleFileUpload = (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  // 模拟图片预览
  form.studentCardImg = URL.createObjectURL(file);
  
  // 模拟 OCR 加载动画 [cite: 63]
  isOcrLoading.value = true;
  setTimeout(() => {
    isOcrLoading.value = false;
    // 模拟自动填充 [cite: 65]
    form.name = '张晓明';
    form.school = '北京师范大学';
    form.major = '数学与应用数学';
    form.studentId = '2021001001';
    alert('OCR识别成功！已自动填充信息。');
  }, 1500);
};

// 提交认证
const handleSubmit = () => {
  // 模拟提交成功
  alert('认证通过！初始信用分：100'); // [cite: 67]
  router.push('/teacher/students'); // 引导进入地图找学生 [cite: 67]
};
</script>

<template>
  <div class="min-h-screen bg-brand-gray pb-20 font-sans">
    
    <div class="bg-white p-4 sticky top-0 z-10 border-b flex items-center justify-center">
      <h1 class="font-bold text-lg text-gray-800">教师资质认证</h1>
    </div>

    <div class="bg-white p-4 mb-4">
      <div class="flex items-center justify-between text-sm font-bold mb-2">
        <span :class="step >= 1 ? 'text-brand-blue' : 'text-gray-400'">1. 基础认证</span>
        <span :class="step >= 2 ? 'text-brand-blue' : 'text-gray-400'">2. 能力补充</span>
      </div>
      <div class="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
        <div class="h-full bg-brand-blue transition-all duration-500" :style="{ width: step === 1 ? '50%' : '100%' }"></div>
      </div>
    </div>

    <div v-if="step === 1" class="p-4 space-y-4">
      <div class="bg-white p-6 rounded-xl shadow-sm">
        <h3 class="font-bold text-lg mb-4 flex items-center gap-2">
          <div class="w-1 h-4 bg-brand-blue rounded"></div>
          身份实名认证
        </h3>

        <div class="border-2 border-dashed border-blue-200 rounded-xl p-8 flex flex-col items-center justify-center bg-blue-50 hover:bg-blue-100 transition-colors relative cursor-pointer group">
          <input type="file" @change="handleFileUpload" class="absolute inset-0 opacity-0 cursor-pointer" />
          
          <div v-if="isOcrLoading" class="flex flex-col items-center text-brand-blue">
            <Loader2 class="animate-spin mb-2" />
            <span class="text-xs">AI 识别中...</span> </div>
          
          <div v-else-if="form.studentCardImg" class="w-full h-32 relative">
             <img :src="form.studentCardImg" class="w-full h-full object-cover rounded-lg" />
             <div class="absolute bottom-1 right-1 bg-black/50 text-white text-xs px-2 py-1 rounded">点击修改</div>
          </div>

          <div v-else class="flex flex-col items-center text-brand-blue">
            <Camera :size="32" class="mb-2 group-hover:scale-110 transition-transform"/> <span class="font-bold">上传学生证 / 校园卡</span>
            <span class="text-xs text-gray-400 mt-1">支持自动识别</span>
          </div>
        </div>

        <div class="mt-6 space-y-4 animate-fade-in">
          <div>
            <label class="text-xs text-gray-500">真实姓名</label>
            <input v-model="form.name" class="w-full border-b border-gray-200 py-2 outline-none focus:border-brand-blue bg-transparent font-bold text-gray-800" placeholder="待识别..." />
          </div>
          <div>
            <label class="text-xs text-gray-500">就读高校</label>
            <input v-model="form.school" class="w-full border-b border-gray-200 py-2 outline-none focus:border-brand-blue bg-transparent" placeholder="待识别..." />
          </div>
          <div>
            <label class="text-xs text-gray-500">主修专业</label>
            <input v-model="form.major" class="w-full border-b border-gray-200 py-2 outline-none focus:border-brand-blue bg-transparent" placeholder="待识别..." />
          </div>
          <div>
            <label class="text-xs text-gray-500">学号</label>
            <input v-model="form.studentId" class="w-full border-b border-gray-200 py-2 outline-none focus:border-brand-blue bg-transparent" placeholder="待识别..." />
          </div>
        </div>
      </div>

      <button @click="step = 2" :disabled="!form.name" class="w-full bg-brand-blue text-white py-3 rounded-xl font-bold shadow-lg active:scale-95 transition-transform disabled:opacity-50 disabled:cursor-not-allowed">
        下一步：能力补充
      </button>
    </div>

    <div v-if="step === 2" class="p-4 space-y-4">
      <div class="bg-white p-6 rounded-xl shadow-sm">
        <h3 class="font-bold text-lg mb-4 flex items-center gap-2">
          <div class="w-1 h-4 bg-brand-orange rounded"></div>
          教学能力展示
        </h3>

        <div class="mb-4">
           <label class="block text-sm font-bold mb-2">擅长科目</label>
           <input v-model="form.subjects" class="w-full border p-2 rounded-lg bg-gray-50 text-sm" placeholder="如：小学奥数, 初中英语 (逗号分隔)" />
        </div>

        <div class="mb-4">
          <div class="flex justify-between items-center mb-2">
            <label class="text-sm font-bold">获奖证书</label>
            <span class="text-xs text-gray-400 bg-gray-100 px-2 py-0.5 rounded">可选</span> </div>
          <div class="border border-dashed border-gray-300 rounded-lg p-4 flex items-center justify-center text-gray-400 gap-2">
            <Upload :size="16" /> 点击上传证书照片
          </div>
        </div>

        <div class="mb-4">
          <div class="flex justify-between items-center mb-2">
            <label class="text-sm font-bold">试讲视频 (5-10分钟)</label>
            <span class="text-xs text-gray-400 bg-gray-100 px-2 py-0.5 rounded">可选</span> </div>
          <div class="border border-dashed border-gray-300 rounded-lg p-4 flex flex-col items-center justify-center text-gray-400 gap-1">
             <div class="flex items-center gap-2"><Camera :size="16" /> 上传视频</div>
             <span class="text-xs text-gray-300">最大支持 500MB</span> </div>
        </div>
      </div>

      <button @click="handleSubmit" class="w-full bg-brand-blue text-white py-3 rounded-xl font-bold shadow-lg active:scale-95 transition-transform">
        提交审核
      </button>
      <button @click="step = 1" class="w-full text-gray-500 py-3 text-sm">
        返回上一步
      </button>
    </div>

  </div>
</template>