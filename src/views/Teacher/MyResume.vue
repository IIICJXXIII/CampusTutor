<script setup>
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { Camera, CheckCircle } from 'lucide-vue-next';

const router = useRouter();

// 模拟教师发布的个人信息
const form = reactive({
  name: '张同学',
  school: '北京师范大学',
  major: '英语教育',
  price: 150,
  subjects: ['小学全科', '初中英语'],
  intro: '本人北师大在读，性格开朗，擅长与孩子沟通。英语专八通过，有两年家教经验。',
  isPublic: true // 是否公开展示
});

const handlePublish = () => {
  // 模拟保存/发布
  alert('发布成功！家长现在可以在列表看到您的信息了。');
  router.push('/teacher/students'); // 发布完去看看生源
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-24">
    <div class="bg-blue-600 p-6 text-white shadow-md">
      <h1 class="text-2xl font-bold">发布家教信息</h1>
      <p class="text-blue-100 text-sm mt-1">完善简历，让家长更容易找到你</p>
    </div>

    <div class="p-4 -mt-4">
      <div class="bg-white rounded-xl shadow-sm p-6 space-y-6">
        
        <div class="flex flex-col items-center">
          <div class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center cursor-pointer hover:bg-gray-200 transition-colors">
            <Camera :size="28" class="text-gray-400" />
          </div>
          <span class="text-xs text-gray-400 mt-2">点击上传真实头像</span>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-bold text-gray-700 mb-1">姓名 / 昵称</label>
            <input v-model="form.name" class="w-full border border-gray-200 p-3 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" placeholder="例如：张同学" />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-bold text-gray-700 mb-1">就读高校</label>
              <input v-model="form.school" class="w-full border border-gray-200 p-3 rounded-lg outline-none" placeholder="学校名称" />
            </div>
            <div>
              <label class="block text-sm font-bold text-gray-700 mb-1">期望时薪</label>
              <div class="relative">
                <span class="absolute left-3 top-3 text-gray-500">¥</span>
                <input v-model="form.price" type="number" class="w-full border border-gray-200 p-3 pl-8 rounded-lg outline-none" />
              </div>
            </div>
          </div>

          <div>
            <label class="block text-sm font-bold text-gray-700 mb-1">擅长科目</label>
            <div class="flex flex-wrap gap-2 mb-2">
              <span v-for="sub in form.subjects" :key="sub" class="bg-blue-50 text-blue-600 px-3 py-1 rounded-full text-sm font-medium flex items-center gap-1">
                {{ sub }} <button class="hover:text-blue-800">×</button>
              </span>
              <button class="text-gray-400 text-sm border border-dashed border-gray-300 px-3 py-1 rounded-full">+ 添加</button>
            </div>
          </div>

          <div>
            <label class="block text-sm font-bold text-gray-700 mb-1">自我介绍 / 教学优势</label>
            <textarea v-model="form.intro" rows="4" class="w-full border border-gray-200 p-3 rounded-lg outline-none" placeholder="请介绍您的教学经验、性格特点等..."></textarea>
          </div>
          
          <div class="flex items-center justify-between pt-2">
            <span class="text-sm font-medium text-gray-700">公开展示我的信息</span>
            <div class="w-12 h-6 bg-blue-600 rounded-full relative cursor-pointer">
              <div class="w-4 h-4 bg-white rounded-full absolute top-1 right-1"></div>
            </div>
          </div>
        </div>

        <button @click="handlePublish" class="w-full bg-black text-white py-4 rounded-xl font-bold hover:bg-gray-800 transition-transform active:scale-95 flex items-center justify-center gap-2">
          <CheckCircle :size="20" /> 确认发布
        </button>

      </div>
    </div>
  </div>
</template>