<script setup>
import { reactive, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Check, ChevronRight } from 'lucide-vue-next';

const router = useRouter();
const step = ref(1); // 1:学生信息, 2:教学需求, 3:授课偏好 

const form = reactive({
  // Step 1: 学生信息
  grade: '小学三年级',
  weakSubjects: [], // 复选框组 [cite: 72]
  character: '内向', // 单选 + 备注 [cite: 75]
  
  // Step 2: 教学需求
  target: '补差', // 模板选择 [cite: 76]
  frequency: '每周2次',
  
  // Step 3: 授课偏好
  gender: '无要求',
  style: '鼓励型',
  budgetMin: 150, // [cite: 73]
  budgetMax: 200, // [cite: 73]
  location: '幸福小区 (系统自动定位)'
});

// 选项数据
const subjectOptions = ['数学', '英语', '语文', '物理', '化学'];
const charOptions = ['内向', '活泼', '敏感']; // [cite: 75]
const targetOptions = ['提分', '补差', '培优']; // [cite: 76]

// 提交处理
const handleSubmit = () => {
  // 简化预览，使用弹窗确认关键信息 
  const summary = `
    请确认发布信息：
    ----------------
    年级：${form.grade}
    科目：${form.weakSubjects.join(', ')}
    预算：${form.budgetMin}-${form.budgetMax} 元/小时
  `;
  
  if (confirm(summary)) {
    alert('需求发布成功！系统正在为您匹配老师...');
    router.push('/teacher/list'); // 跳转到匹配列表
  }
};
</script>

<template>
  <div class="min-h-screen bg-brand-gray pb-24 font-sans">
    <div class="bg-white p-4 sticky top-0 z-10 border-b">
      <h1 class="text-center font-bold text-lg">发布家教需求</h1>
    </div>

    <div class="bg-white px-6 pt-2 pb-0 flex justify-between text-sm font-bold border-b border-gray-100">
      <div class="pb-3 border-b-2 px-2 transition-colors" 
           :class="step === 1 ? 'border-brand-blue text-brand-blue' : 'border-transparent text-gray-400'">
        1. 学生信息
      </div>
      <div class="pb-3 border-b-2 px-2 transition-colors"
           :class="step === 2 ? 'border-brand-orange text-brand-orange' : 'border-transparent text-gray-400'">
        2. 教学需求
      </div>
      <div class="pb-3 border-b-2 px-2 transition-colors"
           :class="step === 3 ? 'border-green-500 text-green-500' : 'border-transparent text-gray-400'">
        3. 授课偏好
      </div>
    </div>

    <div class="p-4">
      <div v-if="step === 1" class="bg-white p-6 rounded-xl shadow-sm space-y-6 animate-fade-in">
        <div>
          <label class="block text-sm font-bold text-gray-700 mb-2">就读年级</label>
          <select v-model="form.grade" class="w-full border p-3 rounded-lg bg-gray-50 outline-none focus:ring-2 focus:ring-brand-blue">
            <option>小学三年级</option>
            <option>小学四年级</option>
            <option>初中二年级</option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-bold text-gray-700 mb-2">薄弱科目 (多选)</label>
          <div class="grid grid-cols-3 gap-3">
            <div v-for="sub in subjectOptions" :key="sub" 
                 @click="form.weakSubjects.includes(sub) ? form.weakSubjects = form.weakSubjects.filter(i => i !== sub) : form.weakSubjects.push(sub)"
                 class="py-2 text-center rounded-lg border cursor-pointer text-sm transition-all"
                 :class="form.weakSubjects.includes(sub) ? 'bg-blue-50 border-brand-blue text-brand-blue font-bold' : 'border-gray-200 text-gray-600'">
              {{ sub }}
            </div>
          </div>
        </div>

        <div>
          <label class="block text-sm font-bold text-gray-700 mb-2">性格特点</label>
          <div class="flex gap-3">
            <label v-for="char in charOptions" :key="char" class="flex items-center gap-2 bg-gray-50 px-4 py-2 rounded-lg cursor-pointer">
              <input type="radio" :value="char" v-model="form.character" class="accent-brand-blue">
              <span class="text-sm">{{ char }}</span>
            </label>
          </div>
        </div>
      </div>

      <div v-if="step === 2" class="bg-white p-6 rounded-xl shadow-sm space-y-6 animate-fade-in">
        <div>
          <label class="block text-sm font-bold text-gray-700 mb-2">教学目标</label>
          <div class="grid grid-cols-3 gap-3">
            <div v-for="t in targetOptions" :key="t"
                 @click="form.target = t"
                 class="py-3 text-center rounded-lg border cursor-pointer text-sm transition-all"
                 :class="form.target === t ? 'bg-orange-50 border-brand-orange text-brand-orange font-bold' : 'border-gray-200 text-gray-600'">
              {{ t }}
            </div>
          </div>
        </div>

        <div>
          <label class="block text-sm font-bold text-gray-700 mb-2">预期频次</label>
          <input v-model="form.frequency" class="w-full border p-3 rounded-lg bg-gray-50 focus:ring-2 focus:ring-brand-orange outline-none" placeholder="例如：每周2次" />
        </div>
      </div>

      <div v-if="step === 3" class="bg-white p-6 rounded-xl shadow-sm space-y-6 animate-fade-in">
        <div>
          <label class="block text-sm font-bold text-gray-700 mb-2">可接受价格区间 (元/小时)</label>
          <div class="flex items-center gap-2">
            <input type="number" v-model="form.budgetMin" class="flex-1 border p-3 rounded-lg bg-gray-50 text-center" />
            <span class="text-gray-400">-</span>
            <input type="number" v-model="form.budgetMax" class="flex-1 border p-3 rounded-lg bg-gray-50 text-center" />
          </div>
          <p class="text-xs text-brand-orange mt-2">当前预算：{{ form.budgetMin }} - {{ form.budgetMax }} 元</p>
        </div>

        <div>
          <label class="block text-sm font-bold text-gray-700 mb-2">教师风格偏好</label>
          <select v-model="form.style" class="w-full border p-3 rounded-lg bg-gray-50 outline-none">
            <option>鼓励型</option>
            <option>严厉型</option>
            <option>趣味型</option>
          </select>
        </div>
      </div>
    </div>

    <div class="fixed bottom-0 left-0 w-full bg-white border-t p-4 flex gap-3 z-20 pb-safe">
      <button v-if="step > 1" @click="step--" class="flex-1 bg-gray-100 text-gray-700 py-3 rounded-xl font-bold">
        上一步
      </button>
      
      <button v-if="step < 3" @click="step++" class="flex-[2] bg-black text-white py-3 rounded-xl font-bold active:scale-95 transition-transform">
        下一步
      </button>
      
      <button v-if="step === 3" @click="handleSubmit" class="flex-[2] bg-brand-blue text-white py-3 rounded-xl font-bold active:scale-95 transition-transform shadow-lg">
        确认提交
      </button>
    </div>
  </div>
</template>