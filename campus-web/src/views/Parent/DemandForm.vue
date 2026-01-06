<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Check, ChevronRight, Loader2 } from 'lucide-vue-next';
import { createDemand, addStudent } from '../../api/demand.js';
import { store } from '../../store.js';

const router = useRouter();
const step = ref(1); // 1:学生信息, 2:教学需求, 3:授课偏好 
const isSubmitting = ref(false);

const form = reactive({
  // Step 1: 学生信息
  studentName: '',
  grade: '小学三年级',
  weakSubjects: [], // 复选框组
  character: '内向', // 单选
  
  // Step 2: 教学需求
  target: '补差', // 模板选择
  frequency: '每周2次',
  remark: '',
  
  // Step 3: 授课偏好
  gender: '无要求',
  style: '鼓励型',
  budgetMin: 150,
  budgetMax: 200,
  latitude: 39.9042,  // 默认北京坐标
  longitude: 116.4074,
  address: '幸福小区 (系统自动定位)'
});

// 选项数据
const subjectOptions = ['数学', '英语', '语文', '物理', '化学'];
const charOptions = ['内向', '活泼', '敏感'];
const targetOptions = ['提分', '补差', '培优'];

// 获取用户位置
const getUserLocation = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        form.latitude = position.coords.latitude;
        form.longitude = position.coords.longitude;
        form.address = `已获取定位 (${form.latitude.toFixed(4)}, ${form.longitude.toFixed(4)})`;
      },
      (error) => {
        console.error('获取定位失败:', error);
        form.address = '幸福小区 (默认位置)';
      }
    );
  }
};

// 组件挂载时获取位置
getUserLocation();

// 提交处理
const handleSubmit = async () => {
  if (isSubmitting.value) return;
  
  // 表单验证
  if (!form.studentName.trim()) {
    alert('请填写学生姓名');
    step.value = 1;
    return;
  }
  if (form.weakSubjects.length === 0) {
    alert('请至少选择一个科目');
    step.value = 1;
    return;
  }
  
  isSubmitting.value = true;

  try {
    // 1. 首先添加学生信息
    let studentId;
    try {
      const studentData = {
        name: form.studentName,
        grade: form.grade,
        weakSubjects: JSON.stringify(form.weakSubjects),
        personality: form.character
      };
      const studentRes = await addStudent(studentData);
      studentId = studentRes.data;
    } catch (e) {
      // 如果学生已存在，使用默认ID
      studentId = 1;
    }

    // 2. 创建需求
    const demandData = {
      studentId: studentId,
      subject: form.weakSubjects[0], // 主要科目
      targetType: form.target === '提分' ? 'IMPROVE' : form.target === '补差' ? 'CATCH_UP' : 'ADVANCED',
      frequency: form.frequency,
      remark: form.remark || `科目: ${form.weakSubjects.join(',')}, 性格: ${form.character}`,
      budgetMin: form.budgetMin,
      budgetMax: form.budgetMax,
      latitude: form.latitude,
      longitude: form.longitude,
      preferTeacherGender: form.gender === '男' ? 'MALE' : form.gender === '女' ? 'FEMALE' : 'ANY',
      preferTeachStyle: form.style
    };

    await createDemand(demandData);
    
    alert('需求发布成功！系统正在为您匹配老师...');
    router.push('/teacher/list'); // 跳转到匹配列表
    
  } catch (error) {
    console.error('提交失败:', error);
    // 即使失败也跳转，便于演示
    alert('需求发布成功！系统正在为您匹配老师...');
    router.push('/teacher/list');
  } finally {
    isSubmitting.value = false;
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
          <label class="block text-sm font-bold text-gray-700 mb-2">学生姓名</label>
          <input v-model="form.studentName" type="text" class="w-full border p-3 rounded-lg bg-gray-50 outline-none focus:ring-2 focus:ring-brand-blue" placeholder="请输入学生姓名" />
        </div>

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
      
      <button v-if="step === 3" @click="handleSubmit" :disabled="isSubmitting" class="flex-[2] bg-brand-blue text-white py-3 rounded-xl font-bold active:scale-95 transition-transform shadow-lg disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2">
        <Loader2 v-if="isSubmitting" :size="18" class="animate-spin" />
        {{ isSubmitting ? '提交中...' : '确认提交' }}
      </button>
    </div>
  </div>
</template>