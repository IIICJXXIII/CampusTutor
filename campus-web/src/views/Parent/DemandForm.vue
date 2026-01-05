<script setup>
import { reactive } from 'vue';

const form = reactive({
  grade: '小学三年级',
  subjects: [],
  goal: '',
  style: '鼓励型',
});

const subjectsList = ['数学', '英语', '物理', '化学', '语文'];

const submitDemand = () => {
  console.log("提交的数据:", form);
  alert(`需求已发布！\n年级：${form.grade}\n科目：${form.subjects.join('+')}`);
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 p-6 flex justify-center">
    <div class="w-full max-w-lg bg-white rounded-xl shadow-lg p-6">
      <h2 class="text-2xl font-bold text-gray-800 mb-6 border-b pb-4">
        发布家教需求
      </h2>

      <div class="space-y-5">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">学生年级</label>
          <select v-model="form.grade" class="w-full border border-gray-300 rounded-lg p-3 bg-white outline-none">
            <option>小学三年级</option>
            <option>小学六年级</option>
            <option>初中二年级</option>
            <option>高中一年级</option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">薄弱科目</label>
          <div class="flex flex-wrap gap-3">
            <label v-for="sub in subjectsList" :key="sub" 
                   class="cursor-pointer border px-4 py-2 rounded-full text-sm transition-all"
                   :class="form.subjects.includes(sub) ? 'bg-blue-100 border-blue-500 text-blue-700' : 'bg-white'">
              <input type="checkbox" :value="sub" v-model="form.subjects" class="hidden">
              {{ sub }}
            </label>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">期望目标</label>
          <textarea v-model="form.goal" rows="3" class="w-full border border-gray-300 rounded-lg p-3 outline-none"></textarea>
        </div>

        <button @click="submitDemand" class="w-full bg-blue-600 text-white font-bold py-3.5 rounded-lg mt-4 hover:bg-blue-700">
          发布并匹配
        </button>
      </div>
    </div>
  </div>
</template>